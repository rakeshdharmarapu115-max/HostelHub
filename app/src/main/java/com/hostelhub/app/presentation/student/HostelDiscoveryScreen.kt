package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hostelhub.app.domain.model.Hostel
import com.hostelhub.app.domain.model.HostelGenderType
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState

@Composable
fun HostelDiscoveryScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateToDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filterOptions = listOf("All Hostels", "Top Rated (4.5+)", "Co-Ed", "Boys Only", "Girls Only", "AC Available", "Gym")
    var selectedFilter by remember { mutableStateOf("All Hostels") }

    val hostelsState by studentViewModel?.hostels?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    LaunchedEffect(Unit) {
        studentViewModel?.loadHostels()
    }

    val allHostels = (hostelsState as? UiState.Success)?.data ?: emptyList()

    val filteredHostels = allHostels.filter { hostel ->
        val matchesSearch = hostel.name.contains(searchQuery, ignoreCase = true) ||
                hostel.city.contains(searchQuery, ignoreCase = true) ||
                hostel.address.contains(searchQuery, ignoreCase = true)
        val matchesFilter = when (selectedFilter) {
            "Top Rated (4.5+)" -> hostel.rating >= 4.5
            "Co-Ed" -> hostel.genderType == HostelGenderType.COED
            "Boys Only" -> hostel.genderType == HostelGenderType.BOYS
            "Girls Only" -> hostel.genderType == HostelGenderType.GIRLS
            "AC Available" -> hostel.amenities.any { it.contains("AC", ignoreCase = true) }
            "Gym" -> hostel.amenities.any { it.contains("Gym", ignoreCase = true) }
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Explore Hostels & Residencies",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Search Input Field
            AppTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search by hostel name or landmark",
                placeholder = "e.g. Green Valley, North Campus",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            FilterChipRow(
                items = filterOptions,
                selectedItem = selectedFilter,
                onItemSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredHostels.isEmpty()) {
                EmptyStateView(
                    title = "No Hostels Match Your Criteria",
                    message = "Try changing your search keywords or rating filter selection."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredHostels) { hostel ->
                        AppCard(
                            padding = 0.dp,
                            onClick = { onNavigateToDetails(hostel.hostelId) }
                        ) {
                            Column {
                                // Cover Image with Rating & Gender Badges
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(170.dp)
                                        .background(PrimaryNavy)
                                ) {
                                    val coverImage = hostel.images.firstOrNull()
                                    if (!coverImage.isNullOrBlank()) {
                                        AsyncImage(
                                            model = coverImage,
                                            contentDescription = hostel.name,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(PrimaryNavy),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(
                                                    imageVector = Icons.Default.Apartment,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.7f),
                                                    modifier = Modifier.size(48.dp)
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = hostel.name,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }

                                    // Top Rating Badge Overlay
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.7f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = TertiaryAmber,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (hostel.rating > 0) "${hostel.rating} ★ (${hostel.ratingCount})" else "New (Unrated)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }

                                        StatusBadge(
                                            text = hostel.genderType.name,
                                            statusType = BadgeStatusType.INFO,
                                            customBgColor = Color.White.copy(alpha = 0.9f),
                                            customTextColor = PrimaryNavy
                                        )
                                    }
                                }

                                // Details content
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = hostel.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${hostel.address}, ${hostel.city}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (hostel.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = hostel.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Amenities Tag Row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        hostel.amenities.take(3).forEach { amenity ->
                                            Surface(
                                                color = SecondaryContainer.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = amenity,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = PrimaryNavy,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        if (hostel.amenities.size > 3) {
                                            Text(
                                                text = "+${hostel.amenities.size - 3} more",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.align(Alignment.CenterVertically)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Price & Bed Availability Footer
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "From ${Formatters.formatCurrencyNoDecimals(hostel.baseMonthlyRent)}/mo",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = SecondaryTeal
                                            )
                                            Text(
                                                text = "${hostel.totalBeds - hostel.occupiedBeds} vacant beds",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = StatusSuccess
                                            )
                                        }

                                        Button(
                                            onClick = { onNavigateToDetails(hostel.hostelId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("View Details & Rate", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
