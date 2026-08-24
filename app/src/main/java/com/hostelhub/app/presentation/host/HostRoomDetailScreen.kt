package com.hostelhub.app.presentation.host

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Bed
import com.hostelhub.app.domain.model.Room
import com.hostelhub.app.domain.model.RoomStatus
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.BadgeStatusType
import com.hostelhub.app.presentation.components.ButtonVariant
import com.hostelhub.app.presentation.components.StatusBadge
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState

@Composable
fun HostRoomDetailScreen(
    roomId: String,
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val roomsState by hostViewModel?.rooms?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val residentsState by hostViewModel?.residents?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val room = (roomsState as? UiState.Success)?.data?.find { it.roomId == roomId }
    val residents = (residentsState as? UiState.Success)?.data ?: emptyList()
    val isLoading = roomsState is UiState.Loading

    var selectedBedForAllocation by remember { mutableStateOf<Bed?>(null) }
    var selectedBedForVacating by remember { mutableStateOf<Bed?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Room Configuration",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SecondaryTeal)
            }
        } else if (room == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Room Not Found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(
                        text = "Back to Room List",
                        onClick = onNavigateBack
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundCool)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Room Specs Header Card
                AppCard(
                    backgroundColor = PrimaryNavy,
                    padding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Room Specifications",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Room ${room.roomNumber}",
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Floor ${room.floor} • Block ${room.block} • ${room.roomType.name} Sharing",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        StatusBadge(
                            text = room.status.name,
                            statusType = when (room.status) {
                                RoomStatus.FULL -> BadgeStatusType.SUCCESS
                                RoomStatus.AVAILABLE -> BadgeStatusType.INFO
                                RoomStatus.MAINTENANCE -> BadgeStatusType.ERROR
                            },
                            customBgColor = Color.White,
                            customTextColor = PrimaryNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Bed Occupancy & Allocations
                Text(
                    text = "Bed Allocations (${room.occupiedCount}/${room.totalCapacity})",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppCard(padding = 16.dp) {
                    if (room.beds.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            room.beds.forEach { bed ->
                                HostBedRow(
                                    bed = bed,
                                    onAllocateClick = {
                                        selectedBedForAllocation = bed
                                    },
                                    onVacateClick = {
                                        selectedBedForVacating = bed
                                    }
                                )
                                if (bed != room.beds.last()) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        thickness = 0.5.dp
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No beds recorded for this room.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Room Financials & Pricing
                Text(
                    text = "Financial Rates",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppCard(padding = 16.dp) {
                    HostRoomRateRow("Base Monthly Rent", "${Formatters.formatCurrency(room.monthlyRent)} / student")
                    HostRoomRateRow("Estimated Total Room Yield", "${Formatters.formatCurrency(room.monthlyRent * room.totalCapacity)} / mo")
                    HostRoomRateRow("Room Maintenance Status", "Verified & Operational")
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = "Back to Room List",
                    variant = ButtonVariant.OUTLINED,
                    onClick = onNavigateBack
                )
            }
        }
    }

    // Allocate Student Dialog
    selectedBedForAllocation?.let { bed ->
        AllocateStudentDialog(
            room = room,
            bed = bed,
            availableStudents = residents,
            onDismiss = { selectedBedForAllocation = null },
            onConfirm = { studentId, studentName ->
                hostViewModel?.assignBed(
                    roomId = roomId,
                    bedId = bed.bedId,
                    studentId = studentId,
                    studentName = studentName,
                    onSuccess = {
                        Toast.makeText(context, "Student $studentName assigned to ${bed.bedNumber}!", Toast.LENGTH_SHORT).show()
                        selectedBedForAllocation = null
                    },
                    onError = { errorMsg ->
                        Toast.makeText(context, "Allocation failed: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }

    // Vacate Bed Dialog
    selectedBedForVacating?.let { bed ->
        AlertDialog(
            onDismissRequest = { selectedBedForVacating = null },
            title = { Text("Vacate ${bed.bedNumber}?", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    "Are you sure you want to remove ${bed.studentName ?: "the resident"} from Room ${room?.roomNumber} ${bed.bedNumber}?\nThis will mark the bed as vacant for new student allocations.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        hostViewModel?.assignBed(
                            roomId = roomId,
                            bedId = bed.bedId,
                            studentId = "",
                            studentName = "",
                            onSuccess = {
                                Toast.makeText(context, "${bed.bedNumber} is now vacant.", Toast.LENGTH_SHORT).show()
                                selectedBedForVacating = null
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, "Vacating failed: $errorMsg", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Vacate Bed")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBedForVacating = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun HostBedRow(
    bed: Bed,
    onAllocateClick: () -> Unit,
    onVacateClick: () -> Unit
) {
    val isOccupied = bed.isOccupied
    val occupantName = bed.studentName ?: "Vacant Bed"
    val studentId = bed.studentId ?: "Ready for allocation"

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
                    .background(
                        if (isOccupied) PrimaryContainer else SecondaryTeal.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isOccupied) Icons.Default.Person else Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = if (isOccupied) PrimaryNavy else SecondaryTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = bed.bedNumber,
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(
                        text = if (isOccupied) "Occupied" else "Vacant",
                        statusType = if (isOccupied) BadgeStatusType.SUCCESS else BadgeStatusType.INFO
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isOccupied) "$occupantName • $studentId" else "Available for student assignment",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOccupied) MaterialTheme.colorScheme.onSurfaceVariant else SecondaryTeal
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        if (isOccupied) {
            OutlinedButton(
                onClick = onVacateClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.PersonRemove, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Vacate", style = MaterialTheme.typography.labelMedium)
            }
        } else {
            Button(
                onClick = onAllocateClick,
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Allocate", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllocateStudentDialog(
    room: Room?,
    bed: Bed,
    availableStudents: List<Student>,
    onDismiss: () -> Unit,
    onConfirm: (studentId: String, studentName: String) -> Unit
) {
    var studentNameInput by remember { mutableStateOf("") }
    var studentIdInput by remember { mutableStateOf("") }
    var selectedStudentFromList by remember { mutableStateOf<Student?>(null) }
    var isManualEntry by remember { mutableStateOf(false) }

    val unassignedResidents = availableStudents.filter { it.roomId.isNullOrBlank() || it.roomNumber.isNullOrBlank() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Allocate ${bed.bedNumber}",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryNavy
                )
                Text(
                    text = "Room ${room?.roomNumber} • ${room?.roomType?.name} Sharing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (unassignedResidents.isNotEmpty() && !isManualEntry) {
                    Text(
                        text = "Select an unallocated student:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        unassignedResidents.take(4).forEach { s ->
                            val isSelected = selectedStudentFromList?.studentId == s.studentId
                            Surface(
                                onClick = {
                                    selectedStudentFromList = s
                                    studentNameInput = s.fullName
                                    studentIdInput = s.rollNumber.ifBlank { s.studentId }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) SecondaryTeal.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SecondaryTeal) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = if (isSelected) SecondaryTeal else PrimaryNavy, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = s.fullName, style = MaterialTheme.typography.titleSmall)
                                        Text(text = "ID: ${s.rollNumber} • ${s.course}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    TextButton(onClick = { isManualEntry = true }) {
                        Text("+ Or enter student details manually", style = MaterialTheme.typography.labelMedium, color = SecondaryTeal)
                    }
                } else {
                    AppTextField(
                        value = studentNameInput,
                        onValueChange = { studentNameInput = it },
                        label = "Student Full Name",
                        placeholder = "e.g. Rahul Sharma",
                        leadingIcon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AppTextField(
                        value = studentIdInput,
                        onValueChange = { studentIdInput = it },
                        label = "Student ID / Roll Number",
                        placeholder = "e.g. 24248-cs-093 or STD-101",
                        leadingIcon = Icons.Default.MeetingRoom
                    )

                    if (unassignedResidents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { isManualEntry = false }) {
                            Text("← Pick from registered students", style = MaterialTheme.typography.labelMedium, color = SecondaryTeal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalId = selectedStudentFromList?.studentId ?: studentIdInput.ifBlank { "STD-${System.currentTimeMillis() % 10000}" }
                    val finalName = selectedStudentFromList?.fullName ?: studentNameInput.ifBlank { "Resident Student" }
                    onConfirm(finalId, finalName)
                },
                enabled = studentNameInput.isNotBlank() || selectedStudentFromList != null || studentIdInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal)
            ) {
                Text("Confirm Allocation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun HostRoomRateRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
