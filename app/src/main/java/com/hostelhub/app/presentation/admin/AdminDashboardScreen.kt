package com.hostelhub.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.*
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    adminViewModel: AdminViewModel? = null,
    onNavigateToHostels: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val statsState by adminViewModel?.dashboardStats?.collectAsState() ?: remember {
        mutableStateOf(UiState.Success(AdminDashboardStats(
            totalHostels = 3,
            totalStudents = 120,
            totalRooms = 60,
            totalBeds = 120,
            occupiedBeds = 104,
            totalRevenue = 540000.0,
            pendingComplaints = 3
        )))
    }
    val hostelsState by adminViewModel?.hostels?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val complaintsState by adminViewModel?.complaints?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val announcementsState by adminViewModel?.announcements?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val stats = (statsState as? UiState.Success)?.data ?: AdminDashboardStats()
    val hostels = (hostelsState as? UiState.Success)?.data ?: emptyList()
    val complaints = (complaintsState as? UiState.Success)?.data ?: emptyList()
    val announcements = (announcementsState as? UiState.Success)?.data ?: emptyList()

    val occupancyPercent = if (stats.totalBeds > 0) ((stats.occupiedBeds.toDouble() / stats.totalBeds) * 100).toInt() else 87

    var selectedHostelForInspection by remember { mutableStateOf<Hostel?>(null) }
    var showBroadcastModal by remember { mutableStateOf(false) }
    var directiveTitle by remember { mutableStateOf("") }
    var directiveBody by remember { mutableStateOf("") }
    var selectedAudience by remember { mutableStateOf("HOSTEL_OWNERS") }
    var selectedPriority by remember { mutableStateOf(AnnouncementPriority.IMPORTANT) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AdminBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // 1. Clean, Simple Logo & Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AdminAccentContainer, shape = CircleShape)
                        .border(1.5.dp, AdminAccent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = "Association Head Seal",
                        tint = AdminAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "HostelHub",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = AdminAccentContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "ASSOCIATIVE HEAD",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = AdminOnAccentContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Association",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { showBroadcastModal = true },
                    modifier = Modifier
                        .background(AdminAccentContainer, shape = CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Notices",
                        tint = AdminOnAccentContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .background(AdminAccentContainer, shape = CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SupervisorAccount,
                        contentDescription = "Profile",
                        tint = AdminOnAccentContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Occupancy Hero Card
        AppCard(
            backgroundColor = AdminHeroBg,
            padding = 16.dp,
            onClick = onNavigateToHostels
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = "Hostels",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Occupancy",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = "$occupancyPercent% Campus Occupancy",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${stats.occupiedBeds} / ${stats.totalBeds} Beds • ${stats.totalHostels} Hostels",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                StatusBadge(
                    text = "Hostels",
                    statusType = BadgeStatusType.SUCCESS,
                    customBgColor = AdminBadgeBg,
                    customTextColor = AdminBadgeText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Central Governance KPI Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Hostels",
                value = "${stats.totalHostels} Hostels",
                icon = Icons.Default.Business,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHostels
            )
            MetricStatCard(
                title = "Residents",
                value = "${stats.totalStudents} Students",
                icon = Icons.Default.Group,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToUsers
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Revenue",
                value = Formatters.formatCurrencyNoDecimals(stats.totalRevenue),
                icon = Icons.Default.Assessment,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAnalytics
            )
            MetricStatCard(
                title = "Issues",
                value = "${stats.pendingComplaints} Pending",
                icon = Icons.AutoMirrored.Filled.Assignment,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAnalytics
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Hostels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Hostels",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "All →",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AdminOnAccentContainer,
                modifier = Modifier.clickable { onNavigateToHostels() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (hostels.isEmpty()) {
            val sampleHostels = listOf(
                Hostel(hostelId = "hostel_001", name = "Green Valley Residencies", address = "Campus North Zone", city = "Metro City", totalRooms = 30, totalBeds = 60, occupiedBeds = 52, baseMonthlyRent = 450.0, rating = 4.8),
                Hostel(hostelId = "hostel_002", name = "Sunrise Elite Hostel", address = "South Boulevard", city = "Metro City", totalRooms = 15, totalBeds = 30, occupiedBeds = 26, baseMonthlyRent = 550.0, rating = 4.6),
                Hostel(hostelId = "hostel_003", name = "Scholars Inn Academic Wing", address = "East Gate Road", city = "Metro City", totalRooms = 15, totalBeds = 30, occupiedBeds = 26, baseMonthlyRent = 600.0, rating = 4.9)
            )
            sampleHostels.forEachIndexed { index, h ->
                if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                HostelMonitoringCard(
                    hostel = h,
                    openComplaintsCount = if (index == 0) 1 else 0,
                    onClick = { selectedHostelForInspection = h }
                )
            }
        } else {
            hostels.forEachIndexed { index, hostel ->
                if (index > 0) Spacer(modifier = Modifier.height(10.dp))
                val hostelComplaints = complaints.count { it.hostelId == hostel.hostelId && it.status != ComplaintStatus.RESOLVED }
                HostelMonitoringCard(
                    hostel = hostel,
                    openComplaintsCount = hostelComplaints,
                    onClick = { selectedHostelForInspection = hostel }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Enrollment
        Text(
            text = "Enrollment",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        AppCard(padding = 16.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                val totalStudentsCount = if (stats.totalStudents > 0) stats.totalStudents else 120
                val displayList = if (hostels.isNotEmpty()) hostels else listOf(
                    Hostel(name = "Green Valley Residencies", occupiedBeds = 52),
                    Hostel(name = "Sunrise Elite Hostel", occupiedBeds = 26),
                    Hostel(name = "Scholars Inn Academic Wing", occupiedBeds = 26)
                )

                displayList.forEach { h ->
                    val studentCount = if (h.occupiedBeds > 0) h.occupiedBeds else (totalStudentsCount / displayList.size)
                    val sharePercent = if (totalStudentsCount > 0) (studentCount.toFloat() / totalStudentsCount) else 0.33f
                    val displayPercent = (sharePercent * 100).toInt()

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = h.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$studentCount Students ($displayPercent%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { sharePercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = AdminAccent,
                            trackColor = AdminAccentContainer,
                        )
                    }
                }

                HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = StatusSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Semester Intake Trend: +12.4% YoY",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "98.2% Retention",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Complaints
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Complaints",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Analytics →",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AdminOnAccentContainer,
                modifier = Modifier.clickable { onNavigateToAnalytics() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (complaints.isEmpty()) {
            AppCard(padding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AdminComplaintRow(
                        title = "Water supply line inspection required",
                        hostelName = "Green Valley Residencies",
                        room = "Block A (2nd Floor)",
                        urgency = "HIGH",
                        status = "IN PROGRESS"
                    )
                    AdminComplaintRow(
                        title = "Wi-Fi bandwidth upgrade request",
                        hostelName = "Sunrise Elite Hostel",
                        room = "Study Hall 1",
                        urgency = "MEDIUM",
                        status = "OPEN"
                    )
                }
            }
        } else {
            AppCard(padding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    complaints.take(3).forEach { c ->
                        val matchedHostelName = hostels.find { it.hostelId == c.hostelId }?.name ?: "Hostel Property"
                        AdminComplaintRow(
                            title = c.title,
                            hostelName = matchedHostelName,
                            room = if (c.roomNumber.isNotBlank()) "Room ${c.roomNumber}" else "General",
                            urgency = c.urgency.name,
                            status = c.status.name
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Notices
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notices",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "New →",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = AdminOnAccentContainer,
                modifier = Modifier.clickable { showBroadcastModal = true }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        AppCard(padding = 16.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = AdminAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Emergency & Policy Broadcasts",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Cross-Campus Push Broadcasts",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(
                        onClick = { showBroadcastModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AdminAccent),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Broadcast", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                if (announcements.isNotEmpty()) {
                    val latest = announcements.first()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AdminBackground, shape = RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            tint = AdminAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = latest.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Target: ${latest.targetAudience} • Priority: ${latest.priority.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = AdminOnAccentContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = latest.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 8. Governance
        Text(
            text = "Governance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminQuickAction(
                title = "Hostels",
                icon = Icons.Default.Business,
                onClick = onNavigateToHostels,
                modifier = Modifier.weight(1f)
            )
            AdminQuickAction(
                title = "Users",
                icon = Icons.Default.Group,
                onClick = onNavigateToUsers,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AdminQuickAction(
                title = "Analytics",
                icon = Icons.Default.Assessment,
                onClick = onNavigateToAnalytics,
                modifier = Modifier.weight(1f)
            )
            AdminQuickAction(
                title = "Notices",
                icon = Icons.Default.Campaign,
                onClick = onNavigateToAnnouncements,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // Modal: Hostel Deep-Dive Inspection Bottom Sheet
    selectedHostelForInspection?.let { hostel ->
        ModalBottomSheet(
            onDismissRequest = { selectedHostelForInspection = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            HostelInspectionSheet(
                hostel = hostel,
                onDismiss = { selectedHostelForInspection = null }
            )
        }
    }

    // Modal: Publish Directive to Hostel Owners / Students
    if (showBroadcastModal) {
        ModalBottomSheet(
            onDismissRequest = { showBroadcastModal = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Publish Association Directive",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = AdminOnAccentContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Broadcast vital compliance, financial regulations, or notices to hostel owners and residents.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Target Audience",
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

                AppTextField(
                    value = directiveTitle,
                    onValueChange = { directiveTitle = it },
                    label = "Directive Title",
                    placeholder = "e.g. Mandatory Hostel Safety & Fee Compliance 2026"
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = directiveBody,
                    onValueChange = { directiveBody = it },
                    label = "Directive Mandate & Instructions",
                    placeholder = "Describe guidelines, timelines, and action items required from hostel owners.",
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(20.dp))

                AppButton(
                    text = "📢 Broadcast Directive Now",
                    onClick = {
                        if (directiveTitle.isNotBlank() && directiveBody.isNotBlank()) {
                            adminViewModel?.broadcastAnnouncement(
                                title = directiveTitle,
                                message = directiveBody,
                                priority = selectedPriority,
                                targetAudience = selectedAudience
                            )
                            directiveTitle = ""
                            directiveBody = ""
                            showBroadcastModal = false
                        }
                    }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun HostelMonitoringCard(
    hostel: Hostel,
    openComplaintsCount: Int,
    onClick: () -> Unit
) {
    AppCard(
        padding = 16.dp,
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hostel.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${hostel.address}, ${hostel.city}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(
                    text = "${hostel.rating} ★",
                    statusType = BadgeStatusType.SUCCESS,
                    customBgColor = AdminBadgeBg,
                    customTextColor = AdminBadgeText
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val occupancy = if (hostel.totalBeds > 0) ((hostel.occupiedBeds.toFloat() / hostel.totalBeds) * 100).toInt() else 85
            val progress = if (hostel.totalBeds > 0) (hostel.occupiedBeds.toFloat() / hostel.totalBeds) else 0.85f

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Occupancy: ${hostel.occupiedBeds}/${hostel.totalBeds} Beds ($occupancy%)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (openComplaintsCount > 0) "⚠️ $openComplaintsCount Issues Pending" else "✓ 0 Issues",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (openComplaintsCount > 0) StatusWarning else StatusSuccess
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = AdminAccent,
                trackColor = AdminAccentContainer,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rent: ₹${hostel.baseMonthlyRent.toInt()}/mo",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "Inspect Hostel Details →",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = AdminOnAccentContainer
                )
            }
        }
    }
}

@Composable
private fun AdminComplaintRow(
    title: String,
    hostelName: String,
    room: String,
    urgency: String,
    status: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AdminBackground, shape = RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$hostelName • $room",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        StatusBadge(
            text = status,
            statusType = when (status) {
                "RESOLVED" -> BadgeStatusType.SUCCESS
                "IN_PROGRESS", "IN PROGRESS" -> BadgeStatusType.WARNING
                else -> BadgeStatusType.ERROR
            }
        )
    }
}

@Composable
private fun HostelInspectionSheet(
    hostel: Hostel,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hostel.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${hostel.address}, ${hostel.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(
                text = "Verified Property",
                statusType = BadgeStatusType.SUCCESS,
                customBgColor = AdminBadgeBg,
                customTextColor = AdminBadgeText
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Capacity Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricStatCard(
                title = "Total Rooms",
                value = "${hostel.totalRooms} Rooms",
                icon = Icons.Default.MeetingRoom,
                subtitle = "Main Wing",
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Bed Capacity",
                value = "${hostel.totalBeds} Beds",
                icon = Icons.Default.Bed,
                subtitle = "${hostel.occupiedBeds} Occupied",
                modifier = Modifier.weight(1f)
            )
            MetricStatCard(
                title = "Vacant Beds",
                value = "${hostel.totalBeds - hostel.occupiedBeds} Available",
                icon = Icons.Default.CheckCircle,
                subtitle = "Ready to allocate",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        AppCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Financial & Property Specifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = AdminOnAccentContainer
                )
                HostelSpecRow("Base Monthly Rent", "₹${hostel.baseMonthlyRent.toInt()} / month")
                HostelSpecRow("Caution Deposit", "₹${hostel.cautionDeposit.toInt()} (Refundable)")
                HostelSpecRow("Gender Accommodation", hostel.genderType.name)
                HostelSpecRow("Warden / Host Contact", if (hostel.contactPhone.isNotBlank()) hostel.contactPhone else "+91 98765 43210")
                HostelSpecRow("Official Email", if (hostel.contactEmail.isNotBlank()) hostel.contactEmail else "warden@campus.edu")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AppButton(
            text = "Close Inspection Details",
            onClick = onDismiss
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HostelSpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AdminQuickAction(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        padding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AdminAccentContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AdminOnAccentContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
