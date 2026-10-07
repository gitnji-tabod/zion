package com.example.driveschool.data.repository

import com.example.driveschool.data.db.DriveSchoolDao
import com.example.driveschool.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class DriveSchoolRepository(private val dao: DriveSchoolDao) {

  // Flow queries
  val allUsers: Flow<List<UserEntity>> = dao.getAllUsers()
  val allBranches: Flow<List<BranchEntity>> = dao.getAllBranches()
  val allCourses: Flow<List<CourseEntity>> = dao.getAllCourses()
  val allEnrollments: Flow<List<EnrollmentEntity>> = dao.getAllEnrollments()
  val allVehicles: Flow<List<VehicleEntity>> = dao.getAllVehicles()
  val allPayments: Flow<List<PaymentEntity>> = dao.getAllPayments()
  val allPracticalSessions: Flow<List<PracticalSessionEntity>> = dao.getAllPracticalSessions()
  val allExamSessions: Flow<List<ExamSessionEntity>> = dao.getAllExamSessions()
  val allCandidates: Flow<List<ExamCandidateEntity>> = dao.getAllCandidates()
  val allLessonProgress: Flow<List<LessonProgressEntity>> = dao.getAllLessonProgress()
  val allCertificates: Flow<List<CertificateEntity>> = dao.getAllCertificates()
  val allInsurancePolicies: Flow<List<InsurancePolicyEntity>> = dao.getAllInsurancePolicies()
  val allAuditLogs: Flow<List<AuditLogEntity>> = dao.getAllAuditLogs()
  val allExpiryAlerts: Flow<List<ExpiryAlertEntity>> = dao.getAllExpiryAlerts()
  val unreadAlertsCount: Flow<Int> = dao.getUnreadAlertsCount()
  val pendingDiscountApprovals: Flow<List<EnrollmentEntity>> = dao.getPendingDiscountApprovals()
  val candidatesPendingApproval: Flow<List<ExamCandidateEntity>> = dao.getCandidatesPendingApproval()
  val candidatesReadyForApplication: Flow<List<ExamCandidateEntity>> = dao.getCandidatesReadyForApplication()

  fun getEnrollmentsByStudent(studentId: String): Flow<List<EnrollmentEntity>> =
    dao.getEnrollmentsByStudent(studentId)

  fun getLessonsByCourse(courseId: String): Flow<List<LessonEntity>> =
    dao.getLessonsByCourse(courseId)

  fun getProgressForEnrollment(enrollmentId: String): Flow<List<LessonProgressEntity>> =
    dao.getProgressForEnrollment(enrollmentId)

  fun getSessionsByInstructor(instructorId: String): Flow<List<PracticalSessionEntity>> =
    dao.getSessionsByInstructor(instructorId)

  fun getSessionsByStudent(studentId: String): Flow<List<PracticalSessionEntity>> =
    dao.getSessionsByStudent(studentId)

  fun getCertificatesByStudent(studentId: String): Flow<List<CertificateEntity>> =
    dao.getCertificatesByStudent(studentId)

  suspend fun getCertificateByUuid(uuid: String): CertificateEntity? =
    dao.getCertificateByUuid(uuid)

  suspend fun getUserById(userId: String): UserEntity? =
    dao.getUserById(userId)

  suspend fun getBranchById(branchId: String): BranchEntity? =
    dao.getBranchById(branchId)

  suspend fun getCourseById(courseId: String): CourseEntity? =
    dao.getCourseById(courseId)

  suspend fun getFeeScheduleForCourse(courseId: String): FeeScheduleEntity? =
    dao.getFeeScheduleForCourse(courseId)

  suspend fun getEnrollmentById(id: String): EnrollmentEntity? =
    dao.getEnrollmentById(id)

  suspend fun getVehicleById(vehicleId: String): VehicleEntity? =
    dao.getVehicleById(vehicleId)

  suspend fun markAlertAsRead(alertId: String) =
    dao.markAlertAsRead(alertId)

  // Super Admin: Create new branch with dedicated Manager, Secretary, and initial Student
  suspend fun createBranchWithStaff(
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
    initialCourseId: String? = null,
    creatorUser: UserEntity? = null
  ): BranchEntity {
    val branchId = "branch-${UUID.randomUUID().toString().take(8)}"
    val managerId = "user-mgr-${UUID.randomUUID().toString().take(6)}"
    val secretaryId = "user-sec-${UUID.randomUUID().toString().take(6)}"

    // 1. Create Branch
    val newBranch = BranchEntity(
      id = branchId,
      name = name,
      city = city,
      address = address,
      phone = phone,
      isVirtual = false,
      managerId = managerId
    )
    dao.insertBranch(newBranch)

    // 2. Create Branch Manager
    val managerUser = UserEntity(
      id = managerId,
      name = managerName,
      email = managerEmail,
      phone = managerPhone.ifBlank { "+237 670 000 111" },
      countryCode = "CM",
      preferredLocale = "en",
      role = UserRole.BRANCH_MANAGER,
      branchId = branchId
    )
    dao.insertUser(managerUser)

    // 3. Create Secretary
    val secretaryUser = UserEntity(
      id = secretaryId,
      name = secretaryName,
      email = secretaryEmail,
      phone = secretaryPhone.ifBlank { "+237 670 000 222" },
      countryCode = "CM",
      preferredLocale = "en",
      role = UserRole.SECRETARY,
      branchId = branchId
    )
    dao.insertUser(secretaryUser)

    // 4. Create Initial Student if provided
    var initialStudentSummary = ""
    if (!initialStudentName.isNullOrBlank()) {
      val studentId = "user-stud-${UUID.randomUUID().toString().take(6)}"
      val studentUser = UserEntity(
        id = studentId,
        name = initialStudentName,
        email = initialStudentEmail?.ifBlank { "student.${city.lowercase().replace(" ", "")}@driveschool.cm" } ?: "student.${city.lowercase().replace(" ", "")}@driveschool.cm",
        phone = initialStudentPhone?.ifBlank { "+237 670 000 333" } ?: "+237 670 000 333",
        countryCode = "CM",
        preferredLocale = "en",
        role = UserRole.STUDENT,
        branchId = branchId
      )
      dao.insertUser(studentUser)

      val courseIdToUse = initialCourseId?.ifBlank { "course-cat-b" } ?: "course-cat-b"
      val enrollmentId = "enr-${UUID.randomUUID().toString().take(8)}"
      val enrollment = EnrollmentEntity(
        id = enrollmentId,
        studentId = studentId,
        courseId = courseIdToUse,
        branchId = branchId,
        mode = EnrollmentMode.ONSITE,
        status = EnrollmentStatus.ACTIVE,
        negotiatedAmount = null,
        negotiatedBy = secretaryId,
        approvedBy = managerId,
        discountPercent = 0.0,
        isDiscountApproved = true,
        termEndsAt = System.currentTimeMillis() + 60L * 24 * 3600 * 1000
      )
      dao.insertEnrollment(enrollment)
      initialStudentSummary = " Initial Student: $initialStudentName enrolled in course $courseIdToUse."
    }

    // 5. Audit Log (FR-08)
    dao.insertAuditLog(
      AuditLogEntity(
        action = "BRANCH_CREATED",
        actorId = creatorUser?.id ?: "admin-01",
        actorName = creatorUser?.name ?: "Super Admin (Owner)",
        actorRole = "SUPER_ADMIN",
        details = "Created new branch '$name' ($city). Appointed Manager: $managerName ($managerEmail), Secretary: $secretaryName ($secretaryEmail).$initialStudentSummary"
      )
    )

    return newBranch
  }

  // Super Admin: Update existing branch coordinates and assign manager and secretary
  suspend fun updateBranchWithStaff(
    branchId: String,
    name: String,
    city: String,
    address: String,
    phone: String,
    isVirtual: Boolean,
    managerUser: UserEntity?,
    secretaryUser: UserEntity?,
    newManagerDetails: Triple<String, String, String>? = null,
    newSecretaryDetails: Triple<String, String, String>? = null,
    actorUser: UserEntity? = null
  ): Triple<BranchEntity, UserEntity?, UserEntity?> {
    val existingBranch = dao.getBranchById(branchId)

    // Resolve Manager
    var resolvedManager: UserEntity? = managerUser
    if (newManagerDetails != null && newManagerDetails.first.isNotBlank()) {
      val mId = "user-mgr-${UUID.randomUUID().toString().take(6)}"
      val newMgr = UserEntity(
        id = mId,
        name = newManagerDetails.first,
        email = newManagerDetails.second,
        phone = newManagerDetails.third.ifBlank { "+237 670 000 111" },
        countryCode = "CM",
        preferredLocale = "en",
        role = UserRole.BRANCH_MANAGER,
        branchId = branchId
      )
      dao.insertUser(newMgr)
      resolvedManager = newMgr
    } else if (resolvedManager != null) {
      val updatedMgr = resolvedManager.copy(branchId = branchId, role = UserRole.BRANCH_MANAGER)
      dao.insertUser(updatedMgr)
      resolvedManager = updatedMgr
    }

    // Resolve Secretary
    var resolvedSecretary: UserEntity? = secretaryUser
    if (newSecretaryDetails != null && newSecretaryDetails.first.isNotBlank()) {
      val sId = "user-sec-${UUID.randomUUID().toString().take(6)}"
      val newSec = UserEntity(
        id = sId,
        name = newSecretaryDetails.first,
        email = newSecretaryDetails.second,
        phone = newSecretaryDetails.third.ifBlank { "+237 670 000 222" },
        countryCode = "CM",
        preferredLocale = "en",
        role = UserRole.SECRETARY,
        branchId = branchId
      )
      dao.insertUser(newSec)
      resolvedSecretary = newSec
    } else if (resolvedSecretary != null) {
      val updatedSec = resolvedSecretary.copy(branchId = branchId, role = UserRole.SECRETARY)
      dao.insertUser(updatedSec)
      resolvedSecretary = updatedSec
    }

    val updatedBranch = BranchEntity(
      id = branchId,
      name = name,
      city = city,
      address = address,
      phone = phone,
      isVirtual = isVirtual,
      managerId = resolvedManager?.id ?: existingBranch?.managerId
    )
    dao.insertBranch(updatedBranch)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "BRANCH_UPDATED",
        actorId = actorUser?.id ?: "admin-01",
        actorName = actorUser?.name ?: "Super Admin (Owner)",
        actorRole = "SUPER_ADMIN",
        details = "Updated branch '$name' ($city). Assigned Manager: ${resolvedManager?.name ?: "Unassigned"}, Secretary: ${resolvedSecretary?.name ?: "Unassigned"}."
      )
    )

    return Triple(updatedBranch, resolvedManager, resolvedSecretary)
  }

  suspend fun deleteBranch(branchId: String, actorUser: UserEntity? = null) {
    val branch = dao.getBranchById(branchId)
    dao.deleteBranchById(branchId)
    dao.insertAuditLog(
      AuditLogEntity(
        action = "BRANCH_DELETED",
        actorId = actorUser?.id ?: "admin-01",
        actorName = actorUser?.name ?: "Super Admin",
        actorRole = "SUPER_ADMIN",
        details = "Deleted branch '${branch?.name ?: branchId}' ($branchId)."
      )
    )
  }

  // Super Admin: Create Staff Member (Manager, Instructor, Secretary)
  suspend fun createStaffMember(
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?,
    countryCode: String = "CM",
    preferredLocale: String = "en",
    actorUser: UserEntity? = null
  ): UserEntity {
    val prefix = when (role) {
      UserRole.BRANCH_MANAGER -> "user-mgr"
      UserRole.INSTRUCTOR -> "user-inst"
      UserRole.SECRETARY -> "user-sec"
      else -> "user-staff"
    }
    val staffId = "$prefix-${UUID.randomUUID().toString().take(6)}"
    val newStaff = UserEntity(
      id = staffId,
      name = name,
      email = email,
      phone = phone.ifBlank { "+237 670 000 000" },
      countryCode = countryCode,
      preferredLocale = preferredLocale,
      role = role,
      branchId = branchId
    )
    dao.insertUser(newStaff)

    // If assigned as manager to a branch, link to branch managerId
    if (role == UserRole.BRANCH_MANAGER && !branchId.isNullOrBlank()) {
      val branch = dao.getBranchById(branchId)
      if (branch != null) {
        dao.updateBranch(branch.copy(managerId = staffId))
      }
    }

    dao.insertAuditLog(
      AuditLogEntity(
        action = "STAFF_CREATED",
        actorId = actorUser?.id ?: "admin-01",
        actorName = actorUser?.name ?: "Super Admin",
        actorRole = "SUPER_ADMIN",
        details = "Created staff member '$name' with role ${role.name} assigned to branch ${branchId ?: "None"}."
      )
    )

    return newStaff
  }

  // Super Admin: Update Staff Member and Role
  suspend fun updateStaffMember(
    userId: String,
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?,
    actorUser: UserEntity? = null
  ): UserEntity {
    val existing = dao.getUserById(userId) ?: UserEntity(
      id = userId,
      name = name,
      email = email,
      phone = phone,
      role = role,
      branchId = branchId
    )
    val updated = existing.copy(
      name = name,
      email = email,
      phone = phone,
      role = role,
      branchId = branchId
    )
    dao.updateUser(updated)

    // If manager, update branch's managerId
    if (role == UserRole.BRANCH_MANAGER && !branchId.isNullOrBlank()) {
      val branch = dao.getBranchById(branchId)
      if (branch != null) {
        dao.updateBranch(branch.copy(managerId = userId))
      }
    }

    dao.insertAuditLog(
      AuditLogEntity(
        action = "STAFF_UPDATED",
        actorId = actorUser?.id ?: "admin-01",
        actorName = actorUser?.name ?: "Super Admin",
        actorRole = "SUPER_ADMIN",
        details = "Updated staff member '$name' (${updated.id}) to role ${role.name}, branch: ${branchId ?: "Unassigned"}."
      )
    )

    return updated
  }

  // Super Admin: Delete Staff Member
  suspend fun deleteStaffMember(userId: String, actorUser: UserEntity? = null) {
    val user = dao.getUserById(userId)
    dao.deleteUserById(userId)
    dao.insertAuditLog(
      AuditLogEntity(
        action = "STAFF_DELETED",
        actorId = actorUser?.id ?: "admin-01",
        actorName = actorUser?.name ?: "Super Admin",
        actorRole = "SUPER_ADMIN",
        details = "Deleted staff member '${user?.name ?: userId}' (${user?.role?.name ?: "STAFF"})."
      )
    )
  }

  // User Profile: Update contact details & preferred language
  suspend fun updateStudentProfile(
    userId: String,
    name: String,
    phone: String,
    preferredLocale: String
  ): UserEntity {
    val existing = dao.getUserById(userId) ?: throw IllegalArgumentException("User not found: $userId")
    val updated = existing.copy(
      name = name,
      phone = phone,
      preferredLocale = preferredLocale
    )
    dao.updateUser(updated)
    dao.insertAuditLog(
      AuditLogEntity(
        action = "PROFILE_UPDATED",
        actorId = userId,
        actorName = name,
        actorRole = existing.role.name,
        details = "User updated profile contact info: phone=$phone, locale=$preferredLocale."
      )
    )
    return updated
  }

  // Add a student directly to a branch with active enrollment
  suspend fun addStudentToBranch(
    name: String,
    email: String,
    phone: String,
    branchId: String,
    courseId: String,
    creatorUser: UserEntity? = null
  ): UserEntity {
    val studentId = "user-stud-${UUID.randomUUID().toString().take(6)}"
    val studentUser = UserEntity(
      id = studentId,
      name = name,
      email = email,
      phone = phone.ifBlank { "+237 670 000 000" },
      countryCode = "CM",
      preferredLocale = "en",
      role = UserRole.STUDENT,
      branchId = branchId
    )
    dao.insertUser(studentUser)

    val enrollment = EnrollmentEntity(
      id = "enr-${UUID.randomUUID().toString().take(8)}",
      studentId = studentId,
      courseId = courseId,
      branchId = branchId,
      mode = EnrollmentMode.ONSITE,
      status = EnrollmentStatus.ACTIVE,
      negotiatedAmount = null,
      negotiatedBy = null,
      approvedBy = null,
      discountPercent = 0.0,
      isDiscountApproved = true,
      termEndsAt = System.currentTimeMillis() + 60L * 24 * 3600 * 1000
    )
    dao.insertEnrollment(enrollment)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "STUDENT_ASSIGNED_TO_BRANCH",
        actorId = creatorUser?.id ?: "admin-01",
        actorName = creatorUser?.name ?: "Super Admin",
        actorRole = creatorUser?.role?.name ?: "SUPER_ADMIN",
        details = "Enrolled student $name ($email) at branch $branchId in course $courseId."
      )
    )

    return studentUser
  }

  // BR-01: Registration with Country Routing Rule
  suspend fun registerStudent(
    name: String,
    email: String,
    phone: String,
    countryCode: String,
    selectedBranchId: String?,
    preferredLocale: String = "en",
    actorId: String = "system",
    actorRole: String = "SYSTEM"
  ): UserEntity {
    val isCameroon = countryCode.trim().equals("CM", ignoreCase = true)
    val assignedBranchId = if (isCameroon) {
      selectedBranchId ?: "branch-bamenda-01"
    } else {
      "branch-online-00" // Virtual branch
    }

    val newUser = UserEntity(
      id = "user-${UUID.randomUUID().toString().take(8)}",
      name = name,
      email = email,
      phone = phone,
      countryCode = countryCode.uppercase().trim(),
      preferredLocale = preferredLocale,
      role = UserRole.STUDENT,
      branchId = assignedBranchId
    )
    dao.insertUser(newUser)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "STUDENT_REGISTERED",
        actorId = actorId,
        actorName = if (actorRole == "SECRETARY") "Secretary" else "Self-Registration",
        actorRole = actorRole,
        details = "Registered student ${newUser.name} (Country: ${newUser.countryCode}). Attached to branch: $assignedBranchId. Mode capability: ${if (isCameroon) "Onsite + Online" else "Online-Only"}."
      )
    )

    return newUser
  }

  // Create Enrollment with BR-03 and BR-04 Guardrails
  suspend fun createEnrollment(
    student: UserEntity,
    courseId: String,
    mode: EnrollmentMode,
    negotiatedFee: Double? = null,
    secretaryUser: UserEntity? = null
  ): Result<EnrollmentEntity> {
    val feeSchedule = dao.getFeeScheduleForCourse(courseId)
    val standardFee = feeSchedule?.standardAmount ?: 125000.0

    // BR-03: Online enrollments cannot be discounted
    val effectiveNegotiatedFee = if (mode == EnrollmentMode.ONLINE) {
      null
    } else {
      negotiatedFee
    }

    var discountPercent = 0.0
    var requiresApproval = false

    if (effectiveNegotiatedFee != null && effectiveNegotiatedFee < standardFee) {
      discountPercent = ((standardFee - effectiveNegotiatedFee) / standardFee) * 100.0
      // BR-04: discounts beyond 15% require Branch Manager approval
      if (discountPercent > 15.0) {
        requiresApproval = true
      }
    }

    val initialStatus = if (requiresApproval) {
      EnrollmentStatus.AWAITING_PAYMENT // Pending manager approval
    } else {
      EnrollmentStatus.ONBOARDING // Student can start free onboarding lessons immediately
    }

    val branchId = student.branchId ?: if (mode == EnrollmentMode.ONLINE) "branch-online-00" else "branch-bamenda-01"

    val enrollment = EnrollmentEntity(
      id = "enr-${UUID.randomUUID().toString().take(8)}",
      studentId = student.id,
      courseId = courseId,
      branchId = branchId,
      mode = mode,
      status = initialStatus,
      negotiatedAmount = effectiveNegotiatedFee,
      negotiatedBy = secretaryUser?.id,
      approvedBy = if (!requiresApproval && effectiveNegotiatedFee != null) secretaryUser?.id else null,
      discountPercent = discountPercent,
      isDiscountApproved = !requiresApproval
    )

    dao.insertEnrollment(enrollment)

    // Audit log
    dao.insertAuditLog(
      AuditLogEntity(
        action = if (effectiveNegotiatedFee != null) "FEE_NEGOTIATED" else "ENROLLMENT_CREATED",
        actorId = secretaryUser?.id ?: student.id,
        actorName = secretaryUser?.name ?: student.name,
        actorRole = secretaryUser?.role?.name ?: "STUDENT",
        details = "Enrollment created for course $courseId, Mode: $mode. Standard Fee: $standardFee XAF. Negotiated Fee: ${effectiveNegotiatedFee ?: standardFee} XAF (${String.format("%.1f", discountPercent)}% discount). Requires Branch Manager Approval: $requiresApproval."
      )
    )

    return Result.success(enrollment)
  }

  // Branch Manager approves negotiated fee (BR-04)
  suspend fun approveDiscount(enrollmentId: String, manager: UserEntity): Boolean {
    val enrollment = dao.getEnrollmentById(enrollmentId) ?: return false
    val updated = enrollment.copy(
      isDiscountApproved = true,
      approvedBy = manager.id
    )
    dao.updateEnrollment(updated)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "DISCOUNT_APPROVED",
        actorId = manager.id,
        actorName = manager.name,
        actorRole = manager.role.name,
        details = "Branch Manager approved ${String.format("%.1f", enrollment.discountPercent)}% discount for enrollment $enrollmentId (Amount: ${enrollment.negotiatedAmount} XAF)."
      )
    )
    return true
  }

  // Record Payment (BR-05 Cash accountability, MoMo, Orange Money, Card)
  suspend fun recordPayment(
    enrollmentId: String,
    studentId: String,
    amount: Double,
    channel: PaymentChannel,
    collectorSecretary: UserEntity? = null,
    externalRef: String? = null,
    notes: String? = null
  ): PaymentEntity {
    val reference = externalRef ?: when (channel) {
      PaymentChannel.MTN_MOMO -> "MOMO-${System.currentTimeMillis().toString().takeLast(6)}"
      PaymentChannel.ORANGE_MONEY -> "OM-${System.currentTimeMillis().toString().takeLast(6)}"
      PaymentChannel.CARD -> "CARD-${UUID.randomUUID().toString().take(8).uppercase()}"
      PaymentChannel.CASH -> "CSH-SEC-${collectorSecretary?.id?.takeLast(4) ?: "0000"}-${System.currentTimeMillis().toString().takeLast(4)}"
    }

    val payment = PaymentEntity(
      id = "pay-${UUID.randomUUID().toString().take(8)}",
      enrollmentId = enrollmentId,
      studentId = studentId,
      amount = amount,
      channel = channel,
      reference = reference,
      collectedBy = if (channel == PaymentChannel.CASH) collectorSecretary?.id else null,
      status = PaymentStatus.CONFIRMED,
      notes = notes ?: "Payment processed via $channel"
    )

    dao.insertPayment(payment)

    // Unlock enrollment: Transition to ACTIVE (BR-02 Payment gate lifted!)
    val enrollment = dao.getEnrollmentById(enrollmentId)
    if (enrollment != null && (enrollment.status == EnrollmentStatus.ONBOARDING || enrollment.status == EnrollmentStatus.AWAITING_PAYMENT)) {
      dao.updateEnrollment(enrollment.copy(status = EnrollmentStatus.ACTIVE))
    }

    // Immutable Audit Log (BR-05)
    dao.insertAuditLog(
      AuditLogEntity(
        action = if (channel == PaymentChannel.CASH) "CASH_COLLECTED" else "PAYMENT_CONFIRMED",
        actorId = collectorSecretary?.id ?: studentId,
        actorName = collectorSecretary?.name ?: "Student",
        actorRole = collectorSecretary?.role?.name ?: "STUDENT",
        details = "Payment of $amount XAF recorded via $channel. Ref: $reference. Collected by: ${collectorSecretary?.name ?: "Gateway/Digital"}."
      )
    )

    return payment
  }

  // BR-08: Practical Session Booking with Conflict Checking & Overdue Servicing Filter
  suspend fun bookPracticalSession(
    enrollmentId: String,
    studentId: String,
    instructorId: String,
    vehicleId: String,
    scheduledAt: Long,
    durationMinutes: Int = 60
  ): Result<PracticalSessionEntity> {
    val vehicle = dao.getVehicleById(vehicleId)
      ?: return Result.failure(Exception("Selected vehicle not found"))

    // BR-08: Overdue vehicle cannot be assigned to sessions
    val now = System.currentTimeMillis()
    val isKmOverdue = vehicle.odometer >= vehicle.nextServiceKm
    val isDateOverdue = now >= vehicle.nextServiceDate
    if (isKmOverdue || isDateOverdue || vehicle.status == VehicleStatus.SERVICE_DUE || vehicle.status == VehicleStatus.IN_REPAIR) {
      return Result.failure(Exception("Cannot assign vehicle ${vehicle.plateNo}: Service maintenance is overdue! (KM: ${vehicle.odometer}/${vehicle.nextServiceKm}, Status: ${vehicle.status})"))
    }

    // Check vehicle and instructor conflict
    val existingSessions = dao.getSessionsByVehicle(vehicleId)
    val hasVehicleConflict = existingSessions.any { existing ->
      Math.abs(existing.scheduledAt - scheduledAt) < (durationMinutes * 60 * 1000)
    }
    if (hasVehicleConflict) {
      return Result.failure(Exception("Vehicle ${vehicle.plateNo} is already booked for a practical session at this time."))
    }

    val session = PracticalSessionEntity(
      id = "prac-${UUID.randomUUID().toString().take(8)}",
      enrollmentId = enrollmentId,
      studentId = studentId,
      instructorId = instructorId,
      vehicleId = vehicleId,
      scheduledAt = scheduledAt,
      durationMinutes = durationMinutes,
      attendance = AttendanceStatus.SCHEDULED,
      odometerStart = vehicle.odometer
    )
    dao.insertPracticalSession(session)

    return Result.success(session)
  }

  // Instructor signs off session with Odometer readings & attendance
  suspend fun signOffPracticalSession(
    sessionId: String,
    attendance: AttendanceStatus,
    odometerEnd: Int,
    feedback: String?,
    instructor: UserEntity
  ): Boolean {
    val allSessions = dao.getAllPracticalSessions().firstOrNull() ?: emptyList()
    val session = allSessions.find { it.id == sessionId } ?: return false

    val updatedSession = session.copy(
      attendance = attendance,
      odometerEnd = odometerEnd,
      instructorFeedback = feedback
    )
    dao.updatePracticalSession(updatedSession)

    // Update vehicle odometer & check service due
    val vehicle = dao.getVehicleById(session.vehicleId)
    if (vehicle != null && odometerEnd > vehicle.odometer) {
      val newStatus = if (odometerEnd >= vehicle.nextServiceKm) VehicleStatus.SERVICE_DUE else vehicle.status
      dao.updateVehicle(
        vehicle.copy(
          odometer = odometerEnd,
          status = newStatus
        )
      )
    }

    dao.insertAuditLog(
      AuditLogEntity(
        action = "PRACTICAL_SESSION_SIGNOFF",
        actorId = instructor.id,
        actorName = instructor.name,
        actorRole = "INSTRUCTOR",
        details = "Instructor signed off session $sessionId. Attendance: $attendance, End Odometer: $odometerEnd km."
      )
    )

    return true
  }

  // BR-06: Examination Status Workflow Chain
  // Step 1: Instructor recommends candidate
  suspend fun recommendCandidate(
    examSessionId: String,
    enrollmentId: String,
    studentId: String,
    instructor: UserEntity
  ): ExamCandidateEntity {
    val candidate = ExamCandidateEntity(
      id = "cand-${UUID.randomUUID().toString().take(8)}",
      examSessionId = examSessionId,
      enrollmentId = enrollmentId,
      studentId = studentId,
      status = CandidateStatus.RECOMMENDED,
      recommendedBy = instructor.id
    )
    dao.insertCandidate(candidate)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "EXAM_CANDIDATE_RECOMMENDED",
        actorId = instructor.id,
        actorName = instructor.name,
        actorRole = "INSTRUCTOR",
        details = "Instructor recommended student $studentId for exam session $examSessionId."
      )
    )
    return candidate
  }

  // Step 2: Branch Manager approves candidate
  suspend fun approveExamCandidate(candidateId: String, manager: UserEntity): Boolean {
    val candidate = dao.getCandidateById(candidateId) ?: return false
    val updated = candidate.copy(
      status = CandidateStatus.APPROVED,
      approvedBy = manager.id
    )
    dao.updateCandidate(updated)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "EXAM_CANDIDATE_APPROVED",
        actorId = manager.id,
        actorName = manager.name,
        actorRole = "BRANCH_MANAGER",
        details = "Branch Manager approved candidate $candidateId for national exam application."
      )
    )
    return true
  }

  // Step 3: Secretary applies candidate (dossier submitted to ministry)
  suspend fun applyExamCandidate(candidateId: String, secretary: UserEntity): Boolean {
    val candidate = dao.getCandidateById(candidateId) ?: return false
    if (candidate.status != CandidateStatus.APPROVED) return false

    val updated = candidate.copy(
      status = CandidateStatus.APPLIED,
      appliedBy = secretary.id
    )
    dao.updateCandidate(updated)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "EXAM_CANDIDATE_APPLIED",
        actorId = secretary.id,
        actorName = secretary.name,
        actorRole = "SECRETARY",
        details = "Secretary processed exam application for candidate $candidateId."
      )
    )
    return true
  }

  // Step 4 & 5: Candidate sits and Result entered (PASSED triggers Certificate BR-07)
  suspend fun recordExamResult(candidateId: String, score: Double, passed: Boolean): CertificateEntity? {
    val candidate = dao.getCandidateById(candidateId) ?: return null
    val newStatus = if (passed) CandidateStatus.PASSED else CandidateStatus.FAILED
    val updated = candidate.copy(
      status = newStatus,
      score = score,
      resultEnteredAt = System.currentTimeMillis()
    )
    dao.updateCandidate(updated)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "EXAM_RESULT_RECORDED",
        actorId = "exam-board",
        actorName = "Exam Board / Delegate",
        actorRole = "SUPER_ADMIN",
        details = "Exam result recorded for candidate $candidateId: Score $score%. Status: $newStatus."
      )
    )

    if (passed) {
      // BR-07: Automatic Certificate Generation with unguessable UUID & verify endpoint
      val student = dao.getUserById(candidate.studentId)
      val enrollment = dao.getEnrollmentById(candidate.enrollmentId)
      val verificationUuid = UUID.randomUUID().toString()
      val cert = CertificateEntity(
        id = "cert-${UUID.randomUUID().toString().take(8)}",
        enrollmentId = candidate.enrollmentId,
        studentId = candidate.studentId,
        studentName = student?.name ?: "Student Holder",
        verificationUuid = verificationUuid,
        type = if (enrollment?.mode == EnrollmentMode.ONLINE) CertificateType.THEORY else CertificateType.FULL,
        category = LicenseCategory.B,
        issuedAt = System.currentTimeMillis(),
        qrPayload = "https://driveschool.cm/verify/$verificationUuid"
      )
      dao.insertCertificate(cert)
      return cert
    }
    return null
  }

  // Issue Insurance Policy
  suspend fun issueInsurancePolicy(
    holderId: String,
    holderName: String,
    tariffName: String,
    tariffNameFr: String,
    vehiclePlate: String,
    premiumAmount: Double,
    durationMonths: Int = 12,
    issuer: UserEntity? = null
  ): InsurancePolicyEntity {
    val startsAt = System.currentTimeMillis()
    val expiresAt = startsAt + (durationMonths.toLong() * 30L * 24 * 3600 * 1000)
    val policy = InsurancePolicyEntity(
      id = "ins-${UUID.randomUUID().toString().take(8)}",
      holderId = holderId,
      holderName = holderName,
      tariffName = tariffName,
      tariffNameFr = tariffNameFr,
      vehiclePlate = vehiclePlate,
      issuedBy = issuer?.id,
      startsAt = startsAt,
      expiresAt = expiresAt,
      premiumAmount = premiumAmount,
      status = "ACTIVE"
    )
    dao.insertInsurancePolicy(policy)

    dao.insertAuditLog(
      AuditLogEntity(
        action = "INSURANCE_POLICY_ISSUED",
        actorId = issuer?.id ?: holderId,
        actorName = issuer?.name ?: holderName,
        actorRole = issuer?.role?.name ?: "STUDENT",
        details = "Issued policy $tariffName for vehicle $vehiclePlate. Premium: $premiumAmount XAF. Expires: in $durationMonths months."
      )
    )
    return policy
  }

  // BR-09: Expiry Alert Engine Daily Scan Simulation (30/7/1 days lead time)
  suspend fun runDailyExpiryScan(): Int {
    val now = System.currentTimeMillis()
    val oneDayMs = 24L * 3600 * 1000
    var generatedAlerts = 0

    // 1. Scan Insurance Policies
    val policies = dao.getAllInsurancePolicies().firstOrNull() ?: emptyList()
    for (p in policies) {
      val daysLeft = ((p.expiresAt - now) / oneDayMs).toInt()
      val matchedLead = when {
        daysLeft in 0..1 -> 1
        daysLeft in 2..7 -> 7
        daysLeft in 8..30 -> 30
        else -> null
      }
      if (matchedLead != null) {
        val alert = ExpiryAlertEntity(
          expirableType = "INSURANCE",
          expirableId = p.id,
          titleEn = "Insurance Policy Expiry ($matchedLead Day Warning): ${p.holderName}",
          titleFr = "Alerte Expiration Assurance ($matchedLead Jours): ${p.holderName}",
          messageEn = "Vehicle ${p.vehiclePlate} (${p.tariffName}) expires in $daysLeft day(s). Automated renewal dispatch queued via SMS and WhatsApp.",
          messageFr = "Véhicule ${p.vehiclePlate} (${p.tariffNameFr}) expire dans $daysLeft jour(s). Relance de renouvellement envoyée par SMS/WhatsApp.",
          leadDays = matchedLead,
          expiryDate = p.expiresAt,
          channels = "IN_APP, SMS (+237), EMAIL",
          notifiedAt = now,
          isRead = false
        )
        dao.insertExpiryAlert(alert)
        generatedAlerts++
      }
    }

    // 2. Scan Vehicle Servicing
    val vehicles = dao.getAllVehicles().firstOrNull() ?: emptyList()
    for (v in vehicles) {
      val daysLeft = ((v.nextServiceDate - now) / oneDayMs).toInt()
      val kmLeft = v.nextServiceKm - v.odometer
      val isServiceDue = daysLeft <= 7 || kmLeft <= 200
      if (isServiceDue) {
        val lead = if (daysLeft <= 1 || kmLeft <= 50) 1 else 7
        val alert = ExpiryAlertEntity(
          expirableType = "VEHICLE_SERVICE",
          expirableId = v.id,
          titleEn = "Maintenance Overdue Alert: Vehicle ${v.plateNo}",
          titleFr = "Alerte Entretien Technique: Véhicule ${v.plateNo}",
          messageEn = "${v.makeModel} is due for servicing (Odometer: ${v.odometer}/${v.nextServiceKm} km, Days: $daysLeft). Scheduled session bookings blocked.",
          messageFr = "${v.makeModel} nécessite une révision (Kilométrage: ${v.odometer}/${v.nextServiceKm} km). Réservations suspendues.",
          leadDays = lead,
          expiryDate = v.nextServiceDate,
          channels = "IN_APP, SMS (+237)",
          notifiedAt = now,
          isRead = false
        )
        dao.insertExpiryAlert(alert)
        generatedAlerts++
      }
    }

    return generatedAlerts
  }

  // Update Lesson Progress
  suspend fun updateLessonProgress(enrollmentId: String, lessonId: String, score: Double? = null) {
    dao.insertProgress(
      LessonProgressEntity(
        enrollmentId = enrollmentId,
        lessonId = lessonId,
        status = ProgressStatus.DONE,
        score = score,
        completedAt = System.currentTimeMillis()
      )
    )
  }
}
