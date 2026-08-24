package com.hostelhub.app.presentation.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

@Composable
fun HostDashboardScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateToRooms: () -> Unit,
    onNavigateToStudents: () -> Unit,
    onNavigateToComplaints: () -> Unit,
    onNavigateToFees: () -> Unit,
    onNavigateToMenu: () -> Unit,
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToAnnouncements: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val statsState by hostViewModel?.dashboardStats?.collectAsState() ?: remember {
        mutableStateOf(UiState.Success(HostDashboardStats(
            totalRooms = 30,
            totalBeds = 60,
            occupiedBeds = 52,
            availableBeds = 8,
            pendingFeeCount = 4,
            pendingFeeAmount = 18000.0,
            pendingComplaints = 3,
            todayPresent = 50
        )))
    }
    val hostelState by hostViewModel?.hostelInfo?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val announcementsState by hostViewModel?.announcements?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val stats = (statsState as? UiState.Success)?.data ?: HostDashboardStats()
    val hostelName = (hostelState as? UiState.Success)?.data?.name ?: "Green Valley Residencies"
    val occupancyRate = if (stats.totalBeds > 0) ((stats.occupiedBeds.toDouble() / stats.totalBeds) * 100).toInt() else 0
    val announcementsList = (announcementsState as? UiState.Success)?.data ?: emptyList()
    val latestCouncilDirective = announcementsList.firstOrNull {
        it.senderRole == UserRole.ADMIN || it.targetAudience.contains("OWNER", ignoreCase = true) || it.targetAudience.contains("HOST", ignoreCase = true) || it.targetAudience == "ALL"
    } ?: Announcement(
        announcementId = "dir_01",
        title = "Annual Safety & Fire Compliance Inspection 2026",
        message = "All hostel owners and wardens are mandated to submit certified fire extinguisher audit and emergency exit clearance reports by month end.",
        senderName = "Campus Housing Association Head",
        senderRole = UserRole.ADMIN,
        priority = AnnouncementPriority.URGENT,
        targetAudience = "HOSTEL_OWNERS"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HostBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Host Property Header with Profile trigger
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .background(HostAccentContainer, shape = CircleShape)
                        .size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Host Profile",
                        tint = HostOnAccentContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hostel Operations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$hostelName • Warden Portal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onNavigateToAnnouncements,
                modifier = Modifier
                    .background(HostAccentContainer, shape = CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Broadcast Notice",
                    tint = HostOnAccentContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🏛️ Association Head Directives Banner (Highlighted for Hostel Owners)
        AppCard(
            padding = 16.dp,
            backgroundColor = AdminAccentContainer.copy(alpha = 0.35f),
            onClick = onNavigateToAnnouncements
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(AdminHeroBg, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏛️ Association Head Directive",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = AdminOnAccentContainer
                        )
                        StatusBadge(
                            text = latestCouncilDirective.priority.name,
                            statusType = when (latestCouncilDirective.priority) {
                                AnnouncementPriority.URGENT -> BadgeStatusType.ERROR
                                AnnouncementPriority.IMPORTANT -> BadgeStatusType.WARNING
                                AnnouncementPriority.NORMAL -> BadgeStatusType.INFO
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = latestCouncilDirective.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = latestCouncilDirective.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "From: ${latestCouncilDirective.senderName} • Tap to view all directives →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = AdminAccent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Occupancy Summary Card
        AppCard(
            backgroundColor = HostHeroBg,
            padding = 20.dp,
            onClick = onNavigateToRooms
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Live Hostel Occupancy Rate",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$occupancyRate% Occupied",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stats.occupiedBeds} / ${stats.totalBeds} Beds Filled • ${stats.availableBeds} Vacant Beds Ready",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    text = "Manage Rooms →",
                    statusType = BadgeStatusType.SUCCESS,
                    customBgColor = HostBadgeBg,
                    customTextColor = HostBadgeText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Key Operational Metrics (Fees, Residents, Complaints, Mess Menu)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Pending Fees (₹)",
                value = if (stats.pendingFeeAmount > 0) Formatters.formatCurrencyNoDecimals(stats.pendingFeeAmount) else "₹0",
                icon = Icons.Default.Payment,
                subtitle = "${stats.pendingFeeCount} Invoices Due",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToFees
            )
            MetricStatCard(
                title = "Total Residents",
                value = "${stats.occupiedBeds} Students",
                icon = Icons.Default.Group,
                subtitle = "Across ${stats.totalRooms} Rooms",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStudents
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Maintenance",
                value = "${stats.pendingComplaints} Tickets",
                icon = Icons.AutoMirrored.Filled.Assignment,
                subtitle = if (stats.pendingComplaints > 0) "Needs Attention" else "All Clear",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToComplaints
            )
            MetricStatCard(
                title = "Weekly Mess Menu",
                value = "7 Days Active",
                icon = Icons.Default.Restaurant,
                subtitle = "Edit & Publish",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToMenu
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Host Management Action Hub
        Text(
            text = "Host Management Controls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HostQuickActionCard(
                title = "Rooms & Beds",
                subtitle = "Allocate Vacant Beds",
                icon = Icons.Default.MeetingRoom,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToRooms,
                modifier = Modifier.weight(1f)
            )
            HostQuickActionCard(
                title = "Resident Directory",
                subtitle = "Student ID Cards",
                icon = Icons.Default.Group,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToStudents,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HostQuickActionCard(
                title = "Food Menu Editor",
                subtitle = "Publish Daily Meals",
                icon = Icons.Default.Restaurant,
                iconTint = TertiaryAmber,
                iconBg = TertiaryAmber.copy(alpha = 0.15f),
                onClick = onNavigateToMenu,
                modifier = Modifier.weight(1f)
            )
            HostQuickActionCard(
                title = "Fee Invoices & Dues",
                subtitle = "Track Payments (₹)",
                icon = Icons.Default.Payment,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToFees,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HostQuickActionCard(
                title = "Complaints & Repairs",
                subtitle = "Manage Work Orders",
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = MaterialTheme.colorScheme.error,
                iconBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                onClick = onNavigateToComplaints,
                modifier = Modifier.weight(1f)
            )
            HostQuickActionCard(
                title = "Broadcast Notice",
                subtitle = "Notify All Residents",
                icon = Icons.Default.Campaign,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToAnnouncements,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Operations Overview
        Text(
            text = "Facility Operations Log",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        AppCard(padding = 16.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ActivityRow("Room allocation and bed assignment database synchronized", "Active", Icons.Default.CheckCircle)
                ActivityRow("Catering food menu updated and published to residents", "Operational", Icons.Default.Restaurant)
                ActivityRow("Hostel maintenance queue and repair tracking active", "Online", Icons.Default.Construction)
            }
        }
    }
}

@Composable
private fun HostQuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier,
        padding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ActivityRow(title: String, time: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SecondaryTeal,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
