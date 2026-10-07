package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun UserProfileScreen(
  currentLocale: String,
  currentUser: UserEntity?,
  branches: List<BranchEntity>,
  courses: List<CourseEntity>,
  enrollments: List<EnrollmentEntity>,
  payments: List<PaymentEntity>,
  onBack: () -> Unit,
  onUpdateProfile: (userId: String, name: String, phone: String, locale: String) -> Unit,
  onOpenPaymentGateway: (EnrollmentEntity) -> Unit,
  onSignOut: () -> Unit
) {
  var nameInput by remember(currentUser) { mutableStateOf(currentUser?.name ?: "") }
  var phoneInput by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
  var selectedLocale by remember(currentUser) { mutableStateOf(currentUser?.preferredLocale ?: currentLocale) }
  var isEditingContact by remember { mutableStateOf(false) }

  // Student's enrollment details
  val studentEnrollment = remember(enrollments, currentUser) {
    enrollments.find { it.studentId == currentUser?.id } ?: enrollments.firstOrNull()
  }

  val activeCourse = remember(courses, studentEnrollment) {
    courses.find { it.id == studentEnrollment?.courseId }
  }

  val assignedBranch = remember(branches, currentUser, studentEnrollment) {
    branches.find { it.id == (currentUser?.branchId ?: studentEnrollment?.branchId) }
  }

  val userPayments = remember(payments, currentUser) {
    payments.filter { it.studentId == currentUser?.id }
  }

  val isLockedByPaymentGate = studentEnrollment?.status == EnrollmentStatus.ONBOARDING ||
    studentEnrollment?.status == EnrollmentStatus.AWAITING_PAYMENT

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (currentLocale == "fr") "Profil Utilisateur" else "User Profile",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("profile_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = onSignOut, modifier = Modifier.testTag("profile_signout_button")) {
            Icon(Icons.Default.Logout, contentDescription = "Sign Out", tint = Color(0xFFFCA5A5))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = NavyDark,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

      // User Header Avatar Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        modifier = Modifier.fillMaxWidth().testTag("profile_header_card")
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(NavyDark, Color(0xFF1E293B))
              )
            )
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(72.dp)
              .clip(CircleShape)
              .background(AmberAccent),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "User Avatar",
              tint = NavyDark,
              modifier = Modifier.size(44.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = currentUser?.name ?: "Student User",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White,
              fontSize = 20.sp
            )
          )

          Text(
            text = currentUser?.email ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
              color = Color(0xFF94A3B8),
              fontSize = 13.sp
            )
          )

          Spacer(modifier = Modifier.height(10.dp))

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Role Badge
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = AmberAccent.copy(alpha = 0.2f)
            ) {
              Text(
                text = Localization.roleName(currentUser?.role?.name ?: "STUDENT", currentLocale),
                color = AmberAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }

            // Country / Mode Badge
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (currentUser?.isOnsiteCapable == true) Color(0xFFDCFCE7) else Color(0xFFDBEAFE)
            ) {
              Text(
                text = if (currentUser?.isOnsiteCapable == true) "🇨🇲 CAMEROON (HYBRID)" else "🌍 DIASPORA / ONLINE",
                color = if (currentUser?.isOnsiteCapable == true) Color(0xFF166534) else Color(0xFF1E40AF),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
      }

      // SECTION 1: SUBSCRIPTION & ENROLLMENT STATUS
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("profile_subscription_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.CardMembership, contentDescription = null, tint = NavyPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLocale == "fr") "Statut d'Abonnement & Formation" else "Current Subscription & Access Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
              )
            }

            // Status Badge
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = when (studentEnrollment?.status) {
                EnrollmentStatus.ACTIVE -> EmeraldSuccess.copy(alpha = 0.15f)
                EnrollmentStatus.ONBOARDING, EnrollmentStatus.AWAITING_PAYMENT -> AmberAccent.copy(alpha = 0.2f)
                EnrollmentStatus.COMPLETED -> Color(0xFFDBEAFE)
                EnrollmentStatus.EXPIRED -> Color(0xFFFEE2E2)
                else -> Color(0xFFF1F5F9)
              }
            ) {
              Text(
                text = Localization.enrollmentStatusName(studentEnrollment?.status?.name ?: "ACTIVE", currentLocale),
                color = when (studentEnrollment?.status) {
                  EnrollmentStatus.ACTIVE -> EmeraldSuccess
                  EnrollmentStatus.ONBOARDING, EnrollmentStatus.AWAITING_PAYMENT -> Color(0xFFB45309)
                  EnrollmentStatus.COMPLETED -> Color(0xFF1D4ED8)
                  EnrollmentStatus.EXPIRED -> Color(0xFFB91C1C)
                  else -> Color.DarkGray
                },
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Enrolled course
          ProfileDetailRow(
            icon = Icons.Default.DirectionsCar,
            label = if (currentLocale == "fr") "Permis préparé" else "Enrolled Course",
            value = "${activeCourse?.licenseCategory?.code ?: "B"} - ${if (currentLocale == "fr") activeCourse?.titleFr ?: "Véhicules légers" else activeCourse?.title ?: "Light Motor Vehicle"}"
          )

          ProfileDetailRow(
            icon = Icons.Default.Storefront,
            label = if (currentLocale == "fr") "Agence de rattachement" else "Assigned Branch",
            value = assignedBranch?.name ?: if (studentEnrollment?.mode == EnrollmentMode.ONLINE) "Online Virtual Campus" else "HQ Branch"
          )

          ProfileDetailRow(
            icon = Icons.Default.CastForEducation,
            label = if (currentLocale == "fr") "Mode d'apprentissage" else "Learning Mode",
            value = if (studentEnrollment?.mode == EnrollmentMode.ONLINE) "100% Online (E-learning)" else "Hybrid (Online Theory + In-Car Driving)"
          )

          ProfileDetailRow(
            icon = Icons.Default.Event,
            label = if (currentLocale == "fr") "Validité inscription" else "Validity Expiry",
            value = if (studentEnrollment != null) Localization.formatDate(studentEnrollment.termEndsAt, currentLocale) else "90 Days"
          )

          // Payment Status and Action
          if (isLockedByPaymentGate && studentEnrollment != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFFEF3C7),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = if (currentLocale == "fr")
                    "⚠️ Accès limité : Initiation Gratuite active. Réglez les frais pour débloquer l'ensemble des modules et séances pratiques."
                  else
                    "⚠️ Limited Access: Free Onboarding active. Pay course fees to unlock remaining LMS modules and driving sessions.",
                  style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF92400E), fontSize = 12.sp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = { onOpenPaymentGateway(studentEnrollment) },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                  shape = RoundedCornerShape(8.dp),
                  modifier = Modifier.fillMaxWidth().testTag("profile_pay_button")
                ) {
                  Icon(Icons.Default.Payment, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = if (currentLocale == "fr") "Régler Frais de Formation" else "Pay Course Fees (Unlock Full Access)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                }
              }
            }
          }
        }
      }

      // SECTION 2: PERSONAL INFORMATION & EDITABLE CONTACT DETAILS
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("profile_contact_card")
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.ContactPhone, contentDescription = null, tint = NavyPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLocale == "fr") "Informations & Coordonnées" else "Contact Information",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
              )
            }

            TextButton(
              onClick = {
                if (isEditingContact) {
                  // Save changes
                  if (currentUser != null && nameInput.isNotBlank()) {
                    onUpdateProfile(currentUser.id, nameInput.trim(), phoneInput.trim(), selectedLocale)
                  }
                  isEditingContact = false
                } else {
                  isEditingContact = true
                }
              },
              modifier = Modifier.testTag("edit_profile_toggle_button")
            ) {
              Icon(
                imageVector = if (isEditingContact) Icons.Default.Save else Icons.Default.Edit,
                contentDescription = null,
                tint = NavyPrimary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = if (isEditingContact) {
                  if (currentLocale == "fr") "Enregistrer" else "Save"
                } else {
                  if (currentLocale == "fr") "Modifier" else "Edit"
                },
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          if (isEditingContact) {
            // Edit Mode Form
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
              OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text(if (currentLocale == "fr") "Nom Complet" else "Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("edit_name_input")
              )

              OutlinedTextField(
                value = currentUser?.email ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(if (currentLocale == "fr") "Email (Identifiant Unique)" else "Email (Fixed ID)") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
              )

              OutlinedTextField(
                value = phoneInput,
                onValueChange = { phoneInput = it },
                label = { Text(if (currentLocale == "fr") "Numéro de Téléphone (WhatsApp / SMS)" else "Phone Number (SMS / WhatsApp)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("edit_phone_input")
              )

              Text(
                text = if (currentLocale == "fr") "Langue Préférée :" else "Preferred Language:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                  selected = selectedLocale == "en",
                  onClick = { selectedLocale = "en" },
                  label = { Text("English 🇬🇧") },
                  modifier = Modifier.testTag("lang_en_chip")
                )
                FilterChip(
                  selected = selectedLocale == "fr",
                  onClick = { selectedLocale = "fr" },
                  label = { Text("Français 🇫🇷") },
                  modifier = Modifier.testTag("lang_fr_chip")
                )
              }
            }
          } else {
            // View Mode
            ProfileDetailRow(
              icon = Icons.Default.Person,
              label = if (currentLocale == "fr") "Nom" else "Name",
              value = currentUser?.name ?: "N/A"
            )

            ProfileDetailRow(
              icon = Icons.Default.Email,
              label = "Email",
              value = currentUser?.email ?: "N/A"
            )

            ProfileDetailRow(
              icon = Icons.Default.Phone,
              label = if (currentLocale == "fr") "Téléphone" else "Phone",
              value = if (currentUser?.phone.isNullOrBlank()) if (currentLocale == "fr") "Non renseigné" else "Not provided" else currentUser!!.phone
            )

            ProfileDetailRow(
              icon = Icons.Default.Language,
              label = if (currentLocale == "fr") "Langue" else "Language",
              value = if (currentUser?.preferredLocale == "fr") "Français 🇫🇷" else "English 🇬🇧"
            )
          }
        }
      }

      // SECTION 3: RECENT TRANSACTION / PAYMENT HISTORY
      if (userPayments.isNotEmpty()) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("profile_payments_card")
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Receipt, contentDescription = null, tint = NavyPrimary)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLocale == "fr") "Historique des Règlements" else "Payment History & Receipts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp)
              )
            }

            Spacer(modifier = Modifier.height(12.dp))

            userPayments.forEach { pay ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = Localization.paymentChannelName(pay.channel.name, currentLocale),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "Ref: ${pay.reference} • ${Localization.formatDate(pay.createdAt, currentLocale)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                  )
                }
                Text(
                  text = Localization.formatCurrency(pay.amount, currentLocale),
                  fontWeight = FontWeight.Bold,
                  color = EmeraldSuccess,
                  fontSize = 13.sp
                )
              }
              HorizontalDivider(color = Color(0xFFF1F5F9))
            }
          }
        }
      }
    }
  }
}

@Composable
fun ProfileDetailRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(32.dp)
        .clip(CircleShape)
        .background(NavyPrimary.copy(alpha = 0.08f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B), fontSize = 11.sp)
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
      )
    }
  }
}
