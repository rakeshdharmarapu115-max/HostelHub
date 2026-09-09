package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.hostelhub.app.domain.model.StudentDashboardStats
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.BadgeStatusType
import com.hostelhub.app.presentation.components.StatusBadge
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState
import java.util.Calendar

@Composable
fun StudentDashboardScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateToRoom: () -> Unit,
    onNavigateToAttendance: () -> Unit = {},
    onNavigateToMenu: () -> Unit,
    onNavigateToComplaints: () -> Unit = {},
    onNavigateToPayments: () -> Unit = {},
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
                        .background(StudentAccentContainer, shape = CircleShape)
                        .border(1.dp, PrimaryNavy.copy(alpha = 0.35f), CircleShape),
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

        // 2. Room & Bed Allocation Summary Hero Card
        AppCard(
            backgroundColor = StudentHeroBg,
            padding = 18.dp,
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
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = "Room",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Room Allocation",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = if (stats.roomNumber.isNotBlank()) "${stats.roomNumber} • ${stats.bedNumber}" else "Room Allotted",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
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
                    text = "Allocated",
                    statusType = BadgeStatusType.SUCCESS,
                    customBgColor = Color.White.copy(alpha = 0.25f),
                    customTextColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. Quick Access Shortcuts (EXACTLY 4 Cards in 2x2 Grid)
        Text(
            text = "Quick Access",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StudentQuickNavCard(
                title = "Fees",
                icon = Icons.Default.Payment,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToPayments,
                modifier = Modifier.weight(1f)
            )
            StudentQuickNavCard(
                title = "Room",
                icon = Icons.Default.MeetingRoom,
                iconTint = SecondaryTeal,
                iconBg = SecondaryContainer,
                onClick = onNavigateToRoom,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StudentQuickNavCard(
                title = "Mess",
                icon = Icons.Default.Restaurant,
                iconTint = TertiaryAmber,
                iconBg = TertiaryContainer,
                onClick = onNavigateToMenu,
                modifier = Modifier.weight(1f)
            )
            StudentQuickNavCard(
                title = "Complaints",
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = MaterialTheme.colorScheme.error,
                iconBg = MaterialTheme.colorScheme.errorContainer,
                onClick = onNavigateToComplaints,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        // 4. Live Today's Mess Schedule Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Today's Mess Schedule",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    color = SecondaryContainer,
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
                Text("Full Week →", color = SecondaryTeal, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        AppCard(
            padding = 16.dp,
            onClick = onNavigateToMenu
        ) {
            MealRow(
                mealType = "Breakfast",
                items = todayMeals?.breakfast?.joinToString(", ") ?: "Poha, Boiled Eggs / Sprouts, Masala Chai, Filter Coffee",
                time = "7:30 AM - 9:30 AM"
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            MealRow(
                mealType = "Lunch",
                items = todayMeals?.lunch?.joinToString(", ") ?: "Steamed Rice, Dal Tadka, Paneer Butter Masala, Curd, Salad",
                time = "12:30 PM - 2:30 PM"
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            MealRow(
                mealType = "Dinner",
                items = todayMeals?.dinner?.joinToString(", ") ?: "Butter Roti, Mixed Veg Curry, Jeera Rice, Gulab Jamun",
                time = "7:30 PM - 9:30 PM"
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StudentQuickNavCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        onClick = onClick,
        modifier = modifier.height(94.dp),
        padding = 12.dp
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
            Spacer(modifier = Modifier.height(6.dp))
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
