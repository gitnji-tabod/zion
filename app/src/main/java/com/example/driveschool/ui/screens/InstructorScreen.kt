package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun InstructorScreen(
  currentLocale: String,
  currentUser: UserEntity?,
  practicalSessions: List<PracticalSessionEntity>,
  users: List<UserEntity>,
  vehicles: List<VehicleEntity>,
  examSessions: List<ExamSessionEntity>,
  enrollments: List<EnrollmentEntity>,
  onSignOffSession: (String, AttendanceStatus, Int, String?) -> Unit,
  onRecommendCandidate: (String, String, String) -> Unit
) {
  var selectedSessionForSignoff by remember { mutableStateOf<PracticalSessionEntity?>(null) }
  var showRecommendDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("instructor_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header
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
            Column {
              Text(
                text = if (currentLocale == "fr") "Espace Moniteur d'Auto-école" else "Instructor Operations & Practical Log",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = "${currentUser?.name ?: "Peter Ngu"} • Bamenda Branch",
                style = MaterialTheme.typography.bodySmall.copy(color = AmberAccent)
              )
            }
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = { showRecommendDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_recommend_candidate_button")
          ) {
            Icon(Icons.Default.Recommend, contentDescription = null, tint = NavyDark, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Recommander un Élève à l'Examen National (FR-20)" else "Recommend Student for National Exam (FR-20)",
              color = NavyDark,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // SECTION 1: Practical Driving Sessions
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.TimeToLeave, contentDescription = null, tint = Color(0xFF0284C7))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLocale == "fr") "Séances Pratiques & Pointage Kilométrique" else "Practical Driving Sessions & Odometer Registry",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }

    items(practicalSessions) { session ->
      val student = users.find { it.id == session.studentId }
      val vehicle = vehicles.find { it.id == session.vehicleId }

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = student?.name ?: "Student Driver",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "${vehicle?.makeModel ?: "Dual Control Car"} • ${vehicle?.plateNo ?: "NW-241-AA"}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
              )
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = when (session.attendance) {
                AttendanceStatus.PRESENT -> EmeraldSuccess
                AttendanceStatus.SCHEDULED -> AmberAccent
                AttendanceStatus.LATE -> Color(0xFFF97316)
                AttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
              }
            ) {
              Text(
                text = session.attendance.name,
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (currentLocale == "fr") "Date & Heure :" else "Scheduled Time:",
              style = MaterialTheme.typography.bodySmall
            )
            Text(
              text = Localization.formatDateTime(session.scheduledAt, currentLocale),
              fontWeight = FontWeight.SemiBold,
              style = MaterialTheme.typography.bodySmall
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = if (currentLocale == "fr") "Kilométrage Début/Fin :" else "Odometer Readings:",
              style = MaterialTheme.typography.bodySmall
            )
            Text(
              text = "${session.odometerStart} km → ${session.odometerEnd?.let { "$it km" } ?: "-- km"}",
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0284C7),
              style = MaterialTheme.typography.bodySmall
            )
          }

          if (session.instructorFeedback != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFF1F5F9),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "💬 ${session.instructorFeedback}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF334155)),
                modifier = Modifier.padding(8.dp)
              )
            }
          }

          if (session.odometerEnd == null || session.attendance == AttendanceStatus.SCHEDULED) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { selectedSessionForSignoff = session },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("signoff_session_button_${session.id}")
            ) {
              Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (currentLocale == "fr") "Valider Présence & Relever Compteur (FR-19)" else "Sign-off Session & Capture Odometer (FR-19)",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }
  }

  // DIALOG 1: Sign-off Practical Session & Capture Odometer (FR-19)
  if (selectedSessionForSignoff != null) {
    val session = selectedSessionForSignoff!!
    val currentVeh = vehicles.find { it.id == session.vehicleId }
    var selectedAttendance by remember { mutableStateOf(AttendanceStatus.PRESENT) }
    var endOdometerInput by remember { mutableStateOf("${(currentVeh?.odometer ?: 45200) + 18}") }
    var notes by remember { mutableStateOf("Good lane discipline, proper observation during 3-point turn.") }

    AlertDialog(
      onDismissRequest = { selectedSessionForSignoff = null },
      title = {
        Text(
          text = if (currentLocale == "fr") "Validation Séance & Compteur (FR-19)" else "Sign-off Practical Driving Session (FR-19)",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (currentLocale == "fr")
              "Compteur début : ${session.odometerStart} km. La saisie du compteur final actualise le registre du véhicule et déclenche l'alerte entretien si nécessaire (BR-08)."
            else
              "Start Odometer: ${session.odometerStart} km. End reading updates vehicle fleet registry and blocks vehicle if maintenance becomes overdue (BR-08).",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )

          // Attendance Selector
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            AttendanceStatus.values().forEach { att ->
              FilterChip(
                selected = selectedAttendance == att,
                onClick = { selectedAttendance = att },
                label = { Text(att.name, fontSize = 11.sp) },
                modifier = Modifier.weight(1f)
              )
            }
          }

          OutlinedTextField(
            value = endOdometerInput,
            onValueChange = { endOdometerInput = it },
            label = { Text(if (currentLocale == "fr") "Compteur Arrivée (km)" else "End Odometer (km)") },
            modifier = Modifier.fillMaxWidth().testTag("end_odometer_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(if (currentLocale == "fr") "Appréciation Moniteur" else "Instructor Assessment") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val endKm = endOdometerInput.toIntOrNull() ?: (session.odometerStart + 15)
            onSignOffSession(session.id, selectedAttendance, endKm, notes)
            selectedSessionForSignoff = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
          modifier = Modifier.testTag("confirm_signoff_button")
        ) {
          Text(if (currentLocale == "fr") "Enregistrer Sign-off" else "Record Sign-off")
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedSessionForSignoff = null }) {
          Text(if (currentLocale == "fr") "Annuler" else "Cancel")
        }
      }
    )
  }

  // DIALOG 2: Recommend Student for National Exam (FR-20 & BR-06)
  if (showRecommendDialog) {
    var selectedStudentId by remember { mutableStateOf(users.firstOrNull { it.role == UserRole.STUDENT }?.id ?: "user-stud-onsite-01") }
    val examSession = examSessions.firstOrNull()

    AlertDialog(
      onDismissRequest = { showRecommendDialog = false },
      title = {
        Text(
          text = if (currentLocale == "fr") "Recommandation d'Examen (FR-20)" else "Recommend Candidate for Exam (BR-06)",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (currentLocale == "fr")
              "Session : ${examSession?.sessionName ?: "Session Nationale d'Octobre 2026"}. Selon BR-06, la recommandation du moniteur est le prérequis légal avant approbation du Chef d'Agence."
            else
              "Session: ${examSession?.sessionName ?: "National Driving Exam Session A"}. Under BR-06, instructor recommendation is mandatory before manager approval.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )

          Text(
            text = if (currentLocale == "fr") "Sélectionner l'élève évalué :" else "Select assessed student:",
            fontWeight = FontWeight.SemiBold
          )

          users.filter { it.role == UserRole.STUDENT }.forEach { student ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedStudentId == student.id) AmberAccent.copy(alpha = 0.2f) else Color.Transparent,
              onClick = { selectedStudentId = student.id },
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = selectedStudentId == student.id,
                  onClick = { selectedStudentId = student.id }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(text = student.name, fontWeight = FontWeight.Bold)
                  Text(text = "${student.phone} • Country: ${student.countryCode}", fontSize = 11.sp, color = Color.Gray)
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val enr = enrollments.find { it.studentId == selectedStudentId }
            val enrId = enr?.id ?: "enr-brenda-01"
            val sessionId = examSession?.id ?: "exam-session-oct2026"
            onRecommendCandidate(sessionId, enrId, selectedStudentId)
            showRecommendDialog = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
          modifier = Modifier.testTag("confirm_recommend_candidate_button")
        ) {
          Text(
            text = if (currentLocale == "fr") "Transmettre Recommandation" else "Submit Recommendation",
            color = NavyDark,
            fontWeight = FontWeight.Bold
          )
        }
      },
      dismissButton = {
        TextButton(onClick = { showRecommendDialog = false }) {
          Text(if (currentLocale == "fr") "Fermer" else "Close")
        }
      }
    )
  }
}
