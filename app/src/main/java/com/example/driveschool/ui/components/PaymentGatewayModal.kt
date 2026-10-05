package com.example.driveschool.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.driveschool.data.model.EnrollmentEntity
import com.example.driveschool.data.model.EnrollmentMode
import com.example.driveschool.data.model.PaymentChannel
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentGatewayModal(
  currentLocale: String,
  enrollment: EnrollmentEntity,
  onDismiss: () -> Unit,
  onProcessPayment: (Double, PaymentChannel, String) -> Unit
) {
  var selectedChannel by remember {
    mutableStateOf(
      if (enrollment.mode == EnrollmentMode.ONLINE) PaymentChannel.CARD else PaymentChannel.MTN_MOMO
    )
  }
  var phoneInput by remember { mutableStateOf("677 889 900") }
  var cardNumberInput by remember { mutableStateOf("4242 •••• •••• 4242") }
  var isProcessing by remember { mutableStateOf(false) }

  val courseFee = enrollment.negotiatedAmount ?: 125000.0

  AlertDialog(
    onDismissRequest = { if (!isProcessing) onDismiss() },
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Paiement Sécurisé de Formation (FR-09)" else "Secure Course Payment Gateway (FR-09)",
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Price banner
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = NavyDark,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (currentLocale == "fr") "Total à régler :" else "Total Payable:",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
              )
              Text(
                text = if (enrollment.mode == EnrollmentMode.ONLINE) "Online Theory (BR-03 Standard)" else "Onsite Dual-Control Practical + Theory",
                style = MaterialTheme.typography.bodySmall.copy(color = AmberAccent, fontSize = 11.sp)
              )
            }
            Text(
              text = Localization.formatCurrency(courseFee, currentLocale),
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = EmeraldSuccess,
                fontSize = 19.sp
              )
            )
          }
        }

        Text(
          text = if (currentLocale == "fr") "Choisir le canal de paiement :" else "Select Payment Channel:",
          style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
        )

        // Payment Channel Selection Chips
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          // MTN MoMo
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedChannel == PaymentChannel.MTN_MOMO) Color(0xFFFFCC00).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            onClick = { selectedChannel = PaymentChannel.MTN_MOMO },
            modifier = Modifier.fillMaxWidth().testTag("channel_mtn_momo")
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedChannel == PaymentChannel.MTN_MOMO,
                onClick = { selectedChannel = PaymentChannel.MTN_MOMO }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("🟡 MTN Mobile Money (Cameroon)", fontWeight = FontWeight.SemiBold)
            }
          }

          // Orange Money
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedChannel == PaymentChannel.ORANGE_MONEY) Color(0xFFFF7900).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            onClick = { selectedChannel = PaymentChannel.ORANGE_MONEY },
            modifier = Modifier.fillMaxWidth().testTag("channel_orange_money")
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedChannel == PaymentChannel.ORANGE_MONEY,
                onClick = { selectedChannel = PaymentChannel.ORANGE_MONEY }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("🟠 Orange Money (Cameroon)", fontWeight = FontWeight.SemiBold)
            }
          }

          // Card
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedChannel == PaymentChannel.CARD) Color(0xFF0284C7).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
            onClick = { selectedChannel = PaymentChannel.CARD },
            modifier = Modifier.fillMaxWidth().testTag("channel_card")
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedChannel == PaymentChannel.CARD,
                onClick = { selectedChannel = PaymentChannel.CARD }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("💳 Visa / MasterCard (International)", fontWeight = FontWeight.SemiBold)
            }
          }
        }

        // Input based on channel
        when (selectedChannel) {
          PaymentChannel.MTN_MOMO, PaymentChannel.ORANGE_MONEY -> {
            OutlinedTextField(
              value = phoneInput,
              onValueChange = { phoneInput = it },
              label = { Text(if (currentLocale == "fr") "Numéro Mobile Money (+237)" else "Mobile Money Phone (+237)") },
              leadingIcon = { Icon(Icons.Default.PhoneIphone, contentDescription = null) },
              modifier = Modifier.fillMaxWidth().testTag("momo_phone_input"),
              singleLine = true
            )
            Text(
              text = if (currentLocale == "fr") "Un message USSD s'affichera sur votre téléphone pour valider le code PIN." else "A USSD push notification will be sent to your phone to confirm with your secret PIN.",
              style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
            )
          }
          PaymentChannel.CARD -> {
            OutlinedTextField(
              value = cardNumberInput,
              onValueChange = { cardNumberInput = it },
              label = { Text(if (currentLocale == "fr") "Numéro de Carte Bancaire" else "Credit Card Number") },
              leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
              modifier = Modifier.fillMaxWidth().testTag("card_number_input"),
              singleLine = true
            )
          }
          else -> {}
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          isProcessing = true
          onProcessPayment(
            courseFee,
            selectedChannel,
            "Online payment via $selectedChannel"
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
        modifier = Modifier.testTag("confirm_payment_submission_button")
      ) {
        if (isProcessing) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
          text = if (currentLocale == "fr") "Valider & Débloquer les Cours" else "Confirm & Unlock Course",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss, enabled = !isProcessing) {
        Text(if (currentLocale == "fr") "Annuler" else "Cancel")
      }
    }
  )
}
