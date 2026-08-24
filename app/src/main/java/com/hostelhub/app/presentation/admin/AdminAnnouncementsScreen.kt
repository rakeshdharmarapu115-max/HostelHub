package com.hostelhub.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnnouncementsScreen(
    adminViewModel: AdminViewModel? = null,
    onNavigateBack: () -> Unit
) {
    var showCreateModal by remember { mutableStateOf(false) }
    var policyTitle by remember { mutableStateOf("") }
    var policyBody by remember { mutableStateOf("") }
    var selectedAudience by remember { mutableStateOf("HOSTEL_OWNERS") }
    var selectedPriority by remember { mutableStateOf(AnnouncementPriority.IMPORTANT) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val announcementsState by adminViewModel?.announcements?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val noticesList = (announcementsState as? UiState.Success)?.data ?: listOf(
        Announcement(
            announcementId = "ann_01",
            title = "Annual Safety & Fire Compliance Inspection 2026",
            message = "All hostel owners and wardens are mandated to submit certified fire extinguisher audit and emergency exit clearance reports by month end.",
            senderName = "Campus Housing Association Head",
            targetAudience = "HOSTEL_OWNERS",
            priority = AnnouncementPriority.URGENT,
            createdAt = System.currentTimeMillis() - 86400000L
        ),
        Announcement(
            announcementId = "ann_02",
            title = "Campus-Wide Standardized Mess Tariff Baseline",
            message = "The Association has enacted a standardized ceiling on meal plans across all affiliated hostels to protect student affordability.",
            senderName = "Campus Housing Association Head",
            targetAudience = "ALL",
            priority = AnnouncementPriority.IMPORTANT,
            createdAt = System.currentTimeMillis() - 172800000L
        )
    )

    val filteredList = when (selectedFilter) {
        "HOSTEL_OWNERS" -> noticesList.filter { it.targetAudience.contains("OWNER", ignoreCase = true) || it.targetAudience.contains("HOST", ignoreCase = true) }
        "STUDENTS" -> noticesList.filter { it.targetAudience.contains("STUDENT", ignoreCase = true) }
        else -> noticesList
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Association Directives & Circulars",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateModal = true },
                containerColor = AdminAccent,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "New Directive")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Publish Directive", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminBackground)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Audience Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Directives (${noticesList.size})") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedFilter == "HOSTEL_OWNERS",
                    onClick = { selectedFilter = "HOSTEL_OWNERS" },
                    label = { Text("🏢 To Owners") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedFilter == "STUDENTS",
                    onClick = { selectedFilter = "STUDENTS" },
                    label = { Text("🎓 To Students") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredList.isEmpty()) {
                EmptyStateView(
                    title = "No Directives Found",
                    message = "Official circulars and policies published by the association head will appear here."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredList) { notice ->
                        AppCard(padding = 16.dp) {
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
                                            .size(40.dp)
                                            .background(AdminAccentContainer, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Campaign,
                                            contentDescription = null,
                                            tint = AdminOnAccentContainer,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = notice.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Target: ${if (notice.targetAudience == "HOSTEL_OWNERS") "🏢 Hostel Owners" else if (notice.targetAudience == "STUDENTS") "🎓 Students" else "🌐 All Campus"} • ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(notice.createdAt))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AdminOnAccentContainer
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = notice.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(
                                    text = notice.priority.name,
                                    statusType = when (notice.priority) {
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
                        text = "Broadcast Association Directive",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = AdminOnAccentContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Publish official mandates, compliance circulars, or notifications to hostel owners or residents.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Target Recipient Audience",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedAudience == "HOSTEL_OWNERS",
                            onClick = { selectedAudience = "HOSTEL_OWNERS" },
                            label = { Text("🏢 Hostel Owners") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedAudience == "STUDENTS",
                            onClick = { selectedAudience = "STUDENTS" },
                            label = { Text("🎓 Students") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedAudience == "ALL",
                            onClick = { selectedAudience = "ALL" },
                            label = { Text("🌐 All") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Priority Classification",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedPriority == AnnouncementPriority.NORMAL,
                            onClick = { selectedPriority = AnnouncementPriority.NORMAL },
                            label = { Text("Normal") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedPriority == AnnouncementPriority.IMPORTANT,
                            onClick = { selectedPriority = AnnouncementPriority.IMPORTANT },
                            label = { Text("Important") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedPriority == AnnouncementPriority.URGENT,
                            onClick = { selectedPriority = AnnouncementPriority.URGENT },
                            label = { Text("Urgent") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = policyTitle,
                        onValueChange = { policyTitle = it },
                        label = "Directive Subject",
                        placeholder = "e.g. Annual Hostel Safety & Fee Compliance 2026"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = policyBody,
                        onValueChange = { policyBody = it },
                        label = "Directive Content & Mandate",
                        placeholder = "Provide complete regulations, deadline, and compliance instructions.",
                        singleLine = false
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "📢 Broadcast Directive Now",
                        onClick = {
                            if (policyTitle.isNotBlank() && policyBody.isNotBlank()) {
                                adminViewModel?.broadcastAnnouncement(
                                    title = policyTitle,
                                    message = policyBody,
                                    priority = selectedPriority,
                                    targetAudience = selectedAudience
                                )
                                policyTitle = ""
                                policyBody = ""
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
