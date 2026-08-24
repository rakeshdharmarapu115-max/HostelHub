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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
}

@Composable
private fun StudentIdCardDialog(
    student: Student,
    onDismiss: () -> Unit,
    onCopyCredentials: () -> Unit,
    onAssignRoomClick: () -> Unit
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
