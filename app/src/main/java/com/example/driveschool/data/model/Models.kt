package com.example.driveschool.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserRole {
  SUPER_ADMIN,
  BRANCH_MANAGER,
  SECRETARY,
  INSTRUCTOR,
  STUDENT
}

enum class LicenseCategory(val code: String, val titleEn: String, val titleFr: String, val vehicleType: String) {
  A("A", "Category A - Motorcycles", "Catégorie A - Motocyclettes", "Motorcycle / Moto"),
  B("B", "Category B - Light Vehicles", "Catégorie B - Véhicules Légers", "Car / Automobile (<= 3.5t)"),
  C("C", "Category C - Heavy Trucks", "Catégorie C - Poids Lourds", "Truck / Camion (> 3.5t)"),
  D("D", "Category D - Passenger Buses", "Catégorie D - Transport en Commun", "Bus (> 9 places)"),
  E("E", "Category E - Articulated / Trailers", "Catégorie E - Remorques", "Semi-Trailer / Remorque"),
  F("F", "Category F - Adapted Mobility", "Catégorie F - Mobilité Réduite", "Specially Adapted Vehicle"),
  G("G", "Category G - Agricultural & Machinery", "Catégorie G - Engins Agricoles", "Tractor / Engin de Chantier")
}

enum class DeliveryType {
  THEORY,
  PRACTICAL,
  COMBINED
}

enum class EnrollmentMode {
  ONLINE,
  ONSITE
}

enum class EnrollmentStatus {
  ONBOARDING,
  AWAITING_PAYMENT,
  ACTIVE,
  COMPLETED,
  EXPIRED,
  SUSPENDED
}

enum class LessonType {
  VIDEO,
  DOCUMENT,
  QUIZ
}

enum class ProgressStatus {
  NOT_STARTED,
  IN_PROGRESS,
  DONE
}

enum class PaymentChannel {
  MTN_MOMO,
  ORANGE_MONEY,
  CARD,
  CASH
}

enum class PaymentStatus {
  PENDING,
  CONFIRMED,
  FAILED
}

enum class AttendanceStatus {
  SCHEDULED,
  PRESENT,
  ABSENT,
  LATE
}

enum class CandidateStatus {
  RECOMMENDED,
  APPROVED,
  APPLIED,
  SAT,
  PASSED,
  FAILED
}

enum class CertificateType {
  THEORY,
  FULL
}

enum class VehicleStatus {
  ACTIVE,
  SERVICE_DUE,
  IN_REPAIR
}

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val name: String,
  val email: String,
  val phone: String,
  val countryCode: String = "CM", // "CM" = Cameroon (onsite-capable), others = Online-only
  val preferredLocale: String = "en", // "en" or "fr"
  val role: UserRole = UserRole.STUDENT,
  val branchId: String? = null // physical branch ID or virtual online branch
) {
  val isOnsiteCapable: Boolean
    get() = countryCode.equals("CM", ignoreCase = true)
}

@Entity(tableName = "branches")
data class BranchEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val name: String,
  val city: String,
  val address: String,
  val phone: String,
  val isVirtual: Boolean = false,
  val managerId: String? = null
)

@Entity(tableName = "courses")
data class CourseEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val title: String,
  val titleFr: String,
  val licenseCategory: LicenseCategory,
  val delivery: DeliveryType,
  val durationWeeks: Int,
  val description: String,
  val descriptionFr: String
)

@Entity(tableName = "fee_schedules")
data class FeeScheduleEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val courseId: String,
  val standardAmount: Double, // XAF
  val platformFee: Double,    // XAF for online theory
  val currency: String = "XAF",
  val effectiveFrom: String = "2026-01-01"
)

@Entity(tableName = "enrollments")
data class EnrollmentEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val studentId: String,
  val courseId: String,
  val branchId: String,
  val mode: EnrollmentMode,
  val status: EnrollmentStatus = EnrollmentStatus.ONBOARDING,
  val negotiatedAmount: Double? = null,
  val negotiatedBy: String? = null, // Secretary ID
  val approvedBy: String? = null,   // Branch Manager ID
  val discountPercent: Double = 0.0,
  val isDiscountApproved: Boolean = false,
  val termEndsAt: Long = System.currentTimeMillis() + 90L * 24 * 3600 * 1000, // 90 days validity (Expirable)
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "lessons")
data class LessonEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val courseId: String,
  val moduleTitle: String,
  val moduleTitleFr: String,
  val orderIndex: Int,
  val title: String,
  val titleFr: String,
  val type: LessonType,
  val isOnboarding: Boolean = false, // Accessible pre-payment
  val durationMinutes: Int = 15,
  val videoUrl: String = "",
  val contentBodyEn: String = "",
  val contentBodyFr: String = "",
  val quizQuestionsJson: String = ""
)

@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val enrollmentId: String,
  val lessonId: String,
  val status: ProgressStatus = ProgressStatus.NOT_STARTED,
  val score: Double? = null,
  val completedAt: Long? = null
)

@Entity(tableName = "payments")
data class PaymentEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val enrollmentId: String,
  val studentId: String,
  val amount: Double,
  val channel: PaymentChannel,
  val reference: String,
  val collectedBy: String? = null, // Secretary ID if cash
  val status: PaymentStatus = PaymentStatus.CONFIRMED,
  val createdAt: Long = System.currentTimeMillis(),
  val notes: String? = null
)

@Entity(tableName = "practical_sessions")
data class PracticalSessionEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val enrollmentId: String,
  val studentId: String,
  val instructorId: String,
  val vehicleId: String,
  val scheduledAt: Long,
  val durationMinutes: Int = 60,
  val attendance: AttendanceStatus = AttendanceStatus.SCHEDULED,
  val odometerStart: Int = 0,
  val odometerEnd: Int? = null,
  val instructorFeedback: String? = null
)

@Entity(tableName = "vehicles")
data class VehicleEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val branchId: String,
  val plateNo: String,
  val makeModel: String,
  val odometer: Int,
  val nextServiceKm: Int,     // Expirable by distance
  val nextServiceDate: Long,  // Expirable by date
  val status: VehicleStatus = VehicleStatus.ACTIVE
)

@Entity(tableName = "exam_sessions")
data class ExamSessionEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val sessionName: String,
  val branchId: String,
  val scheduledDate: Long,
  val category: LicenseCategory,
  val type: DeliveryType = DeliveryType.COMBINED
)

@Entity(tableName = "exam_candidates")
data class ExamCandidateEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val examSessionId: String,
  val enrollmentId: String,
  val studentId: String,
  val status: CandidateStatus = CandidateStatus.RECOMMENDED,
  val recommendedBy: String? = null, // Instructor ID
  val approvedBy: String? = null,    // Branch Manager ID
  val appliedBy: String? = null,     // Secretary ID
  val score: Double? = null,
  val resultEnteredAt: Long? = null
)

@Entity(tableName = "certificates")
data class CertificateEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val enrollmentId: String,
  val studentId: String,
  val studentName: String,
  val verificationUuid: String = UUID.randomUUID().toString(),
  val type: CertificateType = CertificateType.FULL,
  val category: LicenseCategory = LicenseCategory.B,
  val branchName: String = "Bamenda Main Branch",
  val issuedAt: Long = System.currentTimeMillis(),
  val qrPayload: String = ""
)

@Entity(tableName = "insurance_policies")
data class InsurancePolicyEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val holderId: String,
  val holderName: String,
  val tariffName: String,
  val tariffNameFr: String,
  val vehiclePlate: String,
  val issuedBy: String? = null,
  val startsAt: Long,
  val expiresAt: Long, // Expirable
  val premiumAmount: Double,
  val status: String = "ACTIVE"
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val action: String,
  val actorId: String,
  val actorName: String,
  val actorRole: String,
  val details: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expiry_alerts")
data class ExpiryAlertEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val expirableType: String, // "INSURANCE", "ENROLLMENT", "VEHICLE_SERVICE"
  val expirableId: String,
  val titleEn: String,
  val titleFr: String,
  val messageEn: String,
  val messageFr: String,
  val leadDays: Int, // 30, 7, 1
  val expiryDate: Long,
  val channels: String = "IN_APP, SMS (+237), EMAIL",
  val notifiedAt: Long = System.currentTimeMillis(),
  val isRead: Boolean = false
)
