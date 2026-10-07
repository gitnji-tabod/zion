package com.example.driveschool.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.BranchEntity
import com.example.driveschool.data.model.UserEntity
import com.example.driveschool.data.model.UserRole
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun SignInScreen(
  currentLocale: String,
  users: List<UserEntity>,
  branches: List<BranchEntity> = emptyList(),
  onSignIn: (String, String) -> Unit,
  onGoogleSignIn: () -> Unit,
  onQuickSignInUser: (UserEntity) -> Unit,
  onNavigateToSignUp: () -> Unit
) {
  var emailOrPhone by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .statusBarsPadding()
      .navigationBarsPadding()
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(20.dp))

    // App Branding Logo
    Box(
      modifier = Modifier
        .size(68.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(NavyPrimary),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Default.DirectionsCar,
        contentDescription = "DriveSchool",
        tint = AmberAccent,
        modifier = Modifier.size(40.dp)
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    Text(
      text = "ZION digital",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        color = NavyPrimary
      )
    )

    Text(
      text = if (currentLocale == "fr")
        "Plateforme Nationale de Gestion d'Auto-écoles & E-Learning"
      else
        "National Hybrid Driving School Management & LMS Platform",
      style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B)),
      fontSize = 12.sp
    )

    Spacer(modifier = Modifier.height(28.dp))

    // Sign In Card
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Text(
          text = if (currentLocale == "fr") "Connexion à votre Espace" else "Sign In to Your Account",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        OutlinedTextField(
          value = emailOrPhone,
          onValueChange = { emailOrPhone = it },
          label = { Text(if (currentLocale == "fr") "Email ou Téléphone" else "Email or Phone") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          modifier = Modifier.fillMaxWidth().testTag("signin_email_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text(if (currentLocale == "fr") "Mot de Passe" else "Password") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
          modifier = Modifier.fillMaxWidth().testTag("signin_password_input"),
          singleLine = true
        )

        Button(
          onClick = {
            if (emailOrPhone.isNotBlank()) {
              onSignIn(emailOrPhone, password)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("signin_submit_button")
        ) {
          Text(
            text = if (currentLocale == "fr") "Se Connecter" else "Sign In",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }

        // Google Sign-In Button (Firebase Auth integration)
        OutlinedButton(
          onClick = onGoogleSignIn,
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("signin_google_button")
        ) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = Color(0xFF4285F4),
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (currentLocale == "fr") "Continuer avec Google" else "Continue with Google",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(18.dp))

    // Don't have an account link
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (currentLocale == "fr") "Nouveau sur ZION digital ? " else "New to ZION digital? ",
        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
      )
      TextButton(
        onClick = onNavigateToSignUp,
        modifier = Modifier.testTag("navigate_to_signup_button")
      ) {
        Text(
          text = if (currentLocale == "fr") "Créer un Compte Élève" else "Create Student Account",
          fontWeight = FontWeight.Bold,
          color = AmberAccent
        )
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Quick Switch Demo Persona Section
    Surface(
      shape = RoundedCornerShape(14.dp),
      color = Color(0xFFF1F5F9),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = if (currentLocale == "fr") "⚡ Accès Rapide Profils Démo / Test :" else "⚡ Quick Login as Demo Persona:",
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
          )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Grid of roles with branch badges
        users.forEach { user ->
          val branch = branches.find { it.id == user.branchId }
          val branchName = branch?.name ?: if (user.role == UserRole.SUPER_ADMIN) "Headquarters (HQ)" else ""

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            onClick = { onQuickSignInUser(user) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 3.dp)
              .testTag("quick_login_${user.id}")
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  if (branchName.isNotBlank()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = when (user.role) {
                        UserRole.SUPER_ADMIN -> AmberAccent.copy(alpha = 0.2f)
                        UserRole.BRANCH_MANAGER -> Color(0xFFE0E7FF)
                        UserRole.SECRETARY -> Color(0xFFFEF3C7)
                        UserRole.INSTRUCTOR -> Color(0xFFDCFCE7)
                        UserRole.STUDENT -> Color(0xFFF1F5F9)
                      }
                    ) {
                      Text(
                        text = branch?.city?.split("(")?.firstOrNull()?.trim() ?: "HQ",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyDark,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
                Text(
                  text = "${user.role.name.replace("_", " ")} • ${user.email}",
                  style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                )
              }
              Icon(
                imageVector = Icons.Default.Login,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}
