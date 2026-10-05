package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.CertificateEntity
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicVerifyScreen(
  currentLocale: String,
  initialCertificate: CertificateEntity?,
  onBack: () -> Unit,
  onVerifyUuid: (String) -> Unit,
  verificationResult: CertificateEntity?
) {
  var uuidInput by remember {
    mutableStateOf(initialCertificate?.verificationUuid ?: "d9b4f2a1-7c3e-4b28-98e1-5f60bca43210")
  }
  val displayedCert = verificationResult ?: initialCertificate

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (currentLocale == "fr") "Vérification Publique Certificat" else "Public Certificate Verification",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("verify_back_button")) {
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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Endpoint /verify/{uuid} header
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = NavyDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "ENDPOINT: /verify/{uuid} (BR-07 Public & Unauthenticated)",
            color = AmberAccent,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (currentLocale == "fr")
              "Tout employeur, autorité policière ou compagnie d'assurance peut vérifier l'authenticité d'un permis DriveSchool en saisissant son UUID ou en scannant le code QR."
            else
              "Public verification service: Authenticate issued driving certificates against the official unalterable registry.",
            color = Color(0xFFCBD5E1),
            style = MaterialTheme.typography.bodySmall
          )
        }
      }

      // Search Box
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = uuidInput,
          onValueChange = { uuidInput = it },
          label = { Text(if (currentLocale == "fr") "UUID du Certificat" else "Certificate UUID") },
          modifier = Modifier.weight(1f).testTag("verify_uuid_input"),
          singleLine = true
        )
        Button(
          onClick = { onVerifyUuid(uuidInput) },
          colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(56.dp).testTag("verify_uuid_button")
        ) {
          Icon(Icons.Default.Search, contentDescription = null, tint = NavyDark)
        }
      }

      // CERTIFICATE DISPLAY CARD (Official Look)
      if (displayedCert != null) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
          elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, AmberAccent, RoundedCornerShape(16.dp))
            .testTag("official_certificate_card")
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            // Header Republic of Cameroon
            Text(
              text = "RÉPUBLIQUE DU CAMEROUN • REPUBLIC OF CAMEROON",
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = Color(0xFF334155),
              textAlign = TextAlign.Center
            )
            Text(
              text = "DRIVESCHOOL ACADEMY • MINISTÈRE DES TRANSPORTS",
              fontWeight = FontWeight.SemiBold,
              fontSize = 10.sp,
              color = Color(0xFF64748B)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Verified Badge
            Box(
              modifier = Modifier
                .size(68.dp)
                .clip(CircleShape)
                .background(EmeraldSuccess.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(42.dp)
              )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = EmeraldSuccess
            ) {
              Text(
                text = "✓ 100% AUTHENTIC & VERIFIED",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
              )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = if (displayedCert.type.name == "THEORY")
                "CERTIFICAT DE FORMATION THÉORIQUE DU CODE DE LA ROUTE"
              else
                "OFFICIAL DRIVING LICENCE QUALIFICATION CERTIFICATE",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = NavyDark,
              textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(14.dp))

            // Details
            CertificateRow(label = if (currentLocale == "fr") "Titulaire :" else "Holder Name:", value = displayedCert.studentName)
            CertificateRow(label = if (currentLocale == "fr") "Catégorie de Permis :" else "Licence Category:", value = "${displayedCert.category.code} (${displayedCert.category.vehicleType})")
            CertificateRow(label = if (currentLocale == "fr") "Centre d'Examen :" else "Issuing Branch:", value = displayedCert.branchName)
            CertificateRow(label = if (currentLocale == "fr") "Date d'Émission :" else "Date Issued:", value = Localization.formatDate(displayedCert.issuedAt, currentLocale))
            CertificateRow(label = "Security UUID:", value = displayedCert.verificationUuid, isMono = true)

            Spacer(modifier = Modifier.height(16.dp))

            // Simulated QR Code
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color.White,
              modifier = Modifier
                .size(130.dp)
                .border(1.dp, Color.LightGray, RoundedCornerShape(10.dp))
            ) {
              Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = Icons.Default.QrCode2,
                  contentDescription = "QR Code",
                  tint = NavyDark,
                  modifier = Modifier.size(90.dp)
                )
                Text(
                  text = "SCAN TO VERIFY",
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.Gray
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "URL: https://driveschool.cm/verify/${displayedCert.verificationUuid}",
              fontSize = 10.sp,
              color = Color.Gray,
              fontFamily = FontFamily.Monospace,
              textAlign = TextAlign.Center
            )
          }
        }
      }
    }
  }
}

@Composable
fun CertificateRow(label: String, value: String, isMono: Boolean = false) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(
        fontWeight = FontWeight.Bold,
        color = NavyDark,
        fontFamily = if (isMono) FontFamily.Monospace else FontFamily.Default,
        fontSize = if (isMono) 10.sp else 12.sp
      )
    )
  }
}
