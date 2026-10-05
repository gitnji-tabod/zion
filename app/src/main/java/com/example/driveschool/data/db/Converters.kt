package com.example.driveschool.data.db

import androidx.room.TypeConverter
import com.example.driveschool.data.model.*

class DriveSchoolConverters {

  @TypeConverter
  fun fromUserRole(value: UserRole?): String? = value?.name

  @TypeConverter
  fun toUserRole(value: String?): UserRole? = value?.let { enumValueOf<UserRole>(it) }

  @TypeConverter
  fun fromLicenseCategory(value: LicenseCategory?): String? = value?.name

  @TypeConverter
  fun toLicenseCategory(value: String?): LicenseCategory? = value?.let { enumValueOf<LicenseCategory>(it) }

  @TypeConverter
  fun fromDeliveryType(value: DeliveryType?): String? = value?.name

  @TypeConverter
  fun toDeliveryType(value: String?): DeliveryType? = value?.let { enumValueOf<DeliveryType>(it) }

  @TypeConverter
  fun fromEnrollmentMode(value: EnrollmentMode?): String? = value?.name

  @TypeConverter
  fun toEnrollmentMode(value: String?): EnrollmentMode? = value?.let { enumValueOf<EnrollmentMode>(it) }

  @TypeConverter
  fun fromEnrollmentStatus(value: EnrollmentStatus?): String? = value?.name

  @TypeConverter
  fun toEnrollmentStatus(value: String?): EnrollmentStatus? = value?.let { enumValueOf<EnrollmentStatus>(it) }

  @TypeConverter
  fun fromLessonType(value: LessonType?): String? = value?.name

  @TypeConverter
  fun toLessonType(value: String?): LessonType? = value?.let { enumValueOf<LessonType>(it) }

  @TypeConverter
  fun fromProgressStatus(value: ProgressStatus?): String? = value?.name

  @TypeConverter
  fun toProgressStatus(value: String?): ProgressStatus? = value?.let { enumValueOf<ProgressStatus>(it) }

  @TypeConverter
  fun fromPaymentChannel(value: PaymentChannel?): String? = value?.name

  @TypeConverter
  fun toPaymentChannel(value: String?): PaymentChannel? = value?.let { enumValueOf<PaymentChannel>(it) }

  @TypeConverter
  fun fromPaymentStatus(value: PaymentStatus?): String? = value?.name

  @TypeConverter
  fun toPaymentStatus(value: String?): PaymentStatus? = value?.let { enumValueOf<PaymentStatus>(it) }

  @TypeConverter
  fun fromAttendanceStatus(value: AttendanceStatus?): String? = value?.name

  @TypeConverter
  fun toAttendanceStatus(value: String?): AttendanceStatus? = value?.let { enumValueOf<AttendanceStatus>(it) }

  @TypeConverter
  fun fromCandidateStatus(value: CandidateStatus?): String? = value?.name

  @TypeConverter
  fun toCandidateStatus(value: String?): CandidateStatus? = value?.let { enumValueOf<CandidateStatus>(it) }

  @TypeConverter
  fun fromCertificateType(value: CertificateType?): String? = value?.name

  @TypeConverter
  fun toCertificateType(value: String?): CertificateType? = value?.let { enumValueOf<CertificateType>(it) }

  @TypeConverter
  fun fromVehicleStatus(value: VehicleStatus?): String? = value?.name

  @TypeConverter
  fun toVehicleStatus(value: String?): VehicleStatus? = value?.let { enumValueOf<VehicleStatus>(it) }
}
