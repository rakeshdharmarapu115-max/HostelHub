package com.hostelhub.app.presentation.host

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.hostelhub.app.domain.model.Room
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@Composable
fun HostStudentManagementScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val filters = listOf("All Residents", "Floor 1", "Floor 2", "Floor 3", "Unallocated")
    var selectedFilter by remember { mutableStateOf("All Residents") }

    val residentsState by hostViewModel?.residents?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val roomsState by hostViewModel?.rooms?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val allResidents = (residentsState as? UiState.Success)?.data ?: emptyList()
    val allRooms = (roomsState as? UiState.Success)?.data ?: emptyList()

    var selectedStudentForDetails by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForAssignment by remember { mutableStateOf<Student?>(null) }
    var selectedStudentForDeallocation by remember { mutableStateOf<Student?>(null) }
    var deallocationRemarks by remember { mutableStateOf("") }
    var isDeallocating by remember { mutableStateOf(false) }

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var newlyCreatedStudent by remember { mutableStateOf<Student?>(null) }

    // Deallocation Confirmation Dialog
    selectedStudentForDeallocation?.let { student ->
        AlertDialog(
            onDismissRequest = { if (!isDeallocating) selectedStudentForDeallocation = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Confirm Student Deallocation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Are you sure you want to deallocate ${student.fullName} (Student ID: ${student.rollNumber}) from this hostel?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚠️ Immediate Effects of Deallocation:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text("• Student room and bed assignment will be released.", style = MaterialTheme.typography.bodySmall)
                            Text("• Student status will become DEALLOCATED.", style = MaterialTheme.typography.bodySmall)
                            Text("• All active login sessions on student devices will be terminated immediately.", style = MaterialTheme.typography.bodySmall)
                            Text("• Further student portal access will be denied.", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    AppTextField(
                        value = deallocationRemarks,
                        onValueChange = { deallocationRemarks = it },
                        label = "Deallocation Remarks (Optional)",
                        placeholder = "e.g. Course completed / Vacated room"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeallocating = true
                        hostViewModel?.deallocateStudent(
                            studentId = student.studentId.ifBlank { student.userId },
                            remarks = deallocationRemarks.ifBlank { "Deallocated by Hostel Administration" },
                            onSuccess = {
                                isDeallocating = false
                                selectedStudentForDeallocation = null
                                selectedStudentForDetails = null
                                deallocationRemarks = ""
                                Toast.makeText(context, "${student.fullName} has been deallocated.", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                isDeallocating = false
                                Toast.makeText(context, "Deallocation failed: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    enabled = !isDeallocating
                ) {
                    if (isDeallocating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Confirm Deallocation")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedStudentForDeallocation = null },
                    enabled = !isDeallocating
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val filteredList = allResidents.filter { student ->
        val matchesSearch = searchQuery.isBlank() ||
                student.fullName.contains(searchQuery, ignoreCase = true) ||
                student.rollNumber.contains(searchQuery, ignoreCase = true) ||
                (student.roomNumber ?: "").contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Floor 1" -> (student.roomNumber ?: "").contains("1")
            "Floor 2" -> (student.roomNumber ?: "").contains("2")
            "Floor 3" -> (student.roomNumber ?: "").contains("3")
            "Unallocated" -> student.roomNumber.isNullOrBlank() || student.roomId.isNullOrBlank()
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Resident Students Directory",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddStudentDialog = true },
                containerColor = PrimaryNavy,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Add Student") },
                text = { Text("Add Student", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Residents",
                placeholder = "Search by student name, roll number, or room",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(12.dp))

            FilterChipRow(
                items = filters,
                selectedItem = selectedFilter,
                onItemSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Residents Found",
                    message = "No resident records found matching the search criteria."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { student ->
                        val hasRoom = !student.roomNumber.isNullOrBlank()
                        AppCard(
                            onClick = { selectedStudentForDetails = student },
                            padding = 16.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(PrimaryContainer, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = PrimaryNavy,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = student.fullName,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        StatusBadge(
                                            text = if (hasRoom) "Room ${student.roomNumber} (${student.bedNumber ?: "Bed"})" else "Unallocated",
                                            statusType = if (hasRoom) BadgeStatusType.INFO else BadgeStatusType.WARNING
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "ID: ${student.rollNumber} • ${student.course}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📞 ${student.emergencyContactPhone.ifBlank { "N/A" }} • ✉ ${student.email}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Student Details & ID Card Dialog
    selectedStudentForDetails?.let { student ->
        StudentIdCardDialog(
            student = student,
            onDismiss = { selectedStudentForDetails = null },
            onCopyCredentials = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val text = """
                    🏨 HostelHub - Student Access ID Card
                    ━━━━━━━━━━━━━━━━━━━━━━━━
                    • Student Name: ${student.fullName}
                    • Roll / Student ID: ${student.rollNumber}
                    • Campus Email: ${student.email}
                    • Hostel: ${student.hostelName ?: "Campus Hostel"}
                    • Assigned Room: ${student.roomNumber ?: "Unallocated"}
                    • Bed Number: ${student.bedNumber ?: "Unallocated"}
                    ━━━━━━━━━━━━━━━━━━━━━━━━
                    Please log into HostelHub using your campus email and registered password.
                """.trimIndent()
                val clip = ClipData.newPlainText("Student ID Card", text)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Student ID & credentials copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            onAssignRoomClick = {
                selectedStudentForDetails = null
                selectedStudentForAssignment = student
            },
            onDeallocateClick = {
                selectedStudentForDetails = null
                selectedStudentForDeallocation = student
            }
        )
    }

    // Direct Room Picker Dialog for Unallocated Student
    selectedStudentForAssignment?.let { student ->
        AssignStudentToRoomDialog(
            student = student,
            rooms = allRooms,
            onDismiss = { selectedStudentForAssignment = null },
            onAssign = { room, bed ->
                hostViewModel?.assignBed(
                    roomId = room.roomId,
                    bedId = bed.bedId,
                    studentId = student.studentId.ifBlank { student.userId },
                    studentName = student.fullName,
                    onSuccess = {
                        Toast.makeText(context, "${student.fullName} assigned to Room ${room.roomNumber} ${bed.bedNumber}!", Toast.LENGTH_LONG).show()
                        selectedStudentForAssignment = null
                    },
                    onError = { errorMsg ->
                        Toast.makeText(context, "Assignment failed: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Add Student & Issue ID Dialog
    if (showAddStudentDialog && hostViewModel != null) {
        AddStudentDialog(
            hostViewModel = hostViewModel,
            rooms = allRooms,
            onDismiss = { showAddStudentDialog = false },
            onStudentCreated = { createdStudent ->
                showAddStudentDialog = false
                newlyCreatedStudent = createdStudent
            }
        )
    }

    // Student ID Created & Share Card Dialog
    newlyCreatedStudent?.let { student ->
        StudentIdIssuedDialog(
            student = student,
            onDismiss = { newlyCreatedStudent = null },
            onCopy = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val shareText = """
                    🏨 HOSTEL ALLOCATION CREDENTIALS
                    Student Name: ${student.fullName}
                    Unique Student ID: ${student.rollNumber}
                    Hostel: ${student.hostelName ?: "Campus Hostel"}
                    Room: ${student.roomNumber ?: "Unallocated"}
                    
                    Please open HostelHub -> Register -> Student Registration, enter your Unique Student ID (${student.rollNumber}), and set your personal password.
                """.trimIndent()
                val clip = ClipData.newPlainText("Student ID", shareText)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Student ID copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun StudentIdCardDialog(
    student: Student,
    onDismiss: () -> Unit,
    onCopyCredentials: () -> Unit,
    onAssignRoomClick: () -> Unit,
    onDeallocateClick: () -> Unit
) {
    val hasRoom = !student.roomNumber.isNullOrBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PrimaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryNavy)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Student Access ID", style = MaterialTheme.typography.titleLarge, color = PrimaryNavy)
                    Text(text = "Hostel Resident Credentials", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StudentIdInfoRow("Full Name", student.fullName, fontWeight = FontWeight.Bold)
                        StudentIdInfoRow("Roll / Student ID", student.rollNumber, isHighlight = true)
                        StudentIdInfoRow("Registered Email", student.email)
                        StudentIdInfoRow("Mobile Phone", student.emergencyContactPhone.ifBlank { "N/A" })
                        StudentIdInfoRow("Course & Year", "${student.course} • ${student.yearOfStudy}")
                        StudentIdInfoRow(
                            label = "Room & Bed",
                            value = if (hasRoom) "Room ${student.roomNumber} (${student.bedNumber ?: "Bed A"})" else "Unallocated",
                            isWarning = !hasRoom
                        )
                        StudentIdInfoRow(
                            label = "Account Status",
                            value = student.status.name,
                            isHighlight = student.status.name == "ACTIVE" || student.status.name == "ALLOCATED",
                            isWarning = student.status.name == "DEALLOCATED"
                        )
                    }
                }

                if (!hasRoom) {
                    Button(
                        onClick = onAssignRoomClick,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Assign Room & Bed Now")
                    }
                }

                OutlinedButton(
                    onClick = onDeallocateClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deallocate Student from Hostel", color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCopyCredentials,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Student ID")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun StudentIdInfoRow(
    label: String,
    value: String,
    fontWeight: FontWeight = FontWeight.Normal,
    isHighlight: Boolean = false,
    isWarning: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlight || isWarning) FontWeight.Bold else fontWeight,
            color = when {
                isWarning -> MaterialTheme.colorScheme.error
                isHighlight -> SecondaryTeal
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun AssignStudentToRoomDialog(
    student: Student,
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onAssign: (Room, com.hostelhub.app.domain.model.Bed) -> Unit
) {
    val vacantRooms = rooms.filter { it.occupiedCount < it.totalCapacity && it.beds.any { b -> !b.isOccupied } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Assign Room to ${student.fullName}", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            if (vacantRooms.isEmpty()) {
                Text("No rooms with vacant beds are currently available in your hostel.", style = MaterialTheme.typography.bodyMedium)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(vacantRooms) { room ->
                        val availableBeds = room.beds.filter { !it.isOccupied }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Room ${room.roomNumber} (${room.roomType.name} Sharing)",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = PrimaryNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    availableBeds.forEach { bed ->
                                        Button(
                                            onClick = { onAssign(room, bed) },
                                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(bed.bedNumber, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun AddStudentDialog(
    hostViewModel: HostViewModel,
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onStudentCreated: (Student) -> Unit
) {
    val context = LocalContext.current
    var fullName by remember { mutableStateOf("") }
    var generatedStudentId by remember { mutableStateOf("") }
    var collegeName by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var yearOfStudy by remember { mutableStateOf("1st Year") }
    var mobilePhone by remember { mutableStateOf("") }
    var selectedRoom by remember { mutableStateOf<Room?>(null) }
    var selectedBed by remember { mutableStateOf<com.hostelhub.app.domain.model.Bed?>(null) }

    var isGeneratingId by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    // Fetch next unique Student ID on launch
    LaunchedEffect(Unit) {
        isGeneratingId = true
        hostViewModel.generateStudentId { nextId ->
            generatedStudentId = nextId
            isGeneratingId = false
        }
    }

    val availableRooms = rooms.filter { it.occupiedCount < it.totalCapacity }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(PrimaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "Add Student to Hostel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Issues a unique, verifiable Student ID", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Generated Unique Student ID Showcase
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PrimaryNavy.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryNavy.copy(alpha = 0.25f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "GENERATED STUDENT ID", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                            if (isGeneratingId) {
                                Text(text = "Generating unique ID...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                Text(text = generatedStudentId, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = PrimaryNavy)
                            }
                        }
                        IconButton(
                            onClick = {
                                isGeneratingId = true
                                hostViewModel.generateStudentId { nextId ->
                                    generatedStudentId = nextId
                                    isGeneratingId = false
                                }
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate ID", tint = PrimaryNavy)
                        }
                    }
                }

                formError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                AppTextField(
                    value = fullName,
                    onValueChange = { fullName = it; formError = null },
                    label = "Student Full Name *",
                    placeholder = "e.g. Alex Mercer"
                )

                AppTextField(
                    value = collegeName,
                    onValueChange = { collegeName = it; formError = null },
                    label = "College / University Name *",
                    placeholder = "e.g. Institute of Technology"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = course,
                            onValueChange = { course = it; formError = null },
                            label = "Course *",
                            placeholder = "e.g. B.Tech CS"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = yearOfStudy,
                            onValueChange = { yearOfStudy = it },
                            label = "Year",
                            placeholder = "e.g. 1st Year"
                        )
                    }
                }

                AppTextField(
                    value = mobilePhone,
                    onValueChange = { mobilePhone = it; formError = null },
                    label = "Mobile Phone Number *",
                    placeholder = "e.g. 9876543210"
                )

                // Optional Room Picker
                if (availableRooms.isNotEmpty()) {
                    Text(text = "Assign Room (Optional):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableRooms.take(4).forEach { room ->
                            val isSelected = selectedRoom?.roomId == room.roomId
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    if (isSelected) {
                                        selectedRoom = null
                                        selectedBed = null
                                    } else {
                                        selectedRoom = room
                                        selectedBed = room.beds.firstOrNull { !it.isOccupied }
                                    }
                                },
                                label = { Text("Room ${room.roomNumber}") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmedName = fullName.trim()
                    val trimmedCollege = collegeName.trim()
                    val trimmedCourse = course.trim()
                    val trimmedPhone = mobilePhone.trim()

                    if (trimmedName.isBlank()) {
                        formError = "Student Full Name is required."
                        return@Button
                    }
                    if (trimmedCollege.isBlank()) {
                        formError = "College / University Name is required."
                        return@Button
                    }
                    if (trimmedCourse.isBlank()) {
                        formError = "Course name is required."
                        return@Button
                    }
                    if (trimmedPhone.isBlank()) {
                        formError = "Mobile Phone Number is required."
                        return@Button
                    }
                    val cleanDigits = trimmedPhone.filter { it.isDigit() || it == '+' }
                    if (cleanDigits.length < 7 || cleanDigits.length > 15) {
                        formError = "Please enter a valid mobile phone number."
                        return@Button
                    }
                    if (generatedStudentId.isBlank()) {
                        formError = "Please wait for Student ID generation."
                        return@Button
                    }

                    isSubmitting = true
                    formError = null

                    val newStudent = Student(
                        studentId = "",
                        userId = "",
                        fullName = trimmedName,
                        rollNumber = generatedStudentId.trim(),
                        email = "${generatedStudentId.trim().lowercase()}@campus.edu",
                        emergencyContactPhone = cleanDigits,
                        collegeName = trimmedCollege,
                        course = trimmedCourse,
                        yearOfStudy = yearOfStudy.trim().ifBlank { "1st Year" },
                        gender = "Male",
                        permanentAddress = "Campus Resident",
                        emergencyContactName = "Parent/Guardian",
                        hostelId = "",
                        roomId = selectedRoom?.roomId,
                        roomNumber = selectedRoom?.roomNumber,
                        bedNumber = selectedBed?.bedNumber,
                        status = com.hostelhub.app.domain.model.StudentStatus.ACTIVE
                    )

                    hostViewModel.addStudentByOwner(
                        student = newStudent,
                        onSuccess = { created ->
                            isSubmitting = false
                            onStudentCreated(created)
                        },
                        onError = { errMsg ->
                            isSubmitting = false
                            formError = errMsg
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                enabled = !isSubmitting && !isGeneratingId
            ) {
                if (isSubmitting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        Text("Creating Student...")
                    }
                } else {
                    Text("Issue ID & Add Student")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun StudentIdIssuedDialog(
    student: Student,
    onDismiss: () -> Unit,
    onCopy: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(44.dp))
        },
        title = {
            Text(
                text = "Student ID Issued Successfully!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "The unique Student ID has been generated and stored in the database. Share this ID with the student so they can register and create their personal password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PrimaryNavy.copy(alpha = 0.08f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryNavy.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "STUDENT ID", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PrimaryNavy)
                        Text(
                            text = student.rollNumber,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = student.fullName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!student.roomNumber.isNullOrBlank()) {
                            Text(
                                text = "Room: ${student.roomNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryTeal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Student ID")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

