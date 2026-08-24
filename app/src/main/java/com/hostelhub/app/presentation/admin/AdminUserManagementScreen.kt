package com.hostelhub.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.User
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    adminViewModel: AdminViewModel? = null,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableStateOf<UserRole?>(null) }

    val usersState by adminViewModel?.users?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val usersList = (usersState as? UiState.Success)?.data ?: listOf(
        User(userId = "std_001", fullName = "Alex Mercer", email = "alex.mercer@campus.edu", phoneNumber = "+1 555-0199", role = UserRole.STUDENT, isActive = true),
        User(userId = "host_001", fullName = "Robert Vance", email = "warden.greenvalley@campus.edu", phoneNumber = "+1 555-0101", role = UserRole.HOST, isActive = true),
        User(userId = "host_002", fullName = "Eleanor Rigby", email = "warden.sunrise@campus.edu", phoneNumber = "+1 555-0102", role = UserRole.HOST, isActive = true),
        User(userId = "admin_001", fullName = "Dean Henderson", email = "admin@campus.edu", phoneNumber = "+1 555-0100", role = UserRole.ADMIN, isActive = true)
    )

    val filteredUsers = usersList.filter { user ->
        val matchesQuery = user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true)
        val matchesRole = selectedRoleFilter == null || user.role == selectedRoleFilter
        matchesQuery && matchesRole
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Association User & Warden Registry",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Input
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search User Accounts",
                placeholder = "Search by name or email...",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Role Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedRoleFilter == null,
                    onClick = { selectedRoleFilter = null },
                    label = { Text("All (${usersList.size})") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedRoleFilter == UserRole.STUDENT,
                    onClick = { selectedRoleFilter = UserRole.STUDENT },
                    label = { Text("🎓 Students") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedRoleFilter == UserRole.HOST,
                    onClick = { selectedRoleFilter = UserRole.HOST },
                    label = { Text("🏢 Wardens") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No Users Found",
                        message = "No accounts matched your search and filter criteria."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        AppCard(padding = 16.dp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(AdminAccentContainer, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = when (user.role) {
                                                UserRole.STUDENT -> Icons.Default.Person
                                                UserRole.HOST -> Icons.Default.Business
                                                UserRole.ADMIN -> Icons.Default.SupervisorAccount
                                            },
                                            contentDescription = null,
                                            tint = AdminOnAccentContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = user.fullName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            StatusBadge(
                                                text = when (user.role) {
                                                    UserRole.STUDENT -> "Resident"
                                                    UserRole.HOST -> "Warden"
                                                    UserRole.ADMIN -> "Council"
                                                },
                                                statusType = when (user.role) {
                                                    UserRole.STUDENT -> BadgeStatusType.INFO
                                                    UserRole.HOST -> BadgeStatusType.WARNING
                                                    UserRole.ADMIN -> BadgeStatusType.SUCCESS
                                                }
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${user.email} • ${if (user.phoneNumber.isNotBlank()) user.phoneNumber else "No phone"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = user.isActive,
                                        onCheckedChange = { newStatus ->
                                            adminViewModel?.toggleUserStatus(user.userId, newStatus)
                                        },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = AdminAccent
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
