package com.hostelhub.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Student
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

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var createdStudentResult by remember { mutableStateOf<Pair<Student, String>?>(null) }

    val generatedId by adminViewModel?.generatedStudentId?.collectAsState() ?: remember { mutableStateOf("") }

    val hostelsState by adminViewModel?.hostels?.collectAsState() ?: remember { mutableStateOf(UiState.Idle) }
    val availableHostels = (hostelsState as? UiState.Success)?.data ?: emptyList()

    // Add Student Modal Dialog
    if (showAddStudentDialog) {
        var fullName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var collegeName by remember { mutableStateOf("Apex Engineering College") }
        var course by remember { mutableStateOf("B.Tech Computer Science") }
        var yearOfStudy by remember { mutableStateOf("1") }
        var gender by remember { mutableStateOf("male") }
        var address by remember { mutableStateOf("Main Campus Resident") }
        var emergencyName by remember { mutableStateOf("") }
        var emergencyPhone by remember { mutableStateOf("") }
        var initialPassword by remember { mutableStateOf("Password@123") }
        var selectedHostelId by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isSubmitting by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (generatedId.isBlank()) {
                adminViewModel?.fetchGeneratedStudentId()
            }
        }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showAddStudentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = AdminAccent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Student & Generate ID", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Generated Student ID Banner
                    Surface(
                        color = AdminAccentContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Assigned Student ID:", style = MaterialTheme.typography.labelSmall, color = AdminOnAccentContainer)
                                Text(
                                    text = if (generatedId.isNotBlank()) generatedId else "Generating...",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = AdminAccent
                                )
                            }
                            IconButton(onClick = { adminViewModel?.fetchGeneratedStudentId() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh ID", tint = AdminAccent)
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    AppTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = "Full Name *",
                        placeholder = "e.g. John Doe",
                        leadingIcon = Icons.Default.Person
                    )

                    AppTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address (Optional)",
                        placeholder = "Auto-generated if blank",
                        leadingIcon = Icons.Default.Email
                    )

                    AppTextField(
                        value = collegeName,
                        onValueChange = { collegeName = it },
                        label = "College / University *",
                        leadingIcon = Icons.Default.School
                    )

                    AppTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = "Course & Branch *",
                        leadingIcon = Icons.Default.MenuBook
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppTextField(
                                value = yearOfStudy,
                                onValueChange = { yearOfStudy = it },
                                label = "Year",
                                placeholder = "1, 2, 3, 4"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AppTextField(
                                value = gender,
                                onValueChange = { gender = it },
                                label = "Gender",
                                placeholder = "male / female"
                            )
                        }
                    }

                    AppTextField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it },
                        label = "Contact Phone Number *",
                        placeholder = "+91 9876543210",
                        leadingIcon = Icons.Default.Phone
                    )

                    AppTextField(
                        value = initialPassword,
                        onValueChange = { initialPassword = it },
                        label = "Default Password *",
                        leadingIcon = Icons.Default.Lock
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (fullName.isBlank() || collegeName.isBlank() || course.isBlank()) {
                            errorMessage = "Full Name, College, and Course are required."
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null
                        val studentToCreate = Student(
                            fullName = fullName.trim(),
                            email = email.trim(),
                            rollNumber = generatedId,
                            collegeName = collegeName.trim(),
                            course = course.trim(),
                            yearOfStudy = yearOfStudy.trim(),
                            gender = gender.trim(),
                            permanentAddress = address.trim(),
                            emergencyContactName = emergencyName.ifBlank { "$fullName Guardian" },
                            emergencyContactPhone = emergencyPhone.ifBlank { "0000000000" },
                            hostelId = selectedHostelId.ifBlank { null }
                        )

                        adminViewModel?.createStudentByAdmin(
                            student = studentToCreate,
                            password = initialPassword.trim(),
                            onSuccess = { created ->
                                isSubmitting = false
                                showAddStudentDialog = false
                                createdStudentResult = Pair(created, initialPassword)
                                adminViewModel.fetchGeneratedStudentId()
                            },
                            onError = { err ->
                                isSubmitting = false
                                errorMessage = err
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AdminAccent),
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Register & Issue ID")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }, enabled = !isSubmitting) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Credential Dialog
    createdStudentResult?.let { (student, pwd) ->
        AlertDialog(
            onDismissRequest = { createdStudentResult = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Student Registered Successfully!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Student account provisioned and Student ID generated:", style = MaterialTheme.typography.bodyMedium)
                    Surface(color = SurfaceContainer, shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Name: ${student.fullName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("🆔 Student ID: ${student.rollNumber}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = AdminAccent)
                            Text("🔑 Initial Password: $pwd", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text("The student can now sign in using their Student ID and password.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { createdStudentResult = null }, colors = ButtonDefaults.buttonColors(containerColor = AdminAccent)) {
                    Text("Done")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Association User & Warden Registry",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    adminViewModel?.fetchGeneratedStudentId()
                    showAddStudentDialog = true
                },
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Student") },
                text = { Text("Add Student / Generate ID", fontWeight = FontWeight.Bold) },
                containerColor = AdminAccent,
                contentColor = Color.White
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
