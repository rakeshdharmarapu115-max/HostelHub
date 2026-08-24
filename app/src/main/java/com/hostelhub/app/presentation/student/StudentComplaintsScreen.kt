package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Complaint
import com.hostelhub.app.domain.model.ComplaintCategory
import com.hostelhub.app.domain.model.ComplaintStatus
import com.hostelhub.app.domain.model.ComplaintUrgency
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@Composable
fun StudentComplaintsScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateToNewComplaint: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val filters = listOf("All", "Open", "In Progress", "Resolved")
    var selectedFilter by remember { mutableStateOf("All") }

    val complaintsState by studentViewModel?.complaints?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val complaintsList = (complaintsState as? UiState.Success)?.data ?: emptyList()

    val filteredList = complaintsList.filter {
        when (selectedFilter) {
            "Open" -> it.status == ComplaintStatus.OPEN
            "In Progress" -> it.status == ComplaintStatus.IN_PROGRESS
            "Resolved" -> it.status == ComplaintStatus.RESOLVED
            else -> true
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Complaints & Maintenance",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewComplaint,
                containerColor = PrimaryNavy,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "New Complaint")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Ticket", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Category / Status Filter chips
            FilterChipRow(
                items = filters,
                selectedItem = selectedFilter,
                onItemSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Tickets in this Category",
                    message = "Submit a maintenance request or issue ticket using the button below."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { complaint ->
                        AppCard(
                            padding = 16.dp,
                            onClick = { onNavigateToDetails(complaint.complaintId) }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    StatusBadge(
                                        text = complaint.category.name,
                                        statusType = BadgeStatusType.INFO
                                    )
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

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = complaint.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = complaint.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )

                                if (!complaint.resolutionSummary.isNullOrBlank() || !complaint.hostNotes.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        color = SecondaryContainer.copy(alpha = 0.6f),
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                            Text(
                                                text = "Warden / Staff Solution:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                color = PrimaryNavy
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = complaint.resolutionSummary ?: complaint.hostNotes ?: "",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Construction,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = complaint.assignedStaffName ?: "Pending Assignment",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Details",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SecondaryTeal
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = SecondaryTeal,
                                            modifier = Modifier.size(16.dp)
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
}
