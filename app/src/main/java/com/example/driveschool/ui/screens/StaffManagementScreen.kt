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
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffManagementScreen(
  currentLocale: String,
  users: List<UserEntity>,
  branches: List<BranchEntity>,
  syncState: SyncState,
  onBack: () -> Unit,
  onCloudSync: () -> Unit,
  onCreateStaff: (
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?,
    countryCode: String,
    preferredLocale: String
  ) -> Unit,
  onUpdateStaff: (
    userId: String,
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?
  ) -> Unit,
  onDeleteStaff: (userId: String) -> Unit,
  onSwitchUser: (UserEntity) -> Unit = {}
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedRoleFilter by remember { mutableStateOf("ALL") } // "ALL", "BRANCH_MANAGER", "INSTRUCTOR", "SECRETARY", "UNASSIGNED"
  var staffToEdit by remember { mutableStateOf<UserEntity?>(null) }
  var staffToDelete by remember { mutableStateOf<UserEntity?>(null) }
  var showCreateDialog by remember { mutableStateOf(false) }

  // Exclude student and super-admin for pure staff management view, but keep all operational roles
  val staffList = remember(users) {
    users.filter { it.role == UserRole.BRANCH_MANAGER || it.role == UserRole.INSTRUCTOR || it.role == UserRole.SECRETARY }
  }

  val filteredStaff = remember(staffList, searchQuery, selectedRoleFilter, branches) {
    val query = searchQuery.trim()
    staffList.filter { staff ->
      val branch = branches.find { it.id == staff.branchId }
      val branchName = branch?.name ?: ""
      val branchCity = branch?.city ?: ""

      val matchesSearch = query.isEmpty() ||
        staff.name.contains(query, ignoreCase = true) ||
        staff.email.contains(query, ignoreCase = true) ||
        staff.phone.contains(query, ignoreCase = true) ||
        branchName.contains(query, ignoreCase = true) ||
        branchCity.contains(query, ignoreCase = true)

      val matchesRole = when (selectedRoleFilter) {
        "BRANCH_MANAGER" -> staff.role == UserRole.BRANCH_MANAGER
        "INSTRUCTOR" -> staff.role == UserRole.INSTRUCTOR
        "SECRETARY" -> staff.role == UserRole.SECRETARY
        "UNASSIGNED" -> staff.branchId.isNullOrBlank()
        else -> true
      }

      matchesSearch && matchesRole
    }
  }

  val managerCount = staffList.count { it.role == UserRole.BRANCH_MANAGER }
  val instructorCount = staffList.count { it.role == UserRole.INSTRUCTOR }
  val secretaryCount = staffList.count { it.role == UserRole.SECRETARY }
  val unassignedCount = staffList.count { it.branchId.isNullOrBlank() }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = if (currentLocale == "fr") "Gestion du Personnel" else "Staff Management & Roles",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color.White
            )
            Text(
              text = if (currentLocale == "fr") "Création, rôles & affectation d'agences • Firestore" else "Create, edit & assign staff roles • Stored in Firestore",
              fontSize = 11.sp,
              color = Color(0xFF94A3B8)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("staff_mgmt_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
          }
        },
        actions = {
          IconButton(onClick = onCloudSync, modifier = Modifier.testTag("staff_mgmt_sync_btn")) {
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
        modifier = Modifier.testTag("add_staff_fab")
      ) {
        Icon(Icons.Default.PersonAdd, contentDescription = "Add Staff Member")
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
        .testTag("staff_management_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item { Spacer(modifier = Modifier.height(4.dp)) }

      // Firestore Cloud Status Banner
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
          modifier = Modifier.fillMaxWidth().testTag("staff_firestore_sync_banner")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(EmeraldSuccess.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = null,
                tint = EmeraldSuccess,
                modifier = Modifier.size(20.dp)
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (currentLocale == "fr") "Personnel Persisté dans Cloud Firestore" else "Staff Persisted to Cloud Firestore",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = NavyDark
              )
              Text(
                text = if (currentLocale == "fr")
                  "Chaque membre du personnel (Manager, Moniteur, Secrétaire) est synchronisé dans la collection Firestore 'users' avec son agence de rattachement."
                else
                  "Staff records (Managers, Instructors, Secretaries) are synchronized to Firestore collection 'users' with their assigned branch.",
                fontSize = 11.sp,
                color = Color(0xFF475569)
              )
            }
          }
        }
      }

      // Quick Role Metrics Grid
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Managers Card
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = NavyPrimary,
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.SupervisorAccount, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$managerCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
              }
              Text(
                text = if (currentLocale == "fr") "Chefs d'Agence" else "Managers",
                fontSize = 10.sp,
                color = Color(0xFFCBD5E1)
              )
            }
          }

          // Instructors Card
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFB45309),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DriveEta, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$instructorCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
              }
              Text(
                text = if (currentLocale == "fr") "Moniteurs" else "Instructors",
                fontSize = 10.sp,
                color = Color(0xFFFEF3C7)
              )
            }
          }

          // Secretaries Card
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF0F766E),
            modifier = Modifier.weight(1f)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Desk, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "$secretaryCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
              }
              Text(
                text = if (currentLocale == "fr") "Secrétaires" else "Secretaries",
                fontSize = 10.sp,
                color = Color(0xFFCCFBF1)
              )
            }
          }
        }
      }

      // Search and Filter Bar
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (currentLocale == "fr") "Recherche & Filtrage de Personnel :" else "Search Staff by Name, Contact or Branch:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = NavyPrimary)
          )
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
              Text(
                if (currentLocale == "fr") "Rechercher par nom, email, téléphone ou agence..."
                else "Search staff by name, email, phone or branch..."
              )
            },
            leadingIcon = {
              Icon(Icons.Default.Search, contentDescription = "Search", tint = NavyPrimary)
            },
            trailingIcon = {
              if (searchQuery.isNotEmpty()) {
                IconButton(
                  onClick = { searchQuery = "" },
                  modifier = Modifier.testTag("clear_staff_search_btn")
                ) {
                  Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = Color.Gray)
                }
              }
            },
            supportingText = {
              if (searchQuery.isNotBlank()) {
                Text(
                  text = if (currentLocale == "fr")
                    "${filteredStaff.size} collaborateur(s) trouvé(s) pour '$searchQuery'"
                  else
                    "Found ${filteredStaff.size} staff member(s) matching '$searchQuery'",
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
              .testTag("staff_search_input"),
            singleLine = true
          )
        }
      }

      // Filter Chips
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = selectedRoleFilter == "ALL",
            onClick = { selectedRoleFilter = "ALL" },
            label = { Text("All (${staffList.size})", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_staff_all")
          )
          FilterChip(
            selected = selectedRoleFilter == "BRANCH_MANAGER",
            onClick = { selectedRoleFilter = "BRANCH_MANAGER" },
            label = { Text("Managers ($managerCount)", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_staff_managers")
          )
          FilterChip(
            selected = selectedRoleFilter == "INSTRUCTOR",
            onClick = { selectedRoleFilter = "INSTRUCTOR" },
            label = { Text("Instructors ($instructorCount)", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_staff_instructors")
          )
          FilterChip(
            selected = selectedRoleFilter == "SECRETARY",
            onClick = { selectedRoleFilter = "SECRETARY" },
            label = { Text("Secretaries ($secretaryCount)", fontSize = 11.sp) },
            modifier = Modifier.testTag("filter_staff_secretaries")
          )
          if (unassignedCount > 0) {
            FilterChip(
              selected = selectedRoleFilter == "UNASSIGNED",
              onClick = { selectedRoleFilter = "UNASSIGNED" },
              label = { Text("Unassigned ($unassignedCount)", fontSize = 11.sp) },
              modifier = Modifier.testTag("filter_staff_unassigned")
            )
          }
        }
      }

      // List of Staff Cards
      items(filteredStaff) { staff ->
        val assignedBranch = branches.find { it.id == staff.branchId }

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          modifier = Modifier.fillMaxWidth().testTag("staff_card_${staff.id}")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Name, Role Badge, Firestore ID
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Avatar circle
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                      when (staff.role) {
                        UserRole.BRANCH_MANAGER -> NavyPrimary
                        UserRole.INSTRUCTOR -> Color(0xFFB45309)
                        UserRole.SECRETARY -> Color(0xFF0F766E)
                        else -> NavyDark
                      }
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = staff.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString(""),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = staff.name,
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp
                    )
                  }

                  Spacer(modifier = Modifier.height(2.dp))

                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Role Badge
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = when (staff.role) {
                        UserRole.BRANCH_MANAGER -> Color(0xFFEEF2FF)
                        UserRole.INSTRUCTOR -> Color(0xFFFEF3C7)
                        UserRole.SECRETARY -> Color(0xFFF0FDF4)
                        else -> Color(0xFFF1F5F9)
                      }
                    ) {
                      Text(
                        text = when (staff.role) {
                          UserRole.BRANCH_MANAGER -> if (currentLocale == "fr") "👔 CHEF D'AGENCE" else "👔 MANAGER"
                          UserRole.INSTRUCTOR -> if (currentLocale == "fr") "🚗 MONITEUR" else "🚗 INSTRUCTOR"
                          UserRole.SECRETARY -> if (currentLocale == "fr") "📋 SECRÉTAIRE" else "📋 SECRETARY"
                          else -> staff.role.name
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (staff.role) {
                          UserRole.BRANCH_MANAGER -> NavyPrimary
                          UserRole.INSTRUCTOR -> Color(0xFFB45309)
                          UserRole.SECRETARY -> Color(0xFF15803D)
                          else -> Color.DarkGray
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                      )
                    }

                    // Firestore doc badge
                    Surface(
                      shape = RoundedCornerShape(4.dp),
                      color = Color(0xFFF8FAFC)
                    ) {
                      Text(
                        text = "Firestore: ${staff.id.take(14)}",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact details
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF8FAFC),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Email, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = staff.email, fontSize = 11.sp, color = Color(0xFF334155))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Phone, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(text = staff.phone, fontSize = 11.sp, color = Color(0xFF334155))
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Assigned Branch Box
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (assignedBranch != null) Color(0xFFEFF6FF) else Color(0xFFFFFBEB),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = if (assignedBranch != null) Icons.Default.Storefront else Icons.Default.WarningAmber,
                  contentDescription = null,
                  tint = if (assignedBranch != null) Color(0xFF0284C7) else Color(0xFFB45309),
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(
                    text = if (assignedBranch != null) {
                      "${if (currentLocale == "fr") "Agence affectée : " else "Assigned Branch: "}${assignedBranch.name} (${assignedBranch.city})"
                    } else {
                      if (currentLocale == "fr") "Aucune agence affectée (Non rattaché)" else "No Branch Assigned (Float Staff)"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (assignedBranch != null) Color(0xFF0369A1) else Color(0xFFB45309)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Edit Role & Branch
              FilledTonalButton(
                onClick = { staffToEdit = staff },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = NavyDark, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("edit_staff_btn_${staff.id}")
              ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (currentLocale == "fr") "Modifier Rôle & Agence" else "Edit Role & Branch",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Quick Switch / Test Persona
                FilledTonalButton(
                  onClick = { onSwitchUser(staff) },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AmberAccent.copy(alpha = 0.2f),
                    contentColor = Color(0xFF92400E)
                  ),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("switch_staff_btn_${staff.id}")
                ) {
                  Icon(Icons.Default.SwitchAccount, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = if (currentLocale == "fr") "Tester" else "View As",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }

                // Delete Staff Member
                OutlinedButton(
                  onClick = { staffToDelete = staff },
                  shape = RoundedCornerShape(8.dp),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                  modifier = Modifier.testTag("delete_staff_btn_${staff.id}")
                ) {
                  Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", modifier = Modifier.size(15.dp))
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
      if (filteredStaff.isEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp).testTag("empty_staff_card")
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.PersonOff, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = if (currentLocale == "fr") "Aucun personnel trouvé" else "No Staff Members Found",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = if (currentLocale == "fr")
                  "Aucun collaborateur ne correspond à votre filtre ou terme de recherche."
                else
                  "No staff member matched your search query or role filter.",
                fontSize = 12.sp,
                color = Color.Gray
              )
              Spacer(modifier = Modifier.height(16.dp))
              Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = NavyDark),
                modifier = Modifier.testTag("create_staff_empty_state_btn")
              ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = if (currentLocale == "fr") "Créer un Membre du Personnel" else "Create Staff Member",
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

  // Create Staff Member Dialog
  if (showCreateDialog) {
    CreateStaffDialog(
      currentLocale = currentLocale,
      branches = branches,
      onDismiss = { showCreateDialog = false },
      onSave = { name, email, phone, role, branchId, country, locale ->
        onCreateStaff(name, email, phone, role, branchId, country, locale)
        showCreateDialog = false
      }
    )
  }

  // Edit Staff Member Dialog
  staffToEdit?.let { staff ->
    EditStaffDialog(
      currentLocale = currentLocale,
      staff = staff,
      branches = branches,
      onDismiss = { staffToEdit = null },
      onSave = { name, email, phone, role, branchId ->
        onUpdateStaff(staff.id, name, email, phone, role, branchId)
        staffToEdit = null
      }
    )
  }

  // Delete Confirmation Dialog
  staffToDelete?.let { staff ->
    AlertDialog(
      onDismissRequest = { staffToDelete = null },
      title = { Text(if (currentLocale == "fr") "Supprimer ce collaborateur ?" else "Delete Staff Member from Firestore?") },
      text = {
        Text(
          if (currentLocale == "fr")
            "Êtes-vous sûr de vouloir supprimer '${staff.name}' (${staff.role.name}) ? Ce compte sera définitivement retiré de Cloud Firestore (collection 'users') et de la base de données locale."
          else
            "Are you sure you want to delete '${staff.name}' (${staff.role.name})? This record will be permanently deleted from Cloud Firestore ('users' collection) and local storage."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteStaff(staff.id)
            staffToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_delete_staff_btn")
        ) {
          Text(if (currentLocale == "fr") "Supprimer de Firestore" else "Delete from Firestore")
        }
      },
      dismissButton = {
        TextButton(onClick = { staffToDelete = null }) {
          Text(if (currentLocale == "fr") "Annuler" else "Cancel")
        }
      }
    )
  }
}

/**
 * Modal dialog to create a new Staff Member and assign Role & Branch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateStaffDialog(
  currentLocale: String,
  branches: List<BranchEntity>,
  onDismiss: () -> Unit,
  onSave: (
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?,
    countryCode: String,
    preferredLocale: String
  ) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+237 ") }
  var selectedRole by remember { mutableStateOf(UserRole.BRANCH_MANAGER) }
  var selectedBranchId by remember { mutableStateOf<String?>(branches.firstOrNull()?.id) }
  var preferredLocale by remember { mutableStateOf(currentLocale) }
  var branchMenuExpanded by remember { mutableStateOf(false) }

  val selectedBranch = branches.find { it.id == selectedBranchId }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = AmberAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Nouveau Membre du Personnel" else "Create Staff Member",
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
          Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (currentLocale == "fr")
                "Le profil et son rôle seront stockés dans Cloud Firestore (collection 'users')."
              else
                "Staff credentials and role will be pushed to Cloud Firestore collection 'users'.",
              fontSize = 11.sp,
              color = Color(0xFF166534)
            )
          }
        }

        // Section 1: Staff Details
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (currentLocale == "fr") "Nom Complet *" else "Full Name *") },
          modifier = Modifier.fillMaxWidth().testTag("staff_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text(if (currentLocale == "fr") "Adresse Email *" else "Email Address *") },
          modifier = Modifier.fillMaxWidth().testTag("staff_email_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(if (currentLocale == "fr") "Numéro de Téléphone *" else "Phone Number *") },
          modifier = Modifier.fillMaxWidth().testTag("staff_phone_input"),
          singleLine = true
        )

        // Section 2: Role Selection
        Text(
          text = if (currentLocale == "fr") "2. Rôle Opérationnel :" else "2. Operational Role:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          // Branch Manager
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedRole == UserRole.BRANCH_MANAGER) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
            border = if (selectedRole == UserRole.BRANCH_MANAGER) androidx.compose.foundation.BorderStroke(1.5.dp, NavyPrimary) else null,
            onClick = { selectedRole = UserRole.BRANCH_MANAGER },
            modifier = Modifier.fillMaxWidth().testTag("role_option_manager")
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = selectedRole == UserRole.BRANCH_MANAGER,
                onClick = { selectedRole = UserRole.BRANCH_MANAGER }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (currentLocale == "fr") "👔 Chef d'Agence (Manager)" else "👔 Branch Manager",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = if (currentLocale == "fr") "Supervision locale, KPIs & remises" else "Local branch KPIs & approvals",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
            }
          }

          // Instructor
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedRole == UserRole.INSTRUCTOR) Color(0xFFFEF3C7) else Color(0xFFF8FAFC),
            border = if (selectedRole == UserRole.INSTRUCTOR) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFB45309)) else null,
            onClick = { selectedRole = UserRole.INSTRUCTOR },
            modifier = Modifier.fillMaxWidth().testTag("role_option_instructor")
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = selectedRole == UserRole.INSTRUCTOR,
                onClick = { selectedRole = UserRole.INSTRUCTOR }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (currentLocale == "fr") "🚗 Moniteur / Instructeur" else "🚗 Driving Instructor",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = if (currentLocale == "fr") "Séances de conduite & pointage kilométrique" else "Conducts classes & session sign-offs",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
            }
          }

          // Secretary
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (selectedRole == UserRole.SECRETARY) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
            border = if (selectedRole == UserRole.SECRETARY) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF15803D)) else null,
            onClick = { selectedRole = UserRole.SECRETARY },
            modifier = Modifier.fillMaxWidth().testTag("role_option_secretary")
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              RadioButton(
                selected = selectedRole == UserRole.SECRETARY,
                onClick = { selectedRole = UserRole.SECRETARY }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = if (currentLocale == "fr") "📋 Secrétaire d'Accueil" else "📋 Front Desk Secretary",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = if (currentLocale == "fr") "Inscriptions au comptoir & encaissements cash" else "Walk-in enrollments & payments",
                  fontSize = 11.sp,
                  color = Color.Gray
                )
              }
            }
          }
        }

        // Section 3: Branch Assignment
        Text(
          text = if (currentLocale == "fr") "3. Affectation d'Agence :" else "3. Branch Assignment:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { branchMenuExpanded = true },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("select_branch_dropdown_btn")
          ) {
            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = selectedBranch?.let { "${it.name} (${it.city})" } ?: (if (currentLocale == "fr") "Sans agence fixe (Flottant)" else "No Fixed Branch"),
              modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
          }

          DropdownMenu(
            expanded = branchMenuExpanded,
            onDismissRequest = { branchMenuExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text(if (currentLocale == "fr") "Aucune agence fixe (Flottant)" else "No fixed branch (Float staff)") },
              onClick = {
                selectedBranchId = null
                branchMenuExpanded = false
              }
            )
            branches.forEach { b ->
              DropdownMenuItem(
                text = { Text("${b.name} (${b.city})") },
                onClick = {
                  selectedBranchId = b.id
                  branchMenuExpanded = false
                }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && (email.isNotBlank() || phone.isNotBlank())) {
            onSave(name, email, phone, selectedRole, selectedBranchId, "CM", preferredLocale)
          }
        },
        enabled = name.isNotBlank() && email.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = NavyDark),
        modifier = Modifier.testTag("save_staff_member_btn")
      ) {
        Text(if (currentLocale == "fr") "Enregistrer dans Firestore" else "Save to Firestore", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (currentLocale == "fr") "Annuler" else "Cancel")
      }
    }
  )
}

/**
 * Modal dialog to edit an existing Staff Member and reassign Role / Branch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStaffDialog(
  currentLocale: String,
  staff: UserEntity,
  branches: List<BranchEntity>,
  onDismiss: () -> Unit,
  onSave: (
    name: String,
    email: String,
    phone: String,
    role: UserRole,
    branchId: String?
  ) -> Unit
) {
  var name by remember { mutableStateOf(staff.name) }
  var email by remember { mutableStateOf(staff.email) }
  var phone by remember { mutableStateOf(staff.phone) }
  var selectedRole by remember { mutableStateOf(staff.role) }
  var selectedBranchId by remember { mutableStateOf(staff.branchId) }
  var branchMenuExpanded by remember { mutableStateOf(false) }

  val selectedBranch = branches.find { it.id == selectedBranchId }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.EditNote, contentDescription = null, tint = AmberAccent)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (currentLocale == "fr") "Modifier le Rôle & l'Agence" else "Edit Staff Role & Branch",
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
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (currentLocale == "fr") "Nom Complet" else "Full Name") },
          modifier = Modifier.fillMaxWidth().testTag("edit_staff_name_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text(if (currentLocale == "fr") "Adresse Email" else "Email Address") },
          modifier = Modifier.fillMaxWidth().testTag("edit_staff_email_input"),
          singleLine = true
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text(if (currentLocale == "fr") "Téléphone" else "Phone Number") },
          modifier = Modifier.fillMaxWidth().testTag("edit_staff_phone_input"),
          singleLine = true
        )

        // Role picker
        Text(
          text = if (currentLocale == "fr") "Rôle Affecté :" else "Assigned Role:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChip(
            selected = selectedRole == UserRole.BRANCH_MANAGER,
            onClick = { selectedRole = UserRole.BRANCH_MANAGER },
            label = { Text("Manager", fontSize = 11.sp) },
            modifier = Modifier.testTag("edit_role_manager")
          )
          FilterChip(
            selected = selectedRole == UserRole.INSTRUCTOR,
            onClick = { selectedRole = UserRole.INSTRUCTOR },
            label = { Text("Instructor", fontSize = 11.sp) },
            modifier = Modifier.testTag("edit_role_instructor")
          )
          FilterChip(
            selected = selectedRole == UserRole.SECRETARY,
            onClick = { selectedRole = UserRole.SECRETARY },
            label = { Text("Secretary", fontSize = 11.sp) },
            modifier = Modifier.testTag("edit_role_secretary")
          )
        }

        // Branch Assignment
        Text(
          text = if (currentLocale == "fr") "Agence de Rattachement :" else "Assigned Branch:",
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = NavyPrimary
        )

        Box(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(
            onClick = { branchMenuExpanded = true },
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().testTag("edit_staff_branch_dropdown")
          ) {
            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = selectedBranch?.let { "${it.name} (${it.city})" } ?: (if (currentLocale == "fr") "Aucune agence fixe" else "No Fixed Branch"),
              modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
          }

          DropdownMenu(
            expanded = branchMenuExpanded,
            onDismissRequest = { branchMenuExpanded = false }
          ) {
            DropdownMenuItem(
              text = { Text(if (currentLocale == "fr") "Aucune agence fixe (Flottant)" else "No fixed branch (Float staff)") },
              onClick = {
                selectedBranchId = null
                branchMenuExpanded = false
              }
            )
            branches.forEach { b ->
              DropdownMenuItem(
                text = { Text("${b.name} (${b.city})") },
                onClick = {
                  selectedBranchId = b.id
                  branchMenuExpanded = false
                }
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSave(name, email, phone, selectedRole, selectedBranchId)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = NavyDark, contentColor = Color.White),
        modifier = Modifier.testTag("update_staff_member_btn")
      ) {
        Text(if (currentLocale == "fr") "Mettre à jour dans Firestore" else "Update in Firestore", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(if (currentLocale == "fr") "Annuler" else "Cancel")
      }
    }
  )
}
