package com.example.driveschool.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.AuditLogEntity
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun AuditLogScreen(
  currentLocale: String,
  auditLogs: List<AuditLogEntity>
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("audit_log_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
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
                text = if (currentLocale == "fr") "Piste d'Audit Immuable (FR-08 & BR-05)" else "Immutable Financial & Governance Audit Trail",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = if (currentLocale == "fr") "Traçabilité des négociations, remises et encaissements espèces" else "Strict auditability of fee negotiations, discounts & cash receipts",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
              )
            }
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(28.dp)
            )
          }
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (currentLocale == "fr") "Événements Journalisés" else "Recorded Audit Events",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "${auditLogs.size} logs",
          style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
      }
    }

    items(auditLogs) { log ->
      val actionColor = when (log.action) {
        "CASH_COLLECTED" -> EmeraldSuccess
        "FEE_NEGOTIATED", "DISCOUNT_APPROVED" -> AmberAccent
        "EXAM_RECOMMENDED", "EXAM_CANDIDATE_APPROVED" -> Color(0xFF0284C7)
        else -> NavyPrimary
      }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = actionColor.copy(alpha = 0.15f)
            ) {
              Text(
                text = log.action,
                color = actionColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
              )
            }
            Text(
              text = Localization.formatDateTime(log.timestamp, currentLocale),
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = log.details,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp)
          )

          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Actor: ${log.actorName} (${log.actorRole})",
              style = MaterialTheme.typography.bodySmall.copy(
                color = Color.Gray,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              )
            )
          }
        }
      }
    }
  }
}
