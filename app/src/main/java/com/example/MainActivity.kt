package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.driveschool.data.model.*
import com.example.driveschool.ui.components.AppTopBar
import com.example.driveschool.ui.components.PaymentGatewayModal
import com.example.driveschool.ui.screens.*
import com.example.driveschool.ui.screens.auth.SignInScreen
import com.example.driveschool.ui.screens.auth.SignUpScreen
import com.example.driveschool.ui.viewmodel.DriveSchoolViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DriveSchoolTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      DriveSchoolTheme {
        DriveSchoolApp()
      }
    }
  }
}

@Composable
fun DriveSchoolApp(
  viewModel: DriveSchoolViewModel = viewModel()
) {
  val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
  val currentLocale by viewModel.currentLocale.collectAsStateWithLifecycle()
  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val selectedLesson by viewModel.selectedLesson.collectAsStateWithLifecycle()
  val selectedCertificate by viewModel.selectedCertificate.collectAsStateWithLifecycle()
  val verificationResult by viewModel.verificationResult.collectAsStateWithLifecycle()
  val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

  val branches by viewModel.branches.collectAsStateWithLifecycle()
  val users by viewModel.users.collectAsStateWithLifecycle()
  val courses by viewModel.courses.collectAsStateWithLifecycle()
  val enrollments by viewModel.enrollments.collectAsStateWithLifecycle()
  val vehicles by viewModel.vehicles.collectAsStateWithLifecycle()
  val payments by viewModel.payments.collectAsStateWithLifecycle()
  val practicalSessions by viewModel.practicalSessions.collectAsStateWithLifecycle()
  val examSessions by viewModel.examSessions.collectAsStateWithLifecycle()
  val candidates by viewModel.candidates.collectAsStateWithLifecycle()
  val allLessonProgress by viewModel.allLessonProgress.collectAsStateWithLifecycle()
  val certificates by viewModel.certificates.collectAsStateWithLifecycle()
  val insurancePolicies by viewModel.insurancePolicies.collectAsStateWithLifecycle()
  val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()
  val expiryAlerts by viewModel.expiryAlerts.collectAsStateWithLifecycle()
  val unreadAlertsCount by viewModel.unreadAlertsCount.collectAsStateWithLifecycle()
  val pendingDiscountApprovals by viewModel.pendingDiscountApprovals.collectAsStateWithLifecycle()
  val candidatesPendingApproval by viewModel.candidatesPendingApproval.collectAsStateWithLifecycle()
  val candidatesReadyForApplication by viewModel.candidatesReadyForApplication.collectAsStateWithLifecycle()
  val authScreen by viewModel.authScreen.collectAsStateWithLifecycle()
  val syncState by viewModel.syncState.collectAsStateWithLifecycle()
  val isFirestoreLoading by viewModel.isFirestoreLoading.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  var paymentModalEnrollment by remember { mutableStateOf<EnrollmentEntity?>(null) }
  var showVerifyScreen by remember { mutableStateOf(false) }
  var showProfileScreen by remember { mutableStateOf(false) }

  // Authentication Screens (Sign In & Sign Up)
  if (authScreen == "SIGN_IN") {
    SignInScreen(
      currentLocale = currentLocale,
      users = users,
      branches = branches,
      onSignIn = { email, pass -> viewModel.signIn(email, pass) },
      onGoogleSignIn = { viewModel.signInWithGoogle(this@MainActivity) },
      onQuickSignInUser = { user -> viewModel.quickSignInUser(user) },
      onNavigateToSignUp = { viewModel.navigateToAuth("SIGN_UP") }
    )
    return
  } else if (authScreen == "SIGN_UP") {
    SignUpScreen(
      currentLocale = currentLocale,
      branches = branches,
      courses = courses,
      onSignUp = { name, email, password, phone, country, branchId, courseId, mode ->
        viewModel.signUpStudent(name, email, password, phone, country, branchId, courseId, mode)
      },
      onNavigateToSignIn = { viewModel.navigateToAuth("SIGN_IN") }
    )
    return
  }

  // Observe snackbar messages
  LaunchedEffect(snackbarMessage) {
    snackbarMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearMessage()
    }
  }

  // Handle hardware back button
  BackHandler(enabled = selectedLesson != null || showVerifyScreen || showProfileScreen || selectedCertificate != null || currentTab == "BRANCHES" || currentTab == "STAFF") {
    when {
      selectedLesson != null -> viewModel.selectLesson(null)
      showVerifyScreen -> showVerifyScreen = false
      showProfileScreen -> showProfileScreen = false
      selectedCertificate != null -> viewModel.selectCertificate(null)
      currentTab == "BRANCHES" || currentTab == "STAFF" -> viewModel.selectTab("DASHBOARD")
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      if (selectedLesson == null && !showVerifyScreen && !showProfileScreen && currentTab != "BRANCHES" && currentTab != "STAFF") {
        AppTopBar(
          currentRole = currentRole,
          currentUser = currentUser,
          currentLocale = currentLocale,
          unreadAlertsCount = unreadAlertsCount,
          onToggleLocale = { viewModel.toggleLocale() },
          onAlertsClick = { viewModel.selectTab("ALERTS") },
          onVerifyClick = { showVerifyScreen = true },
          onProfileClick = { showProfileScreen = true },
          onSignOut = { viewModel.signOut() }
        )
      }
    },
    bottomBar = {
      if (selectedLesson == null && !showVerifyScreen && !showProfileScreen) {
        NavigationBar(
          containerColor = NavyDark,
          contentColor = Color.White,
          modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = currentTab == "DASHBOARD" || currentTab == "MANAGER_KPIS" || currentTab == "SECRETARY_OPS" || currentTab == "INSTRUCTOR_CLASSES" || currentTab == "STUDENT_LEARNING",
            onClick = {
              viewModel.selectTab(
                when (currentRole) {
                  UserRole.SUPER_ADMIN -> "DASHBOARD"
                  UserRole.BRANCH_MANAGER -> "MANAGER_KPIS"
                  UserRole.SECRETARY -> "SECRETARY_OPS"
                  UserRole.INSTRUCTOR -> "INSTRUCTOR_CLASSES"
                  UserRole.STUDENT -> "STUDENT_LEARNING"
                }
              )
            },
            icon = {
              Icon(
                when (currentRole) {
                  UserRole.SUPER_ADMIN -> Icons.Default.Dashboard
                  UserRole.BRANCH_MANAGER -> Icons.Default.Business
                  UserRole.SECRETARY -> Icons.Default.Desk
                  UserRole.INSTRUCTOR -> Icons.Default.DirectionsCar
                  UserRole.STUDENT -> Icons.Default.School
                },
                contentDescription = null
              )
            },
            label = {
              Text(
                text = when (currentRole) {
                  UserRole.SUPER_ADMIN -> if (currentLocale == "fr") "Vue Générale" else "Overview"
                  UserRole.BRANCH_MANAGER -> if (currentLocale == "fr") "Agence" else "Branch"
                  UserRole.SECRETARY -> if (currentLocale == "fr") "Guichet" else "Front-Desk"
                  UserRole.INSTRUCTOR -> if (currentLocale == "fr") "Séances" else "Sessions"
                  UserRole.STUDENT -> if (currentLocale == "fr") "Mes Cours" else "My Courses"
                },
                fontSize = 11.sp
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyDark,
              selectedTextColor = AmberAccent,
              indicatorColor = AmberAccent,
              unselectedIconColor = Color.LightGray,
              unselectedTextColor = Color.LightGray
            )
          )

          // For Super Admin: Tab 2 is Branches
          if (currentRole == UserRole.SUPER_ADMIN) {
            NavigationBarItem(
              selected = currentTab == "BRANCHES",
              onClick = { viewModel.selectTab("BRANCHES") },
              icon = { Icon(Icons.Default.Storefront, contentDescription = "Branches") },
              label = { Text(if (currentLocale == "fr") "Agences" else "Branches", fontSize = 11.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = AmberAccent,
                indicatorColor = AmberAccent,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray
              ),
              modifier = Modifier.testTag("nav_item_branches")
            )

            NavigationBarItem(
              selected = currentTab == "STAFF",
              onClick = { viewModel.selectTab("STAFF") },
              icon = { Icon(Icons.Default.Badge, contentDescription = "Staff") },
              label = { Text(if (currentLocale == "fr") "Personnel" else "Staff", fontSize = 11.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = AmberAccent,
                indicatorColor = AmberAccent,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray
              ),
              modifier = Modifier.testTag("nav_item_staff")
            )
          }

          // Common Tab 2: Expiry Alerts & Pipeline
          NavigationBarItem(
            selected = currentTab == "ALERTS",
            onClick = { viewModel.selectTab("ALERTS") },
            icon = {
              BadgedBox(badge = {
                if (unreadAlertsCount > 0) {
                  Badge(containerColor = MaterialTheme.colorScheme.error) {
                    Text("$unreadAlertsCount")
                  }
                }
              }) {
                Icon(Icons.Default.Campaign, contentDescription = null)
              }
            },
            label = { Text(if (currentLocale == "fr") "Alertes" else "Alerts", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyDark,
              selectedTextColor = AmberAccent,
              indicatorColor = AmberAccent,
              unselectedIconColor = Color.LightGray,
              unselectedTextColor = Color.LightGray
            )
          )

          // Common Tab 3: Verification (for non-Super Admin)
          if (currentRole != UserRole.SUPER_ADMIN) {
            NavigationBarItem(
              selected = showVerifyScreen || currentTab == "VERIFY",
              onClick = { showVerifyScreen = true },
              icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
              label = { Text(if (currentLocale == "fr") "Vérification" else "Verify", fontSize = 11.sp) },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = NavyDark,
                selectedTextColor = AmberAccent,
                indicatorColor = AmberAccent,
                unselectedIconColor = Color.LightGray,
                unselectedTextColor = Color.LightGray
              )
            )
          }

          // Common Tab 4: Audit Logs
          NavigationBarItem(
            selected = currentTab == "AUDIT",
            onClick = { viewModel.selectTab("AUDIT") },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
            label = { Text(if (currentLocale == "fr") "Audit (FR-08)" else "Audit Trail", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = NavyDark,
              selectedTextColor = AmberAccent,
              indicatorColor = AmberAccent,
              unselectedIconColor = Color.LightGray,
              unselectedTextColor = Color.LightGray
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when {
        // Detailed Lesson Viewer
        selectedLesson != null -> {
          LmsLessonScreen(
            lesson = selectedLesson!!,
            currentLocale = currentLocale,
            onBack = { viewModel.selectLesson(null) },
            onCompleteLesson = { score ->
              val enr = enrollments.find { it.studentId == currentUser?.id } ?: enrollments.firstOrNull()
              if (enr != null) {
                viewModel.completeLesson(enr.id, selectedLesson!!.id, score)
              }
              viewModel.selectLesson(null)
            }
          )
        }

        // User Profile Screen
        showProfileScreen -> {
          UserProfileScreen(
            currentLocale = currentLocale,
            currentUser = currentUser,
            branches = branches,
            courses = courses,
            enrollments = enrollments,
            payments = payments,
            onBack = { showProfileScreen = false },
            onUpdateProfile = { uId, name, phone, loc ->
              viewModel.updateProfile(uId, name, phone, loc)
            },
            onOpenPaymentGateway = { enr ->
              paymentModalEnrollment = enr
            },
            onSignOut = {
              showProfileScreen = false
              viewModel.signOut()
            }
          )
        }

        // Public Certificate Verification Screen
        showVerifyScreen || selectedCertificate != null -> {
          PublicVerifyScreen(
            currentLocale = currentLocale,
            initialCertificate = selectedCertificate,
            onBack = {
              showVerifyScreen = false
              viewModel.selectCertificate(null)
            },
            onVerifyUuid = { uuid -> viewModel.verifyCertificateUuid(uuid) },
            verificationResult = verificationResult
          )
        }

        // Tab Views
        currentTab == "BRANCHES" -> {
          BranchManagementScreen(
            currentLocale = currentLocale,
            branches = branches,
            users = users,
            enrollments = enrollments,
            payments = payments,
            syncState = syncState,
            onBack = { viewModel.selectTab("DASHBOARD") },
            onCloudSync = { viewModel.triggerCloudSync() },
            onCreateBranch = { name, city, addr, phone, mName, mEmail, mPhone, sName, sEmail, sPhone, stName, stEmail, stPhone, cId ->
              viewModel.createBranch(name, city, addr, phone, mName, mEmail, mPhone, sName, sEmail, sPhone, stName, stEmail, stPhone, cId)
            },
            onUpdateBranch = { branchId, name, city, addr, phone, isVirtual, mUser, sUser, newM, newS ->
              viewModel.updateBranch(branchId, name, city, addr, phone, isVirtual, mUser, sUser, newM, newS)
            },
            onDeleteBranch = { branchId -> viewModel.deleteBranch(branchId) },
            onPersistBranchToFirestore = { branch -> viewModel.persistBranchToFirestore(branch) },
            onRefreshFirestore = { viewModel.refreshFirestoreBranches() },
            isFirestoreLoading = isFirestoreLoading
          )
        }

        currentTab == "STAFF" -> {
          StaffManagementScreen(
            currentLocale = currentLocale,
            users = users,
            branches = branches,
            syncState = syncState,
            onBack = { viewModel.selectTab("DASHBOARD") },
            onCloudSync = { viewModel.triggerCloudSync() },
            onCreateStaff = { name, email, phone, role, branchId, country, locale ->
              viewModel.createStaffMember(name, email, phone, role, branchId, country, locale)
            },
            onUpdateStaff = { userId, name, email, phone, role, branchId ->
              viewModel.updateStaffMember(userId, name, email, phone, role, branchId)
            },
            onDeleteStaff = { userId ->
              viewModel.deleteStaffMember(userId)
            },
            onSwitchUser = { user ->
              viewModel.quickSignInUser(user)
            }
          )
        }

        currentTab == "ALERTS" -> {
          AlertsCenterScreen(
            currentLocale = currentLocale,
            alerts = expiryAlerts,
            insurancePolicies = insurancePolicies,
            onRunDailyScan = { viewModel.runDailyExpiryScan() },
            onMarkAlertRead = { id -> viewModel.markAlertRead(id) }
          )
        }

        currentTab == "AUDIT" -> {
          AuditLogScreen(
            currentLocale = currentLocale,
            auditLogs = auditLogs
          )
        }

        // Primary Role Screens
        currentRole == UserRole.SUPER_ADMIN -> {
          OwnerDashboardScreen(
            currentLocale = currentLocale,
            branches = branches,
            enrollments = enrollments,
            payments = payments,
            vehicles = vehicles,
            candidates = candidates,
            insurancePolicies = insurancePolicies,
            users = users,
            courses = courses,
            onRunDailyScan = { viewModel.runDailyExpiryScan() },
            onNavigateTab = { tab -> viewModel.selectTab(tab) },
            onCloudSync = { viewModel.triggerCloudSync() },
            onCreateBranch = { bName, city, addr, phone, mName, mEmail, mPhone, sName, sEmail, sPhone, stName, stEmail, stPhone, cId ->
              viewModel.createBranch(bName, city, addr, phone, mName, mEmail, mPhone, sName, sEmail, sPhone, stName, stEmail, stPhone, cId)
            },
            onAddStudentToBranch = { name, email, phone, branchId, courseId ->
              viewModel.addStudentToBranch(name, email, phone, branchId, courseId)
            },
            onSwitchUser = { user -> viewModel.quickSignInUser(user) },
            onOpenBranchManagement = { viewModel.selectTab("BRANCHES") },
            onOpenStaffManagement = { viewModel.selectTab("STAFF") }
          )
        }

        currentRole == UserRole.BRANCH_MANAGER -> {
          BranchManagerScreen(
            currentLocale = currentLocale,
            currentUser = currentUser,
            pendingDiscountApprovals = pendingDiscountApprovals,
            candidatesPendingApproval = candidatesPendingApproval,
            users = users,
            courses = courses,
            vehicles = vehicles,
            branches = branches,
            onApproveDiscount = { id -> viewModel.approveDiscount(id) },
            onApproveCandidate = { id -> viewModel.approveExamCandidate(id) }
          )
        }

        currentRole == UserRole.SECRETARY -> {
          SecretaryScreen(
            currentLocale = currentLocale,
            currentUser = currentUser,
            branches = branches,
            courses = courses,
            enrollments = enrollments,
            users = users,
            candidatesReadyForApplication = candidatesReadyForApplication,
            onRegisterStudent = { name, email, phone, country, branchId, courseId, mode, fee ->
              viewModel.registerWalkInStudent(name, email, phone, country, branchId, courseId, mode, fee)
            },
            onRecordCashPayment = { enrId, studentId, amount, notes ->
              viewModel.recordPayment(enrId, studentId, amount, PaymentChannel.CASH, notes)
            },
            onApplyCandidate = { id -> viewModel.applyExamCandidate(id) },
            onIssueInsurance = { holderId, name, tariffEn, tariffFr, plate, premium, months ->
              viewModel.issueInsurance(holderId, name, tariffEn, tariffFr, plate, premium, months)
            }
          )
        }

        currentRole == UserRole.INSTRUCTOR -> {
          InstructorScreen(
            currentLocale = currentLocale,
            currentUser = currentUser,
            practicalSessions = practicalSessions,
            users = users,
            vehicles = vehicles,
            examSessions = examSessions,
            enrollments = enrollments,
            onSignOffSession = { sessionId, att, endKm, feedback ->
              viewModel.signOffPracticalSession(sessionId, att, endKm, feedback)
            },
            onRecommendCandidate = { examSessionId, enrId, studentId ->
              viewModel.recommendCandidateForExam(examSessionId, enrId, studentId)
            }
          )
        }

        currentRole == UserRole.STUDENT -> {
          val activeLessons = emptyList<LessonEntity>()

          StudentHomeScreen(
            currentLocale = currentLocale,
            currentUser = currentUser,
            allUsers = users,
            enrollments = enrollments,
            courses = courses,
            lessons = activeLessons,
            lessonProgressList = allLessonProgress,
            vehicles = vehicles,
            practicalSessions = practicalSessions,
            examSessions = examSessions,
            certificates = certificates,
            candidates = candidates,
            onSelectStudentUser = { student: UserEntity ->
              viewModel.quickSignInUser(student)
            },
            onLessonClick = { lesson, isAccessible ->
              if (isAccessible) {
                viewModel.selectLesson(lesson)
              } else {
                val studentEnr = enrollments.find { it.studentId == currentUser?.id } ?: enrollments.firstOrNull()
                if (studentEnr != null) {
                  paymentModalEnrollment = studentEnr
                }
              }
            },
            onOpenPaymentDialog = { enr -> paymentModalEnrollment = enr },
            onBookPracticalSession = { enrId, studentId, instructorId, vehicleId, time ->
              viewModel.bookPracticalSession(enrId, studentId, instructorId, vehicleId, time)
            },
            onViewCertificate = { cert ->
              viewModel.selectCertificate(cert)
            },
            onOpenProfile = { showProfileScreen = true }
          )
        }
      }

      // Payment Gateway Dialog
      paymentModalEnrollment?.let { enr ->
        PaymentGatewayModal(
          currentLocale = currentLocale,
          enrollment = enr,
          onDismiss = { paymentModalEnrollment = null },
          onProcessPayment = { amount, channel, notes ->
            viewModel.recordPayment(
              enrollmentId = enr.id,
              studentId = enr.studentId,
              amount = amount,
              channel = channel,
              notes = notes
            )
            paymentModalEnrollment = null
          }
        )
      }
    }
  }
}
