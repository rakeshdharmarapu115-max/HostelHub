package com.hostelhub.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.hostelhub.app.domain.model.ComplaintStatus
import com.hostelhub.app.domain.model.Hostel
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHostelListScreen(
    adminViewModel: AdminViewModel? = null,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedHostelForInspection by remember { mutableStateOf<Hostel?>(null) }

    val hostelsState by adminViewModel?.hostels?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val complaintsState by adminViewModel?.complaints?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val hostelsList = (hostelsState as? UiState.Success)?.data ?: listOf(
        Hostel(hostelId = "hostel_001", name = "Green Valley Residencies", address = "Campus North Zone", city = "Metro City", totalRooms = 30, totalBeds = 60, occupiedBeds = 52, baseMonthlyRent = 450.0, cautionDeposit = 900.0, rating = 4.8, contactPhone = "+91 98765 43210", contactEmail = "greenvalley@campus.edu", amenities = listOf("High-Speed Wi-Fi", "Mess Dining", "Biometric Access", "24/7 Power Backup", "Laundry Service")),
        Hostel(hostelId = "hostel_002", name = "Sunrise Elite Hostel", address = "South Boulevard", city = "Metro City", totalRooms = 15, totalBeds = 30, occupiedBeds = 26, baseMonthlyRent = 550.0, cautionDeposit = 1100.0, rating = 4.6, contactPhone = "+91 98765 43211", contactEmail = "sunrise@campus.edu", amenities = listOf("AC Rooms", "Study Lounge", "Gym Access", "CCTV Surveillance")),
        Hostel(hostelId = "hostel_003", name = "Scholars Inn Academic Wing", address = "East Gate Road", city = "Metro City", totalRooms = 15, totalBeds = 30, occupiedBeds = 26, baseMonthlyRent = 600.0, cautionDeposit = 1200.0, rating = 4.9, contactPhone = "+91 98765 43212", contactEmail = "scholarsinn@campus.edu", amenities = listOf("Single Occupancy", "Quiet Study Zone", "Cafeteria", "Attached Baths"))
    )

    val complaintsList = (complaintsState as? UiState.Success)?.data ?: emptyList()

    val filteredHostels = hostelsList.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.city.contains(searchQuery, ignoreCase = true) ||
        it.address.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Affiliated Hostels Registry",
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
                .padding(16.dp)
        ) {
            // Search Input
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Hostels",
                placeholder = "Search by property name or location...",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Total Registry Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredHostels.size} Affiliated Properties",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "All Verified Compliant",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusSuccess,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredHostels.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateView(
                        title = "No Hostels Found",
                        message = "No registered hostel matched your search filter."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredHostels) { hostel ->
                        val hostelIssues = complaintsList.count { it.hostelId == hostel.hostelId && it.status != ComplaintStatus.RESOLVED }
                        val occupancyRate = if (hostel.totalBeds > 0) ((hostel.occupiedBeds.toFloat() / hostel.totalBeds) * 100).toInt() else 85
                        val progress = if (hostel.totalBeds > 0) (hostel.occupiedBeds.toFloat() / hostel.totalBeds) else 0.85f

                        AppCard(
                            padding = 16.dp,
                            onClick = { selectedHostelForInspection = hostel }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
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
                                        text = "${hostel.rating} ★ Verified",
                                        statusType = BadgeStatusType.SUCCESS,
                                        customBgColor = AdminBadgeBg,
                                        customTextColor = AdminBadgeText
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Metrics Grid
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    HostelMicroStat("Total Rooms", "${hostel.totalRooms}")
                                    HostelMicroStat("Total Beds", "${hostel.totalBeds}")
                                    HostelMicroStat("Occupied", "${hostel.occupiedBeds}")
                                    HostelMicroStat("Available", "${hostel.totalBeds - hostel.occupiedBeds}")
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Occupancy Rate: $occupancyRate%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (hostelIssues > 0) "⚠️ $hostelIssues Unresolved Issues" else "✓ No Open Grievances",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (hostelIssues > 0) StatusWarning else StatusSuccess
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

                                Spacer(modifier = Modifier.height(12.dp))

                                HorizontalDivider(color = BorderSubtle)

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Base Tariff: ₹${hostel.baseMonthlyRent.toInt()}/mo",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AdminOnAccentContainer
                                    )
                                    Text(
                                        text = "Inspect Full Property Specs →",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = AdminAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Interactive Deep-Dive Inspection Modal
    selectedHostelForInspection?.let { hostel ->
        ModalBottomSheet(
            onDismissRequest = { selectedHostelForInspection = null },
            sheetState = rememberModalBottomSheetState()
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
                            text = "Financial & Regulatory Specifications",
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

                Spacer(modifier = Modifier.height(16.dp))

                if (hostel.amenities.isNotEmpty()) {
                    Text(
                        text = "Property Amenities & Infrastructure",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppCard(padding = 12.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            hostel.amenities.forEach { amenity ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusSuccess,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = amenity,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                AppButton(
                    text = "Close Inspection Details",
                    onClick = { selectedHostelForInspection = null }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun HostelMicroStat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
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
