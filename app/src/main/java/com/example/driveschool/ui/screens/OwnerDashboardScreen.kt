package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.*
import com.example.driveschool.ui.components.AddStudentToBranchDialog
import com.example.driveschool.ui.components.CreateBranchDialog
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@Composable
fun OwnerDashboardScreen(
  currentLocale: String,
  branches: List<BranchEntity>,
  enrollments: List<EnrollmentEntity>,
  payments: List<PaymentEntity>,
  vehicles: List<VehicleEntity>,
  candidates: List<ExamCandidateEntity>,
  insurancePolicies: List<InsurancePolicyEntity>,
  users: List<UserEntity> = emptyList(),
  courses: List<CourseEntity> = emptyList(),
  onRunDailyScan: () -> Unit,
  onNavigateTab: (String) -> Unit,
  onResetSeedData: () -> Unit = {},
  onCloudSync: () -> Unit = {},
  onCreateBranch: (
    name: String,
    city: String,
    address: String,
    phone: String,
    managerName: String,
    managerEmail: String,
    managerPhone: String,
    secretaryName: String,
    secretaryEmail: String,
    secretaryPhone: String,
    studentName: String?,
    studentEmail: String?,
    studentPhone: String?,
    courseId: String?
  ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
  onAddStudentToBranch: (name: String, email: String, phone: String, branchId: String, courseId: String) -> Unit = { _, _, _, _, _ -> },
  onSwitchUser: (UserEntity) -> Unit = {},
  onOpenBranchManagement: () -> Unit = {},
  onOpenStaffManagement: () -> Unit = {}
) {
  var showCreateBranchDialog by remember { mutableStateOf(false) }
  var branchForAddStudent by remember { mutableStateOf<BranchEntity?>(null) }
  var branchSearchQuery by remember { mutableStateOf("") }

  val filteredBranches = remember(branches, branchSearchQuery) {
    val query = branchSearchQuery.trim()
    if (query.isEmpty()) {
      branches
    } else {
      branches.filter {
        it.name.contains(query, ignoreCase = true) ||
        it.city.contains(query, ignoreCase = true) ||
        it.address.contains(query, ignoreCase = true)
      }
    }
  }

  val totalRevenue = payments.filter { it.status == PaymentStatus.CONFIRMED }.sumOf { it.amount }
  val onlineEnrollments = enrollments.count { it.mode == EnrollmentMode.ONLINE }
  val onsiteEnrollments = enrollments.count { it.mode == EnrollmentMode.ONSITE }
  val passedCandidates = candidates.count { it.status == CandidateStatus.PASSED }
  val totalSat = candidates.count { it.status == CandidateStatus.PASSED || it.status == CandidateStatus.FAILED }
  val passRate = if (totalSat > 0) (passedCandidates.toDouble() / totalSat * 100).toInt() else 94
  val vehiclesDueForService = vehicles.count { it.status == VehicleStatus.SERVICE_DUE || it.odometer >= it.nextServiceKm }
  val expiringInsuranceCount = insurancePolicies.count {
    val daysLeft = (it.expiresAt - System.currentTimeMillis()) / (24L * 3600 * 1000)
    daysLeft in 0..30
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("owner_dashboard_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header Banner
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = if (currentLocale == "fr") "Tableau de Bord Promoteur" else "Super Admin (Owner) Executive Overview",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = if (currentLocale == "fr") "Supervision nationale & KPIs multi-agences" else "National branch oversight & real-time governance",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
              )
            }
            Icon(
              imageVector = Icons.Default.Assessment,
              contentDescription = null,
              tint = AmberAccent,
              modifier = Modifier.size(32.dp)
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Daily Expiry Scan Trigger Button
          FilledTonalButton(
            onClick = onRunDailyScan,
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = AmberAccent,
              contentColor = NavyDark
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("run_daily_expiry_scan_button")
          ) {
            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Lancer le Scan Quotidien des Expirables (30/7/1 jours)" else "Run Daily Expiry Engine Scan (30/7/1 Days)",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Cloud Sync Button (Room <-> Firestore)
          Button(
            onClick = onCloudSync,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF0284C7),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("sync_cloud_firestore_button")
          ) {
            Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Synchroniser Room ⇄ Cloud Firestore (Hybride)" else "Sync Room ⇄ Cloud Firestore (Hybrid)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Branch Management Screen Button (Assign Staff & Firestore)
          Button(
            onClick = onOpenBranchManagement,
            colors = ButtonDefaults.buttonColors(
              containerColor = EmeraldSuccess,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_branch_management_screen_btn")
          ) {
            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Gérer les Agences (Personnel & Firestore)" else "Manage Branches (Assign Staff & Firestore)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Staff Management Screen Button (Assign Roles & Firestore)
          Button(
            onClick = onOpenStaffManagement,
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF7C3AED),
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("open_staff_management_screen_btn")
          ) {
            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr") "Gérer le Personnel (Affecter Rôles & Firestore)" else "Manage Staff (Assign Roles & Firestore)",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Reload / Reset Production Seed Data Button
          OutlinedButton(
            onClick = onResetSeedData,
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = Color(0xFF94A3B8)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("reset_production_seed_data_button")
          ) {
            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (currentLocale == "fr") "Réinitialiser Données Initiales de Production (5 Agences)" else "Reload Production Seed Data (5 Branches)",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }

    // Key Metrics Grid
    item {
      Text(
        text = if (currentLocale == "fr") "Indicateurs Clés de Performance" else "Key Performance Indicators",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(
          title = if (currentLocale == "fr") "Chiffre d'Affaires" else "Total Revenue",
          value = Localization.formatCurrency(totalRevenue, currentLocale),
          subtitle = if (currentLocale == "fr") "Espèces + MoMo + OM + Carte" else "Cash, MoMo, OM & Card",
          icon = Icons.Default.MonetizationOn,
          iconColor = EmeraldSuccess,
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = if (currentLocale == "fr") "Taux de Réussite" else "Exam Pass Rate",
          value = "$passRate%",
          subtitle = if (currentLocale == "fr") "$passedCandidates admis officiels" else "$passedCandidates passed candidates",
          icon = Icons.Default.WorkspacePremium,
          iconColor = AmberAccent,
          modifier = Modifier.weight(1f)
        )
      }
    }

    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricCard(
          title = if (currentLocale == "fr") "Inscriptions Actives" else "Active Enrollments",
          value = "${enrollments.size}",
          subtitle = if (currentLocale == "fr") "$onsiteEnrollments Présentiel | $onlineEnrollments En ligne" else "$onsiteEnrollments Onsite | $onlineEnrollments Online",
          icon = Icons.Default.People,
          iconColor = Color(0xFF0284C7),
          modifier = Modifier.weight(1f)
        )
        MetricCard(
          title = if (currentLocale == "fr") "Alertes Maintenance" else "Fleet Service Due",
          value = "$vehiclesDueForService",
          subtitle = if (currentLocale == "fr") "${vehicles.size} véhicules en flotte" else "${vehicles.size} vehicles total",
          icon = Icons.Default.Build,
          iconColor = if (vehiclesDueForService > 0) MaterialTheme.colorScheme.error else EmeraldSuccess,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Branch Performance Breakdown
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = if (currentLocale == "fr") "Agences & Réseau National" else "Branches & National Network",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "${branches.size} ${if (currentLocale == "fr") "agences actives" else "active branches"}",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = onOpenBranchManagement,
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("manage_branches_header_btn")
          ) {
            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(15.dp), tint = NavyPrimary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (currentLocale == "fr") "Gérer" else "Manage",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = NavyPrimary
            )
          }

          FilledTonalButton(
            onClick = { showCreateBranchDialog = true },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = AmberAccent,
              contentColor = NavyDark
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("create_new_branch_button")
          ) {
            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (currentLocale == "fr") "+ Nouvelle Agence" else "+ New Branch",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }
    }

    // Branch Search Bar
    item {
      OutlinedTextField(
        value = branchSearchQuery,
        onValueChange = { branchSearchQuery = it },
        placeholder = {
          Text(
            if (currentLocale == "fr") "Rechercher une agence par nom, ville ou adresse..."
            else "Search branches by name, city or location..."
          )
        },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary)
        },
        trailingIcon = {
          if (branchSearchQuery.isNotEmpty()) {
            IconButton(
              onClick = { branchSearchQuery = "" },
              modifier = Modifier.testTag("clear_dashboard_branch_search")
            ) {
              Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
            }
          }
        },
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = AmberAccent,
          unfocusedBorderColor = Color(0xFFCBD5E1),
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("dashboard_branch_search_input"),
        singleLine = true
      )
    }

    if (filteredBranches.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth().testTag("empty_dashboard_branches_card")
        ) {
          Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.Default.SearchOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = if (currentLocale == "fr") "Aucune agence trouvée pour '$branchSearchQuery'" else "No branches found matching '$branchSearchQuery'",
              fontSize = 13.sp,
              color = Color.Gray
            )
          }
        }
      }
    }

    items(filteredBranches) { branch ->
      val branchEnrollments = enrollments.filter { it.branchId == branch.id }
      val branchPayments = payments.filter { p ->
        branchEnrollments.any { it.id == p.enrollmentId }
      }
      val branchRev = branchPayments.sumOf { it.amount }
      val branchManager = users.find { it.branchId == branch.id && it.role == UserRole.BRANCH_MANAGER }
      val branchSecretary = users.find { it.branchId == branch.id && it.role == UserRole.SECRETARY }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().testTag("branch_card_${branch.id}")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(42.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (branch.isVirtual) Color(0xFF38BDF8) else NavyPrimary),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (branch.isVirtual) Icons.Default.Language else Icons.Default.Business,
                  contentDescription = null,
                  tint = Color.White
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = branch.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                  )
                  if (branch.isVirtual) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = AmberAccent.copy(alpha = 0.2f)
                    ) {
                      Text(
                        text = "VIRTUAL",
                        color = Color(0xFFB45309),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
                Text(
                  text = "${branch.city} • ${branch.phone}",
                  style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
              }
            }

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = Localization.formatCurrency(branchRev, currentLocale),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = EmeraldSuccess
              )
              Text(
                text = "${branchEnrollments.size} ${if (currentLocale == "fr") "élèves" else "students"}",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
              )
            }
          }

          // Dedicated Manager & Secretary Display
          if (!branch.isVirtual) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFCBD5E1).copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Dedicated Branch Manager Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "👔 ${if (currentLocale == "fr") "Chef d'Agence" else "Branch Manager"}: ${branchManager?.name ?: "Pending Appointment"}",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = NavyPrimary, fontWeight = FontWeight.Bold)
                )
                if (branchManager != null) {
                  Text(
                    text = "${branchManager.phone} • ${branchManager.email}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.Gray)
                  )
                }
              }
              if (branchManager != null) {
                OutlinedButton(
                  onClick = { onSwitchUser(branchManager) },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp).testTag("switch_to_manager_${branch.id}")
                ) {
                  Text(if (currentLocale == "fr") "Gérer" else "Log In", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dedicated Branch Secretary Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "📋 ${if (currentLocale == "fr") "Secrétaire" else "Secretary"}: ${branchSecretary?.name ?: "Pending Appointment"}",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = Color(0xFF334155), fontWeight = FontWeight.SemiBold)
                )
                if (branchSecretary != null) {
                  Text(
                    text = "${branchSecretary.phone} • ${branchSecretary.email}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.Gray)
                  )
                }
              }
              if (branchSecretary != null) {
                OutlinedButton(
                  onClick = { onSwitchUser(branchSecretary) },
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp).testTag("switch_to_sec_${branch.id}")
                ) {
                  Text(if (currentLocale == "fr") "Guichet" else "Log In", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))

            // Attached Students Section
            val branchStudents = users.filter { it.branchId == branch.id && it.role == UserRole.STUDENT }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "🎓 ${if (currentLocale == "fr") "Élèves Rattachés" else "Enrolled Students"} (${branchStudents.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = NavyDark
              )
              TextButton(
                onClick = { branchForAddStudent = branch },
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                modifier = Modifier.testTag("add_student_to_branch_${branch.id}")
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (currentLocale == "fr") "+ Inscrire Élève" else "+ Add Student",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = AmberAccent
                )
              }
            }

            if (branchStudents.isNotEmpty()) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                branchStudents.take(3).forEach { student ->
                  val studentEnr = branchEnrollments.find { it.studentId == student.id }
                  Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(
                          text = student.name,
                          fontWeight = FontWeight.SemiBold,
                          fontSize = 11.sp
                        )
                        Text(
                          text = student.email,
                          fontSize = 10.sp,
                          color = Color.Gray
                        )
                      }
                      Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (studentEnr?.status) {
                          EnrollmentStatus.ACTIVE -> EmeraldSuccess.copy(alpha = 0.15f)
                          EnrollmentStatus.ONBOARDING -> AmberAccent.copy(alpha = 0.2f)
                          else -> Color(0xFFE2E8F0)
                        }
                      ) {
                        Text(
                          text = studentEnr?.status?.name ?: "ACTIVE",
                          fontSize = 9.sp,
                          fontWeight = FontWeight.Bold,
                          color = when (studentEnr?.status) {
                            EnrollmentStatus.ACTIVE -> Color(0xFF047857)
                            EnrollmentStatus.ONBOARDING -> Color(0xFFB45309)
                            else -> Color.DarkGray
                          },
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                      }
                    }
                  }
                }
                if (branchStudents.size > 3) {
                  Text(
                    text = "+ ${branchStudents.size - 3} ${if (currentLocale == "fr") "autres élèves rattachés" else "more enrolled students"}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, color = Color.Gray)
                  )
                }
              }
            } else {
              Text(
                text = if (currentLocale == "fr") "Aucun élève encore rattaché. Cliquez sur '+ Inscrire Élève'." else "No students enrolled yet. Click '+ Add Student'.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
              )
            }
          }
        }
      }
    }

    // Expiry and Insurance Revenue Pipeline
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF1D4ED8)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (currentLocale == "fr") "Pipeline Renouvellement Assurances" else "Insurance Renewal Pipeline",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E3A8A)
              )
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (expiringInsuranceCount > 0) AmberAccent else EmeraldSuccess
            ) {
              Text(
                text = "$expiringInsuranceCount ${if (currentLocale == "fr") "à renouveler" else "expiring soon"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (currentLocale == "fr")
              "Le moteur d'alerte envoie des notifications WhatsApp/SMS automatiques à 30, 7 et 1 jour(s) avant l'échéance pour maximiser le taux de conversion en renouvellement."
            else
              "The alert engine automatically dispatches SMS/WhatsApp reminders at 30, 7, and 1 day lead times to convert administrative deadlines into renewal revenue.",
            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF3B82F6))
          )
        }
      }
    }
  }

  if (showCreateBranchDialog) {
    CreateBranchDialog(
      currentLocale = currentLocale,
      onDismiss = { showCreateBranchDialog = false },
      onCreateBranch = onCreateBranch
    )
  }

  branchForAddStudent?.let { targetBranch ->
    AddStudentToBranchDialog(
      currentLocale = currentLocale,
      branch = targetBranch,
      courses = courses,
      onDismiss = { branchForAddStudent = null },
      onAddStudent = { name, email, phone, courseId ->
        onAddStudentToBranch(name, email, phone, targetBranch.id, courseId)
      }
    )
  }
}

@Composable
fun MetricCard(
  title: String,
  value: String,
  subtitle: String,
  icon: ImageVector,
  iconColor: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp),
          maxLines = 1
        )
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconColor,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall.copy(
          color = Color(0xFF64748B),
          fontSize = 10.sp
        ),
        maxLines = 1
      )
    }
  }
}
