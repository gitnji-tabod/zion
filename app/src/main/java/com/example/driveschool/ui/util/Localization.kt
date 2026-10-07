package com.example.driveschool.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Localization {

  fun formatCurrency(amount: Double, locale: String): String {
    val formatter = NumberFormat.getNumberInstance(if (locale == "fr") Locale.FRENCH else Locale.ENGLISH)
    val formatted = formatter.format(amount.toLong())
    return if (locale == "fr") "$formatted FCFA" else "$formatted XAF"
  }

  fun formatDate(timestamp: Long, locale: String): String {
    val pattern = if (locale == "fr") "dd MMM yyyy" else "MMM dd, yyyy"
    val sdf = SimpleDateFormat(pattern, if (locale == "fr") Locale.FRENCH else Locale.ENGLISH)
    return sdf.format(Date(timestamp))
  }

  fun formatDateTime(timestamp: Long, locale: String): String {
    val pattern = if (locale == "fr") "dd MMM yyyy HH:mm" else "MMM dd, yyyy hh:mm a"
    val sdf = SimpleDateFormat(pattern, if (locale == "fr") Locale.FRENCH else Locale.ENGLISH)
    return sdf.format(Date(timestamp))
  }

  fun roleName(role: String, locale: String): String {
    return when (role) {
      "SUPER_ADMIN" -> if (locale == "fr") "Super Administrateur (Promoteur)" else "Super Admin (Owner)"
      "BRANCH_MANAGER" -> if (locale == "fr") "Chef d'Agence" else "Branch Manager"
      "SECRETARY" -> if (locale == "fr") "Secrétaire" else "Secretary"
      "INSTRUCTOR" -> if (locale == "fr") "Moniteur d'Auto-école" else "Instructor"
      "STUDENT" -> if (locale == "fr") "Élève Conducteur" else "Student"
      else -> role
    }
  }

  fun enrollmentStatusName(status: String, locale: String): String {
    return when (status) {
      "ONBOARDING" -> if (locale == "fr") "Initiation Gratuite" else "Free Onboarding"
      "AWAITING_PAYMENT" -> if (locale == "fr") "En Attente de Paiement" else "Awaiting Payment"
      "ACTIVE" -> if (locale == "fr") "Actif (Accès Complet)" else "Active (Full Access)"
      "COMPLETED" -> if (locale == "fr") "Formation Terminée" else "Completed"
      "EXPIRED" -> if (locale == "fr") "Expiré" else "Expired"
      "SUSPENDED" -> if (locale == "fr") "Suspendu" else "Suspended"
      else -> status
    }
  }

  fun candidateStatusName(status: String, locale: String): String {
    return when (status) {
      "RECOMMENDED" -> if (locale == "fr") "1. Recommandé par le Moniteur" else "1. Recommended by Instructor"
      "APPROVED" -> if (locale == "fr") "2. Validé par le Chef d'Agence" else "2. Approved by Branch Manager"
      "APPLIED" -> if (locale == "fr") "3. Dossier Déposé par Secrétaire" else "3. Applied by Secretary"
      "SAT" -> if (locale == "fr") "4. Examen Effectué" else "4. Exam Sat"
      "PASSED" -> if (locale == "fr") "5. Admis (Certificat Généré)" else "5. Passed (Certificate Issued)"
      "FAILED" -> if (locale == "fr") "Ajourné" else "Failed"
      else -> status
    }
  }

  fun attendanceStatusName(status: String, locale: String): String {
    return when (status) {
      "SCHEDULED" -> if (locale == "fr") "Programmé" else "Scheduled"
      "PRESENT" -> if (locale == "fr") "Effectué" else "Completed"
      "ABSENT" -> if (locale == "fr") "Absent" else "Absent"
      "LATE" -> if (locale == "fr") "En retard" else "Late"
      else -> status
    }
  }

  fun paymentChannelName(channel: String, locale: String): String {
    return when (channel) {
      "MTN_MOMO" -> "MTN Mobile Money"
      "ORANGE_MONEY" -> "Orange Money"
      "CARD" -> if (locale == "fr") "Carte Bancaire Internationale" else "International Card"
      "CASH" -> if (locale == "fr") "Espèces au Guichet" else "Cash at Desk"
      else -> channel
    }
  }
}
