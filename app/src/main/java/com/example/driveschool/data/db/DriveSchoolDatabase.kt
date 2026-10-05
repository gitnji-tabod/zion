package com.example.driveschool.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.driveschool.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    UserEntity::class,
    BranchEntity::class,
    CourseEntity::class,
    FeeScheduleEntity::class,
    EnrollmentEntity::class,
    LessonEntity::class,
    LessonProgressEntity::class,
    PaymentEntity::class,
    PracticalSessionEntity::class,
    VehicleEntity::class,
    ExamSessionEntity::class,
    ExamCandidateEntity::class,
    CertificateEntity::class,
    InsurancePolicyEntity::class,
    AuditLogEntity::class,
    ExpiryAlertEntity::class
  ],
  version = 1,
  exportSchema = false
)
@TypeConverters(DriveSchoolConverters::class)
abstract class DriveSchoolDatabase : RoomDatabase() {

  abstract fun driveSchoolDao(): DriveSchoolDao

  companion object {
    @Volatile
    private var INSTANCE: DriveSchoolDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): DriveSchoolDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          DriveSchoolDatabase::class.java,
          "driveschool_platform.db"
        )
        .addCallback(DriveSchoolDatabaseCallback(scope))
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }

  private class DriveSchoolDatabaseCallback(
    private val scope: CoroutineScope
  ) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
      super.onCreate(db)
      INSTANCE?.let { database ->
        scope.launch(Dispatchers.IO) {
          populateInitialDatabase(database.driveSchoolDao())
        }
      }
    }
  }
}

suspend fun populateInitialDatabase(dao: DriveSchoolDao) {
  // Seed branches
  dao.insertBranches(DriveSchoolSeedData.branches)
  // Seed users
  DriveSchoolSeedData.users.forEach { dao.insertUser(it) }
  // Seed courses
  dao.insertCourses(DriveSchoolSeedData.courses)
  // Seed fee schedules
  dao.insertFeeSchedules(DriveSchoolSeedData.feeSchedules)
  // Seed lessons
  dao.insertLessons(DriveSchoolSeedData.lessons)
  // Seed vehicles
  dao.insertVehicles(DriveSchoolSeedData.vehicles)
  // Seed enrollments
  dao.insertEnrollment(DriveSchoolSeedData.enrollmentOnsiteBrenda)
  dao.insertEnrollment(DriveSchoolSeedData.enrollmentOnlineThomas)
  dao.insertEnrollment(DriveSchoolSeedData.enrollmentPendingDiscount)
  dao.insertEnrollment(DriveSchoolSeedData.enrollmentLucasTruck)
  dao.insertEnrollment(DriveSchoolSeedData.enrollmentYaounde)
  dao.insertEnrollment(DriveSchoolSeedData.enrollmentBafoussam)
  dao.insertEnrollment(DriveSchoolSeedData.enrollmentGaroua)
  // Seed exam sessions and candidates
  dao.insertExamSessions(listOf(DriveSchoolSeedData.examSessionBamenda, DriveSchoolSeedData.examSessionDouala))
  dao.insertCandidate(DriveSchoolSeedData.candidate1)
  dao.insertCandidate(DriveSchoolSeedData.candidate2)
  dao.insertCandidate(DriveSchoolSeedData.candidate3)
  dao.insertCandidate(DriveSchoolSeedData.candidatePassedBrenda)
  // Seed certificate
  dao.insertCertificate(DriveSchoolSeedData.certificateBrenda)
  // Seed insurance policies
  dao.insertInsurancePolicies(DriveSchoolSeedData.insurancePolicies)
  // Seed alerts
  dao.insertExpiryAlerts(DriveSchoolSeedData.expiryAlerts)
  // Seed audit logs
  DriveSchoolSeedData.auditLogs.forEach { dao.insertAuditLog(it) }
  // Seed payments
  DriveSchoolSeedData.payments.forEach { dao.insertPayment(it) }
  // Seed practical sessions
  DriveSchoolSeedData.practicalSessions.forEach { dao.insertPracticalSession(it) }
}

suspend fun resetToProductionSeedData(dao: DriveSchoolDao) {
  dao.clearUsers()
  dao.clearBranches()
  dao.clearCourses()
  dao.clearFeeSchedules()
  dao.clearEnrollments()
  dao.clearLessons()
  dao.clearVehicles()
  dao.clearExamSessions()
  dao.clearCandidates()
  dao.clearCertificates()
  dao.clearInsurancePolicies()
  dao.clearAuditLogs()
  dao.clearExpiryAlerts()
  dao.clearPayments()
  dao.clearPracticalSessions()

  populateInitialDatabase(dao)
}
