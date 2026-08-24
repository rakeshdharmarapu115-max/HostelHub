package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.BadgeStatusType
import com.hostelhub.app.presentation.components.StatusBadge
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@Composable
fun MyRoomScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val profileState by studentViewModel?.studentProfile?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val roomState by studentViewModel?.roomDetails?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val currentStudent = (profileState as? UiState.Success)?.data
    val room = (roomState as? UiState.Success)?.data
    val isLoading = profileState is UiState.Loading

    Scaffold(
        topBar = {
            AppTopBar(
                title = "My Hostel Room",
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
                CircularProgressIndicator(color = StudentAccent)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(StudentBackground)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Room Overview Card
                AppCard(
                    backgroundColor = StudentHeroBg,
                    padding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Room Allocation",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (room != null) "Room ${room.roomNumber}" else if (!currentStudent?.roomNumber.isNullOrBlank()) "Room ${currentStudent?.roomNumber}" else "No Room Assigned",
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (room != null) "Block ${room.block} • Floor ${room.floor} • ${room.roomType.name} Sharing" else currentStudent?.hostelName ?: "Contact hostel warden for room allocation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        StatusBadge(
                            text = if (!currentStudent?.bedNumber.isNullOrBlank()) "${currentStudent?.bedNumber} (Assigned)" else if (room != null) "Active" else "Pending",
                            statusType = if (room != null || !currentStudent?.roomNumber.isNullOrBlank()) BadgeStatusType.SUCCESS else BadgeStatusType.WARNING,
                            customBgColor = Color.White,
                            customTextColor = PrimaryNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Roommates Section from Real DB Beds
                Text(
                    text = "Roommates & Bed Occupancy",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppCard(padding = 16.dp) {
                    val beds = room?.beds ?: emptyList()
                    if (beds.isNotEmpty()) {
                        beds.forEachIndexed { index, bed ->
                            if (index > 0) Spacer(modifier = Modifier.height(16.dp))
                            val isYou = bed.studentId == currentStudent?.studentId || bed.bedNumber == currentStudent?.bedNumber
                            RoommateItem(
                                bed = if (isYou) "${bed.bedNumber} (You)" else bed.bedNumber,
                                name = bed.studentName ?: if (bed.isOccupied) "Resident Occupant" else "Available Bed",
                                course = if (bed.isOccupied) "Enrolled Resident" else "Vacant for allocation",
                                isYou = isYou
                            )
                        }
                    } else if (currentStudent?.roomNumber != null) {
                        RoommateItem(
                            bed = currentStudent.bedNumber ?: "Assigned Bed",
                            name = "${currentStudent.fullName} (You)",
                            course = "${currentStudent.course} (${currentStudent.yearOfStudy} Year)",
                            isYou = true
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No roommate allocations recorded for this room.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Room Inventory & Amenities from DB
                Text(
                    text = "Room Inventory & Fixtures",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppCard(padding = 16.dp) {
                    val amenities = room?.amenities ?: listOf("Study Table & Chair", "Wardrobe", "Power Outlets", "Fan & Lighting")
                    amenities.forEach { item ->
                        InventoryRow(item, "Active & Functional")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // House Rules Card
                Text(
                    text = "Hostel & Room Rules",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppCard(padding = 16.dp) {
                    RuleItem("Night Curfew is strictly enforced at 10:30 PM.")
                    RuleItem("Quiet hours: 11:00 PM to 6:00 AM daily.")
                    RuleItem("Cooking inside rooms using heating coils is prohibited.")
                    RuleItem("Day visitors are permitted in common lounge areas only.")
                }
            }
        }
    }
}

@Composable
private fun RoommateItem(
    bed: String,
    name: String,
    course: String,
    isYou: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (isYou) StudentAccent else StudentAccentContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = if (isYou) Color.White else StudentOnAccentContainer,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    text = bed,
                    statusType = if (isYou) BadgeStatusType.SUCCESS else BadgeStatusType.INFO
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = course,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InventoryRow(
    item: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun RuleItem(rule: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.titleMedium,
            color = StudentAccent,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = rule,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
