package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
                        .size(46.dp)
                        .background(PrimaryNavy.copy(alpha = 0.12f), shape = CircleShape)
                        .border(1.dp, PrimaryNavy.copy(alpha = 0.25f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "HostelHub Logo",
                        tint = PrimaryNavy,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "HostelHub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (studentName.isNotBlank()) "Student • $studentName" else "Student",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryTeal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onNavigateToNotifications,
                    modifier = Modifier
                        .background(StudentAccentContainer, shape = CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notices",
                        tint = StudentOnAccentContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .background(StudentAccentContainer, shape = CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = StudentOnAccentContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Room & Bed Banner Card
        AppCard(
            backgroundColor = StudentHeroBg,
            padding = 16.dp,
            onClick = onNavigateToRoom
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
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = "Room",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Room",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = if (stats.roomNumber.isNotBlank()) "${stats.roomNumber} • ${stats.bedNumber}" else "Allocated",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = stats.hostelName.ifBlank { "Green Valley Residencies" },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
                StatusBadge(
                    text = "Info",
                    statusType = BadgeStatusType.SUCCESS,
                    customBgColor = StudentBadgeBg,
                    customTextColor = StudentBadgeText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Key Metrics: Fees & Complaints
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EqualMetricCard(
                title = "Fees",
                value = if (stats.pendingFees > 0) Formatters.formatCurrency(stats.pendingFees) else "₹0.00",
                subtitle = if (stats.pendingFees > 0) "Pending" else "All Cleared",
                icon = Icons.Default.Payment,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToPayments,
                modifier = Modifier.weight(1f)
            )
            EqualMetricCard(
                title = "Complaints",
                value = if (stats.activeComplaints > 0) "${stats.activeComplaints} Active" else "0 Active",
                subtitle = if (stats.activeComplaints > 0) "In Progress" else "Resolved",
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToComplaints,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Portal Navigation - Icon + Single-Word Title (Zero Subtitles)
        Text(
            text = "Portal",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EqualQuickNavCard(
                title = "Fees",
                icon = Icons.Default.Payment,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToPayments,
                modifier = Modifier.weight(1f)
            )
            EqualQuickNavCard(
                title = "Room",
                icon = Icons.Default.MeetingRoom,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToRoom,
                modifier = Modifier.weight(1f)
            )
            EqualQuickNavCard(
                title = "Mess",
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
            EqualQuickNavCard(
                title = "Complaints",
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = MaterialTheme.colorScheme.error,
                iconBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                onClick = onNavigateToComplaints,
                modifier = Modifier.weight(1f)
            )
            EqualQuickNavCard(
                title = "Notices",
                icon = Icons.Default.Campaign,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToNotifications,
                modifier = Modifier.weight(1f)
            )
            EqualQuickNavCard(
                title = "Hostels",
                icon = Icons.Default.Apartment,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToHostelDiscovery,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Mess Menu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Mess",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = SecondaryTeal.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = currentDayName.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            TextButton(onClick = onNavigateToMenu) {
                Text("Week →", color = SecondaryTeal, fontWeight = FontWeight.Bold)
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

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Support
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
                    imageVector = Icons.Default.SupportAgent,
                    contentDescription = null,
                    tint = SecondaryTeal,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Support",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Warden Assistance & Maintenance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateToComplaints) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Complaints",
                        tint = SecondaryTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun EqualMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier.height(112.dp),
        padding = 14.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(iconBg, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
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
private fun EqualQuickNavCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        padding = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
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
