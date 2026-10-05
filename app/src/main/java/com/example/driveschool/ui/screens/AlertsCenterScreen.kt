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
import com.example.driveschool.data.model.ExpiryAlertEntity
import com.example.driveschool.data.model.InsurancePolicyEntity
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun AlertsCenterScreen(
  currentLocale: String,
  alerts: List<ExpiryAlertEntity>,
  insurancePolicies: List<InsurancePolicyEntity>,
  onRunDailyScan: () -> Unit,
  onMarkAlertRead: (String) -> Unit
) {
  var selectedFilter by remember { mutableStateOf("ALL") }

  val filteredAlerts = remember(alerts, selectedFilter) {
    when (selectedFilter) {
      "LEAD_1" -> alerts.filter { it.leadDays == 1 }
      "LEAD_7" -> alerts.filter { it.leadDays == 7 }
      "LEAD_30" -> alerts.filter { it.leadDays == 30 }
      "INSURANCE" -> alerts.filter { it.expirableType == "INSURANCE" }
      "VEHICLE" -> alerts.filter { it.expirableType == "VEHICLE_SERVICE" }
      else -> alerts
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("alerts_center_screen"),
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
                text = if (currentLocale == "fr") "Moteur d'Alerte Proactif (FR-26)" else "Proactive Expiry Alert Engine (FR-26)",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = if (currentLocale == "fr") "Notifications multicanales : SMS (+237), WhatsApp & In-App" else "Multi-channel fan-out: SMS (+237), WhatsApp & In-App",
                style = MaterialTheme.typography.bodySmall.copy(color = AmberAccent)
              )
            }
            Icon(
              imageVector = Icons.Default.Campaign,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          Button(
            onClick = onRunDailyScan,
            colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("trigger_expiry_scan_btn")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, tint = NavyDark, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (currentLocale == "fr") "Exécuter Scan Quotidien (Idempotence BR-09)" else "Run Daily Scan (BR-09 Idempotency)",
              color = NavyDark,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = selectedFilter == "ALL",
          onClick = { selectedFilter = "ALL" },
          label = { Text("All (${alerts.size})", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == "LEAD_1",
          onClick = { selectedFilter = "LEAD_1" },
          label = { Text("1 Day", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == "LEAD_7",
          onClick = { selectedFilter = "LEAD_7" },
          label = { Text("7 Days", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == "INSURANCE",
          onClick = { selectedFilter = "INSURANCE" },
          label = { Text("Insurance", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == "VEHICLE",
          onClick = { selectedFilter = "VEHICLE" },
          label = { Text("Fleet", fontSize = 11.sp) }
        )
      }
    }

    // Alert Cards
    items(filteredAlerts) { alert ->
      val leadColor = when (alert.leadDays) {
        1 -> MaterialTheme.colorScheme.error
        7 -> AmberAccent
        else -> Color(0xFF0284C7)
      }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (alert.isRead) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (alert.isRead) 1.dp else 3.dp),
        modifier = Modifier.fillMaxWidth().testTag("alert_card_${alert.id}")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = leadColor.copy(alpha = 0.2f)
            ) {
              Text(
                text = "${alert.leadDays} DAY LEAD TIME",
                color = leadColor,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFF1F5F9)
            ) {
              Text(
                text = alert.expirableType,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (currentLocale == "fr") alert.titleFr else alert.titleEn,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = if (currentLocale == "fr") alert.messageFr else alert.messageEn,
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
          )

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Send, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = alert.channels,
                fontSize = 10.sp,
                color = Color.Gray
              )
            }

            if (!alert.isRead) {
              TextButton(
                onClick = { onMarkAlertRead(alert.id) },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(if (currentLocale == "fr") "Marquer lu" else "Mark Read", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }

    // Active Insurance Policies Pipeline
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF0284C7))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Portefeuille des Polices d'Assurance" else "Insurance Policies Portfolio",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
      }
    }

    items(insurancePolicies) { pol ->
      val daysLeft = ((pol.expiresAt - System.currentTimeMillis()) / (24L * 3600 * 1000)).toInt()
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = pol.holderName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
              text = "${pol.vehiclePlate} • ${if (currentLocale == "fr") pol.tariffNameFr else pol.tariffName}",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
            Text(
              text = if (currentLocale == "fr") "Expire dans $daysLeft jour(s)" else "Expires in $daysLeft day(s)",
              color = if (daysLeft <= 7) MaterialTheme.colorScheme.error else EmeraldSuccess,
              fontWeight = FontWeight.SemiBold,
              fontSize = 11.sp
            )
          }
          Text(
            text = Localization.formatCurrency(pol.premiumAmount, currentLocale),
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            fontSize = 13.sp
          )
        }
      }
    }
  }
}
