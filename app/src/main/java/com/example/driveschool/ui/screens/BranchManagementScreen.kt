package com.example.driveschool.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.driveschool.data.model.*
import com.example.driveschool.data.sync.SyncState
import com.example.driveschool.ui.components.CreateBranchDialog
import com.example.driveschool.ui.util.Localization
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchManagementScreen(
  currentLocale: String,
  branches: List<BranchEntity>,
  users: List<UserEntity>,
  enrollments: List<EnrollmentEntity>,
  payments: List<PaymentEntity>,
  syncState: SyncState,
  onBack: () -> Unit,
  onCloudSync: () -> Unit,
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
    initialStudentName: String?,
    initialStudentEmail: String?,
    initialStudentPhone: String?,
    initialCourseId: String?
  ) -> Unit,
  onUpdateBranch: (
    branchId: String,
    name: String,
    city: String,
    address: String,
    phone: String,
    isVirtual: Boolean,
    managerUser: UserEntity?,
    secretaryUser: UserEntity?,
    newManagerDetails: Triple<String, String, String>?,
    newSecretaryDetails: Triple<String, String, String>?
  ) -> Unit,
  onDeleteBranch: (String) -> Unit,
  onPersistBranchToFirestore: (BranchEntity) -> Unit,
  onRefreshFirestore: () -> Unit = {},
  isFirestoreLoading: Boolean = false
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "PHYSICAL", "VIRTUAL", "NEEDS_STAFF"
  var branchToEdit by remember { mutableStateOf<BranchEntity?>(null) }
  var showCreateDialog by remember { mutableStateOf(false) }
  var branchToDelete by remember { mutableStateOf<BranchEntity?>(null) }

  val filteredBranches = remember(branches, searchQuery, selectedFilter, users) {
    val query = searchQuery.trim()
    branches.filter { branch ->
      val matchesSearch = query.isEmpty() ||
        branch.name.contains(query, ignoreCase = true) ||
        branch.city.contains(query, ignoreCase = true) ||
        branch.address.contains(query, ignoreCase = true)

      val manager = users.find { it.branchId == branch.id && it.role == UserRole.BRANCH_MANAGER }
      val secretary = users.find { it.branchId == branch.id && it.role == UserRole.SECRETARY }

      val matchesFilter = when (selectedFilter) {
        "PHYSICAL" -> !branch.isVirtual
        "VIRTUAL" -> branch.isVirtual
        "NEEDS_STAFF" -> !branch.isVirtual && (manager == null || secretary == null)
        else -> true
      }
      matchesSearch && matchesFilter
    }
  }

  val physicalCount = branches.count { !it.isVirtual }
  val virtualCount = branches.count { it.isVirtual }
  val branchesWithBothStaff = branches.count { b ->
    if (b.isVirtual) true else {
      val m = users.any { it.branchId == b.id && it.role == UserRole.BRANCH_MANAGER }
      val s = users.any { it.branchId == b.id && it.role == UserRole.SECRETARY }
      m && s
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (currentLocale == "fr") "Agences Cloud Firestore" else "Firestore Cloud Branches",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color.White
            )
            Text(
              text = if (currentLocale == "fr") "Liste, modification & suppression temps-réel" else "Live Firestore view with edit & delete controls",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("branch_mgmt_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(
            onClick = onRefreshFirestore,
            modifier = Modifier.testTag("branch_mgmt_refresh_firestore_btn")
          ) {
            if (isFirestoreLoading) {
              CircularProgressIndicator(
                color = AmberAccent,
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp
              )
            } else {
              Icon(Icons.Default.Refresh, contentDescription = "Refresh from Firestore", tint = Color.White)
            }
          }
          IconButton(onClick = onCloudSync, modifier = Modifier.testTag("branch_mgmt_sync_button")) {
            Icon(Icons.Default.CloudSync, contentDescription = "Sync Cloud Firestore", tint = AmberAccent)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateDialog = true },
        containerColor = AmberAccent,
        contentColor = NavyDark,
        modifier = Modifier.testTag("add_branch_fab")
      ) {
        Icon(Icons.Default.AddBusiness, contentDescription = "Create Branch")
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
        .testTag("branch_management_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item { Spacer(modifier = Modifier.height(4.dp)) }

      // Cloud Persistence Banner
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = when (syncState) {
              is SyncState.Success -> Color(0xFFECFDF5)
              is SyncState.Syncing -> Color(0xFFEFF6FF)
              is SyncState.Error -> Color(0xFFFEF2F2)
              else -> Color(0xFFF8FAFC)
            }
          ),
          modifier = Modifier.fillMaxWidth().testTag("firestore_sync_banner")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                  when (syncState) {
                    is SyncState.Success -> EmeraldSuccess.copy(alpha = 0.2f)
                    is SyncState.Syncing -> Color(0xFF38BDF8).copy(alpha = 0.2f)
                    else -> AmberAccent.copy(alpha = 0.2f)
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when (syncState) {
                  is SyncState.Success -> Icons.Default.CloudDone
                  is SyncState.Syncing -> Icons.Default.CloudSync
                  else -> Icons.Default.Cloud
                },
                contentDescription = null,
                tint = when (syncState) {
                  is SyncState.Success -> EmeraldSuccess
                  is SyncState.Syncing -> Color(0xFF0284C7)
                  else -> Color(0xFFB45309)
                },
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (currentLocale == "fr") "Persistance Hybride Cloud Firestore" else "Cloud Firestore Persistence Active",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NavyDark
              )
              Text(
                text = if (currentLocale == "fr")
                  "Toute agence créée ou modifiée avec ses responsables est synchronisée sur la collection Firestore 'branches'."
                else
                  "Every branch and assigned staff member is stored locally in Room and persisted to Firestore '/branches'.",
                style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569), fontSize = 11.sp)
              )
            }
          }
        }
      }

      // Quick Summary Cards
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = NavyPrimary,
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "${branches.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
              )
              Text(
                text = if (currentLocale == "fr") "Total Agences" else "Total Branches",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )
              Text(
                text = "$physicalCount ${if (currentLocale == "fr") "physiques" else "onsite"} • $virtualCount web",
                fontSize = 10.sp,
                color = AmberAccent
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F766E),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "$branchesWithBothStaff / ${branches.size}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.White
              )
              Text(
                text = if (currentLocale == "fr") "Personnel Affecté" else "Staff Assigned",
                fontSize = 11.sp,
                color = Color(0xFF99F6E4)
              )
              Text(
                text = if (currentLocale == "fr") "Managers + Secrétaires" else "Managers & Secretaries",
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
              )
            }
          }
        }
      }

      // Search and Filter Bar
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (currentLocale == "fr") "Recherche d'Agences :" else "Search Branches by Name or Location:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
          )
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                if (currentLocale == "fr") "Rechercher par nom d'agence, ville ou adresse..."
                else "Filter branches by name or location (city, street)..."
              )
            },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(
                  onClick = { searchQuery = "" },
                  modifier = Modifier.testTag("clear_branch_search_btn")
                ) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                }
              }
            },
            supportingText = {
              if (searchQuery.isNotBlank()) {
                Text(
                  text = if (currentLocale == "fr")
                    "${filteredBranches.size} agence(s) trouvée(s) pour '$searchQuery'"
                  else
                    "Found ${filteredBranches.size} branch(es) matching '$searchQuery'",
                  color = EmeraldSuccess,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = 11.sp
                )
              }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = AmberAccent,
              unfocusedBorderColor = Color(0xFFCBD5E1),
              focusedContainerColor = Color.White,
              unfocusedContainerColor = Color(0xFFF8FAFC)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("branch_search_input"),
            singleLine = true
          )
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = selectedFilter == "ALL",
            onClick = { selectedFilter = "ALL" },
            label = { Text("All (${branches.size})", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_all_branches")
          )
          FilterChip(
            selected = selectedFilter == "PHYSICAL",
            onClick = { selectedFilter = "PHYSICAL" },
            label = { Text("Physical ($physicalCount)", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_physical_branches")
          )
          FilterChip(
            selected = selectedFilter == "VIRTUAL",
            onClick = { selectedFilter = "VIRTUAL" },
            label = { Text("Online ($virtualCount)", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_virtual_branches")
          )
          FilterChip(
            selected = selectedFilter == "NEEDS_STAFF",
            onClick = { selectedFilter = "NEEDS_STAFF" },
            label = { Text("Needs Staff", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_needs_staff")
          )
        }
      }

      // List of Branch Cards
      items(filteredBranches) { branch ->
        val branchEnrollments = enrollments.filter { it.branchId == branch.id }
        val branchPayments = payments.filter { p -> branchEnrollments.any { it.id == p.enrollmentId } }
        val branchRevenue = branchPayments.sumOf { it.amount }
        val manager = users.find { it.branchId == branch.id && it.role == UserRole.BRANCH_MANAGER }
        val secretary = users.find { it.branchId == branch.id && it.role == UserRole.SECRETARY }

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("managed_branch_card_${branch.id}")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Icon, Name, Type, Revenue
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (branch.isVirtual) Color(0xFF0284C7) else NavyDark),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (branch.isVirtual) Icons.Default.CloudQueue else Icons.Default.Storefront,
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
                      fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = if (branch.isVirtual) Color(0xFFE0F2FE) else AmberAccent.copy(alpha = 0.2f)
                    ) {
                      Text(
                        text = if (branch.isVirtual) "ONLINE" else "ONSITE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (branch.isVirtual) Color(0xFF0369A1) else Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                      )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = Color(0xFFFEF3C7)
                    ) {
                      Text(
                        text = "Firestore: ${branch.id}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF92400E),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }
                  Text(
                    text = "${branch.city} • ${branch.phone}",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                  )
                }
              }

              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = Localization.formatCurrency(branchRevenue, currentLocale),
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = EmeraldSuccess
                )
                Text(
                  text = "${branchEnrollments.size} ${if (currentLocale == "fr") "élèves" else "students"}",
                  fontSize = 10.sp,
                  color = Color.Gray
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Location Box
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF8FAFC),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "${branch.address}, ${branch.city}",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF334155))
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Staff Cards
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              // Manager Box
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (manager != null) Color(0xFFEEF2FF) else Color(0xFFFFFBEB),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.SupervisorAccount,
                      contentDescription = null,
                      tint = if (manager != null) NavyPrimary else Color(0xFFB45309),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (currentLocale == "fr") "Chef d'Agence" else "Manager",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (manager != null) NavyPrimary else Color(0xFFB45309)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = manager?.name ?: (if (currentLocale == "fr") "Non assigné" else "Unassigned"),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    maxLines = 1
                  )
                  Text(
                    text = manager?.phone ?: (if (currentLocale == "fr") "À pourvoir" else "Pending"),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1
                  )
                }
              }

              // Secretary Box
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (secretary != null) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                modifier = Modifier.weight(1f)
              ) {
                Column(modifier = Modifier.padding(10.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.Desk,
                      contentDescription = null,
                      tint = if (secretary != null) Color(0xFF15803D) else Color(0xFFB45309),
                      modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (currentLocale == "fr") "Secrétaire" else "Secretary",
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (secretary != null) Color(0xFF15803D) else Color(0xFFB45309)
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = secretary?.name ?: (if (currentLocale == "fr") "Non assigné" else "Unassigned"),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    maxLines = 1
                  )
                  Text(
                    text = secretary?.phone ?: (if (currentLocale == "fr") "À pourvoir" else "Pending"),
                    fontSize = 10.sp,
                    color = Color.Gray,
                    maxLines = 1
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Edit, Sync, Delete
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Edit Option
              FilledTonalButton(
                onClick = { branchToEdit = branch },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = NavyDark, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("edit_branch_btn_${branch.id}")
              ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (currentLocale == "fr") "Modifier" else "Edit",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Direct Firestore Push Shortcut
                FilledTonalButton(
                  onClick = { onPersistBranchToFirestore(branch) },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFE0F2FE),
                    contentColor = Color(0xFF0369A1)
                  ),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("sync_branch_${branch.id}")
                ) {
                  Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = "Persist to Firestore",
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (currentLocale == "fr") "Pousser" else "Sync",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                // Delete Option (Available for all entries)
                OutlinedButton(
                  onClick = { branchToDelete = branch },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                  ),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("delete_branch_${branch.id}")
                ) {
                  Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = "Delete Branch",
                    modifier = Modifier.size(15.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (currentLocale == "fr") "Supprimer" else "Delete",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }

      // Empty State
      if (filteredBranches.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).testTag("empty_branches_card")
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                Icons.Default.Storefront,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (currentLocale == "fr") "Aucune agence trouvée" else "No Branches Found",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (currentLocale == "fr")
                  "Aucune agence ne correspond à votre recherche ou filtre."
                else
                  "No branch entries matched your search query or filter.",
                fontSize = 12.sp,
                color = Color.Gray
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = NavyDark),
                modifier = Modifier.testTag("create_branch_empty_state_btn")
              ) {
                Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (currentLocale == "fr") "Créer une Agence dans Firestore" else "Create Branch in Firestore",
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      item { Spacer(modifier = Modifier.height(60.dp)) }
    }
  }

  // Edit / Manage Staff Dialog
  branchToEdit?.let { currentBranch ->
    BranchEditorModal(
      currentLocale = currentLocale,
      branch = currentBranch,
      users = users,
      onDismiss = { branchToEdit = null },
      onSave = { name, city, address, phone, isVirtual, mUser, sUser, newMDetails, newSDetails ->
        onUpdateBranch(
          currentBranch.id,
          name,
          city,
          address,
          phone,
          isVirtual,
          mUser,
          sUser,
          newMDetails,
          newSDetails
        )
        branchToEdit = null
      }
    )
  }

  // Create New Branch Dialog
  if (showCreateDialog) {
    CreateBranchDialog(
      currentLocale = currentLocale,
      onDismiss = { showCreateDialog = false },
      onCreateBranch = { name, city, address, phone, mName, mEmail, mPhone, sName, sEmail, sPhone, stName, stEmail, stPhone, courseId ->
        onCreateBranch(name, city, address, phone, mName, mEmail, mPhone, sName, sEmail, sPhone, stName, stEmail, stPhone, courseId)
        showCreateDialog = false
      }
    )
  }

  // Delete Confirmation Dialog
  branchToDelete?.let { branch ->
    AlertDialog(
      onDismissRequest = { branchToDelete = null },
      title = { Text(if (currentLocale == "fr") "Supprimer l'Agence ?" else "Delete Branch from Firestore?") },
      text = {
        Text(
          if (currentLocale == "fr")
            "Êtes-vous sûr de vouloir supprimer l'agence '${branch.name}' (ID: ${branch.id}) ? Cette entrée sera définitivement supprimée de Cloud Firestore (collection 'branches') et de la base de données locale."
          else
            "Are you sure you want to delete '${branch.name}' (ID: ${branch.id})? This entry will be permanently deleted from Cloud Firestore ('branches' collection) and local storage."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteBranch(branch.id)
            branchToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_branch_btn")
        ) {
          Text(if (currentLocale == "fr") "Supprimer de Firestore" else "Delete from Firestore")
        }
      },
      dismissButton = {
        TextButton(onClick = { branchToDelete = null }) {
          Text(if (currentLocale == "fr") "Annuler" else "Cancel")
        }
      }
    )
  }
}

/**
 * Comprehensive Branch Editor Modal for modifying coordinates and assigning Manager & Secretary.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BranchEditorModal(
  currentLocale: String,
  branch: BranchEntity,
  users: List<UserEntity>,
  onDismiss: () -> Unit,
  onSave: (
    name: String,
    city: String,
    address: String,
    phone: String,
    isVirtual: Boolean,
    managerUser: UserEntity?,
    secretaryUser: UserEntity?,
    newManagerDetails: Triple<String, String, String>?,
    newSecretaryDetails: Triple<String, String, String>?
  ) -> Unit
) {
  var name by remember { mutableStateOf(branch.name) }
  var city by remember { mutableStateOf(branch.city) }
  var address by remember { mutableStateOf(branch.address) }
  var phone by remember { mutableStateOf(branch.phone) }
  var isVirtual by remember { mutableStateOf(branch.isVirtual) }

  // Manager Assignment State
  val currentManager = remember(users, branch) {
    users.find { it.branchId == branch.id && it.role == UserRole.BRANCH_MANAGER }
  }
  var managerMode by remember { mutableStateOf("EXISTING") } // "EXISTING" or "NEW"
  var selectedManagerUser by remember { mutableStateOf(currentManager) }
  var newManagerName by remember { mutableStateOf("") }
  var newManagerEmail by remember { mutableStateOf("") }
  var newManagerPhone by remember { mutableStateOf("+237 ") }
  var managerMenuExpanded by remember { mutableStateOf(false) }

  // Secretary Assignment State
  val currentSecretary = remember(users, branch) {
    users.find { it.branchId == branch.id && it.role == UserRole.SECRETARY }
  }
  var secretaryMode by remember { mutableStateOf("EXISTING") } // "EXISTING" or "NEW"
  var selectedSecretaryUser by remember { mutableStateOf(currentSecretary) }
  var newSecretaryName by remember { mutableStateOf("") }
  var newSecretaryEmail by remember { mutableStateOf("") }
  var newSecretaryPhone by remember { mutableStateOf("+237 ") }
  var secretaryMenuExpanded by remember { mutableStateOf(false) }

  val eligibleManagers = remember(users) {
    users.filter { it.role == UserRole.BRANCH_MANAGER || it.role == UserRole.SUPER_ADMIN }
  }
  val eligibleSecretaries = remember(users) {
    users.filter { it.role == UserRole.SECRETARY || it.role == UserRole.STUDENT }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.EditNote, contentDescription = null, tint = AmberAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Modifier l'Agence & Affecter le Personnel" else "Edit Branch & Assign Staff",
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Firestore Persistence Notice
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFF0FDF4),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (currentLocale == "fr")
                "Les modifications seront enregistrées en local et persistées instantanément dans Firestore."
              else
                "Changes will be stored in Room and pushed immediately to Cloud Firestore collection '/branches'.",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color(0xFF166534))
            )
          }
        }

        // Section 1: Coordinates
        Text(
          text = if (currentLocale == "fr") "1. Coordonnées de l'Agence :" else "1. Branch Coordinates:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (currentLocale == "fr") "Nom de l'Agence" else "Branch Name") },
          modifier = Modifier.fillMaxWidth().testTag("edit_branch_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = city,
          onValueChange = { city = it },
          label = { Text(if (currentLocale == "fr") "Ville / Région" else "City / Region") },
          modifier = Modifier.fillMaxWidth().testTag("edit_branch_city_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = address,
          onValueChange = { address = it },
          label = { Text(if (currentLocale == "fr") "Adresse Physique" else "Physical Address") },
          modifier = Modifier.fillMaxWidth().testTag("edit_branch_address_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(if (currentLocale == "fr") "Téléphone" else "Phone Number") },
          modifier = Modifier.fillMaxWidth().testTag("edit_branch_phone_input"),
          singleLine = true
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (currentLocale == "fr") "Campus Virtuel (100% En Ligne)" else "Virtual Campus (Online)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )
          Switch(
            checked = isVirtual,
            onCheckedChange = { isVirtual = it },
            modifier = Modifier.testTag("edit_branch_virtual_switch")
          )
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        // Section 2: Manager Assignment
        Text(
          text = if (currentLocale == "fr") "2. Chef d'Agence (Manager) :" else "2. Branch Manager Assignment:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = managerMode == "EXISTING",
            onClick = { managerMode = "EXISTING" },
            label = { Text(if (currentLocale == "fr") "Choisir Existant" else "Existing Staff", fontSize = 11.sp) },
            modifier = Modifier.weight(1f).testTag("manager_mode_existing")
          )
          FilterChip(
            selected = managerMode == "NEW",
            onClick = { managerMode = "NEW" },
            label = { Text(if (currentLocale == "fr") "+ Nouveau Manager" else "+ New Manager", fontSize = 11.sp) },
            modifier = Modifier.weight(1f).testTag("manager_mode_new")
          )
        }

        if (managerMode == "EXISTING") {
          Box {
            OutlinedButton(
              onClick = { managerMenuExpanded = true },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("select_manager_dropdown_btn")
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = selectedManagerUser?.let { "${it.name} (${it.email})" }
                    ?: (if (currentLocale == "fr") "Sélectionner un Manager" else "Select Manager"),
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 12.sp
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
              }
            }
            DropdownMenu(
              expanded = managerMenuExpanded,
              onDismissRequest = { managerMenuExpanded = false }
            ) {
              eligibleManagers.forEach { user ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                      Text(text = "${user.role.name} • ${user.email}", fontSize = 10.sp, color = Color.Gray)
                    }
                  },
                  onClick = {
                    selectedManagerUser = user
                    managerMenuExpanded = false
                  }
                )
              }
            }
          }
        } else {
          OutlinedTextField(
            value = newManagerName,
            onValueChange = { newManagerName = it },
            label = { Text(if (currentLocale == "fr") "Nom du Nouveau Manager" else "New Manager Name") },
            modifier = Modifier.fillMaxWidth().testTag("new_manager_name_field"),
            singleLine = true
          )
          OutlinedTextField(
            value = newManagerEmail,
            onValueChange = { newManagerEmail = it },
            label = { Text("Manager Email") },
            modifier = Modifier.fillMaxWidth().testTag("new_manager_email_field"),
            singleLine = true
          )
          OutlinedTextField(
            value = newManagerPhone,
            onValueChange = { newManagerPhone = it },
            label = { Text(if (currentLocale == "fr") "Téléphone (+237)" else "Phone Number") },
            modifier = Modifier.fillMaxWidth().testTag("new_manager_phone_field"),
            singleLine = true
          )
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        // Section 3: Secretary Assignment
        Text(
          text = if (currentLocale == "fr") "3. Secrétaire d'Agence :" else "3. Branch Secretary Assignment:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = secretaryMode == "EXISTING",
            onClick = { secretaryMode = "EXISTING" },
            label = { Text(if (currentLocale == "fr") "Choisir Existant" else "Existing Staff", fontSize = 11.sp) },
            modifier = Modifier.weight(1f).testTag("sec_mode_existing")
          )
          FilterChip(
            selected = secretaryMode == "NEW",
            onClick = { secretaryMode = "NEW" },
            label = { Text(if (currentLocale == "fr") "+ Nouveau Secrétaire" else "+ New Secretary", fontSize = 11.sp) },
            modifier = Modifier.weight(1f).testTag("sec_mode_new")
          )
        }

        if (secretaryMode == "EXISTING") {
          Box {
            OutlinedButton(
              onClick = { secretaryMenuExpanded = true },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth().testTag("select_sec_dropdown_btn")
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = selectedSecretaryUser?.let { "${it.name} (${it.email})" }
                    ?: (if (currentLocale == "fr") "Sélectionner un Secrétaire" else "Select Secretary"),
                  color = MaterialTheme.colorScheme.onSurface,
                  fontSize = 12.sp
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
              }
            }
            DropdownMenu(
              expanded = secretaryMenuExpanded,
              onDismissRequest = { secretaryMenuExpanded = false }
            ) {
              eligibleSecretaries.forEach { user ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(text = user.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                      Text(text = "${user.role.name} • ${user.email}", fontSize = 10.sp, color = Color.Gray)
                    }
                  },
                  onClick = {
                    selectedSecretaryUser = user
                    secretaryMenuExpanded = false
                  }
                )
              }
            }
          }
        } else {
          OutlinedTextField(
            value = newSecretaryName,
            onValueChange = { newSecretaryName = it },
            label = { Text(if (currentLocale == "fr") "Nom du Nouveau Secrétaire" else "New Secretary Name") },
            modifier = Modifier.fillMaxWidth().testTag("new_sec_name_field"),
            singleLine = true
          )
          OutlinedTextField(
            value = newSecretaryEmail,
            onValueChange = { newSecretaryEmail = it },
            label = { Text("Secretary Email") },
            modifier = Modifier.fillMaxWidth().testTag("new_sec_email_field"),
            singleLine = true
          )
          OutlinedTextField(
            value = newSecretaryPhone,
            onValueChange = { newSecretaryPhone = it },
            label = { Text(if (currentLocale == "fr") "Téléphone (+237)" else "Phone Number") },
            modifier = Modifier.fillMaxWidth().testTag("new_sec_phone_field"),
            singleLine = true
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && city.isNotBlank()) {
            val mDetails = if (managerMode == "NEW" && newManagerName.isNotBlank()) {
              Triple(
                newManagerName,
                newManagerEmail.ifBlank { "manager.${city.lowercase().replace(" ", "")}@driveschool.cm" },
                newManagerPhone
              )
            } else null

            val sDetails = if (secretaryMode == "NEW" && newSecretaryName.isNotBlank()) {
              Triple(
                newSecretaryName,
                newSecretaryEmail.ifBlank { "secretary.${city.lowercase().replace(" ", "")}@driveschool.cm" },
                newSecretaryPhone
              )
            } else null

            onSave(
              name,
              city,
              address,
              phone,
              isVirtual,
              if (managerMode == "EXISTING") selectedManagerUser else null,
              if (secretaryMode == "EXISTING") selectedSecretaryUser else null,
              mDetails,
              sDetails
            )
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
        modifier = Modifier.testTag("submit_edit_branch_btn")
      ) {
        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = if (currentLocale == "fr") "Enregistrer & Persister Firestore" else "Save & Persist to Firestore",
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (currentLocale == "fr") "Annuler" else "Cancel")
      }
    }
  )
}
