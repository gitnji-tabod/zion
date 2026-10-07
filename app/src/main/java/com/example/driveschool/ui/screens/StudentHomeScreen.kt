package com.example.driveschool.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.*
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(
  currentLocale: String,
  currentUser: UserEntity?,
  allUsers: List<UserEntity>,
  enrollments: List<EnrollmentEntity>,
  courses: List<CourseEntity>,
  lessons: List<LessonEntity>,
  lessonProgressList: List<LessonProgressEntity> = emptyList(),
  vehicles: List<VehicleEntity>,
  practicalSessions: List<PracticalSessionEntity>,
  examSessions: List<ExamSessionEntity> = emptyList(),
  candidates: List<ExamCandidateEntity>,
  certificates: List<CertificateEntity>,
  onSelectStudentUser: (UserEntity) -> Unit,
  onLessonClick: (LessonEntity, Boolean) -> Unit,
  onOpenPaymentDialog: (EnrollmentEntity) -> Unit,
  onBookPracticalSession: (String, String, String, String, Long) -> Unit,
  onViewCertificate: (CertificateEntity) -> Unit,
  onOpenProfile: () -> Unit = {}
) {
  // Find current student's active enrollment
  val studentEnrollment = remember(enrollments, currentUser) {
    enrollments.find { it.studentId == currentUser?.id } ?: enrollments.firstOrNull()
  }

  val activeCourse = remember(courses, studentEnrollment) {
    courses.find { it.id == studentEnrollment?.courseId } ?: courses.firstOrNull()
  }

  val isLockedByPaymentGate = studentEnrollment?.status == EnrollmentStatus.ONBOARDING ||
    studentEnrollment?.status == EnrollmentStatus.AWAITING_PAYMENT

  // 1. Calculate Theory Course Progress
  val myEnrollmentProgress = remember(lessonProgressList, studentEnrollment) {
    if (studentEnrollment != null) {
      lessonProgressList.filter { it.enrollmentId == studentEnrollment.id }
    } else emptyList()
  }

  val completedLessonsCount = remember(myEnrollmentProgress) {
    myEnrollmentProgress.count { it.status == ProgressStatus.DONE }
  }

  val totalLessonsCount = lessons.size
  val progressPercent = remember(completedLessonsCount, totalLessonsCount) {
    if (totalLessonsCount > 0) {
      (completedLessonsCount.toFloat() / totalLessonsCount.toFloat()).coerceIn(0f, 1f)
    } else 0f
  }
  val animatedProgress by animateFloatAsState(targetValue = progressPercent, label = "course_progress")

  // 2. Upcoming & Completed Driving Lessons (Practical Sessions)
  val myPracticalSessions = remember(practicalSessions, currentUser) {
    practicalSessions
      .filter { it.studentId == currentUser?.id }
      .sortedBy { it.scheduledAt }
  }
  val now = System.currentTimeMillis()
  val upcomingSessions = remember(myPracticalSessions, now) {
    myPracticalSessions.filter { it.scheduledAt >= now - 3600_000 }
  }
  val pastSessions = remember(myPracticalSessions, now) {
    myPracticalSessions.filter { it.scheduledAt < now - 3600_000 }
  }

  // 3. Theory Exam & Official Candidate Status
  val myCandidateRecord = remember(candidates, currentUser) {
    candidates.find { it.studentId == currentUser?.id }
  }
  val myExamSession = remember(examSessions, myCandidateRecord) {
    if (myCandidateRecord != null) {
      examSessions.find { it.id == myCandidateRecord.examSessionId }
    } else null
  }

  // Active filter tab: "ALL", "OVERVIEW", "LESSONS", "DRIVING", "EXAM"
  var selectedTabFilter by remember { mutableStateOf("ALL") }
  var showBookingDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp, vertical = 12.dp)
      .testTag("student_home_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {

    // Persona switch preview (if multiple student accounts exist for demo)
    if (allUsers.count { it.role == UserRole.STUDENT } > 1) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.SwitchAccount, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (currentLocale == "fr") "Élève Actif :" else "Student Persona:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
              )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              allUsers.filter { it.role == UserRole.STUDENT }.forEach { student ->
                val isSelected = currentUser?.id == student.id
                FilterChip(
                  selected = isSelected,
                  onClick = { onSelectStudentUser(student) },
                  label = {
                    Text(
                      text = "${if (student.isOnsiteCapable) "🇨🇲" else "🌍"} ${student.name.split(" ").first()}",
                      fontSize = 11.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AmberAccent,
                    selectedLabelColor = NavyDark
                  ),
                  modifier = Modifier.testTag("persona_student_${student.id}")
                )
              }
            }
          }
        }
      }
    }

    // Hero Enrolled Course & Live Progress Banner
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth().testTag("student_hero_course_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(NavyDark, Color(0xFF1E293B))
              )
            )
            .padding(18.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Category & Delivery Pill
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = AmberAccent
            ) {
              Text(
                text = "PERMIS ${activeCourse?.licenseCategory?.code ?: "B"} • ${if (studentEnrollment?.mode == EnrollmentMode.ONLINE) "100% ONLINE" else "HYBRID (CM)"}",
                color = NavyDark,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            // Enrollment Status Pill
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (isLockedByPaymentGate) AmberAccent.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isLockedByPaymentGate) AmberAccent else EmeraldSuccess)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text = Localization.enrollmentStatusName(studentEnrollment?.status?.name ?: "ACTIVE", currentLocale),
                  color = if (isLockedByPaymentGate) AmberAccent else EmeraldSuccess,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = if (currentLocale == "fr") activeCourse?.titleFr ?: "Permis Catégorie B" else activeCourse?.title ?: "Category B - Light Motor Vehicle",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 18.sp
            )
          )

          Spacer(modifier = Modifier.height(4.dp))

          Text(
            text = if (currentLocale == "fr")
              "Formation officielle au Code de la route CEMAC & Conduite Pratique"
            else
              "Official Highway Code & Practical Driving Preparation Program",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 12.sp)
          )

          Spacer(modifier = Modifier.height(16.dp))

          // Theory Progress Bar & Percentage
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (currentLocale == "fr") "Progression Théorique (LMS)" else "Theory Course Progress",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "${(animatedProgress * 100).toInt()}% ($completedLessonsCount/$totalLessonsCount)",
                style = MaterialTheme.typography.bodySmall.copy(color = AmberAccent, fontWeight = FontWeight.Bold)
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = { animatedProgress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .testTag("course_progress_indicator"),
              color = AmberAccent,
              trackColor = Color(0xFF334155)
            )
          }

          // Actions row: Booking and Profile
          Spacer(modifier = Modifier.height(14.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            if (currentUser?.isOnsiteCapable == true && studentEnrollment?.status == EnrollmentStatus.ACTIVE) {
              Button(
                onClick = { showBookingDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                  .weight(1f)
                  .testTag("book_practical_session_button")
              ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (currentLocale == "fr") "Réserver Conduite" else "Book Lesson",
                  color = NavyDark,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }

            OutlinedButton(
              onClick = onOpenProfile,
              colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("open_user_profile_button")
            ) {
              Icon(Icons.Default.AccountCircle, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (currentLocale == "fr") "Mon Profil & Abonnement" else "My Profile & Status",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // Dashboard Quick Navigation Filter Tabs
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().testTag("student_filter_tabs")
      ) {
        val tabs = listOf(
          "ALL" to (if (currentLocale == "fr") "Vue Complète" else "All Overview"),
          "LESSONS" to (if (currentLocale == "fr") "Cours & LMS" else "Course Progress"),
          "DRIVING" to (if (currentLocale == "fr") "Séances Pratiques" else "Driving Lessons"),
          "EXAM" to (if (currentLocale == "fr") "Statut Examen" else "Theory Exam")
        )
        items(tabs) { (tabKey, tabLabel) ->
          val isSelected = selectedTabFilter == tabKey
          FilterChip(
            selected = isSelected,
            onClick = { selectedTabFilter = tabKey },
            label = {
              Text(
                text = tabLabel,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 12.sp
              )
            },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = NavyPrimary,
              selectedLabelColor = Color.White
            ),
            modifier = Modifier.testTag("tab_filter_$tabKey")
          )
        }
      }
    }

    // PAYMENT GATE BANNER (BR-02 Enforced)
    if (isLockedByPaymentGate && studentEnrollment != null && (selectedTabFilter == "ALL" || selectedTabFilter == "LESSONS")) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("payment_gate_banner")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.LockClock,
                contentDescription = null,
                tint = Color(0xFFB45309),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = if (currentLocale == "fr") "Passerelle de Paiement Active (BR-02)" else "Payment Gate Enforced (BR-02)",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF92400E),
                fontSize = 14.sp
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (currentLocale == "fr")
                "Les leçons d'initiation sont gratuites ci-dessous. Débloquez tous les modules, les évaluations interactives et les créneaux pratiques en validant vos frais de scolarité (MTN MoMo, Orange Money, Carte ou Espèces)."
              else
                "Onboarding lessons are unlocked free of charge below. Settle your tuition fees (MTN Mobile Money, Orange Money, Card, or Cash) to unlock all modules, interactive quizzes, and in-car driving lessons.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F), fontSize = 12.sp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = { onOpenPaymentDialog(studentEnrollment) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("pay_course_fee_button")
            ) {
              Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLocale == "fr") "Régler les Frais & Débloquer Tout" else "Pay Tuition & Unlock Full Program",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // SECTION 1: UPCOMING & SCHEDULED DRIVING LESSONS
    if (selectedTabFilter == "ALL" || selectedTabFilter == "DRIVING") {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Leçons de Conduite Pratique" else "Upcoming Driving Lessons",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
          if (currentUser?.isOnsiteCapable == true && studentEnrollment?.status == EnrollmentStatus.ACTIVE) {
            TextButton(
              onClick = { showBookingDialog = true },
              modifier = Modifier.testTag("book_lesson_text_button")
            ) {
              Text(
                text = if (currentLocale == "fr") "+ Réserver" else "+ Book Lesson",
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
            }
          }
        }
      }

      if (upcomingSessions.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().testTag("no_upcoming_sessions_card")
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray)
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = if (currentLocale == "fr") "Aucune leçon pratique à venir" else "No upcoming driving lessons booked",
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 13.sp
                )
                Text(
                  text = if (currentUser?.isOnsiteCapable == true)
                    if (currentLocale == "fr") "Touchez 'Réserver' pour choisir un créneau avec un moniteur." else "Tap '+ Book Lesson' to schedule a session with an instructor."
                  else
                    if (currentLocale == "fr") "Profil 100% en ligne : formation théorique sur campus virtuel." else "100% Online profile: theory preparation on virtual campus.",
                  style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                )
              }
            }
          }
        }
      } else {
        items(upcomingSessions) { session ->
          val instructor = allUsers.find { it.id == session.instructorId }
          val vehicle = vehicles.find { it.id == session.vehicleId }

          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().testTag("session_card_${session.id}")
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = when (session.attendance) {
                    AttendanceStatus.SCHEDULED -> Color(0xFFDBEAFE)
                    AttendanceStatus.PRESENT -> Color(0xFFDCFCE7)
                    AttendanceStatus.ABSENT -> Color(0xFFFEE2E2)
                    AttendanceStatus.LATE -> Color(0xFFFEF3C7)
                  }
                ) {
                  Text(
                    text = Localization.attendanceStatusName(session.attendance.name, currentLocale),
                    color = when (session.attendance) {
                      AttendanceStatus.SCHEDULED -> Color(0xFF1E40AF)
                      AttendanceStatus.PRESENT -> Color(0xFF166534)
                      AttendanceStatus.ABSENT -> Color(0xFF991B1B)
                      AttendanceStatus.LATE -> Color(0xFF92400E)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = Localization.formatDate(session.scheduledAt, currentLocale),
                    fontSize = 11.sp,
                    color = Color.Gray
                  )
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(NavyPrimary.copy(alpha = 0.1f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Person, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${if (currentLocale == "fr") "Moniteur : " else "Instructor: "}${instructor?.name ?: "Assigned Instructor"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "${if (currentLocale == "fr") "Véhicule : " else "Car: "}${vehicle?.plateNo ?: "Dual-control"} • ${vehicle?.makeModel ?: "Dual-Control Fleet"}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                  )
                }
              }

              if (session.instructorFeedback != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = Color(0xFFF1F5F9),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "📝 ${session.instructorFeedback}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155), fontSize = 11.sp),
                    modifier = Modifier.padding(8.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // SECTION 2: OFFICIAL THEORY EXAM STATUS & CANDIDACY PIPELINE
    if (selectedTabFilter == "ALL" || selectedTabFilter == "EXAM") {
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Assignment, contentDescription = null, tint = NavyPrimary)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLocale == "fr") "Statut d'Examen Théorique & Pratique" else "Theory & Practical Exam Status",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("exam_status_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            if (myCandidateRecord != null) {
              // Candidate has been entered into the official exam pipeline
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (currentLocale == "fr") "Dossier de Candidat Examen" else "Official Exam Candidacy File",
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = when (myCandidateRecord.status) {
                    CandidateStatus.RECOMMENDED -> Color(0xFFFEF3C7)
                    CandidateStatus.APPROVED -> Color(0xFFDBEAFE)
                    CandidateStatus.APPLIED -> Color(0xFFE0E7FF)
                    CandidateStatus.SAT -> Color(0xFFF3E8FF)
                    CandidateStatus.PASSED -> EmeraldSuccess.copy(alpha = 0.2f)
                    CandidateStatus.FAILED -> Color(0xFFFEE2E2)
                  }
                ) {
                  Text(
                    text = Localization.candidateStatusName(myCandidateRecord.status.name, currentLocale),
                    color = when (myCandidateRecord.status) {
                      CandidateStatus.RECOMMENDED -> Color(0xFF92400E)
                      CandidateStatus.APPROVED -> Color(0xFF1E40AF)
                      CandidateStatus.APPLIED -> Color(0xFF3730A3)
                      CandidateStatus.SAT -> Color(0xFF6B21A8)
                      CandidateStatus.PASSED -> EmeraldSuccess
                      CandidateStatus.FAILED -> Color(0xFF991B1B)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Step-by-step pipeline status indicator
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                ExamPipelineStep(
                  title = if (currentLocale == "fr") "Recommandé" else "Recommended",
                  isDone = myCandidateRecord.status != CandidateStatus.RECOMMENDED,
                  isActive = myCandidateRecord.status == CandidateStatus.RECOMMENDED
                )
                ExamPipelineStep(
                  title = if (currentLocale == "fr") "Validé Chef" else "Manager Approved",
                  isDone = myCandidateRecord.status == CandidateStatus.APPLIED || myCandidateRecord.status == CandidateStatus.SAT || myCandidateRecord.status == CandidateStatus.PASSED,
                  isActive = myCandidateRecord.status == CandidateStatus.APPROVED
                )
                ExamPipelineStep(
                  title = if (currentLocale == "fr") "Inscrit Guichet" else "Delegation Applied",
                  isDone = myCandidateRecord.status == CandidateStatus.SAT || myCandidateRecord.status == CandidateStatus.PASSED,
                  isActive = myCandidateRecord.status == CandidateStatus.APPLIED
                )
                ExamPipelineStep(
                  title = if (currentLocale == "fr") "Résultat" else "Result",
                  isDone = myCandidateRecord.status == CandidateStatus.PASSED,
                  isActive = myCandidateRecord.status == CandidateStatus.SAT
                )
              }

              if (myExamSession != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color(0xFFF8FAFC),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                      Text(
                        text = myExamSession.sessionName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                      Text(
                        text = "${if (currentLocale == "fr") "Date programmée : " else "Scheduled Date: "}${Localization.formatDate(myExamSession.scheduledDate, currentLocale)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                      )
                    }
                  }
                }
              }

              if (myCandidateRecord.score != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = if (currentLocale == "fr") "Note Officielle de l'Examen :" else "Official Exam Score:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "${myCandidateRecord.score} / 20",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if ((myCandidateRecord.score ?: 0.0) >= 12.0) EmeraldSuccess else MaterialTheme.colorScheme.error
                  )
                }
              }
            } else {
              // Not yet presented for exam
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFFB45309))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = if (currentLocale == "fr") "Examen en Cours de Préparation" else "Exam Qualification in Progress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  Text(
                    text = if (currentLocale == "fr")
                      "Terminez vos leçons théoriques ($completedLessonsCount/$totalLessonsCount complétées) pour être présenté à la session officielle."
                    else
                      "Complete your theory modules ($completedLessonsCount/$totalLessonsCount completed) to be presented for the official theory exam.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // SECTION 3: COURSE PROGRESS CURRICULUM (LMS MODULES & LESSONS)
    if (selectedTabFilter == "ALL" || selectedTabFilter == "LESSONS") {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.MenuBook, contentDescription = null, tint = NavyPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Programme des Cours (LMS)" else "Theory Course Curriculum (LMS)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
          Text(
            text = "$completedLessonsCount / $totalLessonsCount",
            style = MaterialTheme.typography.bodySmall.copy(color = NavyPrimary, fontWeight = FontWeight.Bold)
          )
        }
      }

      items(lessons) { lesson ->
        val isAccessible = !isLockedByPaymentGate || lesson.isOnboarding
        val progress = myEnrollmentProgress.find { it.lessonId == lesson.id }
        val isCompleted = progress?.status == ProgressStatus.DONE

        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isAccessible) MaterialTheme.colorScheme.surface else Color(0xFFF1F5F9).copy(alpha = 0.8f)
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = if (isAccessible) 1.5.dp else 0.dp),
          onClick = { onLessonClick(lesson, isAccessible) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("lesson_card_${lesson.id}")
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(
                    when {
                      !isAccessible -> Color.LightGray
                      isCompleted -> EmeraldSuccess.copy(alpha = 0.15f)
                      lesson.type == LessonType.VIDEO -> Color(0xFFE0E7FF)
                      lesson.type == LessonType.QUIZ -> Color(0xFFFEF3C7)
                      else -> Color(0xFFE2E8F0)
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = when {
                    !isAccessible -> Icons.Default.Lock
                    isCompleted -> Icons.Default.CheckCircle
                    lesson.type == LessonType.VIDEO -> Icons.Default.PlayArrow
                    lesson.type == LessonType.QUIZ -> Icons.Default.Quiz
                    else -> Icons.Default.Description
                  },
                  contentDescription = null,
                  tint = when {
                    !isAccessible -> Color.Gray
                    isCompleted -> EmeraldSuccess
                    else -> NavyPrimary
                  },
                  modifier = Modifier.size(20.dp)
                )
              }

              Spacer(modifier = Modifier.width(12.dp))

              Column {
                Text(
                  text = if (currentLocale == "fr") lesson.titleFr else lesson.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = if (isAccessible) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "${lesson.durationMinutes} min • ${lesson.type.name}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                  )
                  if (lesson.isOnboarding) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = EmeraldSuccess.copy(alpha = 0.15f)
                    ) {
                      Text(
                        text = "FREE INTRO",
                        color = EmeraldSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }
                  if (isCompleted && progress?.score != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "• ${progress.score.toInt()}%",
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp,
                      color = EmeraldSuccess
                    )
                  }
                }
              }
            }

            Icon(
              imageVector = if (isAccessible) Icons.Default.ChevronRight else Icons.Default.Lock,
              contentDescription = null,
              tint = if (isAccessible) AmberAccent else Color.Gray
            )
          }
        }
      }
    }

    // SECTION 4: OFFICIAL DIGITAL CERTIFICATES & QR CODE (FR-23)
    if (selectedTabFilter == "ALL" || selectedTabFilter == "EXAM") {
      val myCerts = certificates.filter { it.studentId == currentUser?.id }
      if (myCerts.isNotEmpty()) {
        item {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AmberAccent)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Certificats Numériques Officiels (FR-23)" else "Official Digital Certificates (FR-23)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
        }

        items(myCerts) { cert ->
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            modifier = Modifier.fillMaxWidth().testTag("cert_card_${cert.id}")
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Verified, contentDescription = null, tint = EmeraldSuccess)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "VERIFIED CERTIFICATE",
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
                Text(
                  text = Localization.formatDate(cert.issuedAt, currentLocale),
                  color = Color.LightGray,
                  fontSize = 11.sp
                )
              }

              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = "${cert.type.name} DRIVING LICENCE CERTIFICATE",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
              Text(
                text = "Category ${cert.category.code} • Holder: ${cert.studentName}",
                color = AmberAccent,
                fontSize = 13.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "UUID: ${cert.verificationUuid}",
                color = Color.Gray,
                fontSize = 11.sp
              )

              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = { onViewCertificate(cert) },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("view_digital_certificate_button")
              ) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = NavyDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (currentLocale == "fr") "Consulter le Certificat & Code QR" else "View Certificate & QR Verification",
                  color = NavyDark,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }

  // DIALOG: Book Practical Driving Lesson (FR-18 & BR-08)
  if (showBookingDialog && studentEnrollment != null) {
    val instructors = allUsers.filter { it.role == UserRole.INSTRUCTOR }
    var selectedInstructorId by remember { mutableStateOf(instructors.firstOrNull()?.id ?: "user-inst-01") }
    val availableVehicles = vehicles.filter {
      it.odometer < it.nextServiceKm && it.status == VehicleStatus.ACTIVE
    }
    var selectedVehicleId by remember { mutableStateOf(availableVehicles.firstOrNull()?.id ?: "veh-01") }

    AlertDialog(
      onDismissRequest = { showBookingDialog = false },
      title = {
        Text(
          text = if (currentLocale == "fr") "Réservation Leçon Pratique (FR-18)" else "Book Practical Driving Lesson (FR-18)",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (currentLocale == "fr")
              "Contrôle de conformité BR-08 : Seuls les moniteurs certifiés et véhicules à jour de révision kilométrique sont réservables."
            else
              "BR-08 Safety Enforcement: Certified instructors and dual-control vehicles with valid maintenance status only.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )

          Text(text = if (currentLocale == "fr") "Moniteur d'auto-école :" else "Instructor:", fontWeight = FontWeight.SemiBold)
          instructors.forEach { inst ->
            FilterChip(
              selected = selectedInstructorId == inst.id,
              onClick = { selectedInstructorId = inst.id },
              label = { Text(inst.name) }
            )
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(text = if (currentLocale == "fr") "Véhicule double-commande homologué :" else "Dual-control vehicle:", fontWeight = FontWeight.SemiBold)
          if (availableVehicles.isEmpty()) {
            Text(
              text = "⚠️ All branch vehicles currently have maintenance overdue!",
              color = MaterialTheme.colorScheme.error,
              fontSize = 12.sp
            )
          } else {
            availableVehicles.forEach { veh ->
              FilterChip(
                selected = selectedVehicleId == veh.id,
                onClick = { selectedVehicleId = veh.id },
                label = { Text("${veh.plateNo} (${veh.makeModel})") }
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (selectedVehicleId.isNotBlank() && selectedInstructorId.isNotBlank()) {
              onBookPracticalSession(
                studentEnrollment.id,
                currentUser?.id ?: "student",
                selectedInstructorId,
                selectedVehicleId,
                System.currentTimeMillis() + 48L * 3600 * 1000
              )
              showBookingDialog = false
            }
          },
          enabled = availableVehicles.isNotEmpty(),
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
          modifier = Modifier.testTag("confirm_booking_button")
        ) {
          Text(if (currentLocale == "fr") "Confirmer Réservation" else "Confirm Booking")
        }
      },
      dismissButton = {
        TextButton(onClick = { showBookingDialog = false }) {
          Text(if (currentLocale == "fr") "Fermer" else "Close")
        }
      }
    )
  }
}

@Composable
fun ExamPipelineStep(
  title: String,
  isDone: Boolean,
  isActive: Boolean
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Box(
      modifier = Modifier
        .size(24.dp)
        .clip(CircleShape)
        .background(
          when {
            isDone -> EmeraldSuccess
            isActive -> AmberAccent
            else -> Color(0xFFE2E8F0)
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      if (isDone) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
      } else {
        Box(
          modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isActive) NavyDark else Color.Gray)
        )
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = title,
      fontSize = 9.sp,
      fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
      color = if (isActive) AmberAccent else if (isDone) EmeraldSuccess else Color.Gray
    )
  }
}
