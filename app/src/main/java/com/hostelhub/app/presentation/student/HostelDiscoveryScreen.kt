package com.hostelhub.app.presentation.student

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hostelhub.app.domain.model.Hostel
import com.hostelhub.app.domain.model.HostelGenderType
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState

enum class DiscoveryViewMode {
    LIST,
    MAP
}

@Composable
fun HostelDiscoveryScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateToDetails: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedViewMode by remember { mutableStateOf(DiscoveryViewMode.LIST) }

    val radiusOptions = listOf("All Distance", "1 km", "5 km", "10 km", "25 km", "50 km")
    var selectedRadius by remember { mutableStateOf("All Distance") }

    val filterOptions = listOf("All Hostels", "Top Rated (4.5+)", "Co-Ed", "Boys Only", "Girls Only", "AC Available")
    var selectedFilter by remember { mutableStateOf("All Hostels") }

    var selectedHostelOnMap by remember { mutableStateOf<Hostel?>(null) }
    var showUploadHostelDialog by remember { mutableStateOf(false) }

    val hostelsState by studentViewModel?.hostels?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    LaunchedEffect(selectedRadius) {
        val radiusKm = when (selectedRadius) {
            "1 km" -> 1.0
            "5 km" -> 5.0
            "10 km" -> 10.0
            "25 km" -> 25.0
            "50 km" -> 50.0
            else -> null
        }
        studentViewModel?.searchNearbyHostels(radius = radiusKm)
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
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Explore Hostels & Residencies",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    // View Mode Toggle (List vs Map)
                    IconButton(
                        onClick = {
                            selectedViewMode = if (selectedViewMode == DiscoveryViewMode.LIST) DiscoveryViewMode.MAP else DiscoveryViewMode.LIST
                        }
                    ) {
                        Icon(
                            imageVector = if (selectedViewMode == DiscoveryViewMode.LIST) Icons.Default.Map else Icons.Default.ViewList,
                            contentDescription = "Toggle View",
                            tint = PrimaryNavy
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showUploadHostelDialog = true },
                containerColor = SecondaryTeal,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) },
                text = { Text("List Your Hostel", fontWeight = FontWeight.Bold) }
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
                label = "Search by hostel name, city, or landmark",
                placeholder = "e.g. Green Valley, HiTech City",
                leadingIcon = Icons.Default.Search
            )

            Spacer(modifier = Modifier.height(10.dp))

            // View Mode & Distance Radius Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Radius Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(radiusOptions) { radius ->
                        FilterChip(
                            selected = selectedRadius == radius,
                            onClick = { selectedRadius = radius },
                            label = { Text(radius, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SecondaryTeal,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // View Switch Button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryContainer,
                    modifier = Modifier.clickable {
                        selectedViewMode = if (selectedViewMode == DiscoveryViewMode.LIST) DiscoveryViewMode.MAP else DiscoveryViewMode.LIST
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (selectedViewMode == DiscoveryViewMode.LIST) Icons.Default.Map else Icons.Default.List,
                            contentDescription = null,
                            tint = PrimaryNavy,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedViewMode == DiscoveryViewMode.LIST) "Map" else "List",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Tag Row
            FilterChipRow(
                items = filterOptions,
                selectedItem = selectedFilter,
                onItemSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredHostels.isEmpty()) {
                EmptyStateView(
                    title = "No Hostels Found Nearby",
                    message = "Try expanding your distance radius filter or adjusting search keywords."
                )
            } else if (selectedViewMode == DiscoveryViewMode.MAP) {
                // ==========================================
                // MAP VIEW MODE
                // ==========================================
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8ECEF))
                ) {
                    // Map Background Canvas with Grid & Radar Circle
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        // User location radius rings
                        drawCircle(color = SecondaryTeal.copy(alpha = 0.08f), radius = size.minDimension * 0.45f, center = center)
                        drawCircle(color = SecondaryTeal.copy(alpha = 0.15f), radius = size.minDimension * 0.30f, center = center)
                        drawCircle(color = SecondaryTeal.copy(alpha = 0.25f), radius = size.minDimension * 0.15f, center = center)

                        // Grid road guidelines
                        drawLine(color = Color.LightGray.copy(alpha = 0.5f), start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 2f)
                        drawLine(color = Color.LightGray.copy(alpha = 0.5f), start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 2f)
                    }

                    // User Location Marker (Center)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                            .background(Color.White, shape = CircleShape)
                            .padding(3.dp)
                            .background(SecondaryTeal, shape = CircleShape)
                    )

                    Text(
                        text = "📍 Your Location",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTeal,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 20.dp)
                    )

                    // Pin Markers for Discovered Hostels
                    val basePositions = listOf(
                        Pair(-0.25f, -0.30f),
                        Pair(0.28f, -0.22f),
                        Pair(-0.20f, 0.25f),
                        Pair(0.30f, 0.20f),
                        Pair(0.05f, -0.40f)
                    )

                    filteredHostels.forEachIndexed { index, hostel ->
                        val pos = basePositions.getOrElse(index) { Pair(((index * 37) % 60 - 30) / 100f, ((index * 53) % 60 - 30) / 100f) }
                        val isSelected = selectedHostelOnMap?.hostelId == hostel.hostelId

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .offset(
                                    x = (pos.first * 400).dp,
                                    y = (pos.second * 400).dp
                                )
                                .clickable { selectedHostelOnMap = hostel }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryNavy else SecondaryTeal,
                                shadowElevation = if (isSelected) 8.dp else 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = hostel.name,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (hostel.distanceKm != null) "${hostel.distanceKm} km" else "★ ${hostel.rating}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TertiaryAmber
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Selected Hostel Card on Map
                    selectedHostelOnMap?.let { selectedHostel ->
                        AppCard(
                            padding = 12.dp,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val image = selectedHostel.images.firstOrNull()
                                if (!image.isNullOrBlank()) {
                                    AsyncImage(
                                        model = image,
                                        contentDescription = selectedHostel.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(70.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(PrimaryNavy),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Apartment, contentDescription = null, tint = Color.White)
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedHostel.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryNavy
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${selectedHostel.distanceKm ?: 0.0} km away • ${selectedHostel.city}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SecondaryTeal,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = "From ${Formatters.formatCurrencyNoDecimals(selectedHostel.baseMonthlyRent)}/mo",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Google Maps Navigation Intent Button
                                    Button(
                                        onClick = {
                                            val lat = if (selectedHostel.latitude != 0.0) selectedHostel.latitude else 17.3850
                                            val lng = if (selectedHostel.longitude != 0.0) selectedHostel.longitude else 78.4867
                                            val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(selectedHostel.name)})")
                                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                            try {
                                                context.startActivity(mapIntent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Opening browser maps...", Toast.LENGTH_SHORT).show()
                                                val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
                                                context.startActivity(webMapIntent)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Icon(Icons.Default.Navigation, contentDescription = "Navigate", modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Directions", style = MaterialTheme.typography.labelSmall)
                                    }

                                    OutlinedButton(
                                        onClick = { onNavigateToDetails(selectedHostel.hostelId) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text("Details", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // LIST VIEW MODE
                // ==========================================
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

                                    // Top Badges Overlay
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

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // Distance Badge
                                            if (hostel.distanceKm != null) {
                                                Surface(
                                                    color = SecondaryTeal.copy(alpha = 0.9f),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(
                                                            text = "${hostel.distanceKm} km",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
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
                                }

                                // Details content
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = hostel.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryNavy
                                    )

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

                                    // Price, Vacant Beds & Directions Footer
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

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // Navigation intent shortcut
                                            IconButton(
                                                onClick = {
                                                    val lat = if (hostel.latitude != 0.0) hostel.latitude else 17.3850
                                                    val lng = if (hostel.longitude != 0.0) hostel.longitude else 78.4867
                                                    val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(hostel.name)})")
                                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                                    try {
                                                        context.startActivity(mapIntent)
                                                    } catch (e: Exception) {
                                                        val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
                                                        context.startActivity(webMapIntent)
                                                    }
                                                }
                                            ) {
                                                Icon(Icons.Default.Directions, contentDescription = "Navigate", tint = SecondaryTeal)
                                            }

                                            Button(
                                                onClick = { onNavigateToDetails(hostel.hostelId) },
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                            ) {
                                                 Text("View Details", style = MaterialTheme.typography.labelMedium)
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

    if (showUploadHostelDialog && studentViewModel != null) {
        UploadHostelDialog(
            onDismiss = { showUploadHostelDialog = false },
            onUploadHostel = { newHostel ->
                studentViewModel.createHostel(
                    hostel = newHostel,
                    onSuccess = {
                        showUploadHostelDialog = false
                        Toast.makeText(context, "Hostel '${newHostel.name}' successfully listed on map!", Toast.LENGTH_LONG).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "Error uploading hostel: $err", Toast.LENGTH_LONG).show()
                    }
                )
            }
        )
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun UploadHostelDialog(
    onDismiss: () -> Unit,
    onUploadHostel: (Hostel) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Campus City") }
    var postalCode by remember { mutableStateOf("500001") }
    var latitudeStr by remember { mutableStateOf("17.4399") }
    var longitudeStr by remember { mutableStateOf("78.3800") }
    var totalRoomsStr by remember { mutableStateOf("20") }
    var totalBedsStr by remember { mutableStateOf("60") }
    var monthlyRentStr by remember { mutableStateOf("7500") }
    var contactPhone by remember { mutableStateOf("") }
    var contactEmail by remember { mutableStateOf("") }
    var genderType by remember { mutableStateOf(HostelGenderType.COED) }

    val availableAmenities = listOf("WiFi", "AC", "Attached Bath", "Food / Mess", "CCTV Security", "Power Backup", "Gym", "Study Room")
    val selectedAmenities = remember { mutableStateListOf("WiFi", "CCTV Security", "Food / Mess") }

    var formError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(SecondaryTeal.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AddLocationAlt, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = "List Hostel on Map", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = "Upload property & GPS coordinates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                formError?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }

                AppTextField(
                    value = name,
                    onValueChange = { name = it; formError = null },
                    label = "Hostel / Property Name *",
                    placeholder = "e.g. Royal Heights Student Hostel"
                )

                AppTextField(
                    value = address,
                    onValueChange = { address = it; formError = null },
                    label = "Street Address *",
                    placeholder = "e.g. Plot 45, Near University Gate 2"
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = "City *",
                            placeholder = "e.g. Hyderabad"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = postalCode,
                            onValueChange = { postalCode = it },
                            label = "Postal Code",
                            placeholder = "e.g. 500001"
                        )
                    }
                }

                Text(
                    text = "Map Coordinates (Google Maps GPS):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = latitudeStr,
                            onValueChange = { latitudeStr = it },
                            label = "Latitude",
                            placeholder = "e.g. 17.4399"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = longitudeStr,
                            onValueChange = { longitudeStr = it },
                            label = "Longitude",
                            placeholder = "e.g. 78.3800"
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = monthlyRentStr,
                            onValueChange = { monthlyRentStr = it },
                            label = "Monthly Rent (₹) *",
                            placeholder = "e.g. 7000"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = totalRoomsStr,
                            onValueChange = { totalRoomsStr = it },
                            label = "Total Rooms",
                            placeholder = "e.g. 25"
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = contactPhone,
                            onValueChange = { contactPhone = it },
                            label = "Contact Phone *",
                            placeholder = "e.g. 9876543210"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        AppTextField(
                            value = contactEmail,
                            onValueChange = { contactEmail = it },
                            label = "Contact Email",
                            placeholder = "e.g. warden@hostel.com"
                        )
                    }
                }

                Text(
                    text = "Gender Policy:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HostelGenderType.values().forEach { gType ->
                        FilterChip(
                            selected = genderType == gType,
                            onClick = { genderType = gType },
                            label = { Text(gType.name) }
                        )
                    }
                }

                Text(
                    text = "Amenities Available:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableAmenities.forEach { amenity ->
                        val isSelected = selectedAmenities.contains(amenity)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedAmenities.remove(amenity) else selectedAmenities.add(amenity)
                            },
                            label = { Text(amenity, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || address.isBlank() || contactPhone.isBlank()) {
                        formError = "Please fill in hostel name, address, and contact phone."
                        return@Button
                    }

                    isSubmitting = true
                    formError = null

                    val newHostel = Hostel(
                        hostelId = "",
                        hostId = "host_user",
                        name = name.trim(),
                        address = address.trim(),
                        city = city.trim(),
                        state = "State",
                        postalCode = postalCode.trim(),
                        latitude = latitudeStr.toDoubleOrNull() ?: 17.4399,
                        longitude = longitudeStr.toDoubleOrNull() ?: 78.3800,
                        description = "Premium student residence with modern amenities.",
                        genderType = genderType,
                        amenities = selectedAmenities.toList(),
                        rules = listOf("Gate closes at 10 PM", "Quiet hours after 11 PM"),
                        images = emptyList(),
                        totalRooms = totalRoomsStr.toIntOrNull() ?: 20,
                        totalBeds = totalBedsStr.toIntOrNull() ?: 60,
                        occupiedBeds = 0,
                        baseMonthlyRent = monthlyRentStr.toDoubleOrNull() ?: 7000.0,
                        cautionDeposit = 5000.0,
                        rating = 4.8,
                        ratingCount = 1,
                        contactEmail = contactEmail.trim(),
                        contactPhone = contactPhone.trim()
                    )

                    onUploadHostel(newHostel)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload & Publish Hostel")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        }
    )
}
