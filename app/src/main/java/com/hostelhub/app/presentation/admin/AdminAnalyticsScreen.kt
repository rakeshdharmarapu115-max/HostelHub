package com.hostelhub.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.AdminDashboardStats
import com.hostelhub.app.domain.model.Hostel
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.MetricStatCard
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState

@Composable
fun AdminAnalyticsScreen(
    adminViewModel: AdminViewModel? = null,
    onNavigateBack: () -> Unit
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

    val stats = (statsState as? UiState.Success)?.data ?: AdminDashboardStats()
    val hostels = (hostelsState as? UiState.Success)?.data ?: listOf(
        Hostel(name = "Green Valley Residencies", totalBeds = 60, occupiedBeds = 52),
        Hostel(name = "Sunrise Elite Hostel", totalBeds = 30, occupiedBeds = 26),
        Hostel(name = "Scholars Inn Academic Wing", totalBeds = 30, occupiedBeds = 26)
    )

    val overallOccupancyRate = if (stats.totalBeds > 0) (stats.occupiedBeds.toFloat() / stats.totalBeds) else 0.87f

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Association Housing Analytics",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Exactly 4 Primary Analysis Dashboard Metric Cards (2x2 Grid)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Student Growth Rate",
                    value = "+12.4%",
                    icon = Icons.Default.TrendingUp,
                    subtitle = "Annual Influx Growth",
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Average Occupancy",
                    value = "${(overallOccupancyRate * 100).toInt()}%",
                    icon = Icons.Default.Bed,
                    subtitle = "${stats.occupiedBeds} / ${stats.totalBeds} Beds Allotted",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Grievance Velocity",
                    value = "3.2 Hours",
                    icon = Icons.Default.CheckCircle,
                    subtitle = "Average Resolution",
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Fee Collection Rate",
                    value = "96.8%",
                    icon = Icons.Default.Payment,
                    subtitle = "${Formatters.formatCurrencyNoDecimals(stats.totalRevenue)} Realized",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
