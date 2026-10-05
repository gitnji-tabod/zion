package com.example.driveschool.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.BranchEntity
import com.example.driveschool.data.model.CourseEntity
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentToBranchDialog(
  currentLocale: String,
  branch: BranchEntity,
  courses: List<CourseEntity>,
  onDismiss: () -> Unit,
  onAddStudent: (name: String, email: String, phone: String, courseId: String) -> Unit
) {
  var studentName by remember { mutableStateOf("") }
  var studentEmail by remember { mutableStateOf("") }
  var studentPhone by remember { mutableStateOf("+237 ") }
  var selectedCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: "course-cat-b") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AmberAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Inscrire un Élève à ${branch.name}" else "Enroll Student at ${branch.name}",
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
            "L'élève sera rattaché à cette agence physique pour ses cours de code et ses heures de conduite pratique."
          else
            "The student will be attached to this branch for theory classroom sessions and dual-control practical driving.",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        OutlinedTextField(
          value = studentName,
          onValueChange = { studentName = it },
          label = { Text(if (currentLocale == "fr") "Nom & Prénom de l'Élève" else "Student Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("add_student_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = studentEmail,
          onValueChange = { studentEmail = it },
          label = { Text("Email") },
          modifier = Modifier.fillMaxWidth().testTag("add_student_email_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = studentPhone,
          onValueChange = { studentPhone = it },
          label = { Text(if (currentLocale == "fr") "Téléphone (+237)" else "Phone Number (+237)") },
          modifier = Modifier.fillMaxWidth().testTag("add_student_phone_input"),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = if (currentLocale == "fr") "Catégorie de Permis :" else "Licence Category:",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = NavyPrimary
        )

        courses.take(4).forEach { course ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedCourseId == course.id) AmberAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            onClick = { selectedCourseId = course.id },
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedCourseId == course.id,
                onClick = { selectedCourseId = course.id }
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column {
                Text(
                  text = if (currentLocale == "fr") course.titleFr else course.title,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                Text(
                  text = course.licenseCategory.vehicleType,
                  fontSize = 10.sp,
                  color = Color.Gray
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (studentName.isNotBlank()) {
            onAddStudent(
              studentName,
              studentEmail.ifBlank { "student.${System.currentTimeMillis()}@driveschool.cm" },
              studentPhone,
              selectedCourseId
            )
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
        modifier = Modifier.testTag("submit_add_student_button")
      ) {
        Text(
          text = if (currentLocale == "fr") "Confirmer Inscription" else "Confirm Enrollment",
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
