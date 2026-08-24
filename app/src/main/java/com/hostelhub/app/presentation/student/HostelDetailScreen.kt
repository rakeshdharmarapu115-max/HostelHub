package com.hostelhub.app.presentation.student

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HostelDetailScreen(
    hostelId: String,
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var showReviewModal by remember { mutableStateOf(false) }

    val hostelsState by studentViewModel?.hostels?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val reviewsState by studentViewModel?.hostelReviews?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    LaunchedEffect(hostelId) {
        studentViewModel?.loadHostels()
        studentViewModel?.loadHostelReviews(hostelId)
    }

    val hostel = (hostelsState as? UiState.Success)?.data?.find { it.hostelId == hostelId } ?: Hostel(
        hostelId = hostelId,
        name = "Green Valley Residencies",
        address = "12 North Campus Road, University District",
        city = "Academic City",
        description = "Modern student residency featuring high-speed Wi-Fi, air conditioning, daily meal services, and round-the-clock security.",
        genderType = HostelGenderType.COED,
        amenities = listOf("High-Speed Wi-Fi", "Air Conditioned Rooms", "4-Meal Mess Included", "Fitness Gym", "24/7 Security", "Biometric Entry"),
        rules = listOf("Curfew enforced at 10:30 PM", "No smoking or loud music after 10 PM", "Visitor passes required for non-residents"),
        baseMonthlyRent = 5500.0,
        cautionDeposit = 3000.0,
        rating = 4.8,
        ratingCount = 124,
        totalRooms = 30,
        totalBeds = 60,
        occupiedBeds = 52,
        images = listOf(
            "https://images.unsplash.com/photo-1555854877-bab0e564b8d5?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1595526114035-0d45ed16cfbf?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=800&q=80"
        ),
        contactPhone = "+91 98765 43210",
        contactEmail = "warden@greenvalley.edu"
    )

    val reviewsList = (reviewsState as? UiState.Success)?.data ?: hostel.reviews

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Hostel Showcase & Reviews",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showReviewModal = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryNavy)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Rate Hostel", fontWeight = FontWeight.Bold)
                    }

                    AppButton(
                        text = "Apply for Room",
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Hostel Photos Gallery Showcase
            Text(
                text = "Photo Gallery (${hostel.images.size} Photos)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (hostel.images.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(hostel.images) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryNavy)
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
            } else {
                AppCard(
                    backgroundColor = PrimaryNavy,
                    padding = 24.dp
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Apartment,
                            contentDescription = null,
                            tint = PrimaryNavy,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = hostel.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${hostel.address}, ${hostel.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Hostel Title, Location & Star Rating
            AppCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = hostel.name,
                            style = MaterialTheme.typography.headlineSmall,
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
                    }

                    Surface(
                        color = SecondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = TertiaryAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${hostel.rating} ★",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider()
                Spacer(modifier = Modifier.height(14.dp))

                // Price & Beds row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Monthly Rent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${Formatters.formatCurrency(hostel.baseMonthlyRent)} / mo",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTeal
                        )
                    }
                    StatusBadge(
                        text = "${hostel.totalBeds - hostel.occupiedBeds} Beds Vacant",
                        statusType = BadgeStatusType.SUCCESS
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Overview & Description
            Text(
                text = "Overview & Facilities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            AppCard(padding = 16.dp) {
                Text(
                    text = hostel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Included Amenities
            Text(
                text = "Included Amenities",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            AppCard(padding = 16.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    hostel.amenities.forEach { amenity ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = amenity,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. 🌟 Ratings & Student Reviews Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Student Reviews & Ratings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { showReviewModal = true }) {
                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryNavy)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Write Review", color = PrimaryNavy, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Rating Score Summary Card
            AppCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${hostel.rating}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = if (index < hostel.rating.toInt()) TertiaryAmber else Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Based on ${hostel.ratingCount} reviews",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f).padding(start = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RatingCategoryBar("Cleanliness", 4.9f)
                        RatingCategoryBar("Mess Food", 4.7f)
                        RatingCategoryBar("Facilities", 4.8f)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of Reviews
            if (reviewsList.isEmpty()) {
                AppCard(padding = 20.dp) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = TertiaryAmber,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No Student Reviews Yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Be the first resident to rate this hostel and help other students!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    reviewsList.forEach { review ->
                        AppCard(padding = 16.dp) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(PrimaryContainer, shape = CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = review.studentName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryNavy
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = review.studentName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(review.createdAt)),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Row {
                                        repeat(review.rating.toInt().coerceIn(1, 5)) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = TertiaryAmber,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                if (!review.comment.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = review.comment,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Modal to Rate & Review the Hostel
    if (showReviewModal) {
        var selectedStars by remember { mutableIntStateOf(5) }
        var reviewComment by remember { mutableStateOf("") }
        var cleanlinessRating by remember { mutableFloatStateOf(5.0f) }
        var foodRating by remember { mutableFloatStateOf(5.0f) }
        var amenitiesRating by remember { mutableFloatStateOf(5.0f) }
        var isSubmitting by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showReviewModal = false },
            title = {
                Text(
                    text = "Rate & Review ${hostel.name}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Overall Rating:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Interactive 5-Star Selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { star ->
                            Icon(
                                imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star Stars",
                                tint = if (star <= selectedStars) TertiaryAmber else Color.Gray,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { selectedStars = star }
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (selectedStars) {
                                5 -> "Excellent"
                                4 -> "Very Good"
                                3 -> "Good"
                                2 -> "Fair"
                                else -> "Poor"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNavy
                        )
                    }

                    Divider()

                    // Category Criteria Ratings
                    Text("Cleanliness & Hygiene:", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = cleanlinessRating,
                        onValueChange = { cleanlinessRating = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = PrimaryNavy, activeTrackColor = PrimaryNavy)
                    )

                    Text("Mess Food Quality:", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = foodRating,
                        onValueChange = { foodRating = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = SecondaryTeal, activeTrackColor = SecondaryTeal)
                    )

                    Text("Amenities & Wi-Fi:", style = MaterialTheme.typography.labelSmall)
                    Slider(
                        value = amenitiesRating,
                        onValueChange = { amenitiesRating = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(thumbColor = TertiaryAmber, activeTrackColor = TertiaryAmber)
                    )

                    AppTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = "Your Review & Feedback",
                        placeholder = "e.g. Spacious rooms, fast Wi-Fi, and courteous warden staff.",
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isSubmitting = true
                        studentViewModel?.submitHostelReview(
                            hostelId = hostel.hostelId,
                            rating = selectedStars.toDouble(),
                            comment = reviewComment.ifBlank { "Great residency with excellent facilities." },
                            cleanliness = cleanlinessRating.toDouble(),
                            foodQuality = foodRating.toDouble(),
                            amenitiesRating = amenitiesRating.toDouble(),
                            onSuccess = {
                                isSubmitting = false
                                Toast.makeText(context, "Thank you! Your review has been published.", Toast.LENGTH_LONG).show()
                                showReviewModal = false
                            },
                            onError = { err ->
                                isSubmitting = false
                                Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Submitting...")
                    } else {
                        Text("Submit Review ⭐")
                    }
                }
            },
            dismissButton = {
                if (!isSubmitting) {
                    TextButton(onClick = { showReviewModal = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun RatingCategoryBar(label: String, score: Float) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = "$score", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { score / 5.0f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = PrimaryNavy,
            trackColor = PrimaryNavy.copy(alpha = 0.15f)
        )
    }
}
