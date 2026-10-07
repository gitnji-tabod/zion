package com.example.driveschool.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.BranchEntity
import com.example.driveschool.data.model.CourseEntity
import com.example.driveschool.data.model.EnrollmentMode
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
  currentLocale: String,
  branches: List<BranchEntity>,
  courses: List<CourseEntity>,
  onSignUp: (String, String, String, String, String, String?, String, EnrollmentMode) -> Unit,
  onNavigateToSignIn: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+237 ") }
  var password by remember { mutableStateOf("") }
  var countryCode by remember { mutableStateOf("CM") } // "CM" for Cameroon, others for International
  val physicalBranches = remember(branches) { branches.filter { !it.isVirtual } }
  var selectedBranchId by remember(physicalBranches) { mutableStateOf(physicalBranches.firstOrNull()?.id ?: "") }
  var selectedCourseId by remember(courses) { mutableStateOf(courses.firstOrNull()?.id ?: "course-cat-b") }
  var mode by remember { mutableStateOf(EnrollmentMode.ONSITE) }
  var branchMenuExpanded by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (currentLocale == "fr") "Inscription Nouveau Compte Élève" else "Create New Student Account",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateToSignIn, modifier = Modifier.testTag("signup_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = NavyPrimary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Intro Banner
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = NavyDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = if (currentLocale == "fr") "Règle de Routage Pays (BR-01)" else "Country Routing Rule (BR-01)",
            color = AmberAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (currentLocale == "fr")
              "• Cameroun : Accès mixte cours en ligne + leçons pratiques dans l'agence physique choisie.\n• Autres pays : Accès 100% en ligne sur le campus virtuel avec certificat PDF téléchargeable."
            else
              "• Cameroon: Onsite-capable with practical lessons at your chosen physical branch.\n• Other Countries: 100% online pathway attached to the Global Virtual Campus with verifiable certificate.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFCBD5E1))
          )
        }
      }

      // Input Form Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(if (currentLocale == "fr") "Nom et Prénom" else "Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(if (currentLocale == "fr") "Numéro de Téléphone (+237)" else "Phone Number (+237)") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().testTag("signup_phone_input"),
            singleLine = true
          )

          OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(if (currentLocale == "fr") "Mot de Passe" else "Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
            singleLine = true
          )

          // Country Selection (BR-01 Routing Mechanism)
          Text(
            text = if (currentLocale == "fr") "Sélection obligatoire du Pays (BR-01) :" else "Mandatory Country Selection (BR-01):",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = countryCode == "CM",
              onClick = {
                countryCode = "CM"
                mode = EnrollmentMode.ONSITE
              },
              label = { Text("🇨🇲 Cameroon (Onsite)", fontSize = 11.sp) },
              modifier = Modifier.weight(1f).testTag("country_cameroon_chip")
            )
            FilterChip(
              selected = countryCode != "CM",
              onClick = {
                countryCode = "FR"
                mode = EnrollmentMode.ONLINE
              },
              label = { Text("🌍 Other (Online-Only)", fontSize = 11.sp) },
              modifier = Modifier.weight(1f).testTag("country_other_chip")
            )
          }

          // Branch Selection (If Cameroon)
          if (countryCode == "CM") {
            val selectedBranch = branches.find { it.id == selectedBranchId }
            Text(
              text = if (currentLocale == "fr") "Rattachement à l'Agence Physique :" else "Attach to Physical Branch:",
              fontWeight = FontWeight.SemiBold,
              fontSize = 13.sp
            )
            Box {
              OutlinedButton(
                onClick = { branchMenuExpanded = true },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("select_branch_dropdown_button")
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = selectedBranch?.name ?: "Select Branch",
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
              }
              DropdownMenu(
                expanded = branchMenuExpanded,
                onDismissRequest = { branchMenuExpanded = false }
              ) {
                physicalBranches.forEach { branch ->
                  DropdownMenuItem(
                    text = {
                      Column {
                        Text(text = branch.name, fontWeight = FontWeight.Bold)
                        Text(text = "${branch.city} • ${branch.address}", fontSize = 11.sp, color = Color.Gray)
                      }
                    },
                    onClick = {
                      selectedBranchId = branch.id
                      branchMenuExpanded = false
                    }
                  )
                }
              }
            }
          }

          // Course Selection
          Text(
            text = if (currentLocale == "fr") "Catégorie de Permis Souhaitée :" else "Desired Licence Category:",
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
          )
          courses.take(4).forEach { course ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (selectedCourseId == course.id) AmberAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
              onClick = { selectedCourseId = course.id },
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                RadioButton(
                  selected = selectedCourseId == course.id,
                  onClick = { selectedCourseId = course.id }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text = if (currentLocale == "fr") course.titleFr else course.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "${course.licenseCategory.vehicleType} • ${course.durationWeeks} weeks",
                    fontSize = 11.sp,
                    color = Color.Gray
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Submit Sign Up Button
          Button(
            onClick = {
              if (name.isNotBlank() && email.isNotBlank()) {
                val assignedBranch = if (countryCode == "CM") selectedBranchId else "branch-online-00"
                onSignUp(
                  name,
                  email,
                  password,
                  phone,
                  countryCode,
                  assignedBranch,
                  selectedCourseId,
                  mode
                )
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("signup_submit_button")
          ) {
            Text(
              text = if (currentLocale == "fr") "Créer mon Compte & Débuter l'Initiation" else "Create Account & Start Onboarding",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
          }
        }
      }

      // Link to Sign In
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (currentLocale == "fr") "Vous avez déjà un compte ? " else "Already have an account? ",
          style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
        )
        TextButton(
          onClick = onNavigateToSignIn,
          modifier = Modifier.testTag("navigate_to_signin_button")
        ) {
          Text(
            text = if (currentLocale == "fr") "Se Connecter" else "Sign In",
            fontWeight = FontWeight.Bold,
            color = NavyPrimary
          )
        }
      }
    }
  }
}
