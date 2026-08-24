package com.hostelhub.app.presentation.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.AttendanceRecord
import com.hostelhub.app.domain.model.AttendanceStatus
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HostAttendanceScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val attendanceState by hostViewModel?.todayAttendance?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val residentsState by hostViewModel?.residents?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val todayAttendance = (attendanceState as? UiState.Success)?.data ?: emptyList()
    val residents = (residentsState as? UiState.Success)?.data ?: emptyList()

    val presentCount = todayAttendance.count { it.status == AttendanceStatus.PRESENT }
    val leaveCount = todayAttendance.count { it.status == AttendanceStatus.ON_LEAVE }
    val absentCount = todayAttendance.count { it.status == AttendanceStatus.ABSENT }

    val todayStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
    val todayDateCode = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Daily Attendance Roll-Call",
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
            AppCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Today: $todayStr",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$presentCount Present • $leaveCount Leave • $absentCount Absent",
                            style = MaterialTheme.typography.bodySmall,
                            color = SecondaryTeal
                        )
                    }
                    AppButton(
                        text = "Mark All Present",
                        onClick = {
                            val newRecords = residents.map { res ->
                                AttendanceRecord(
                                    attendanceId = "att_${res.studentId}_${System.currentTimeMillis()}",
                                    hostelId = res.hostelId ?: (hostViewModel?.currentHostelId?.value ?: ""),
                                    studentId = res.studentId,
                                    studentName = res.fullName,
                                    roomNumber = res.roomNumber ?: "N/A",
                                    date = todayDateCode,
                                    status = AttendanceStatus.PRESENT,
                                    checkInTime = System.currentTimeMillis(),
                                    markedBy = "WARDEN_HOST"
                                )
                            }
                            hostViewModel?.markBatchAttendance(newRecords)
                        },
                        isFullWidth = false,
                        variant = ButtonVariant.SECONDARY
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (residents.isEmpty()) {
                EmptyStateView(
                    title = "No Residents Registered",
                    message = "Students allocated to hostel rooms will appear here for night curfew roll-call."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(residents) { student ->
                        val record = todayAttendance.find { it.studentId == student.studentId }
                        val currentStatus = record?.status ?: AttendanceStatus.PRESENT

                        AppCard(padding = 14.dp) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = student.fullName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${student.roomNumber ?: "N/A"} (${student.bedNumber ?: "Unallocated"}) • ${student.rollNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = currentStatus == AttendanceStatus.PRESENT,
                                        onClick = {
                                            val newRec = AttendanceRecord(
                                                attendanceId = "att_${student.studentId}_${System.currentTimeMillis()}",
                                                hostelId = student.hostelId ?: (hostViewModel?.currentHostelId?.value ?: ""),
                                                studentId = student.studentId,
                                                studentName = student.fullName,
                                                roomNumber = student.roomNumber ?: "N/A",
                                                date = todayDateCode,
                                                status = AttendanceStatus.PRESENT,
                                                markedBy = "WARDEN_HOST"
                                            )
                                            hostViewModel?.markBatchAttendance(listOf(newRec))
                                        },
                                        label = { Text("P", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = currentStatus == AttendanceStatus.ABSENT,
                                        onClick = {
                                            val newRec = AttendanceRecord(
                                                attendanceId = "att_${student.studentId}_${System.currentTimeMillis()}",
                                                hostelId = student.hostelId ?: (hostViewModel?.currentHostelId?.value ?: ""),
                                                studentId = student.studentId,
                                                studentName = student.fullName,
                                                roomNumber = student.roomNumber ?: "N/A",
                                                date = todayDateCode,
                                                status = AttendanceStatus.ABSENT,
                                                markedBy = "WARDEN_HOST"
                                            )
                                            hostViewModel?.markBatchAttendance(listOf(newRec))
                                        },
                                        label = { Text("A", style = MaterialTheme.typography.labelSmall) }
                                    )
                                    FilterChip(
                                        selected = currentStatus == AttendanceStatus.ON_LEAVE,
                                        onClick = {
                                            val newRec = AttendanceRecord(
                                                attendanceId = "att_${student.studentId}_${System.currentTimeMillis()}",
                                                hostelId = student.hostelId ?: (hostViewModel?.currentHostelId?.value ?: ""),
                                                studentId = student.studentId,
                                                studentName = student.fullName,
                                                roomNumber = student.roomNumber ?: "N/A",
                                                date = todayDateCode,
                                                status = AttendanceStatus.ON_LEAVE,
                                                markedBy = "WARDEN_HOST"
                                            )
                                            hostViewModel?.markBatchAttendance(listOf(newRec))
                                        },
                                        label = { Text("L", style = MaterialTheme.typography.labelSmall) }
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
