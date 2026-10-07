package com.example.driveschool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.driveschool.data.model.UserEntity
import com.example.driveschool.data.model.UserRole
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
  currentRole: UserRole,
  currentUser: UserEntity? = null,
  currentLocale: String,
  unreadAlertsCount: Int,
  onToggleLocale: () -> Unit,
  onAlertsClick: () -> Unit,
  onVerifyClick: () -> Unit,
  onProfileClick: () -> Unit = {},
  onSignOut: () -> Unit = {}
) {
  Surface(
    color = NavyPrimary,
    tonalElevation = 6.dp,
    shadowElevation = 4.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .statusBarsPadding()
        .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // App Brand
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.testTag("app_brand_header")
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(AmberAccent),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.DirectionsCar,
              contentDescription = "ZION digital Logo",
              tint = NavyDark,
              modifier = Modifier.size(24.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "ZION digital",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 18.sp
              )
            )
            Text(
              text = if (currentLocale == "fr") "Plateforme Permis & Gestion" else "Driving Platform & LMS",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color(0xFFCBD5E1),
                fontSize = 11.sp
              )
            )
          }
        }

        // Actions: QR Verify, Alert Bell, Language Toggle, Sign Out
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Public verify shortcut
          IconButton(
            onClick = onVerifyClick,
            modifier = Modifier.testTag("verify_shortcut_button")
          ) {
            Icon(
              imageVector = Icons.Default.QrCodeScanner,
              contentDescription = "Verify Certificate",
              tint = AmberAccent
            )
          }

          // Alerts Bell
          IconButton(
            onClick = onAlertsClick,
            modifier = Modifier.testTag("alerts_bell_button")
          ) {
            BadgedBox(
              badge = {
                if (unreadAlertsCount > 0) {
                  Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White
                  ) {
                    Text("$unreadAlertsCount")
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = Color.White
              )
            }
          }

          // Language Toggle (EN / FR)
          FilledTonalButton(
            onClick = onToggleLocale,
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = Color(0xFF1E293B),
              contentColor = AmberAccent
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.height(34.dp).testTag("language_toggle_button")
          ) {
            Text(
              text = if (currentLocale == "en") "FR 🇫🇷" else "EN 🇬🇧",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Profile Shortcut Button
          IconButton(
            onClick = onProfileClick,
            modifier = Modifier.size(34.dp).testTag("top_bar_profile_button")
          ) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = "User Profile",
              tint = AmberAccent,
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(modifier = Modifier.width(4.dp))

          // Sign Out Button
          IconButton(
            onClick = onSignOut,
            modifier = Modifier.size(34.dp).testTag("signout_button")
          ) {
            Icon(
              imageVector = Icons.Default.Logout,
              contentDescription = "Sign Out",
              tint = Color(0xFFFCA5A5),
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Authenticated User Identity Bar (Replaces Persona Switcher)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.testTag("authenticated_user_identity_bar")
        ) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(AmberAccent.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(16.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = currentUser?.name ?: "Authenticated User",
              style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 12.sp
              )
            )
            Text(
              text = "${Localization.roleName(currentRole.name, currentLocale)}${if (currentUser?.email?.isNotBlank() == true) " • ${currentUser.email}" else ""}",
              style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
              )
            )
          }
        }

        // Region / Live Status Badge
        Surface(
          shape = CircleShape,
          color = Color(0xFF10B981).copy(alpha = 0.2f),
          modifier = Modifier.padding(start = 8.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "CEMAC / CM",
              color = Color(0xFF10B981),
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }
  }
}
