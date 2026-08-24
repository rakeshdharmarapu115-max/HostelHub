package com.hostelhub.app.presentation.host

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@Composable
fun HostProfileScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showAddPhotoModal by remember { mutableStateOf(false) }

    val currentUser by hostViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(null)
    }
    val hostelInfoState by hostViewModel?.hostelInfo?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val hostel = (hostelInfoState as? UiState.Success)?.data

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Hostel Owner Profile",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = PrimaryNavy
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PrimaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = PrimaryNavy,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentUser?.fullName ?: "Hostel Warden",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (hostel != null) "Hostel Administrator • ${hostel.name}" else "Hostel Administrator",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Property Details Card
            AppCard(padding = 16.dp) {
                HostInfoRow(Icons.Default.Business, "Managed Property", hostel?.name ?: "No Property Assigned")
                HostInfoRow(Icons.Default.LocationOn, "Hostel Address", hostel?.address ?: "Campus Zone")
                HostInfoRow(Icons.Default.Email, "Registered Email", currentUser?.email ?: "N/A")
                HostInfoRow(Icons.Default.Phone, "Contact Phone", currentUser?.phoneNumber?.ifBlank { "+91 98765 43210" } ?: "+91 98765 43210")
                HostInfoRow(Icons.Default.Star, "Overall Student Rating", if (hostel != null) "${hostel.rating} ★ (${hostel.ratingCount} reviews)" else "Unrated")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Hostel Photo Gallery Management Card
            AppCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Hostel Showcase Photos",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Text(
                            text = "${hostel?.images?.size ?: 0} Photos uploaded",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { showAddPhotoModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Photos", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (!hostel?.images.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(hostel!!.images) { imageUrl ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PrimaryContainer)
                            ) {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = "Hostel Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = "Sign Out",
                onClick = onLogout,
                variant = ButtonVariant.DANGER
            )
        }
    }

    // Modal to Add / Upload Hostel Images
    if (showAddPhotoModal) {
        var imageUrlInput by remember {
            mutableStateOf("https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80")
        }
        var isUploading by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isUploading) showAddPhotoModal = false },
            title = {
                Text(
                    text = "Upload Hostel Photos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Add photos of the building, student rooms, mess dining hall, or amenities:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AppTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        label = "Image URL / Photo Link",
                        placeholder = "https://example.com/hostel-room.jpg"
                    )

                    Text(
                        text = "Quick Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = false,
                            onClick = {
                                imageUrlInput = "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=800&q=80"
                            },
                            label = { Text("Study Room", style = MaterialTheme.typography.labelSmall) }
                        )
                        FilterChip(
                            selected = false,
                            onClick = {
                                imageUrlInput = "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80"
                            },
                            label = { Text("AC Bedroom", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (imageUrlInput.isBlank()) {
                            Toast.makeText(context, "Please enter image URL", Toast.LENGTH_SHORT).show()
                        } else {
                            isUploading = true
                            hostViewModel?.uploadHostelImages(
                                images = listOf(imageUrlInput),
                                onSuccess = {
                                    isUploading = false
                                    Toast.makeText(context, "Photo added to hostel gallery!", Toast.LENGTH_LONG).show()
                                    showAddPhotoModal = false
                                },
                                onError = { err ->
                                    isUploading = false
                                    Toast.makeText(context, "Upload failed: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Uploading...")
                    } else {
                        Text("Save to Gallery")
                    }
                }
            },
            dismissButton = {
                if (!isUploading) {
                    TextButton(onClick = { showAddPhotoModal = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun HostInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
