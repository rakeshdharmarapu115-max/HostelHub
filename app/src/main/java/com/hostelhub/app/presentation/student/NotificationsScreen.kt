package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.AppNotification
import com.hostelhub.app.domain.model.NotificationType
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.EmptyStateView
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationsScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val notifState by studentViewModel?.notifications?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val notificationsList = (notifState as? UiState.Success)?.data ?: emptyList()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Notifications & Alerts",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        if (notificationsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundCool)
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateView(
                    title = "No New Notifications",
                    message = "You are all caught up! Updates regarding hostel rules, fees, and tickets will appear here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundCool)
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notificationsList) { notif ->
                    AppCard(
                        padding = 16.dp,
                        onClick = {
                            studentViewModel?.markNotificationAsRead(notif.notificationId)
                        }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(if (notif.isRead) PrimaryContainer.copy(alpha = 0.5f) else SecondaryTeal.copy(alpha = 0.15f), shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getNotifIcon(notif.type),
                                    contentDescription = null,
                                    tint = if (notif.isRead) PrimaryNavy else SecondaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(notif.createdAt)),
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

private fun getNotifIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.PAYMENT_CONFIRMED -> Icons.Default.CheckCircle
        NotificationType.PAYMENT_DUE -> Icons.Default.Payment
        NotificationType.COMPLAINT_UPDATE -> Icons.Default.Assignment
        NotificationType.ANNOUNCEMENT -> Icons.Default.Campaign
        NotificationType.LEAVE_APPROVED -> Icons.Default.CheckCircle
        NotificationType.ATTENDANCE_ALERT -> Icons.Default.Warning
    }
}
