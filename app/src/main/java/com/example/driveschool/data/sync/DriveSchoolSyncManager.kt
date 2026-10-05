package com.example.driveschool.data.sync

import android.content.Context
import android.util.Log
import com.example.driveschool.data.db.DriveSchoolDao
import com.example.driveschool.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncState {
  object Idle : SyncState()
  object Syncing : SyncState()
  data class Success(val syncedCount: Int, val message: String) : SyncState()
  data class Offline(val queuedCount: Int, val message: String) : SyncState()
  data class Error(val error: String) : SyncState()
}

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
        FirebaseFirestore.getInstance()
      } else {
        null
      }
    } catch (e: Exception) {
      Log.w(TAG, "FirebaseFirestore instance not ready: ${e.message}")
      null
    }
  }

  /**
   * Synchronize all local Room tables with cloud Firestore.
   * Supports offline tolerance for Cameroonian branches: if offline, records remain in Room
   * and sync state reports graceful offline queuing.
   */
  suspend fun syncAll(): SyncState = withContext(Dispatchers.IO) {
    _syncState.value = SyncState.Syncing
    val firestore = getFirestore()

    if (firestore == null) {
      val message = "Local-First Mode Active: Room persistence running. Remote Firestore will sync once cloud credentials connect."
      val offlineState = SyncState.Offline(queuedCount = 0, message = message)
      _syncState.value = offlineState
      return@withContext offlineState
    }

    try {
      var syncedItems = 0

      // 1. Sync Branches
      val branches = dao.getAllBranches().firstOrNull() ?: emptyList()
      for (b in branches) {
        val data = mapOf(
          "id" to b.id,
          "name" to b.name,
          "city" to b.city,
          "address" to b.address,
          "phone" to b.phone,
          "isVirtual" to b.isVirtual,
          "managerId" to b.managerId,
          "syncedAt" to System.currentTimeMillis()
        )
        firestore.collection("branches").document(b.id).set(data, SetOptions.merge()).await()
        syncedItems++
      }

      // 2. Sync Users
      val users = dao.getAllUsers().firstOrNull() ?: emptyList()
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
        firestore.collection("users").document(u.id).set(data, SetOptions.merge()).await()
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
        firestore.collection("enrollments").document(e.id).set(data, SetOptions.merge()).await()
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
        firestore.collection("payments").document(p.id).set(data, SetOptions.merge()).await()
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
        firestore.collection("practical_sessions").document(s.id).set(data, SetOptions.merge()).await()
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
        firestore.collection("vehicles").document(v.id).set(data, SetOptions.merge()).await()
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
        firestore.collection("certificates").document(c.verificationUuid).set(data, SetOptions.merge()).await()
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
        firestore.collection("insurance_policies").document(pol.id).set(data, SetOptions.merge()).await()
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
      firestore.collection("payments").document(payment.id).set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to push payment ${payment.id} to Firestore: ${e.message}")
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
      firestore.collection("certificates").document(cert.verificationUuid).set(data, SetOptions.merge()).await()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to push certificate ${cert.verificationUuid} to Firestore: ${e.message}")
    }
  }

  companion object {
    private const val TAG = "DriveSchoolSyncManager"
  }
}
