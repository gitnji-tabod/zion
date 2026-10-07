package com.example.driveschool.data.sync

import android.content.Context
import android.util.Log
import com.example.R
import com.example.driveschool.data.db.DriveSchoolDao
import com.example.driveschool.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncState {
  object Idle : SyncState()
  object Syncing : SyncState()
  data class Success(val syncedCount: Int, val message: String) : SyncState()
  data class Offline(val queuedCount: Int, val message: String) : SyncState()
  data class Error(val error: String) : SyncState()
}

data class FirestoreBranchDocument(
  val id: String = "",
  val name: String = "",
  val city: String = "",
  val address: String = "",
  val phone: String = "",
  val isVirtual: Boolean = false,
  val managerId: String = "",
  val managerName: String = "",
  val managerEmail: String = "",
  val managerPhone: String = "",
  val secretaryId: String = "",
  val secretaryName: String = "",
  val secretaryEmail: String = "",
  val secretaryPhone: String = "",
  val syncedAt: Long = 0L
)

class DriveSchoolSyncManager(
  private val context: Context,
  private val dao: DriveSchoolDao
) {
  private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
  val syncState = _syncState.asStateFlow()

  private val _lastSyncTimestamp = MutableStateFlow(System.currentTimeMillis())
  val lastSyncTimestamp = _lastSyncTimestamp.asStateFlow()

  /**
   * Safely obtain FirebaseFirestore instance if Firebase is initialized.
   */
  private fun getFirestore(): FirebaseFirestore? {
    return try {
      if (FirebaseApp.getApps(context).isNotEmpty()) {
        val dbId = context.getString(R.string.firestore_database_id)
        FirebaseFirestore.getInstance(dbId)
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(TAG, "FirebaseFirestore instance not ready: ${e.message}")
      null
    }
  }

  private val listenerRegistrations = mutableListOf<ListenerRegistration>()

  suspend fun pullAllFromFirestore(): SyncState = withContext(Dispatchers.IO) {
    val firestore = authenticatedFirestore() ?: return@withContext SyncState.Error("Firebase authentication is required before downloading data.")
    try {
      pullCollection(firestore, "users", UserEntity::class.java) { dao.clearUsers(); it.forEach(dao::insertUser) }
      pullCollection(firestore, "branches", BranchEntity::class.java) { dao.clearBranches(); dao.insertBranches(it) }
      pullCollection(firestore, "courses", CourseEntity::class.java) { dao.clearCourses(); dao.insertCourses(it) }
      pullCollection(firestore, "fee_schedules", FeeScheduleEntity::class.java) { dao.clearFeeSchedules(); dao.insertFeeSchedules(it) }
      pullCollection(firestore, "enrollments", EnrollmentEntity::class.java) { dao.clearEnrollments(); it.forEach(dao::insertEnrollment) }
      pullCollection(firestore, "lessons", LessonEntity::class.java) { dao.clearLessons(); dao.insertLessons(it) }
      pullCollection(firestore, "lesson_progress", LessonProgressEntity::class.java) { dao.clearLessonProgress(); it.forEach(dao::insertProgress) }
      pullCollection(firestore, "payments", PaymentEntity::class.java) { dao.clearPayments(); it.forEach(dao::insertPayment) }
      pullCollection(firestore, "practical_sessions", PracticalSessionEntity::class.java) { dao.clearPracticalSessions(); it.forEach(dao::insertPracticalSession) }
      pullCollection(firestore, "vehicles", VehicleEntity::class.java) { dao.clearVehicles(); dao.insertVehicles(it) }
      pullCollection(firestore, "exam_sessions", ExamSessionEntity::class.java) { dao.clearExamSessions(); dao.insertExamSessions(it) }
      pullCollection(firestore, "exam_candidates", ExamCandidateEntity::class.java) { dao.clearCandidates(); it.forEach(dao::insertCandidate) }
      pullCollection(firestore, "certificates", CertificateEntity::class.java) { dao.clearCertificates(); it.forEach(dao::insertCertificate) }
      pullCollection(firestore, "insurance_policies", InsurancePolicyEntity::class.java) { dao.clearInsurancePolicies(); dao.insertInsurancePolicies(it) }
      pullCollection(firestore, "audit_logs", AuditLogEntity::class.java) { dao.clearAuditLogs(); it.forEach(dao::insertAuditLog) }
      pullCollection(firestore, "expiry_alerts", ExpiryAlertEntity::class.java) { dao.clearExpiryAlerts(); dao.insertExpiryAlerts(it) }
      val state = SyncState.Success(0, "Downloaded the latest shared data from Firestore.")
      _syncState.value = state
      state
    } catch (e: Exception) {
      val state = SyncState.Error("Download failed: ${e.message ?: "Network error"}")
      _syncState.value = state
      state
    }
  }

  fun startRealtimeListeners(scope: CoroutineScope) {
    stopRealtimeListeners()
    val firestore = authenticatedFirestore() ?: return
    listenCollection(firestore, "users", UserEntity::class.java, scope) { dao.clearUsers(); it.forEach(dao::insertUser) }
    listenCollection(firestore, "branches", BranchEntity::class.java, scope) { dao.clearBranches(); dao.insertBranches(it) }
    listenCollection(firestore, "courses", CourseEntity::class.java, scope) { dao.clearCourses(); dao.insertCourses(it) }
    listenCollection(firestore, "fee_schedules", FeeScheduleEntity::class.java, scope) { dao.clearFeeSchedules(); dao.insertFeeSchedules(it) }
    listenCollection(firestore, "enrollments", EnrollmentEntity::class.java, scope) { dao.clearEnrollments(); it.forEach(dao::insertEnrollment) }
    listenCollection(firestore, "lessons", LessonEntity::class.java, scope) { dao.clearLessons(); dao.insertLessons(it) }
    listenCollection(firestore, "lesson_progress", LessonProgressEntity::class.java, scope) { dao.clearLessonProgress(); it.forEach(dao::insertProgress) }
    listenCollection(firestore, "payments", PaymentEntity::class.java, scope) { dao.clearPayments(); it.forEach(dao::insertPayment) }
    listenCollection(firestore, "practical_sessions", PracticalSessionEntity::class.java, scope) { dao.clearPracticalSessions(); it.forEach(dao::insertPracticalSession) }
    listenCollection(firestore, "vehicles", VehicleEntity::class.java, scope) { dao.clearVehicles(); dao.insertVehicles(it) }
    listenCollection(firestore, "exam_sessions", ExamSessionEntity::class.java, scope) { dao.clearExamSessions(); dao.insertExamSessions(it) }
    listenCollection(firestore, "exam_candidates", ExamCandidateEntity::class.java, scope) { dao.clearCandidates(); it.forEach(dao::insertCandidate) }
    listenCollection(firestore, "certificates", CertificateEntity::class.java, scope) { dao.clearCertificates(); it.forEach(dao::insertCertificate) }
    listenCollection(firestore, "insurance_policies", InsurancePolicyEntity::class.java, scope) { dao.clearInsurancePolicies(); dao.insertInsurancePolicies(it) }
    listenCollection(firestore, "audit_logs", AuditLogEntity::class.java, scope) { dao.clearAuditLogs(); it.forEach(dao::insertAuditLog) }
    listenCollection(firestore, "expiry_alerts", ExpiryAlertEntity::class.java, scope) { dao.clearExpiryAlerts(); dao.insertExpiryAlerts(it) }
  }

  fun stopRealtimeListeners() {
    listenerRegistrations.forEach { it.remove() }
    listenerRegistrations.clear()
  }

  private fun authenticatedFirestore(): FirebaseFirestore? {
    if (FirebaseAuth.getInstance().currentUser == null) return null
    return getFirestore()
  }

  private suspend fun writeDocument(
    firestore: FirebaseFirestore,
    collection: String,
    documentId: String,
    data: Map<String, Any?>
  ) {
    retry("write $collection/$documentId") {
      firestore.collection(collection).document(documentId).set(
        data + mapOf(
          "serverUpdatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
          "updatedBy" to FirebaseAuth.getInstance().currentUser?.uid
        ),
        SetOptions.merge()
      ).await()
    }
  }

  private suspend fun deleteDocument(
    firestore: FirebaseFirestore,
    collection: String,
    documentId: String
  ) {
    retry("delete $collection/$documentId") {
      firestore.collection(collection).document(documentId).delete().await()
    }
  }

  private suspend fun <T> retry(operation: String, action: suspend () -> T): T {
    var lastError: Exception? = null
    repeat(3) { attempt ->
      try {
        return action()
      } catch (e: Exception) {
        lastError = e
        Log.w(TAG, "$operation failed on attempt ${attempt + 1}: ${e.message}")
        if (attempt < 2) delay(500L * (attempt + 1))
      }
    }
    throw lastError ?: IllegalStateException("$operation failed")
  }

  private suspend fun <T : Any> pullCollection(
    firestore: FirebaseFirestore,
    collection: String,
    type: Class<T>,
    replace: suspend (List<T>) -> Unit
  ) {
    val values = firestore.collection(collection).get().await().documents.mapNotNull { it.toObject(type) }
    replace(values)
  }

  private fun <T : Any> listenCollection(
    firestore: FirebaseFirestore,
    collection: String,
    type: Class<T>,
    scope: CoroutineScope,
    replace: suspend (List<T>) -> Unit
  ) {
    val registration = firestore.collection(collection).addSnapshotListener { snapshot, error ->
      if (error != null) {
        Log.w(TAG, "Realtime listener failed for $collection: ${error.message}")
      } else if (snapshot != null) {
        scope.launch(Dispatchers.IO) {
          replace(snapshot.documents.mapNotNull { it.toObject(type) })
        }
      }
    }
    listenerRegistrations += registration
  }

  /**
   * Synchronize all local Room tables with cloud Firestore.
   * Supports offline tolerance for Cameroonian branches: if offline, records remain in Room
   * and sync state reports graceful offline queuing.
   */
  suspend fun syncAll(): SyncState = withContext(Dispatchers.IO) {
    _syncState.value = SyncState.Syncing
    if (FirebaseAuth.getInstance().currentUser == null) {
      val errorState = SyncState.Error("Firebase authentication is required before syncing data.")
      _syncState.value = errorState
      return@withContext errorState
    }
    val firestore = getFirestore()

    if (firestore == null) {
      val message = "Local-First Mode Active: Room persistence running. Remote Firestore will sync once cloud credentials connect."
      val offlineState = SyncState.Offline(queuedCount = 0, message = message)
      _syncState.value = offlineState
      return@withContext offlineState
    }

    try {
      var syncedItems = 0

      // 1. Sync Branches with assigned staff details
      val branches = dao.getAllBranches().firstOrNull() ?: emptyList()
      val users = dao.getAllUsers().firstOrNull() ?: emptyList()
      for (b in branches) {
        val manager = users.find { it.branchId == b.id && it.role == UserRole.BRANCH_MANAGER }
        val secretary = users.find { it.branchId == b.id && it.role == UserRole.SECRETARY }
        val data = mapOf(
          "id" to b.id,
          "name" to b.name,
          "city" to b.city,
          "address" to b.address,
          "phone" to b.phone,
          "isVirtual" to b.isVirtual,
          "managerId" to (manager?.id ?: b.managerId ?: ""),
          "managerName" to (manager?.name ?: ""),
          "managerEmail" to (manager?.email ?: ""),
          "managerPhone" to (manager?.phone ?: ""),
          "secretaryId" to (secretary?.id ?: ""),
          "secretaryName" to (secretary?.name ?: ""),
          "secretaryEmail" to (secretary?.email ?: ""),
          "secretaryPhone" to (secretary?.phone ?: ""),
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "branches", b.id, data)
        syncedItems++
      }

      // 2. Sync Users
      for (u in users) {
        val data = mapOf(
          "id" to u.id,
          "name" to u.name,
          "email" to u.email,
          "phone" to u.phone,
          "countryCode" to u.countryCode,
          "role" to u.role.name,
          "branchId" to u.branchId,
          "preferredLocale" to u.preferredLocale,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "users", u.id, data)
        syncedItems++
      }

      // 3. Sync Enrollments (Hybrid Online / Onsite state)
      val enrollments = dao.getAllEnrollments().firstOrNull() ?: emptyList()
      for (e in enrollments) {
        val data = mapOf(
          "id" to e.id,
          "studentId" to e.studentId,
          "courseId" to e.courseId,
          "branchId" to e.branchId,
          "mode" to e.mode.name,
          "status" to e.status.name,
          "negotiatedAmount" to e.negotiatedAmount,
          "negotiatedBy" to e.negotiatedBy,
          "approvedBy" to e.approvedBy,
          "discountPercent" to e.discountPercent,
          "isDiscountApproved" to e.isDiscountApproved,
          "termEndsAt" to e.termEndsAt,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "enrollments", e.id, data)
        syncedItems++
      }

      // 4. Sync Payments (Financial Ledger)
      val payments = dao.getAllPayments().firstOrNull() ?: emptyList()
      for (p in payments) {
        val data = mapOf(
          "id" to p.id,
          "enrollmentId" to p.enrollmentId,
          "studentId" to p.studentId,
          "amount" to p.amount,
          "channel" to p.channel.name,
          "reference" to p.reference,
          "collectedBy" to p.collectedBy,
          "status" to p.status.name,
          "createdAt" to p.createdAt,
          "notes" to p.notes,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "payments", p.id, data)
        syncedItems++
      }

      // 5. Sync Practical Sessions (Vehicle & Instructor Scheduling)
      val practicalSessions = dao.getAllPracticalSessions().firstOrNull() ?: emptyList()
      for (s in practicalSessions) {
        val data = mapOf(
          "id" to s.id,
          "enrollmentId" to s.enrollmentId,
          "studentId" to s.studentId,
          "instructorId" to s.instructorId,
          "vehicleId" to s.vehicleId,
          "scheduledAt" to s.scheduledAt,
          "durationMinutes" to s.durationMinutes,
          "attendance" to s.attendance.name,
          "odometerStart" to s.odometerStart,
          "odometerEnd" to s.odometerEnd,
          "instructorFeedback" to s.instructorFeedback,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "practical_sessions", s.id, data)
        syncedItems++
      }

      // 6. Sync Vehicles (Fleet maintenance & Odometer tracking)
      val vehicles = dao.getAllVehicles().firstOrNull() ?: emptyList()
      for (v in vehicles) {
        val data = mapOf(
          "id" to v.id,
          "branchId" to v.branchId,
          "plateNo" to v.plateNo,
          "makeModel" to v.makeModel,
          "odometer" to v.odometer,
          "nextServiceKm" to v.nextServiceKm,
          "nextServiceDate" to v.nextServiceDate,
          "status" to v.status.name,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "vehicles", v.id, data)
        syncedItems++
      }

      // 7. Sync Verifiable Certificates (Public UUIDs & QR verification records)
      val certs = dao.getAllCertificates().firstOrNull() ?: emptyList()
      for (c in certs) {
        val data = mapOf(
          "id" to c.id,
          "enrollmentId" to c.enrollmentId,
          "studentId" to c.studentId,
          "studentName" to c.studentName,
          "verificationUuid" to c.verificationUuid,
          "type" to c.type.name,
          "category" to c.category.name,
          "branchName" to c.branchName,
          "issuedAt" to c.issuedAt,
          "qrPayload" to c.qrPayload,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "certificates", c.verificationUuid, data)
        syncedItems++
      }

      // 8. Sync Insurance Policies & Expiry Pipeline
      val policies = dao.getAllInsurancePolicies().firstOrNull() ?: emptyList()
      for (pol in policies) {
        val data = mapOf(
          "id" to pol.id,
          "holderId" to pol.holderId,
          "holderName" to pol.holderName,
          "tariffName" to pol.tariffName,
          "tariffNameFr" to pol.tariffNameFr,
          "vehiclePlate" to pol.vehiclePlate,
          "issuedBy" to pol.issuedBy,
          "startsAt" to pol.startsAt,
          "expiresAt" to pol.expiresAt,
          "premiumAmount" to pol.premiumAmount,
          "status" to pol.status,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "insurance_policies", pol.id, data)
        syncedItems++
      }

      _lastSyncTimestamp.value = System.currentTimeMillis()
      val success = SyncState.Success(syncedCount = syncedItems, message = "Successfully synced $syncedItems records to Cloud Firestore!")
      _syncState.value = success
      success
    } catch (e: Exception) {
      Log.e(TAG, "Sync failed: ${e.message}", e)
      val error = SyncState.Error("Sync error: ${e.message ?: "Network error"}")
      _syncState.value = error
      error
    }
  }

  /**
   * Push a single Payment record to Firestore immediately upon receipt (BR-05 Cash accountability).
   */
  suspend fun pushPayment(payment: PaymentEntity) = withContext(Dispatchers.IO) {
    val firestore = getFirestore() ?: return@withContext
    try {
      val data = mapOf(
        "id" to payment.id,
        "enrollmentId" to payment.enrollmentId,
        "studentId" to payment.studentId,
        "amount" to payment.amount,
        "channel" to payment.channel.name,
        "reference" to payment.reference,
        "collectedBy" to payment.collectedBy,
        "status" to payment.status.name,
        "createdAt" to payment.createdAt,
        "notes" to payment.notes,
        "syncedAt" to System.currentTimeMillis()
      )
      writeDocument(firestore, "payments", payment.id, data)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to push payment ${payment.id} to Firestore: ${e.message}")
    }
  }

  /**
   * Push a single Branch record and associated staff to Firestore immediately upon save/update.
   */
  suspend fun pushBranch(
    branch: BranchEntity,
    manager: UserEntity? = null,
    secretary: UserEntity? = null
  ): Boolean = withContext(Dispatchers.IO) {
    val firestore = getFirestore()
    if (firestore == null) {
      Log.d(TAG, "Local-First mode: branch ${branch.id} stored in Room, queued for Firestore.")
      return@withContext false
    }
    try {
      val branchData = mapOf(
        "id" to branch.id,
        "name" to branch.name,
        "city" to branch.city,
        "address" to branch.address,
        "phone" to branch.phone,
        "isVirtual" to branch.isVirtual,
        "managerId" to (manager?.id ?: branch.managerId ?: ""),
        "managerName" to (manager?.name ?: ""),
        "managerEmail" to (manager?.email ?: ""),
        "managerPhone" to (manager?.phone ?: ""),
        "secretaryId" to (secretary?.id ?: ""),
        "secretaryName" to (secretary?.name ?: ""),
        "secretaryEmail" to (secretary?.email ?: ""),
        "secretaryPhone" to (secretary?.phone ?: ""),
        "syncedAt" to System.currentTimeMillis()
      )
      writeDocument(firestore, "branches", branch.id, branchData)

      // Also persist manager & secretary to users collection if provided
      manager?.let { m ->
        val mgrData = mapOf(
          "id" to m.id,
          "name" to m.name,
          "email" to m.email,
          "phone" to m.phone,
          "countryCode" to m.countryCode,
          "role" to m.role.name,
          "branchId" to branch.id,
          "preferredLocale" to m.preferredLocale,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "users", m.id, mgrData)
      }

      secretary?.let { s ->
        val secData = mapOf(
          "id" to s.id,
          "name" to s.name,
          "email" to s.email,
          "phone" to s.phone,
          "countryCode" to s.countryCode,
          "role" to s.role.name,
          "branchId" to branch.id,
          "preferredLocale" to s.preferredLocale,
          "syncedAt" to System.currentTimeMillis()
        )
        writeDocument(firestore, "users", s.id, secData)
      }

      _lastSyncTimestamp.value = System.currentTimeMillis()
      _syncState.value = SyncState.Success(1, "Branch '${branch.name}' persisted to Firestore.")
      true
    } catch (e: Exception) {
      Log.e(TAG, "Error persisting branch ${branch.id} to Firestore: ${e.message}", e)
      _syncState.value = SyncState.Error("Firestore push error: ${e.message}")
      false
    }
  }

  /**
   * Delete a branch document from Firestore.
   */
  suspend fun deleteBranchFromFirestore(branchId: String) = withContext(Dispatchers.IO) {
    val firestore = getFirestore() ?: return@withContext
    try {
      deleteDocument(firestore, "branches", branchId)
    } catch (e: Exception) {
      Log.w(TAG, "Error deleting branch from Firestore: ${e.message}")
    }
  }

  /**
   * Push a staff member or user entity to Firestore collection 'users' immediately.
   */
  suspend fun pushUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
    val firestore = getFirestore() ?: return@withContext false
    try {
      val data = mapOf(
        "id" to user.id,
        "name" to user.name,
        "email" to user.email,
        "phone" to user.phone,
        "countryCode" to user.countryCode,
        "role" to user.role.name,
        "branchId" to (user.branchId ?: ""),
        "preferredLocale" to user.preferredLocale,
        "syncedAt" to System.currentTimeMillis()
      )
      writeDocument(firestore, "users", user.id, data)
      true
    } catch (e: Exception) {
      Log.w(TAG, "Error pushing user ${user.id} to Firestore: ${e.message}")
      false
    }
  }

  /**
   * Delete a user document from Firestore collection 'users'.
   */
  suspend fun deleteUserFromFirestore(userId: String) = withContext(Dispatchers.IO) {
    val firestore = getFirestore() ?: return@withContext
    try {
      deleteDocument(firestore, "users", userId)
    } catch (e: Exception) {
      Log.w(TAG, "Error deleting user from Firestore: ${e.message}")
    }
  }

  /**
   * Push a newly generated certificate to Firestore public verification collection (BR-07).
   */
  suspend fun pushCertificate(cert: CertificateEntity) = withContext(Dispatchers.IO) {
    val firestore = getFirestore() ?: return@withContext
    try {
      val data = mapOf(
        "id" to cert.id,
        "enrollmentId" to cert.enrollmentId,
        "studentId" to cert.studentId,
        "studentName" to cert.studentName,
        "verificationUuid" to cert.verificationUuid,
        "type" to cert.type.name,
        "category" to cert.category.name,
        "branchName" to cert.branchName,
        "issuedAt" to cert.issuedAt,
        "qrPayload" to cert.qrPayload,
        "syncedAt" to System.currentTimeMillis()
      )
      writeDocument(firestore, "certificates", cert.verificationUuid, data)
    } catch (e: Exception) {
      Log.w(TAG, "Failed to push certificate ${cert.verificationUuid} to Firestore: ${e.message}")
    }
  }

  /**
   * Fetch all branch documents directly from Firestore 'branches' collection.
   * If Firestore is offline or empty, seeds and falls back smoothly to local Room cache.
   */
  suspend fun fetchBranchesFromFirestore(): List<FirestoreBranchDocument> = withContext(Dispatchers.IO) {
    val firestore = getFirestore()
    if (firestore == null) {
      Log.d(TAG, "Firestore not connected: serving branches from Room cache")
      val branches = dao.getAllBranches().firstOrNull() ?: emptyList()
      val users = dao.getAllUsers().firstOrNull() ?: emptyList()
      return@withContext branches.map { b ->
        val m = users.find { it.branchId == b.id && it.role == UserRole.BRANCH_MANAGER }
        val s = users.find { it.branchId == b.id && it.role == UserRole.SECRETARY }
        FirestoreBranchDocument(
          id = b.id,
          name = b.name,
          city = b.city,
          address = b.address,
          phone = b.phone,
          isVirtual = b.isVirtual,
          managerId = m?.id ?: b.managerId ?: "",
          managerName = m?.name ?: "",
          managerEmail = m?.email ?: "",
          managerPhone = m?.phone ?: "",
          secretaryId = s?.id ?: "",
          secretaryName = s?.name ?: "",
          secretaryEmail = s?.email ?: "",
          secretaryPhone = s?.phone ?: "",
          syncedAt = System.currentTimeMillis()
        )
      }
    }
    try {
      val snapshot = firestore.collection("branches").get().await()
      if (snapshot.isEmpty) {
        val branches = dao.getAllBranches().firstOrNull() ?: emptyList()
        val users = dao.getAllUsers().firstOrNull() ?: emptyList()
        for (b in branches) {
          val m = users.find { it.branchId == b.id && it.role == UserRole.BRANCH_MANAGER }
          val s = users.find { it.branchId == b.id && it.role == UserRole.SECRETARY }
          pushBranch(b, m, s)
        }
        val reSnapshot = firestore.collection("branches").get().await()
        return@withContext reSnapshot.documents.map { doc -> parseFirestoreBranch(doc) }
      }
      snapshot.documents.map { doc -> parseFirestoreBranch(doc) }
    } catch (e: Exception) {
      Log.w(TAG, "Error fetching branches from Firestore: ${e.message}")
      val branches = dao.getAllBranches().firstOrNull() ?: emptyList()
      val users = dao.getAllUsers().firstOrNull() ?: emptyList()
      branches.map { b ->
        val m = users.find { it.branchId == b.id && it.role == UserRole.BRANCH_MANAGER }
        val s = users.find { it.branchId == b.id && it.role == UserRole.SECRETARY }
        FirestoreBranchDocument(
          id = b.id,
          name = b.name,
          city = b.city,
          address = b.address,
          phone = b.phone,
          isVirtual = b.isVirtual,
          managerId = m?.id ?: b.managerId ?: "",
          managerName = m?.name ?: "",
          managerEmail = m?.email ?: "",
          managerPhone = m?.phone ?: "",
          secretaryId = s?.id ?: "",
          secretaryName = s?.name ?: "",
          secretaryEmail = s?.email ?: "",
          secretaryPhone = s?.phone ?: "",
          syncedAt = System.currentTimeMillis()
        )
      }
    }
  }

  private fun parseFirestoreBranch(doc: com.google.firebase.firestore.DocumentSnapshot): FirestoreBranchDocument {
    return FirestoreBranchDocument(
      id = doc.getString("id") ?: doc.id,
      name = doc.getString("name") ?: "",
      city = doc.getString("city") ?: "",
      address = doc.getString("address") ?: "",
      phone = doc.getString("phone") ?: "",
      isVirtual = doc.getBoolean("isVirtual") ?: false,
      managerId = doc.getString("managerId") ?: "",
      managerName = doc.getString("managerName") ?: "",
      managerEmail = doc.getString("managerEmail") ?: "",
      managerPhone = doc.getString("managerPhone") ?: "",
      secretaryId = doc.getString("secretaryId") ?: "",
      secretaryName = doc.getString("secretaryName") ?: "",
      secretaryEmail = doc.getString("secretaryEmail") ?: "",
      secretaryPhone = doc.getString("secretaryPhone") ?: "",
      syncedAt = doc.getLong("syncedAt") ?: System.currentTimeMillis()
    )
  }

  companion object {
    private const val TAG = "DriveSchoolSyncManager"
  }
}
