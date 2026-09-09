package com.hostelhub.app.presentation.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Roommate
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
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        studentViewModel?.loadMyRoomDetails()
    }

    val profileState by studentViewModel?.studentProfile?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val myRoomState by studentViewModel?.myRoomDetails?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val currentStudent = (profileState as? UiState.Success)?.data
    val myRoomDetails = (myRoomState as? UiState.Success)?.data
    val room = myRoomDetails?.room ?: (studentViewModel?.roomDetails?.collectAsState()?.value as? UiState.Success)?.data
    val roommates = myRoomDetails?.roommates ?: emptyList()

    val isLoading = profileState is UiState.Loading || (myRoomState is UiState.Loading && room == null)

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
                // 1. Room Overview Card
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
                                text = if (room != null) "Block ${room.block} • Floor ${room.floor} • ${room.roomType.name} Sharing" else (currentStudent?.hostelName ?: "Contact hostel warden for room allocation"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        StatusBadge(
                            text = if (!myRoomDetails?.myBed.isNullOrBlank()) "${myRoomDetails?.myBed} (Assigned)" else if (!currentStudent?.bedNumber.isNullOrBlank()) "${currentStudent?.bedNumber} (Assigned)" else if (room != null) "Active" else "Pending",
                            statusType = if (room != null || !currentStudent?.roomNumber.isNullOrBlank()) BadgeStatusType.SUCCESS else BadgeStatusType.WARNING,
                            customBgColor = Color.White,
                            customTextColor = PrimaryNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 2. Roommates Section (Only Roommates in the Same Room)
                Text(
                    text = "Roommates (${roommates.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppCard(padding = 16.dp) {
                    if (room == null && currentStudent?.roomNumber.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No room assigned. Please contact your hostel warden.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (roommates.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No roommates assigned.",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "You are currently the only assigned occupant in this room.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            roommates.forEach { roommate ->
                                RoommateCard(
                                    roommate = roommate,
                                    onCall = { phone ->
                                        if (phone.isNotBlank()) {
                                            try {
                                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
                                                context.startActivity(dialIntent)
                                            } catch (_: Exception) {}
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Room Inventory & Amenities
                Text(
                    text = "Room Inventory & Fixtures",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppCard(padding = 16.dp) {
                    val amenities = room?.amenities?.takeIf { it.isNotEmpty() } ?: listOf("Study Table & Chair", "Wardrobe", "Power Outlets", "Fan & Lighting")
                    amenities.forEach { item ->
                        InventoryRow(item, "Active & Functional")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. House Rules Card
                Text(
                    text = "Hostel & Room Rules",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
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
private fun RoommateCard(
    roommate: Roommate,
    onCall: (String) -> Unit
) {
    Surface(
        color = StudentAccentContainer.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(StudentAccent, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = roommate.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (roommate.bedNumber.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(
                                text = roommate.bedNumber,
                                statusType = BadgeStatusType.INFO
                            )
                        }
                    }
                    if (roommate.course.isNotBlank()) {
                        Text(
                            text = "${roommate.course} (${roommate.yearOfStudy} Year)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (roommate.phoneNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📞 ${roommate.phoneNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = StudentOnAccentContainer
                        )
                    }
                }
            }

            if (roommate.phoneNumber.isNotBlank()) {
                IconButton(
                    onClick = { onCall(roommate.phoneNumber) },
                    modifier = Modifier
                        .background(StudentAccent, CircleShape)
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Call Roommate",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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
