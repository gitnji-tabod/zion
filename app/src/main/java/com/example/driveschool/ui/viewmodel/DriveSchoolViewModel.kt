package com.example.driveschool.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.driveschool.data.db.DriveSchoolDatabase
import com.example.driveschool.data.db.ProductionSeedData
import com.example.driveschool.data.db.populateInitialDatabase
import com.example.driveschool.data.model.*
import com.example.driveschool.data.repository.DriveSchoolRepository
import com.example.driveschool.data.sync.DriveSchoolSyncManager
import com.example.driveschool.data.sync.FirestoreBranchDocument
import com.example.driveschool.data.sync.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class DriveSchoolViewModel(application: Application) : AndroidViewModel(application) {

  private val database = DriveSchoolDatabase.getDatabase(application, viewModelScope)
  private val repository = DriveSchoolRepository(database.driveSchoolDao())
  val syncManager = DriveSchoolSyncManager(application, database.driveSchoolDao())
  val syncState = syncManager.syncState
  val lastSyncTimestamp = syncManager.lastSyncTimestamp

  // Current session & role state
  private val _currentRole = MutableStateFlow(UserRole.STUDENT)
  val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

  private val _activeUserOverride = MutableStateFlow<UserEntity?>(null)
  private val _authScreen = MutableStateFlow("SIGN_IN") // "SIGN_IN", "SIGN_UP", "MAIN_APP"
  val authScreen: StateFlow<String> = _authScreen.asStateFlow()

  private val _currentLocale = MutableStateFlow("en") // "en" or "fr"
  val currentLocale: StateFlow<String> = _currentLocale.asStateFlow()

  private val _currentTab = MutableStateFlow("DASHBOARD")
  val currentTab: StateFlow<String> = _currentTab.asStateFlow()

  private val _selectedLesson = MutableStateFlow<LessonEntity?>(null)
  val selectedLesson: StateFlow<LessonEntity?> = _selectedLesson.asStateFlow()

  private val _selectedCertificate = MutableStateFlow<CertificateEntity?>(null)
  val selectedCertificate: StateFlow<CertificateEntity?> = _selectedCertificate.asStateFlow()

  private val _verificationResult = MutableStateFlow<CertificateEntity?>(null)
  val verificationResult: StateFlow<CertificateEntity?> = _verificationResult.asStateFlow()

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  // Repository data flows
  val branches = repository.allBranches.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val users = repository.allUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val courses = repository.allCourses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val enrollments = repository.allEnrollments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val vehicles = repository.allVehicles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val payments = repository.allPayments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val practicalSessions = repository.allPracticalSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val examSessions = repository.allExamSessions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val candidates = repository.allCandidates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val allLessonProgress = repository.allLessonProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val certificates = repository.allCertificates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val insurancePolicies = repository.allInsurancePolicies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val auditLogs = repository.allAuditLogs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val expiryAlerts = repository.allExpiryAlerts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val unreadAlertsCount = repository.unreadAlertsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
  val pendingDiscountApprovals = repository.pendingDiscountApprovals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val candidatesPendingApproval = repository.candidatesPendingApproval.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  val candidatesReadyForApplication = repository.candidatesReadyForApplication.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Direct Firestore Branches Flow
  private val _firestoreBranches = MutableStateFlow<List<FirestoreBranchDocument>>(emptyList())
  val firestoreBranches: StateFlow<List<FirestoreBranchDocument>> = _firestoreBranches.asStateFlow()

  private val _isFirestoreLoading = MutableStateFlow(false)
  val isFirestoreLoading: StateFlow<Boolean> = _isFirestoreLoading.asStateFlow()

  init {
    // Ensure initial seed data is loaded if database was freshly created
    viewModelScope.launch(Dispatchers.IO) {
      val existingBranches = database.driveSchoolDao().getAllBranches().firstOrNull()
      if (existingBranches.isNullOrEmpty()) {
        populateInitialDatabase(database.driveSchoolDao())
      }
      refreshFirestoreBranches()
    }
  }

  fun refreshFirestoreBranches() {
    viewModelScope.launch(Dispatchers.IO) {
      _isFirestoreLoading.value = true
      try {
        val list = syncManager.fetchBranchesFromFirestore()
        _firestoreBranches.value = list
      } catch (e: Exception) {
        // Handled in sync manager
      } finally {
        _isFirestoreLoading.value = false
      }
    }
  }

  fun switchRole(role: UserRole) {
    _activeUserOverride.value = null
    _currentRole.value = role
    _currentTab.value = when (role) {
      UserRole.SUPER_ADMIN -> "DASHBOARD"
      UserRole.BRANCH_MANAGER -> "MANAGER_KPIS"
      UserRole.SECRETARY -> "SECRETARY_OPS"
      UserRole.INSTRUCTOR -> "INSTRUCTOR_CLASSES"
      UserRole.STUDENT -> "STUDENT_LEARNING"
    }
  }

  fun toggleLocale() {
    _currentLocale.value = if (_currentLocale.value == "en") "fr" else "en"
  }

  fun selectTab(tab: String) {
    _currentTab.value = tab
  }

  fun selectLesson(lesson: LessonEntity?) {
    _selectedLesson.value = lesson
  }

  fun selectCertificate(cert: CertificateEntity?) {
    _selectedCertificate.value = cert
  }

  fun showMessage(msg: String) {
    _snackbarMessage.value = msg
  }

  fun clearMessage() {
    _snackbarMessage.value = null
  }

  // Active current user entity based on active role & override
  val currentUser: StateFlow<UserEntity?> = combine(users, currentRole, _activeUserOverride) { userList, role, overrideUser ->
    if (overrideUser != null) {
      userList.find { it.id == overrideUser.id } ?: overrideUser
    } else {
      userList.find { it.role == role } ?: userList.firstOrNull()
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  fun navigateToAuth(screen: String) {
    _authScreen.value = screen
  }

  fun signIn(emailOrPhone: String, password: String) {
    viewModelScope.launch {
      val cleanInput = emailOrPhone.trim()
      try {
        require(cleanInput.contains("@")) { "Use your email address to sign in." }
        val result = FirebaseAuth.getInstance()
          .signInWithEmailAndPassword(cleanInput, password)
          .await()
        val downloadResult = syncManager.pullAllFromFirestore()
        if (downloadResult is SyncState.Error) {
          showMessage(downloadResult.error)
          return@launch
        }
        var syncedUsers = database.driveSchoolDao().getAllUsers().first()
        if (syncedUsers.isEmpty() && cleanInput.equals(ProductionSeedData.admin.email, ignoreCase = true)) {
          database.driveSchoolDao().insertUser(ProductionSeedData.admin)
          syncManager.pushUser(ProductionSeedData.admin)
          syncedUsers = listOf(ProductionSeedData.admin)
        }
        val matched = syncedUsers.firstOrNull {
          it.email.equals(result.user?.email ?: cleanInput, ignoreCase = true)
        }
        if (matched == null) {
          FirebaseAuth.getInstance().signOut()
          showMessage("Your Firebase account has no ZION digital profile yet.")
        } else {
          _activeUserOverride.value = matched
          _currentRole.value = matched.role
          _authScreen.value = "MAIN_APP"
          syncManager.startRealtimeListeners(viewModelScope)
          showMessage("Welcome back, ${matched.name}!")
        }
      } catch (e: Exception) {
        showMessage(e.message ?: "Sign-in failed.")
      }
    }
  }

  fun signInWithGoogle(activity: Activity) {
    viewModelScope.launch {
      try {
        val webClientId = activity.getString(R.string.google_web_client_id)
        require(webClientId.isNotBlank() && !webClientId.startsWith("REPLACE_")) {
          "Google sign-in is not configured. Add the Firebase web client ID to google-services.json."
        }

        val googleIdOption = GetGoogleIdOption.Builder()
          .setServerClientId(webClientId)
          .setFilterByAuthorizedAccounts(false)
          .setAutoSelectEnabled(false)
          .build()
        val request = GetCredentialRequest.Builder()
          .addCredentialOption(googleIdOption)
          .build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential
        require(credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          "Google credential was not returned."
        }

        val googleCredential = try {
          GoogleIdTokenCredential.createFrom(credential.data)
        } catch (e: GoogleIdTokenParsingException) {
          throw IllegalStateException("Google sign-in returned an invalid token.", e)
        }
        val firebaseCredential = GoogleAuthProvider.getCredential(googleCredential.idToken, null)
        val firebaseUser = FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await().user
          ?: error("Firebase did not return a signed-in user.")
        val downloadResult = syncManager.pullAllFromFirestore()
        if (downloadResult is SyncState.Error) {
          showMessage(downloadResult.error)
          return@launch
        }
        val matched = database.driveSchoolDao().getAllUsers().firstOrNull { it.email.equals(firebaseUser.email, ignoreCase = true) }

        if (matched == null) {
          FirebaseAuth.getInstance().signOut()
          showMessage("This Google account is not registered in ZION digital.")
        } else {
          _activeUserOverride.value = matched
          _currentRole.value = matched.role
          _authScreen.value = "MAIN_APP"
          syncManager.startRealtimeListeners(viewModelScope)
          showMessage("Welcome back, ${matched.name}!")
        }
      } catch (e: Exception) {
        showMessage(e.message ?: "Google sign-in failed.")
      }
    }
  }

  fun quickSignInUser(user: UserEntity) {
    _activeUserOverride.value = user
    _currentRole.value = user.role
    _authScreen.value = "MAIN_APP"
    showMessage("Signed in as ${user.name} (${user.role.name})")
  }

  fun signOut() {
    syncManager.stopRealtimeListeners()
    FirebaseAuth.getInstance().signOut()
    _activeUserOverride.value = null
    _authScreen.value = "SIGN_IN"
    showMessage("Signed out successfully.")
  }

  fun updateProfile(userId: String, name: String, phone: String, locale: String) {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val updated = repository.updateStudentProfile(userId, name, phone, locale)
        syncManager.pushUser(updated)
        withContext(Dispatchers.Main) {
          _activeUserOverride.value = updated
          if (locale != _currentLocale.value) {
            _currentLocale.value = locale
          }
          showMessage(if (locale == "fr") "Profil mis à jour avec succès !" else "Profile updated successfully!")
        }
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          showMessage("Error updating profile: ${e.message}")
        }
      }
    }
  }

  fun signUpStudent(
    name: String,
    email: String,
    password: String,
    phone: String,
    countryCode: String,
    branchId: String?,
    courseId: String,
    mode: EnrollmentMode
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      var firebaseUserCreated = false
      try {
        require(password.length >= 6) { "Password must contain at least 6 characters." }
        val authResult = FirebaseAuth.getInstance()
          .createUserWithEmailAndPassword(email.trim(), password)
          .await()
        firebaseUserCreated = true

        val newStudent = repository.registerStudent(
          name = name,
          email = email,
          phone = phone,
          countryCode = countryCode,
          selectedBranchId = branchId,
          preferredLocale = _currentLocale.value,
          actorId = authResult.user?.uid ?: "self-signup",
          actorRole = "STUDENT"
        )

        if (courseId.isNotBlank()) {
          repository.createEnrollment(
            student = newStudent,
            courseId = courseId,
            mode = mode
          )
        }

        val syncResult = syncManager.syncAll()
        withContext(Dispatchers.Main) {
          _activeUserOverride.value = newStudent
          _currentRole.value = UserRole.STUDENT
          _authScreen.value = "MAIN_APP"
          syncManager.startRealtimeListeners(viewModelScope)
          when (syncResult) {
            is SyncState.Success -> showMessage("Account created and synced to Firebase.")
            is SyncState.Error -> showMessage("Account created locally, but Firebase sync failed: ${syncResult.error}")
            else -> showMessage("Account created locally. Firebase sync is pending.")
          }
        }
      } catch (e: Exception) {
        if (firebaseUserCreated) {
          FirebaseAuth.getInstance().currentUser?.delete()?.await()
        }
        withContext(Dispatchers.Main) {
          showMessage(e.message ?: "Account creation failed.")
        }
      }
    }
  }

  override fun onCleared() {
    syncManager.stopRealtimeListeners()
    super.onCleared()
  }

  fun createBranch(
    name: String,
    city: String,
    address: String,
    phone: String,
    managerName: String,
    managerEmail: String,
    managerPhone: String,
    secretaryName: String,
    secretaryEmail: String,
    secretaryPhone: String,
    initialStudentName: String? = null,
    initialStudentEmail: String? = null,
    initialStudentPhone: String? = null,
    initialCourseId: String? = null
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val admin = currentUser.value
      val branch = repository.createBranchWithStaff(
        name = name,
        city = city,
        address = address,
        phone = phone,
        managerName = managerName,
        managerEmail = managerEmail,
        managerPhone = managerPhone,
        secretaryName = secretaryName,
        secretaryEmail = secretaryEmail,
        secretaryPhone = secretaryPhone,
        initialStudentName = initialStudentName,
        initialStudentEmail = initialStudentEmail,
        initialStudentPhone = initialStudentPhone,
        initialCourseId = initialCourseId,
        creatorUser = admin
      )
      // Sync all tables to Firestore
      syncManager.syncAll()
      refreshFirestoreBranches()
      withContext(Dispatchers.Main) {
        val studentMsg = if (!initialStudentName.isNullOrBlank()) " and Student $initialStudentName" else ""
        showMessage("New branch '${branch.name}' created with Manager $managerName, Secretary $secretaryName$studentMsg!")
      }
    }
  }

  fun addStudentToBranch(
    name: String,
    email: String,
    phone: String,
    branchId: String,
    courseId: String
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val admin = currentUser.value
      repository.addStudentToBranch(name, email, phone, branchId, courseId, admin)
      syncManager.syncAll()
      withContext(Dispatchers.Main) {
        showMessage("Enrolled student $name at branch successfully!")
      }
    }
  }

  fun updateBranch(
    branchId: String,
    name: String,
    city: String,
    address: String,
    phone: String,
    isVirtual: Boolean,
    managerUser: UserEntity?,
    secretaryUser: UserEntity?,
    newManagerDetails: Triple<String, String, String>? = null,
    newSecretaryDetails: Triple<String, String, String>? = null
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val admin = currentUser.value
      val (updatedBranch, resolvedManager, resolvedSecretary) = repository.updateBranchWithStaff(
        branchId = branchId,
        name = name,
        city = city,
        address = address,
        phone = phone,
        isVirtual = isVirtual,
        managerUser = managerUser,
        secretaryUser = secretaryUser,
        newManagerDetails = newManagerDetails,
        newSecretaryDetails = newSecretaryDetails,
        actorUser = admin
      )
      // Push directly to Firestore
      syncManager.pushBranch(updatedBranch, resolvedManager, resolvedSecretary)
      refreshFirestoreBranches()
      withContext(Dispatchers.Main) {
        showMessage("Branch '${updatedBranch.name}' updated and persisted to Firestore!")
      }
    }
  }

  fun deleteBranch(branchId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val admin = currentUser.value
      repository.deleteBranch(branchId, admin)
      syncManager.deleteBranchFromFirestore(branchId)
      refreshFirestoreBranches()
      withContext(Dispatchers.Main) {
        showMessage("Branch deleted and removed from Firestore.")
      }
    }
  }

  fun persistBranchToFirestore(branch: BranchEntity) {
    viewModelScope.launch(Dispatchers.IO) {
      val userList = users.value
      val manager = userList.find { it.branchId == branch.id && it.role == UserRole.BRANCH_MANAGER }
      val secretary = userList.find { it.branchId == branch.id && it.role == UserRole.SECRETARY }
      val success = syncManager.pushBranch(branch, manager, secretary)
      refreshFirestoreBranches()
      withContext(Dispatchers.Main) {
        if (success) {
          showMessage("Branch '${branch.name}' synced directly to Firestore!")
        } else {
          showMessage("Branch saved in local cache (queued for Firestore).")
        }
      }
    }
  }

  // Super Admin: Staff Management (Manager, Instructor, Secretary)
  fun createStaffMember(
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?,
    countryCode: String = "CM",
    preferredLocale: String = "en"
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val admin = currentUser.value
      val newStaff = repository.createStaffMember(
        name = name,
        email = email,
        phone = phone,
        role = role,
        branchId = branchId,
        countryCode = countryCode,
        preferredLocale = preferredLocale,
        actorUser = admin
      )
      // Push to Firestore immediately
      syncManager.pushUser(newStaff)
      withContext(Dispatchers.Main) {
        val roleLabel = when (role) {
          UserRole.BRANCH_MANAGER -> "Branch Manager"
          UserRole.INSTRUCTOR -> "Instructor"
          UserRole.SECRETARY -> "Secretary"
          else -> role.name
        }
        showMessage("Staff member '${newStaff.name}' ($roleLabel) created & saved to Firestore!")
      }
    }
  }

  fun updateStaffMember(
    userId: String,
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val admin = currentUser.value
      val updated = repository.updateStaffMember(
        userId = userId,
        name = name,
        email = email,
        phone = phone,
        role = role,
        branchId = branchId,
        actorUser = admin
      )
      // Push to Firestore immediately
      syncManager.pushUser(updated)
      withContext(Dispatchers.Main) {
        val roleLabel = when (role) {
          UserRole.BRANCH_MANAGER -> "Branch Manager"
          UserRole.INSTRUCTOR -> "Instructor"
          UserRole.SECRETARY -> "Secretary"
          else -> role.name
        }
        showMessage("Staff member '${updated.name}' updated to $roleLabel & saved to Firestore!")
      }
    }
  }

  fun deleteStaffMember(userId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val admin = currentUser.value
      repository.deleteStaffMember(userId, admin)
      syncManager.deleteUserFromFirestore(userId)
      withContext(Dispatchers.Main) {
        showMessage("Staff member deleted and removed from Firestore.")
      }
    }
  }

  // Actions
  fun registerWalkInStudent(
    name: String,
    email: String,
    phone: String,
    countryCode: String,
    branchId: String?,
    courseId: String,
    mode: EnrollmentMode,
    negotiatedFee: Double?
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val secretary = currentUser.value
      val newStudent = repository.registerStudent(
        name = name,
        email = email,
        phone = phone,
        countryCode = countryCode,
        selectedBranchId = branchId,
        preferredLocale = _currentLocale.value,
        actorId = secretary?.id ?: "sec-01",
        actorRole = "SECRETARY"
      )
      val result = repository.createEnrollment(
        student = newStudent,
        courseId = courseId,
        mode = mode,
        negotiatedFee = negotiatedFee,
        secretaryUser = secretary
      )
      withContext(Dispatchers.Main) {
        if (result.isSuccess) {
          val enrollment = result.getOrNull()
          val msg = if (enrollment?.isDiscountApproved == false) {
            "Student registered! Discount exceeds 15% - queued for Branch Manager approval."
          } else {
            "Student registered successfully with credentials sent via SMS!"
          }
          showMessage(msg)
        } else {
          showMessage("Error: ${result.exceptionOrNull()?.message}")
        }
      }
    }
  }

  fun approveDiscount(enrollmentId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val manager = currentUser.value ?: return@launch
      val ok = repository.approveDiscount(enrollmentId, manager)
      withContext(Dispatchers.Main) {
        showMessage(if (ok) "Negotiated fee discount approved!" else "Failed to approve discount.")
      }
    }
  }

  fun recordPayment(
    enrollmentId: String,
    studentId: String,
    amount: Double,
    channel: PaymentChannel,
    notes: String?
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val secretary = if (channel == PaymentChannel.CASH) currentUser.value else null
      val payment = repository.recordPayment(
        enrollmentId = enrollmentId,
        studentId = studentId,
        amount = amount,
        channel = channel,
        collectorSecretary = secretary,
        notes = notes
      )
      // Sync payment to cloud Firestore immediately (BR-05)
      syncManager.pushPayment(payment)
      withContext(Dispatchers.Main) {
        showMessage("Payment confirmed! Ref: ${payment.reference}. Payment gate lifted.")
      }
    }
  }

  fun bookPracticalSession(
    enrollmentId: String,
    studentId: String,
    instructorId: String,
    vehicleId: String,
    scheduledAt: Long
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val result = repository.bookPracticalSession(
        enrollmentId = enrollmentId,
        studentId = studentId,
        instructorId = instructorId,
        vehicleId = vehicleId,
        scheduledAt = scheduledAt
      )
      withContext(Dispatchers.Main) {
        if (result.isSuccess) {
          showMessage("Practical driving session successfully scheduled!")
        } else {
          showMessage(result.exceptionOrNull()?.message ?: "Booking failed")
        }
      }
    }
  }

  fun signOffPracticalSession(
    sessionId: String,
    attendance: AttendanceStatus,
    odometerEnd: Int,
    feedback: String?
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val instructor = currentUser.value ?: return@launch
      val ok = repository.signOffPracticalSession(
        sessionId = sessionId,
        attendance = attendance,
        odometerEnd = odometerEnd,
        feedback = feedback,
        instructor = instructor
      )
      withContext(Dispatchers.Main) {
        showMessage(if (ok) "Session sign-off recorded & odometer registry updated!" else "Error updating session.")
      }
    }
  }

  fun recommendCandidateForExam(
    examSessionId: String,
    enrollmentId: String,
    studentId: String
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val instructor = currentUser.value ?: return@launch
      repository.recommendCandidate(examSessionId, enrollmentId, studentId, instructor)
      withContext(Dispatchers.Main) {
        showMessage("Candidate recommended for exam session! Forwarded to Branch Manager.")
      }
    }
  }

  fun approveExamCandidate(candidateId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val manager = currentUser.value ?: return@launch
      val ok = repository.approveExamCandidate(candidateId, manager)
      withContext(Dispatchers.Main) {
        showMessage(if (ok) "Candidate approved! Ready for Secretary to apply dossier." else "Failed to approve.")
      }
    }
  }

  fun applyExamCandidate(candidateId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val secretary = currentUser.value ?: return@launch
      val ok = repository.applyExamCandidate(candidateId, secretary)
      withContext(Dispatchers.Main) {
        showMessage(if (ok) "Dossier officially submitted for ministerial examination!" else "Failed to apply.")
      }
    }
  }

  fun recordExamResult(candidateId: String, score: Double, passed: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      val cert = repository.recordExamResult(candidateId, score, passed)
      if (cert != null) {
        // Sync generated certificate to cloud verification registry
        syncManager.pushCertificate(cert)
      }
      withContext(Dispatchers.Main) {
        if (cert != null) {
          _selectedCertificate.value = cert
          showMessage("Candidate PASSED with ${score.toInt()}%! Verifiable digital certificate generated with QR Code.")
        } else {
          showMessage("Result recorded: ${if (passed) "Passed" else "Failed"} ($score%).")
        }
      }
    }
  }

  fun triggerCloudSync() {
    viewModelScope.launch {
      val result = syncManager.syncAll()
      refreshFirestoreBranches()
      withContext(Dispatchers.Main) {
        when (result) {
          is SyncState.Success -> showMessage(result.message)
          is SyncState.Offline -> showMessage(result.message)
          is SyncState.Error -> showMessage(result.error)
          else -> {}
        }
      }
    }
  }

  fun issueInsurance(
    holderId: String,
    holderName: String,
    tariffName: String,
    tariffNameFr: String,
    plate: String,
    premium: Double,
    durationMonths: Int
  ) {
    viewModelScope.launch(Dispatchers.IO) {
      val issuer = currentUser.value
      val policy = repository.issueInsurancePolicy(
        holderId = holderId,
        holderName = holderName,
        tariffName = tariffName,
        tariffNameFr = tariffNameFr,
        vehiclePlate = plate,
        premiumAmount = premium,
        durationMonths = durationMonths,
        issuer = issuer
      )
      withContext(Dispatchers.Main) {
        showMessage("Insurance policy ${policy.tariffName} issued for $plate!")
      }
    }
  }

  fun runDailyExpiryScan() {
    viewModelScope.launch(Dispatchers.IO) {
      val count = repository.runDailyExpiryScan()
      withContext(Dispatchers.Main) {
        showMessage("Daily Expirable Scan Complete! Dispatched $count renewal/overdue alerts via In-App, SMS & Email.")
      }
    }
  }

  fun verifyCertificateUuid(uuid: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val cert = repository.getCertificateByUuid(uuid.trim())
      withContext(Dispatchers.Main) {
        _verificationResult.value = cert
        if (cert == null) {
          showMessage("Verification Failed: No official certificate found with UUID $uuid")
        }
      }
    }
  }

  fun markAlertRead(alertId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.markAlertAsRead(alertId)
    }
  }

  fun completeLesson(enrollmentId: String, lessonId: String, score: Double? = null) {
    viewModelScope.launch(Dispatchers.IO) {
      repository.updateLessonProgress(enrollmentId, lessonId, score)
      withContext(Dispatchers.Main) {
        showMessage("Lesson progress updated!")
      }
    }
  }

}
