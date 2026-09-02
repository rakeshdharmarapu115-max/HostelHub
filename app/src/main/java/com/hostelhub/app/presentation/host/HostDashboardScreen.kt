package com.hostelhub.app.presentation.host

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
    val context = LocalContext.current

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

    val stats = (statsState as? UiState.Success)?.data ?: HostDashboardStats()
    val hostel = (hostelState as? UiState.Success)?.data
    val hostelName = hostel?.name ?: "Green Valley Residencies"
    val occupancyRate = if (stats.totalBeds > 0) ((stats.occupiedBeds.toDouble() / stats.totalBeds) * 100).toInt() else 85

    // Gallery Photo Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val uriStrings = uris.map { it.toString() }
            hostViewModel?.uploadHostelImages(
                images = uriStrings,
                onSuccess = {
                    Toast.makeText(context, "Added ${uris.size} photo(s) from Gallery to Hostel Showcase!", Toast.LENGTH_LONG).show()
                },
                onError = { err ->
                    Toast.makeText(context, "Upload note: $err", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HostBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // 1. Clean, Distinct Emerald Logo & Title Header
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
                        .background(HostAccentContainer, shape = CircleShape)
                        .border(1.5.dp, HostAccent.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Apartment,
                        contentDescription = "Hostel Owner Crest",
                        tint = HostAccent,
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
                            color = HostAccentContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "OWNER",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = HostOnAccentContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (hostelName.isNotBlank()) "Owner • $hostelName" else "Owner",
                        style = MaterialTheme.typography.bodySmall,
                        color = HostAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = onNavigateToAnnouncements,
                    modifier = Modifier
                        .background(HostAccentContainer, shape = CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Notices",
                        tint = HostOnAccentContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onNavigateToProfile,
                    modifier = Modifier
                        .background(HostAccentContainer, shape = CircleShape)
                        .size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = "Profile",
                        tint = HostOnAccentContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Real-Time Occupancy Summary Card
        AppCard(
            backgroundColor = HostHeroBg,
            padding = 16.dp,
            onClick = onNavigateToRooms
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
                            contentDescription = "Rooms",
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
                            text = "$occupancyRate% Occupied",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${stats.occupiedBeds} / ${stats.totalBeds} Beds • ${stats.availableBeds} Vacant",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
                StatusBadge(
                    text = "Rooms",
                    statusType = BadgeStatusType.SUCCESS,
                    customBgColor = HostBadgeBg,
                    customTextColor = HostBadgeText
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Hostel Showcase Photos & Gallery Picker
        AppCard(
            padding = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SecondaryTeal.copy(alpha = 0.15f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${hostel?.images?.size ?: 0} Uploaded",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val displayImages = if (!hostel?.images.isNullOrEmpty()) {
                hostel!!.images
            } else {
                listOf(
                    "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=800&q=80",
                    "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80"
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(displayImages) { imgUrl ->
                    Box(
                        modifier = Modifier
                            .size(width = 130.dp, height = 90.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceContainer)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
                    ) {
                        AsyncImage(
                            model = imgUrl,
                            contentDescription = "Hostel Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .size(width = 100.dp, height = 90.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SecondaryTeal.copy(alpha = 0.08f))
                            .border(1.5.dp, SecondaryTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .clickable { galleryLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("+ Gallery", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SecondaryTeal)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Key Property Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HostEqualMetricCard(
                title = "Residents",
                value = "${stats.occupiedBeds} Students",
                subtitle = "${stats.totalRooms} Rooms",
                icon = Icons.Default.Group,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToStudents,
                modifier = Modifier.weight(1f)
            )
            HostEqualMetricCard(
                title = "Dues",
                value = if (stats.pendingFeeAmount > 0) Formatters.formatCurrencyNoDecimals(stats.pendingFeeAmount) else "₹0",
                subtitle = "${stats.pendingFeeCount} Due",
                icon = Icons.Default.Payment,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToFees,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Controls - Icon + Single-Word Titles
        Text(
            text = "Controls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HostEqualActionCard(
                title = "Rooms",
                icon = Icons.Default.MeetingRoom,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToRooms,
                modifier = Modifier.weight(1f)
            )
            HostEqualActionCard(
                title = "Students",
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
            HostEqualActionCard(
                title = "Mess",
                icon = Icons.Default.Restaurant,
                iconTint = TertiaryAmber,
                iconBg = TertiaryAmber.copy(alpha = 0.15f),
                onClick = onNavigateToMenu,
                modifier = Modifier.weight(1f)
            )
            HostEqualActionCard(
                title = "Complaints",
                icon = Icons.AutoMirrored.Filled.Assignment,
                iconTint = MaterialTheme.colorScheme.error,
                iconBg = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                onClick = onNavigateToComplaints,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HostEqualActionCard(
                title = "Fees",
                icon = Icons.Default.Payment,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToFees,
                modifier = Modifier.weight(1f)
            )
            HostEqualActionCard(
                title = "Notices",
                icon = Icons.Default.Campaign,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToAnnouncements,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HostEqualActionCard(
                title = "Attendance",
                icon = Icons.Default.FactCheck,
                iconTint = SecondaryTeal,
                iconBg = SecondaryTeal.copy(alpha = 0.15f),
                onClick = onNavigateToAttendance,
                modifier = Modifier.weight(1f)
            )
            HostEqualActionCard(
                title = "Profile",
                icon = Icons.Default.Business,
                iconTint = PrimaryNavy,
                iconBg = PrimaryContainer,
                onClick = onNavigateToProfile,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HostEqualMetricCard(
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
        modifier = modifier
            .height(115.dp)
            .clickable { onClick() },
        padding = 12.dp
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
                        .size(30.dp)
                        .background(iconBg, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = iconTint,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun HostEqualActionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier
            .height(72.dp)
            .clickable { onClick() },
        padding = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
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
