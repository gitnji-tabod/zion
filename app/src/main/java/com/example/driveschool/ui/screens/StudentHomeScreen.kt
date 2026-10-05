package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
  vehicles: List<VehicleEntity>,
  practicalSessions: List<PracticalSessionEntity>,
  certificates: List<CertificateEntity>,
  candidates: List<ExamCandidateEntity>,
  onSelectStudentUser: (UserEntity) -> Unit,
  onLessonClick: (LessonEntity, Boolean) -> Unit,
  onOpenPaymentDialog: (EnrollmentEntity) -> Unit,
  onBookPracticalSession: (String, String, String, String, Long) -> Unit,
  onViewCertificate: (CertificateEntity) -> Unit
) {
  // Find current student's enrollment
  val studentEnrollment = enrollments.find { it.studentId == currentUser?.id } ?: enrollments.firstOrNull()
  val activeCourse = courses.find { it.id == studentEnrollment?.courseId } ?: courses.firstOrNull()
  val isLockedByPaymentGate = studentEnrollment?.status == EnrollmentStatus.ONBOARDING || studentEnrollment?.status == EnrollmentStatus.AWAITING_PAYMENT

  var showBookingDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("student_home_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Switch between Onsite (Brenda - Cameroon) and Online (Thomas - France) student
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (currentLocale == "fr") "Sélectionner Profil Élève :" else "Active Student Persona:",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val onsiteUser = allUsers.find { it.id == "user-stud-onsite-01" }
            val onlineUser = allUsers.find { it.id == "user-stud-online-02" }

            if (onsiteUser != null) {
              FilterChip(
                selected = currentUser?.id == onsiteUser.id,
                onClick = { onSelectStudentUser(onsiteUser) },
                label = { Text("🇨🇲 Brenda (Onsite)", fontSize = 11.sp) },
                modifier = Modifier.testTag("persona_brenda_onsite")
              )
            }
            if (onlineUser != null) {
              FilterChip(
                selected = currentUser?.id == onlineUser.id,
                onClick = { onSelectStudentUser(onlineUser) },
                label = { Text("🌍 Thomas (Online)", fontSize = 11.sp) },
                modifier = Modifier.testTag("persona_thomas_online")
              )
            }
          }
        }
      }
    }

    // Hero Course Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = AmberAccent
            ) {
              Text(
                text = "${activeCourse?.licenseCategory?.code ?: "B"} • ${studentEnrollment?.mode ?: "ONSITE"}",
                color = NavyDark,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isLockedByPaymentGate) AmberAccent.copy(alpha = 0.2f) else EmeraldSuccess.copy(alpha = 0.2f)
            ) {
              Text(
                text = Localization.enrollmentStatusName(studentEnrollment?.status?.name ?: "ACTIVE", currentLocale),
                color = if (isLockedByPaymentGate) AmberAccent else EmeraldSuccess,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = if (currentLocale == "fr") activeCourse?.titleFr ?: "Catégorie B" else activeCourse?.title ?: "Category B - Light Motor Vehicle",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 17.sp
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (currentLocale == "fr") activeCourse?.descriptionFr ?: "" else activeCourse?.description ?: "",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Practical Booking Button (For Onsite Students)
          if (currentUser?.isOnsiteCapable == true && studentEnrollment?.status == EnrollmentStatus.ACTIVE) {
            Button(
              onClick = { showBookingDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("book_practical_session_button")
            ) {
              Icon(Icons.Default.Event, contentDescription = null, tint = NavyDark, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (currentLocale == "fr") "Réserver une Séancé Pratique de Conduite (FR-18)" else "Book Practical Driving Session (FR-18)",
                color = NavyDark,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // PAYMENT GATE BANNER (BR-02 Enforced)
    if (isLockedByPaymentGate && studentEnrollment != null) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                fontSize = 15.sp
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (currentLocale == "fr")
                "Les leçons d'initiation sont gratuites et débloquées ci-dessous. Pour accéder aux modules avancés, évaluations interactives et séances pratiques, confirmez le paiement du cours (MTN MoMo, Orange Money, Carte ou Espèces)."
              else
                "Onboarding lessons are unlocked free of charge below. Pay the course platform fee (MTN Mobile Money, Orange Money, Card, or Cash) to unlock full LMS modules, official assessments, and in-car sessions.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF78350F))
            )

            Spacer(modifier = Modifier.height(14.dp))
            Button(
              onClick = { onOpenPaymentDialog(studentEnrollment) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("pay_course_fee_button")
            ) {
              Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLocale == "fr") "Payer les Frais de Formation (Débloquer Tout)" else "Pay Course Fee (Unlock Remaining Modules)",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // SECTION: LMS Modules & Lessons List
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (currentLocale == "fr") "Programme Pédagogique (LMS)" else "Theory Course Curriculum (LMS)",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "${lessons.size} ${if (currentLocale == "fr") "leçons" else "lessons"}",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
      }
    }

    items(lessons) { lesson ->
      val isAccessible = !isLockedByPaymentGate || lesson.isOnboarding

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isAccessible) MaterialTheme.colorScheme.surface else Color(0xFFF1F5F9).copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isAccessible) 2.dp else 0.dp),
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
                  lesson.type == LessonType.VIDEO -> Icons.Default.PlayArrow
                  lesson.type == LessonType.QUIZ -> Icons.Default.Quiz
                  else -> Icons.Default.Description
                },
                contentDescription = null,
                tint = if (!isAccessible) Color.Gray else NavyPrimary,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = if (currentLocale == "fr") lesson.titleFr else lesson.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = if (isAccessible) MaterialTheme.colorScheme.onSurface else Color.Gray
                )
              }
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

    // SECTION: Verifiable Certificates & Official Exam Status (FR-22 & FR-23)
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = AmberAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Mes Certificats Numériques Officiels (FR-23)" else "Official Digital Certificates (FR-23)",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    val myCerts = certificates.filter { it.studentId == currentUser?.id }
    if (myCerts.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (currentLocale == "fr")
                "Aucun certificat émis pour le moment. Votre certificat infalsifiable avec QR code sera généré dès validation de l'examen officiel."
              else
                "No certificate issued yet. Complete your course & pass the exam to receive your tamper-proof digital certificate with QR code.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
          }
        }
      }
    } else {
      items(myCerts) { cert ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
          modifier = Modifier.fillMaxWidth()
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
              modifier = Modifier
                .fillMaxWidth()
                .testTag("view_digital_certificate_button")
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

  // DIALOG: Practical Driving Session Booking (FR-18 & BR-08)
  if (showBookingDialog && studentEnrollment != null) {
    val instructors = allUsers.filter { it.role == UserRole.INSTRUCTOR }
    var selectedInstructorId by remember { mutableStateOf(instructors.firstOrNull()?.id ?: "user-inst-01") }
    // BR-08: Filter out overdue vehicles!
    val availableVehicles = vehicles.filter {
      it.odometer < it.nextServiceKm && it.status == VehicleStatus.ACTIVE
    }
    var selectedVehicleId by remember { mutableStateOf(availableVehicles.firstOrNull()?.id ?: "veh-01") }

    AlertDialog(
      onDismissRequest = { showBookingDialog = false },
      title = {
        Text(
          text = if (currentLocale == "fr") "Réservation Séance Pratique (FR-18)" else "Book Practical Session (FR-18)",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (currentLocale == "fr")
              "Contrôle de conformité BR-08 : Seuls les véhicules à jour de révision kilométrique et temporelle sont réservables."
            else
              "BR-08 Safety Enforcement: Vehicles with overdue maintenance are automatically blocked from practical booking.",
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

          Spacer(modifier = Modifier.height(6.dp))
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
                label = { Text("${veh.plateNo} (${veh.makeModel} - ${veh.odometer} km)") }
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
