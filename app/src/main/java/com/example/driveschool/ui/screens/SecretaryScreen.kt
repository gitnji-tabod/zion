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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecretaryScreen(
  currentLocale: String,
  currentUser: UserEntity?,
  branches: List<BranchEntity>,
  courses: List<CourseEntity>,
  enrollments: List<EnrollmentEntity>,
  users: List<UserEntity>,
  candidatesReadyForApplication: List<ExamCandidateEntity>,
  onRegisterStudent: (String, String, String, String, String?, String, EnrollmentMode, Double?) -> Unit,
  onRecordCashPayment: (String, String, Double, String) -> Unit,
  onApplyCandidate: (String) -> Unit,
  onIssueInsurance: (String, String, String, String, String, Double, Int) -> Unit
) {
  var showRegisterDialog by remember { mutableStateOf(false) }
  var showCashPaymentDialog by remember { mutableStateOf(false) }
  var showInsuranceDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("secretary_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top Card
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
              val myBranch = branches.find { it.id == currentUser?.branchId }
              val branchLabel = myBranch?.name ?: if (currentLocale == "fr") "Guichet Central" else "Main Front-Desk"
              Text(
                text = "${if (currentLocale == "fr") "Guichet Secrétariat" else "Front-Desk"} • $branchLabel",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = "${currentUser?.name ?: "Che Roland"} (Collected By: ${currentUser?.id ?: "sec-01"})",
                style = MaterialTheme.typography.bodySmall.copy(color = AmberAccent)
              )
            }
            Icon(
              imageVector = Icons.Default.PointOfSale,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = { showRegisterDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("open_register_student_button")
            ) {
              Icon(Icons.Default.PersonAdd, contentDescription = null, tint = NavyDark, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (currentLocale == "fr") "Inscrire Élève" else "Register Walk-in",
                color = NavyDark,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }

            Button(
              onClick = { showCashPaymentDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("open_cash_payment_button")
            ) {
              Icon(Icons.Default.Payments, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (currentLocale == "fr") "Encaisser Cash" else "Collect Cash",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }

            Button(
              onClick = { showInsuranceDialog = true },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .weight(1f)
                .testTag("open_issue_insurance_button")
            ) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (currentLocale == "fr") "Assurance" else "Insurance",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
              )
            }
          }
        }
      }
    }

    // SECTION 1: Approved Candidates Ready for Application (BR-06)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = EmeraldSuccess)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLocale == "fr") "Dépôt des Dossiers d'Examen (BR-06)" else "Exam Candidate Applications (Step 3)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
        Badge(containerColor = if (candidatesReadyForApplication.isNotEmpty()) AmberAccent else EmeraldSuccess) {
          Text("${candidatesReadyForApplication.size}")
        }
      }
    }

    if (candidatesReadyForApplication.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DoneAll, contentDescription = null, tint = EmeraldSuccess)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = if (currentLocale == "fr") "Aucun candidat en attente de dépôt de dossier." else "No approved candidates pending official examination application.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
          }
        }
      }
    } else {
      items(candidatesReadyForApplication) { candidate ->
        val student = users.find { it.id == candidate.studentId }
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = student?.name ?: "Candidate",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = if (currentLocale == "fr") "Statut : Validé par le Chef d'Agence" else "Status: Approved by Branch Manager",
                style = MaterialTheme.typography.bodySmall.copy(color = EmeraldSuccess)
              )
            }
            Button(
              onClick = { onApplyCandidate(candidate.id) },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("apply_candidate_button_${candidate.id}")
            ) {
              Text(
                text = if (currentLocale == "fr") "Déposer Dossier" else "Submit Dossier",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // SECTION 2: Recent Students & Enrollment Ledger
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (currentLocale == "fr") "Registre des Inscriptions" else "Recent Enrollments Register",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "${enrollments.size} ${if (currentLocale == "fr") "inscrits" else "enrolled"}",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
      }
    }

    items(enrollments) { enrollment ->
      val student = users.find { it.id == enrollment.studentId }
      val course = courses.find { it.id == enrollment.courseId }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = student?.name ?: "Student",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "${course?.title ?: "Category B"} • Mode: ${enrollment.mode}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
              )
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = when (enrollment.status) {
                EnrollmentStatus.ACTIVE -> EmeraldSuccess
                EnrollmentStatus.ONBOARDING -> AmberAccent
                EnrollmentStatus.AWAITING_PAYMENT -> Color(0xFFEF4444)
                else -> Color.Gray
              }
            ) {
              Text(
                text = Localization.enrollmentStatusName(enrollment.status.name, currentLocale),
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          if (enrollment.negotiatedAmount != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Sell, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (currentLocale == "fr")
                  "Tarif négocié : ${Localization.formatCurrency(enrollment.negotiatedAmount, currentLocale)} (-${enrollment.discountPercent.toInt()}%)"
                else
                  "Negotiated Fee: ${Localization.formatCurrency(enrollment.negotiatedAmount, currentLocale)} (-${enrollment.discountPercent.toInt()}%)",
                style = MaterialTheme.typography.bodySmall.copy(
                  color = if (enrollment.isDiscountApproved) EmeraldSuccess else MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.Medium
                )
              )
            }
          }
        }
      }
    }
  }

  // DIALOG 1: Walk-In Student Registration (FR-01, FR-02, FR-07)
  if (showRegisterDialog) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+237 ") }
    var countryCode by remember { mutableStateOf("CM") }
    var selectedCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: "course-cat-b") }
    var selectedBranchId by remember { mutableStateOf(branches.firstOrNull { !it.isVirtual }?.id ?: "branch-bamenda-01") }
    var mode by remember { mutableStateOf(EnrollmentMode.ONSITE) }
    var negotiatedFeeInput by remember { mutableStateOf("") }

    val standardFee = 125000.0 // Standard for B
    val negotiatedAmount = negotiatedFeeInput.toDoubleOrNull()
    val discountPercent = if (negotiatedAmount != null && negotiatedAmount < standardFee) {
      ((standardFee - negotiatedAmount) / standardFee) * 100
    } else 0.0
    val exceedsThreshold = discountPercent > 15.0

    AlertDialog(
      onDismissRequest = { showRegisterDialog = false },
      title = {
        Text(
          text = if (currentLocale == "fr") "Inscription Élève Présentiel (FR-02)" else "Walk-in Student Registration (FR-02)",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (currentLocale == "fr") "Nom Complet" else "Full Name") },
            modifier = Modifier.fillMaxWidth().testTag("reg_student_name_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(if (currentLocale == "fr") "Téléphone (pour SMS identifiants)" else "Phone (for SMS credentials)") },
            modifier = Modifier.fillMaxWidth().testTag("reg_student_phone_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )

          // Country Selector (Routing mechanism - BR-01)
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = countryCode == "CM",
              onClick = {
                countryCode = "CM"
                mode = EnrollmentMode.ONSITE
              },
              label = { Text("🇨🇲 Cameroon (Onsite)") },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = countryCode != "CM",
              onClick = {
                countryCode = "FR"
                mode = EnrollmentMode.ONLINE
              },
              label = { Text("🌍 Other (Online-only)") },
              modifier = Modifier.weight(1f)
            )
          }

          // Negotiated Fee & Guardrail
          if (mode == EnrollmentMode.ONSITE) {
            OutlinedTextField(
              value = negotiatedFeeInput,
              onValueChange = { negotiatedFeeInput = it },
              label = { Text(if (currentLocale == "fr") "Frais Négociés (Standard: 125 000 XAF)" else "Negotiated Fee (Standard: 125,000 XAF)") },
              modifier = Modifier.fillMaxWidth().testTag("reg_negotiated_fee_input"),
              singleLine = true
            )

            if (exceedsThreshold) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
              ) {
                Text(
                  text = if (currentLocale == "fr")
                    "⚠️ Remise de ${discountPercent.toInt()}% (> 15%) : Nécessite l'approbation du Chef d'Agence avant activation (BR-04)."
                  else
                    "⚠️ Discount of ${discountPercent.toInt()}% exceeds 15% threshold: Will require Branch Manager approval before activation (BR-04).",
                  color = MaterialTheme.colorScheme.error,
                  style = MaterialTheme.typography.bodySmall,
                  modifier = Modifier.padding(8.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              onRegisterStudent(
                name,
                email.ifBlank { "${name.lowercase().replace(" ", "")}@driveschool.cm" },
                phone,
                countryCode,
                selectedBranchId,
                selectedCourseId,
                mode,
                negotiatedAmount
              )
              showRegisterDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
          modifier = Modifier.testTag("submit_registration_button")
        ) {
          Text(if (currentLocale == "fr") "Enregistrer & Envoyer SMS" else "Register & Dispatch SMS")
        }
      },
      dismissButton = {
        TextButton(onClick = { showRegisterDialog = false }) {
          Text(if (currentLocale == "fr") "Annuler" else "Cancel")
        }
      }
    )
  }

  // DIALOG 2: Cash Payment Collection (BR-05)
  if (showCashPaymentDialog) {
    var selectedEnrollmentId by remember { mutableStateOf(enrollments.firstOrNull()?.id ?: "") }
    var cashAmountInput by remember { mutableStateOf("50000") }
    var cashNotes by remember { mutableStateOf("Cash installment at front-desk") }

    AlertDialog(
      onDismissRequest = { showCashPaymentDialog = false },
      title = {
        Text(
          text = if (currentLocale == "fr") "Encaissement Espèces au Guichet (BR-05)" else "Record Cash Payment (BR-05)",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = if (currentLocale == "fr")
              "Secrétaire collecteur : ${currentUser?.name ?: "Che Roland"}. Un reçu numérique numéroté sera généré et consigné dans l'audit financier immuable."
            else
              "Collecting Secretary: ${currentUser?.name ?: "Che Roland"}. A numbered digital receipt will be generated and logged to the immutable financial audit trail.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )

          OutlinedTextField(
            value = cashAmountInput,
            onValueChange = { cashAmountInput = it },
            label = { Text(if (currentLocale == "fr") "Montant Espèces (XAF)" else "Cash Amount (XAF)") },
            modifier = Modifier.fillMaxWidth().testTag("cash_amount_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = cashNotes,
            onValueChange = { cashNotes = it },
            label = { Text(if (currentLocale == "fr") "Référence / Motif" else "Notes / Reference") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = cashAmountInput.toDoubleOrNull() ?: 0.0
            if (selectedEnrollmentId.isNotBlank() && amount > 0) {
              val enr = enrollments.find { it.id == selectedEnrollmentId }
              onRecordCashPayment(selectedEnrollmentId, enr?.studentId ?: "student", amount, cashNotes)
              showCashPaymentDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
          modifier = Modifier.testTag("confirm_cash_payment_button")
        ) {
          Text(if (currentLocale == "fr") "Confirmer Reçu & Débloquer" else "Confirm Receipt & Unlock")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCashPaymentDialog = false }) {
          Text(if (currentLocale == "fr") "Fermer" else "Close")
        }
      }
    )
  }

  // DIALOG 3: Issue Insurance Policy
  if (showInsuranceDialog) {
    var holderName by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("NW-") }
    var premiumInput by remember { mutableStateOf("45000") }

    AlertDialog(
      onDismissRequest = { showInsuranceDialog = false },
      title = {
        Text(
          text = if (currentLocale == "fr") "Émission Police d'Assurance (FR-24)" else "Issue Insurance Policy (FR-24)",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = holderName,
            onValueChange = { holderName = it },
            label = { Text(if (currentLocale == "fr") "Nom de l'Assuré" else "Policy Holder Name") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = plate,
            onValueChange = { plate = it },
            label = { Text(if (currentLocale == "fr") "Immatriculation Véhicule" else "Vehicle Plate Number") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = premiumInput,
            onValueChange = { premiumInput = it },
            label = { Text(if (currentLocale == "fr") "Prime Annuelle (XAF)" else "Annual Premium (XAF)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (holderName.isNotBlank() && plate.isNotBlank()) {
              onIssueInsurance(
                "user-${holderName.take(3)}",
                holderName,
                "Third-Party Liability + Passenger Cover",
                "Responsabilité Civile + Protection Passagers",
                plate,
                premiumInput.toDoubleOrNull() ?: 45000.0,
                12
              )
              showInsuranceDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
        ) {
          Text(if (currentLocale == "fr") "Émettre Police" else "Issue Policy")
        }
      },
      dismissButton = {
        TextButton(onClick = { showInsuranceDialog = false }) {
          Text(if (currentLocale == "fr") "Annuler" else "Cancel")
        }
      }
    )
  }
}
