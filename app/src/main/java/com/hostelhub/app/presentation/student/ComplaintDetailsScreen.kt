package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Complaint
import com.hostelhub.app.domain.model.ComplaintStatus
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.BadgeStatusType
import com.hostelhub.app.presentation.components.StatusBadge
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@Composable
fun ComplaintDetailsScreen(
    complaintId: String,
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val complaintsState by studentViewModel?.complaints?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val complaint = (complaintsState as? UiState.Success)?.data?.find { it.complaintId == complaintId } ?: Complaint(
        complaintId = complaintId,
        title = "Study lamp socket sparking",
        description = "Intermittent electrical spark when plugging in laptop adapter near desk 1.",
        roomNumber = "A-204",
        status = ComplaintStatus.IN_PROGRESS,
        assignedStaffName = "Carl Johnson (Electrician)",
        hostNotes = "Technician dispatched to replace the wall socket panel."
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Complaint Timeline",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Complaint Header Card
            AppCard(padding = 20.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SecondaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = PrimaryNavy,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = complaint.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ticket ID: #${complaint.complaintId} • Room ${complaint.roomNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    StatusBadge(
                        text = complaint.status.name.replace("_", " "),
                        statusType = when (complaint.status) {
                            ComplaintStatus.OPEN -> BadgeStatusType.WARNING
                            ComplaintStatus.IN_PROGRESS -> BadgeStatusType.INFO
                            ComplaintStatus.RESOLVED -> BadgeStatusType.SUCCESS
                            ComplaintStatus.REJECTED -> BadgeStatusType.ERROR
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Description",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = complaint.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Assigned Staff Card
            Text(
                text = "Assigned Support Staff",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
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
                        Text(
                            text = complaint.assignedStaffName ?: "Hostel Maintenance Desk",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (!complaint.hostNotes.isNullOrBlank()) complaint.hostNotes else "Warden Notes: Ticket logged into queue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress Timeline
            Text(
                text = "Resolution Timeline",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            AppCard(padding = 20.dp) {
                TimelineItem(
                    title = "Complaint Registered",
                    time = "Initial Submission",
                    description = "Issue reported by student and ticket generated.",
                    icon = Icons.Default.Pending,
                    isCompleted = true,
                    isLast = false
                )

                TimelineItem(
                    title = "Staff Assigned & In Progress",
                    time = "Warden Review",
                    description = if (!complaint.hostNotes.isNullOrBlank()) complaint.hostNotes else "Assigned to maintenance technician.",
                    icon = Icons.Default.Engineering,
                    isCompleted = complaint.status == ComplaintStatus.IN_PROGRESS || complaint.status == ComplaintStatus.RESOLVED,
                    isLast = false
                )

                TimelineItem(
                    title = "Issue Resolved & Closed",
                    time = if (complaint.status == ComplaintStatus.RESOLVED) "Complete" else "Pending Resolution",
                    description = if (!complaint.resolutionSummary.isNullOrBlank()) complaint.resolutionSummary else "Verification and feedback collection.",
                    icon = Icons.Default.CheckCircle,
                    isCompleted = complaint.status == ComplaintStatus.RESOLVED,
                    isLast = true
                )
            }
        }
    }
}

@Composable
private fun TimelineItem(
    title: String,
    time: String,
    description: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isLast: Boolean
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isCompleted) StatusSuccess.copy(alpha = 0.15f) else Color.LightGray.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isCompleted) StatusSuccess else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(48.dp)
                        .background(if (isCompleted) StatusSuccess.copy(alpha = 0.4f) else Color.LightGray)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (!isLast) 20.dp else 0.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
