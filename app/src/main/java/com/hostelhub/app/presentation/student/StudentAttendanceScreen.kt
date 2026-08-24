package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.AttendanceRecord
import com.hostelhub.app.domain.model.AttendanceStatus
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentAttendanceScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    var showLeaveModal by remember { mutableStateOf(false) }
    var leaveStartDate by remember { mutableStateOf("") }
    var leaveEndDate by remember { mutableStateOf("") }
    var leaveReason by remember { mutableStateOf("") }

    val attendanceState by studentViewModel?.attendance?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val records = (attendanceState as? UiState.Success)?.data ?: emptyList()
    val presentCount = records.count { it.status == AttendanceStatus.PRESENT }
    val absentCount = records.count { it.status == AttendanceStatus.ABSENT }
    val leaveCount = records.count { it.status == AttendanceStatus.ON_LEAVE }
    val totalCount = maxOf(1, records.size)
    val percentage = if (records.isNotEmpty()) (presentCount.toFloat() / totalCount) else 0.95f

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Attendance Details",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showLeaveModal = true },
                containerColor = SecondaryTeal,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Apply Leave")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Leave", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Overview Summary Card from DB
                AppCard(padding = 20.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Monthly Attendance",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Current Academic Term",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "• Present: $presentCount Day(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryTeal
                            )
                            Text(
                                text = "• Absent: $absentCount Day(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "• On Leave: $leaveCount Day(s)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        CircularAttendanceIndicator(
                            percentage = percentage,
                            size = 90.dp,
                            label = "Attendance"
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Roll-Call History",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = {
                        studentViewModel?.markSelfAttendance()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = SecondaryTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Daily Check-In", color = SecondaryTeal)
                    }
                }
            }

            if (records.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Attendance Records Yet",
                        message = "Mark your daily roll call check-in using the trigger above."
                    )
                }
            } else {
                items(records) { record ->
                    AppCard(padding = 14.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = record.date,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (!record.remarks.isNullOrBlank()) record.remarks else "Marked by ${record.markedBy ?: "Host"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(
                                text = record.status.name.replace("_", " "),
                                statusType = when (record.status) {
                                    AttendanceStatus.PRESENT -> BadgeStatusType.SUCCESS
                                    AttendanceStatus.ABSENT -> BadgeStatusType.ERROR
                                    AttendanceStatus.ON_LEAVE -> BadgeStatusType.WARNING
                                    AttendanceStatus.LATE -> BadgeStatusType.WARNING
                                }
                            )
                        }
                    }
                }
            }
        }

        // Apply Leave Bottom Sheet
        if (showLeaveModal) {
            ModalBottomSheet(
                onDismissRequest = { showLeaveModal = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Apply for Hostel Leave",
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = leaveStartDate,
                        onValueChange = { leaveStartDate = it },
                        label = "Leave Start Date",
                        placeholder = "YYYY-MM-DD"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = leaveEndDate,
                        onValueChange = { leaveEndDate = it },
                        label = "Leave End Date",
                        placeholder = "YYYY-MM-DD"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it },
                        label = "Reason for Leave",
                        placeholder = "e.g. Vacation, Medical, Family Emergency",
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "Submit Leave Request",
                        onClick = {
                            studentViewModel?.markSelfAttendance(status = AttendanceStatus.ON_LEAVE)
                            showLeaveModal = false
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
