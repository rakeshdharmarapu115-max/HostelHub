package com.hostelhub.app.presentation.host

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Complaint
import com.hostelhub.app.domain.model.ComplaintStatus
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HostComplaintsManagementScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val filters = listOf("All Complaints", "Open / In Progress", "Resolved")
    var selectedFilter by remember { mutableStateOf("All Complaints") }

    var selectedComplaintForResolution by remember { mutableStateOf<Complaint?>(null) }

    val complaintsState by hostViewModel?.complaints?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val complaintsList = (complaintsState as? UiState.Success)?.data ?: emptyList()

    val filteredList = complaintsList.filter {
        when (selectedFilter) {
            "Open / In Progress" -> it.status != ComplaintStatus.RESOLVED
            "Resolved" -> it.status == ComplaintStatus.RESOLVED
            else -> true
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Hostel Maintenance & Complaints",
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
            FilterChipRow(
                items = filters,
                selectedItem = selectedFilter,
                onItemSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Tickets in this Category",
                    message = "Maintenance requests logged by residents will appear here."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { complaint ->
                        AppCard(padding = 16.dp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = complaint.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusBadge(
                                            text = "Room ${complaint.roomNumber}",
                                            statusType = BadgeStatusType.INFO
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${complaint.studentName} • ${complaint.category.name} • ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(complaint.createdAt))}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = complaint.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (!complaint.resolutionSummary.isNullOrBlank() || !complaint.hostNotes.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = SecondaryContainer.copy(alpha = 0.5f),
                                            shape = MaterialTheme.shapes.small
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text(
                                                    text = "Resolution Solution:",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryNavy
                                                )
                                                Text(
                                                    text = complaint.resolutionSummary ?: complaint.hostNotes ?: "",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = complaint.assignedStaffName ?: "Unassigned Staff",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    StatusBadge(
                                        text = complaint.status.name.replace("_", " "),
                                        statusType = when (complaint.status) {
                                            ComplaintStatus.OPEN -> BadgeStatusType.WARNING
                                            ComplaintStatus.IN_PROGRESS -> BadgeStatusType.INFO
                                            ComplaintStatus.RESOLVED -> BadgeStatusType.SUCCESS
                                            ComplaintStatus.REJECTED -> BadgeStatusType.ERROR
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))
                                    Button(
                                        onClick = { selectedComplaintForResolution = complaint },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (complaint.status == ComplaintStatus.RESOLVED) SecondaryTeal else PrimaryNavy
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = if (complaint.status == ComplaintStatus.RESOLVED) "Update Solution" else "Solve Ticket",
                                            style = MaterialTheme.typography.labelSmall
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

    // Modal to Solve / Update Complaint Ticket
    selectedComplaintForResolution?.let { complaint ->
        var selectedStatus by remember { mutableStateOf(if (complaint.status == ComplaintStatus.RESOLVED) ComplaintStatus.RESOLVED else ComplaintStatus.IN_PROGRESS) }
        var staffName by remember { mutableStateOf(complaint.assignedStaffName ?: "Hostel Maintenance Team") }
        var solutionNotes by remember { mutableStateOf(complaint.resolutionSummary ?: complaint.hostNotes ?: "") }

        AlertDialog(
            onDismissRequest = { selectedComplaintForResolution = null },
            title = {
                Text(
                    text = "Update / Resolve Complaint",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Ticket: ${complaint.title} (Room ${complaint.roomNumber})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Resident: ${complaint.studentName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text("Update Status:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED, ComplaintStatus.REJECTED).forEach { stat ->
                            FilterChip(
                                selected = selectedStatus == stat,
                                onClick = { selectedStatus = stat },
                                label = { Text(stat.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    AppTextField(
                        value = staffName,
                        onValueChange = { staffName = it },
                        label = "Assigned Technician / Staff",
                        placeholder = "e.g. Electrician Suresh"
                    )

                    AppTextField(
                        value = solutionNotes,
                        onValueChange = { solutionNotes = it },
                        label = "Resolution Solution / Notes for Student",
                        placeholder = "e.g. Replaced faulty heater switch and tested.",
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        hostViewModel?.updateComplaintStatus(
                            complaintId = complaint.complaintId,
                            status = selectedStatus,
                            notes = solutionNotes,
                            assignedStaff = staffName,
                            resolutionSummary = solutionNotes,
                            onSuccess = {
                                Toast.makeText(context, "Complaint updated! Student notified.", Toast.LENGTH_LONG).show()
                                selectedComplaintForResolution = null
                            },
                            onError = { err ->
                                Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Save & Notify Student")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedComplaintForResolution = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
