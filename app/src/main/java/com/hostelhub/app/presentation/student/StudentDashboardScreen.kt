package com.hostelhub.app.presentation.student

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
import com.hostelhub.app.domain.model.StudentDashboardStats
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.BadgeStatusType
import com.hostelhub.app.presentation.components.MetricStatCard
import com.hostelhub.app.presentation.components.StatusBadge
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState
import java.util.Calendar

@Composable
fun StudentDashboardScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateToRoom: () -> Unit,
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToMenu: () -> Unit,
    onNavigateToComplaints: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToHostelDiscovery: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val statsState by studentViewModel?.dashboardStats?.collectAsState() ?: remember {
        mutableStateOf(UiState.Success(StudentDashboardStats()))
    }
    val profileState by studentViewModel?.studentProfile?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val foodMenuState by studentViewModel?.foodMenu?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val stats = (statsState as? UiState.Success)?.data ?: StudentDashboardStats()
    val studentName = (profileState as? UiState.Success)?.data?.fullName ?: ""

    // Calculate current day name for menu preview
    val currentDayName = remember {
        when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "monday"
            Calendar.TUESDAY -> "tuesday"
            Calendar.WEDNESDAY -> "wednesday"
            Calendar.THURSDAY -> "thursday"
            Calendar.FRIDAY -> "friday"
            Calendar.SATURDAY -> "saturday"
            else -> "sunday"
        }
    }

    val menu = (foodMenuState as? UiState.Success)?.data
    val todayMeals = menu?.schedule?.get(currentDayName) ?: menu?.schedule?.get("monday")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StudentBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Welcome Top Header with Profile and Notifications
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
                        .background(StudentAccentContainer, shape = CircleShape)
                        .size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "My Profile",
                        tint = StudentOnAccentContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (studentName.isNotBlank()) "Hello, $studentName 👋" else "Welcome Student 👋",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${stats.hostelName} • ${if (stats.roomNumber.isNotBlank()) "Room ${stats.roomNumber} (${stats.bedNumber})" else "Resident Student"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = onNavigateToNotifications,
                modifier = Modifier
                    .background(StudentAccentContainer, shape = CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Announcements",
                    tint = StudentOnAccentContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 1. My Room & Bed Banner Card
        AppCard(
            backgroundColor = StudentHeroBg,
            padding = 20.dp,
            onClick = onNavigateToRoom
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Assigned Accommodation",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (stats.roomNumber.isNotBlank()) "Room ${stats.roomNumber} • ${stats.bedNumber}" else "Room Allocation Active",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stats.hostelName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    text = "Room Specs →",
                    statusType = BadgeStatusType.SUCCESS,
                    customBgColor = StudentBadgeBg,
                    customTextColor = StudentBadgeText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Key Metrics: Live Fees & Maintenance Tickets
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Fees & Dues (₹)",
                value = if (stats.pendingFees > 0) Formatters.formatCurrency(stats.pendingFees) else "₹0.00",
                icon = Icons.Default.Payment,
                subtitle = if (stats.pendingFees > 0) "Pay Pending Dues" else "All Invoices Settled",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPayments
            )
            MetricStatCard(
                title = "Maintenance Tickets",
                value = if (stats.activeComplaints > 0) "${stats.activeComplaints} Active" else "0 Open",
                icon = Icons.AutoMirrored.Filled.Assignment,
                subtitle = if (stats.activeComplaints > 0) "In Progress" else "Everything Working",
                modifier = Modifier.weight(1f),
                onClick = onNavigateToComplaints
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 3. Quick Actions Hub (Easy to tap grid)
        Text(
            text = "Quick Navigation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudentQuickNavButton(
                title = "Pay Fees",
                subtitle = "Invoices in ₹",
                icon = Icons.Default.Payment,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToPayments,
                modifier = Modifier.weight(1f)
            )
            StudentQuickNavButton(
                title = "My Room",
                subtitle = "Bed & Specs",
                icon = Icons.Default.MeetingRoom,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToRoom,
                modifier = Modifier.weight(1f)
            )
            StudentQuickNavButton(
                title = "Mess Menu",
                subtitle = "Daily Meals",
                icon = Icons.Default.Restaurant,
                iconTint = TertiaryAmber,
                iconBg = TertiaryAmber.copy(alpha = 0.15f),
                onClick = onNavigateToMenu,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudentQuickNavButton(
                title = "Complaints",
                subtitle = "File Ticket",
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = MaterialTheme.colorScheme.error,
                iconBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                onClick = onNavigateToComplaints,
                modifier = Modifier.weight(1f)
            )
            StudentQuickNavButton(
                title = "Notices",
                subtitle = "Broadcasts",
                icon = Icons.Default.Campaign,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToNotifications,
                modifier = Modifier.weight(1f)
            )
            StudentQuickNavButton(
                title = "Explore Hostels",
                subtitle = "Catalog & Pricing",
                icon = Icons.Default.Apartment,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToHostelDiscovery,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Today's Mess Food Menu Preview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Mess Menu (${currentDayName.replaceFirstChar { it.uppercase() }})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = onNavigateToMenu) {
                Text("Full Week Menu →", color = SecondaryTeal)
            }
        }

        AppCard(
            padding = 16.dp,
            onClick = onNavigateToMenu
        ) {
            MealRow(
                mealType = "Breakfast",
                items = todayMeals?.breakfast?.joinToString(", ") ?: "Poha, Boiled Eggs / Sprouts, Masala Chai, Filter Coffee",
                time = "7:30 AM - 9:30 AM"
            )
            Spacer(modifier = Modifier.height(10.dp))
            MealRow(
                mealType = "Lunch",
                items = todayMeals?.lunch?.joinToString(", ") ?: "Steamed Rice, Dal Tadka, Paneer Butter Masala, Curd, Salad",
                time = "12:30 PM - 2:30 PM"
            )
            Spacer(modifier = Modifier.height(10.dp))
            MealRow(
                mealType = "Dinner",
                items = todayMeals?.dinner?.joinToString(", ") ?: "Butter Roti, Mixed Veg Curry, Jeera Rice, Gulab Jamun",
                time = "7:30 PM - 9:30 PM"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Help & Emergency Contact Card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Need Assistance or Maintenance?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Submit a complaint ticket anytime to notify the hostel warden immediately.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateToComplaints) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Go to complaints",
                        tint = SecondaryTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentQuickNavButton(
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
        padding = 12.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
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
            Spacer(modifier = Modifier.height(8.dp))
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

@Composable
private fun MealRow(mealType: String, items: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = mealType,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryNavy
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = items,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
