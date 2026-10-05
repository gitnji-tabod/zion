package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
fun BranchManagerScreen(
  currentLocale: String,
  currentUser: UserEntity?,
  pendingDiscountApprovals: List<EnrollmentEntity>,
  candidatesPendingApproval: List<ExamCandidateEntity>,
  users: List<UserEntity>,
  courses: List<CourseEntity>,
  vehicles: List<VehicleEntity>,
  branches: List<BranchEntity> = emptyList(),
  onApproveDiscount: (String) -> Unit,
  onApproveCandidate: (String) -> Unit
) {
  val myBranch = branches.find { it.id == currentUser?.branchId }
  val branchTitle = myBranch?.name ?: (if (currentLocale == "fr") "Chef d'Agence" else "Branch Manager")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("branch_manager_screen"),
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
                text = "$branchTitle • ${if (currentLocale == "fr") "Chef d'Agence" else "Manager"}",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = "${currentUser?.name ?: "Beatrice Fongang"} | ${currentUser?.email ?: ""} | ${myBranch?.city ?: ""}",
                style = MaterialTheme.typography.bodySmall.copy(color = AmberAccent)
              )
            }
            Icon(
              imageVector = Icons.Default.SupervisorAccount,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = if (currentLocale == "fr")
              "Rôle réglementaire : Approbation des remises tarifaires (> 15%) et validation des candidatures à l'examen officiel."
            else
              "Regulatory governance: Approval of negotiated student discounts (> 15%) and sign-off on instructor exam candidate recommendations.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
          )
        }
      }
    }

    // SECTION 1: Pending Negotiated Fee Approvals (BR-04)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.PriceCheck, contentDescription = null, tint = AmberAccent)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLocale == "fr") "Validations de Remises Tarifaires (BR-04)" else "Pending Fee Approvals (BR-04 Guardrail)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
        Badge(containerColor = if (pendingDiscountApprovals.isNotEmpty()) MaterialTheme.colorScheme.error else EmeraldSuccess) {
          Text("${pendingDiscountApprovals.size}")
        }
      }
    }

    if (pendingDiscountApprovals.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = if (currentLocale == "fr") "Aucune demande de remise en attente d'approbation." else "No pending fee discount requests awaiting approval.",
              style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
          }
        }
      }
    } else {
      items(pendingDiscountApprovals) { enrollment ->
        val student = users.find { it.id == enrollment.studentId }
        val course = courses.find { it.id == enrollment.courseId }

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = student?.name ?: "Student Candidate",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
              ) {
                Text(
                  text = "-${String.format("%.1f", enrollment.discountPercent)}% DISCOUNT",
                  color = MaterialTheme.colorScheme.error,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "${course?.title ?: "Category B"} • Mode: ${enrollment.mode}",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = if (currentLocale == "fr") "Montant Négocié :" else "Negotiated Amount:",
                style = MaterialTheme.typography.bodySmall
              )
              Text(
                text = Localization.formatCurrency(enrollment.negotiatedAmount ?: 0.0, currentLocale),
                fontWeight = FontWeight.Bold,
                color = EmeraldSuccess
              )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = { onApproveDiscount(enrollment.id) },
              colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("approve_discount_button_${enrollment.id}")
            ) {
              Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (currentLocale == "fr") "Valider la Remise (Activer Inscription)" else "Approve Discount & Activate Enrollment",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // SECTION 2: Pending Exam Candidate Approvals (BR-06)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.FactCheck, contentDescription = null, tint = EmeraldSuccess)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLocale == "fr") "Approbations Candidats à l'Examen (BR-06)" else "Exam Candidate Approvals (BR-06 Workflow)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
        Badge(containerColor = if (candidatesPendingApproval.isNotEmpty()) AmberAccent else EmeraldSuccess) {
          Text("${candidatesPendingApproval.size}")
        }
      }
    }

    if (candidatesPendingApproval.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = if (currentLocale == "fr") "Aucun candidat en attente d'approbation d'examen." else "All instructor recommendations have been processed.",
              style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
          }
        }
      }
    } else {
      items(candidatesPendingApproval) { candidate ->
        val student = users.find { it.id == candidate.studentId }
        val instructor = users.find { it.id == candidate.recommendedBy }

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
              Text(
                text = student?.name ?: "Exam Candidate",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = AmberAccent.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "RECOMMENDED",
                  color = Color(0xFFB45309),
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = if (currentLocale == "fr")
                "Recommandé par le moniteur : ${instructor?.name ?: "Moniteur Principal"}"
              else
                "Recommended by instructor: ${instructor?.name ?: "Peter Ngu"}",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { onApproveCandidate(candidate.id) },
              colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("approve_candidate_button_${candidate.id}")
            ) {
              Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (currentLocale == "fr") "Approuver pour Inscription Ministérielle" else "Approve Candidate for Ministerial Exam",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // SECTION 3: Vehicle Fleet Status
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color(0xFF0284C7))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Parc Automobile de l'Agence" else "Branch Vehicle Fleet Status",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    items(vehicles) { veh ->
      val isOverdue = veh.odometer >= veh.nextServiceKm || veh.status == VehicleStatus.SERVICE_DUE
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isOverdue) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant
        ),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = veh.plateNo,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isOverdue) MaterialTheme.colorScheme.error else EmeraldSuccess
              ) {
                Text(
                  text = if (isOverdue) "OVERDUE (BLOCKED)" else "ACTIVE",
                  color = Color.White,
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
            Text(
              text = veh.makeModel,
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "${veh.odometer} km",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
            Text(
              text = "Next: ${veh.nextServiceKm} km",
              style = MaterialTheme.typography.bodySmall.copy(
                color = if (isOverdue) MaterialTheme.colorScheme.error else Color.Gray,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }
  }
}
