package com.example.driveschool.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBranchDialog(
  currentLocale: String,
  onDismiss: () -> Unit,
  onCreateBranch: (
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
    initialStudentName: String?,
    initialStudentEmail: String?,
    initialStudentPhone: String?,
    initialCourseId: String?
  ) -> Unit
) {
  var branchName by remember { mutableStateOf("") }
  var city by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var branchPhone by remember { mutableStateOf("+237 ") }

  var managerName by remember { mutableStateOf("") }
  var managerEmail by remember { mutableStateOf("") }
  var managerPhone by remember { mutableStateOf("+237 ") }

  var secretaryName by remember { mutableStateOf("") }
  var secretaryEmail by remember { mutableStateOf("") }
  var secretaryPhone by remember { mutableStateOf("+237 ") }

  var studentName by remember { mutableStateOf("") }
  var studentEmail by remember { mutableStateOf("") }
  var studentPhone by remember { mutableStateOf("+237 ") }
  var selectedCourseId by remember { mutableStateOf("course-cat-b") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.AddBusiness, contentDescription = null, tint = AmberAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Créer une Nouvelle Agence & Assigner Staff" else "Create Branch (Manager, Secretary & Student)",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = if (currentLocale == "fr")
            "Chaque agence physique dispose de son propre Chef d'Agence (approbations), Secrétaire (guichet & encaissement) et ses propres élèves rattachés."
          else
            "Each branch operates with its own Branch Manager (approvals), Secretary (front-desk & collections), and attached students.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        // Branch Details Section
        Text(
          text = if (currentLocale == "fr") "1. Coordonnées de l'Agence :" else "1. Branch Coordinates:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        OutlinedTextField(
          value = branchName,
          onValueChange = { branchName = it },
          label = { Text(if (currentLocale == "fr") "Nom de l'Agence (ex: Agence de Kumba)" else "Branch Name (e.g. Kumba Commercial)") },
          modifier = Modifier.fillMaxWidth().testTag("new_branch_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = city,
          onValueChange = { city = it },
          label = { Text(if (currentLocale == "fr") "Ville / Région (ex: Kumba, Sud-Ouest)" else "City / Region (e.g. Kumba, South West)") },
          modifier = Modifier.fillMaxWidth().testTag("new_branch_city_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text(if (currentLocale == "fr") "Adresse Physique" else "Physical Address") },
          modifier = Modifier.fillMaxWidth().testTag("new_branch_address_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = branchPhone,
          onValueChange = { branchPhone = it },
          label = { Text(if (currentLocale == "fr") "Téléphone de l'Agence" else "Branch Phone (+237)") },
          modifier = Modifier.fillMaxWidth().testTag("new_branch_phone_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Manager Section
        Text(
          text = if (currentLocale == "fr") "2. Chef d'Agence (Manager) :" else "2. Dedicated Branch Manager:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        OutlinedTextField(
          value = managerName,
          onValueChange = { managerName = it },
          label = { Text(if (currentLocale == "fr") "Nom du Chef d'Agence" else "Manager Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("new_manager_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = managerEmail,
          onValueChange = { managerEmail = it },
          label = { Text("Manager Email") },
          modifier = Modifier.fillMaxWidth().testTag("new_manager_email_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Secretary Section
        Text(
          text = if (currentLocale == "fr") "3. Secrétaire d'Agence :" else "3. Dedicated Branch Secretary:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        OutlinedTextField(
          value = secretaryName,
          onValueChange = { secretaryName = it },
          label = { Text(if (currentLocale == "fr") "Nom du Secrétaire" else "Secretary Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("new_secretary_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = secretaryEmail,
          onValueChange = { secretaryEmail = it },
          label = { Text("Secretary Email") },
          modifier = Modifier.fillMaxWidth().testTag("new_secretary_email_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Student Section
        Text(
          text = if (currentLocale == "fr") "4. Premier Élève Rattaché (Optionnel) :" else "4. First Enrolled Student (Optional):",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        OutlinedTextField(
          value = studentName,
          onValueChange = { studentName = it },
          label = { Text(if (currentLocale == "fr") "Nom de l'Élève" else "Student Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("new_student_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = studentEmail,
          onValueChange = { studentEmail = it },
          label = { Text("Student Email") },
          modifier = Modifier.fillMaxWidth().testTag("new_student_email_input"),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (branchName.isNotBlank() && city.isNotBlank() && managerName.isNotBlank() && secretaryName.isNotBlank()) {
            onCreateBranch(
              branchName,
              city,
              address.ifBlank { "Commercial Center, $city" },
              branchPhone,
              managerName,
              managerEmail.ifBlank { "manager.${city.lowercase().replace(" ", "")}@driveschool.cm" },
              managerPhone,
              secretaryName,
              secretaryEmail.ifBlank { "secretary.${city.lowercase().replace(" ", "")}@driveschool.cm" },
              secretaryPhone,
              studentName.ifBlank { null },
              studentEmail.ifBlank { null },
              studentPhone.ifBlank { null },
              selectedCourseId
            )
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
        modifier = Modifier.testTag("submit_create_branch_button")
      ) {
        Text(
          text = if (currentLocale == "fr") "Créer Agence, Staff & Élève" else "Create Branch & Staff",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (currentLocale == "fr") "Annuler" else "Cancel")
      }
    }
  )
}
