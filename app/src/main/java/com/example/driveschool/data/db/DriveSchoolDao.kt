package com.example.driveschool.data.db

import androidx.room.*
import com.example.driveschool.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DriveSchoolDao {

  // Users
  @Query("SELECT * FROM users")
  fun getAllUsers(): Flow<List<UserEntity>>

  @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
  suspend fun getUserById(userId: String): UserEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserEntity)

  @Update
  suspend fun updateUser(user: UserEntity)

  // Branches
  @Query("SELECT * FROM branches ORDER BY isVirtual ASC, name ASC")
  fun getAllBranches(): Flow<List<BranchEntity>>

  @Query("SELECT * FROM branches WHERE id = :branchId LIMIT 1")
  suspend fun getBranchById(branchId: String): BranchEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBranch(branch: BranchEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBranches(branches: List<BranchEntity>)

  // Courses
  @Query("SELECT * FROM courses ORDER BY licenseCategory ASC")
  fun getAllCourses(): Flow<List<CourseEntity>>

  @Query("SELECT * FROM courses WHERE id = :courseId LIMIT 1")
  suspend fun getCourseById(courseId: String): CourseEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCourses(courses: List<CourseEntity>)

  // Fee Schedules
  @Query("SELECT * FROM fee_schedules")
  fun getAllFeeSchedules(): Flow<List<FeeScheduleEntity>>

  @Query("SELECT * FROM fee_schedules WHERE courseId = :courseId LIMIT 1")
  suspend fun getFeeScheduleForCourse(courseId: String): FeeScheduleEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFeeSchedules(schedules: List<FeeScheduleEntity>)

  // Enrollments
  @Query("SELECT * FROM enrollments ORDER BY createdAt DESC")
  fun getAllEnrollments(): Flow<List<EnrollmentEntity>>

  @Query("SELECT * FROM enrollments WHERE studentId = :studentId ORDER BY createdAt DESC")
  fun getEnrollmentsByStudent(studentId: String): Flow<List<EnrollmentEntity>>

  @Query("SELECT * FROM enrollments WHERE branchId = :branchId ORDER BY createdAt DESC")
  fun getEnrollmentsByBranch(branchId: String): Flow<List<EnrollmentEntity>>

  @Query("SELECT * FROM enrollments WHERE id = :id LIMIT 1")
  suspend fun getEnrollmentById(id: String): EnrollmentEntity?

  @Query("SELECT * FROM enrollments WHERE discountPercent > 15.0 AND isDiscountApproved = 0")
  fun getPendingDiscountApprovals(): Flow<List<EnrollmentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertEnrollment(enrollment: EnrollmentEntity)

  @Update
  suspend fun updateEnrollment(enrollment: EnrollmentEntity)

  // Lessons
  @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
  fun getLessonsByCourse(courseId: String): Flow<List<LessonEntity>>

  @Query("SELECT * FROM lessons WHERE id = :lessonId LIMIT 1")
  suspend fun getLessonById(lessonId: String): LessonEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLessons(lessons: List<LessonEntity>)

  // Lesson Progress
  @Query("SELECT * FROM lesson_progress WHERE enrollmentId = :enrollmentId")
  fun getProgressForEnrollment(enrollmentId: String): Flow<List<LessonProgressEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProgress(progress: LessonProgressEntity)

  // Payments
  @Query("SELECT * FROM payments ORDER BY createdAt DESC")
  fun getAllPayments(): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE enrollmentId = :enrollmentId ORDER BY createdAt DESC")
  fun getPaymentsForEnrollment(enrollmentId: String): Flow<List<PaymentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentEntity)

  // Practical Sessions
  @Query("SELECT * FROM practical_sessions ORDER BY scheduledAt ASC")
  fun getAllPracticalSessions(): Flow<List<PracticalSessionEntity>>

  @Query("SELECT * FROM practical_sessions WHERE instructorId = :instructorId ORDER BY scheduledAt ASC")
  fun getSessionsByInstructor(instructorId: String): Flow<List<PracticalSessionEntity>>

  @Query("SELECT * FROM practical_sessions WHERE studentId = :studentId ORDER BY scheduledAt ASC")
  fun getSessionsByStudent(studentId: String): Flow<List<PracticalSessionEntity>>

  @Query("SELECT * FROM practical_sessions WHERE vehicleId = :vehicleId")
  suspend fun getSessionsByVehicle(vehicleId: String): List<PracticalSessionEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPracticalSession(session: PracticalSessionEntity)

  @Update
  suspend fun updatePracticalSession(session: PracticalSessionEntity)

  // Vehicles
  @Query("SELECT * FROM vehicles ORDER BY plateNo ASC")
  fun getAllVehicles(): Flow<List<VehicleEntity>>

  @Query("SELECT * FROM vehicles WHERE branchId = :branchId")
  fun getVehiclesByBranch(branchId: String): Flow<List<VehicleEntity>>

  @Query("SELECT * FROM vehicles WHERE id = :vehicleId LIMIT 1")
  suspend fun getVehicleById(vehicleId: String): VehicleEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVehicle(vehicle: VehicleEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVehicles(vehicles: List<VehicleEntity>)

  @Update
  suspend fun updateVehicle(vehicle: VehicleEntity)

  // Exam Sessions
  @Query("SELECT * FROM exam_sessions ORDER BY scheduledDate ASC")
  fun getAllExamSessions(): Flow<List<ExamSessionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExamSessions(sessions: List<ExamSessionEntity>)

  // Exam Candidates
  @Query("SELECT * FROM exam_candidates")
  fun getAllCandidates(): Flow<List<ExamCandidateEntity>>

  @Query("SELECT * FROM exam_candidates WHERE id = :id LIMIT 1")
  suspend fun getCandidateById(id: String): ExamCandidateEntity?

  @Query("SELECT * FROM exam_candidates WHERE status = 'RECOMMENDED'")
  fun getCandidatesPendingApproval(): Flow<List<ExamCandidateEntity>>

  @Query("SELECT * FROM exam_candidates WHERE status = 'APPROVED'")
  fun getCandidatesReadyForApplication(): Flow<List<ExamCandidateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCandidate(candidate: ExamCandidateEntity)

  @Update
  suspend fun updateCandidate(candidate: ExamCandidateEntity)

  // Certificates
  @Query("SELECT * FROM certificates ORDER BY issuedAt DESC")
  fun getAllCertificates(): Flow<List<CertificateEntity>>

  @Query("SELECT * FROM certificates WHERE verificationUuid = :uuid LIMIT 1")
  suspend fun getCertificateByUuid(uuid: String): CertificateEntity?

  @Query("SELECT * FROM certificates WHERE studentId = :studentId")
  fun getCertificatesByStudent(studentId: String): Flow<List<CertificateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCertificate(cert: CertificateEntity)

  // Insurance Policies
  @Query("SELECT * FROM insurance_policies ORDER BY expiresAt ASC")
  fun getAllInsurancePolicies(): Flow<List<InsurancePolicyEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInsurancePolicy(policy: InsurancePolicyEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInsurancePolicies(policies: List<InsurancePolicyEntity>)

  @Update
  suspend fun updateInsurancePolicy(policy: InsurancePolicyEntity)

  // Audit Logs
  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
  fun getAllAuditLogs(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAuditLog(log: AuditLogEntity)

  // Expiry Alerts
  @Query("SELECT * FROM expiry_alerts ORDER BY leadDays ASC, expiryDate ASC")
  fun getAllExpiryAlerts(): Flow<List<ExpiryAlertEntity>>

  @Query("SELECT COUNT(*) FROM expiry_alerts WHERE isRead = 0")
  fun getUnreadAlertsCount(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpiryAlert(alert: ExpiryAlertEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExpiryAlerts(alerts: List<ExpiryAlertEntity>)

  @Query("UPDATE expiry_alerts SET isRead = 1 WHERE id = :id")
  suspend fun markAlertAsRead(id: String)

  // Clear / Reset queries for seeding
  @Query("DELETE FROM users")
  suspend fun clearUsers()

  @Query("DELETE FROM branches")
  suspend fun clearBranches()

  @Query("DELETE FROM courses")
  suspend fun clearCourses()

  @Query("DELETE FROM fee_schedules")
  suspend fun clearFeeSchedules()

  @Query("DELETE FROM enrollments")
  suspend fun clearEnrollments()

  @Query("DELETE FROM lessons")
  suspend fun clearLessons()

  @Query("DELETE FROM payments")
  suspend fun clearPayments()

  @Query("DELETE FROM practical_sessions")
  suspend fun clearPracticalSessions()

  @Query("DELETE FROM vehicles")
  suspend fun clearVehicles()

  @Query("DELETE FROM exam_sessions")
  suspend fun clearExamSessions()

  @Query("DELETE FROM exam_candidates")
  suspend fun clearCandidates()

  @Query("DELETE FROM certificates")
  suspend fun clearCertificates()

  @Query("DELETE FROM insurance_policies")
  suspend fun clearInsurancePolicies()

  @Query("DELETE FROM audit_logs")
  suspend fun clearAuditLogs()

  @Query("DELETE FROM expiry_alerts")
  suspend fun clearExpiryAlerts()
}
