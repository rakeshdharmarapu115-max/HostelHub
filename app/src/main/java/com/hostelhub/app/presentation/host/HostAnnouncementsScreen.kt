package com.hostelhub.app.presentation.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Announcement
import com.hostelhub.app.domain.model.AnnouncementPriority
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostAnnouncementsScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    var showCreateModal by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val announcementsState by hostViewModel?.announcements?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val announcementsList = (announcementsState as? UiState.Success)?.data ?: listOf(
        Announcement(
            announcementId = "dir_01",
            title = "Annual Safety & Fire Compliance Inspection 2026",
            message = "All hostel owners and wardens are mandated to submit certified fire extinguisher audit and emergency exit clearance reports by month end.",
            senderName = "Campus Housing Association Head",
            senderRole = UserRole.ADMIN,
            priority = AnnouncementPriority.URGENT,
            targetAudience = "HOSTEL_OWNERS",
            createdAt = System.currentTimeMillis() - 86400000L
        ),
        Announcement(
            announcementId = "dir_02",
            title = "Campus-Wide Standardized Mess Tariff Baseline",
            message = "The Association has enacted a standardized ceiling on meal plans across all affiliated hostels to protect student affordability.",
            senderName = "Campus Housing Association Head",
            senderRole = UserRole.ADMIN,
            priority = AnnouncementPriority.IMPORTANT,
            targetAudience = "ALL",
            createdAt = System.currentTimeMillis() - 172800000L
        ),
        Announcement(
            announcementId = "anc_01",
            title = "Scheduled Water Tank & RO Filter Maintenance",
            message = "Water supply will be temporarily suspended tomorrow between 10 AM and 1 PM for regular hygiene maintenance.",
            senderName = "Robert Vance (Warden)",
            senderRole = UserRole.HOST,
            priority = AnnouncementPriority.NORMAL,
            targetAudience = "STUDENTS",
            createdAt = System.currentTimeMillis() - 43200000L
        )
    )

    val filteredList = when (selectedFilter) {
        "COUNCIL" -> announcementsList.filter { it.senderRole == UserRole.ADMIN }
        "HOSTEL" -> announcementsList.filter { it.senderRole != UserRole.ADMIN }
        else -> announcementsList
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Directives & Hostel Notices",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateModal = true },
                containerColor = HostAccent,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Broadcast")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Notice", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HostBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${announcementsList.size})") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedFilter == "COUNCIL",
                    onClick = { selectedFilter = "COUNCIL" },
                    label = { Text("🏛️ Council Directives") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedFilter == "HOSTEL",
                    onClick = { selectedFilter = "HOSTEL" },
                    label = { Text("📢 My Notices") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Notices Found",
                    message = "Official directives from the Association Head and notices published by wardens will appear here."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { anc ->
                        val isCouncilDirective = anc.senderRole == UserRole.ADMIN

                        AppCard(
                            padding = 16.dp,
                            backgroundColor = if (isCouncilDirective) AdminAccentContainer.copy(alpha = 0.25f) else SurfaceWhite
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(
                                                if (isCouncilDirective) AdminHeroBg else HostAccentContainer,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = if (isCouncilDirective) Color.White else HostOnAccentContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        if (isCouncilDirective) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "🏛️ Association Head Mandate",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AdminOnAccentContainer
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                        }
                                        Text(
                                            text = anc.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "By ${anc.senderName} • ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(anc.createdAt))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = anc.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(
                                    text = anc.priority.name,
                                    statusType = when (anc.priority) {
                                        AnnouncementPriority.URGENT -> BadgeStatusType.ERROR
                                        AnnouncementPriority.IMPORTANT -> BadgeStatusType.WARNING
                                        AnnouncementPriority.NORMAL -> BadgeStatusType.INFO
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showCreateModal) {
            ModalBottomSheet(
                onDismissRequest = { showCreateModal = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Broadcast Notice to Hostel Residents",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = HostHeroBg
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notify your hostel residents about water maintenance, curfew timings, meal updates, or hostel events.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = "Notice Subject",
                        placeholder = "e.g. Scheduled Water Tank Cleaning"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        label = "Detailed Message",
                        placeholder = "Provide details, instructions, and schedule.",
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "📢 Publish Notice",
                        onClick = {
                            if (titleInput.isNotBlank() && messageInput.isNotBlank()) {
                                hostViewModel?.publishAnnouncement(
                                    title = titleInput,
                                    message = messageInput,
                                    priority = AnnouncementPriority.IMPORTANT
                                )
                                titleInput = ""
                                messageInput = ""
                                showCreateModal = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
