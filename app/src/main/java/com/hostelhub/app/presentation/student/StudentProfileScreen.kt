package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.ButtonVariant
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryContainer
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.utils.UiState

@Composable
fun StudentProfileScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val profileState by studentViewModel?.studentProfile?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val student = (profileState as? UiState.Success)?.data
    val isLoading = profileState is UiState.Loading

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Student Profile",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SecondaryTeal)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundCool)
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar & Name
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PrimaryContainer, shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = student?.fullName ?: "Resident Student",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (student != null) "${student.rollNumber} • ${student.course}" else "Enrolled Student",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Academic & Identity Info Card
                AppCard(padding = 16.dp) {
                    ProfileDetailRow(Icons.Default.School, "College / Department", student?.collegeName ?: "Campus University")
                    ProfileDetailRow(Icons.Default.Person, "Year of Study", "${student?.yearOfStudy ?: "1"} Year")
                    ProfileDetailRow(Icons.Default.Email, "Registered Email", student?.email?.ifBlank { "${student.rollNumber.lowercase()}@campus.edu" } ?: "N/A")
                    ProfileDetailRow(Icons.Default.Badge, "Roll / Student ID", student?.rollNumber ?: "N/A")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Housing & Room Details
                AppCard(padding = 16.dp) {
                    ProfileDetailRow(Icons.Default.Home, "Assigned Hostel", student?.hostelName ?: "No Hostel Assigned")
                    ProfileDetailRow(Icons.Default.MeetingRoom, "Room & Bed", if (student?.roomNumber != null) "Room ${student.roomNumber} (${student.bedNumber ?: "Bed Unassigned"})" else "Unallocated")
                    ProfileDetailRow(Icons.Default.ContactPhone, "Emergency Contact", "${student?.emergencyContactName ?: "Guardian"} (${student?.emergencyContactPhone ?: "N/A"})")
                    ProfileDetailRow(Icons.Default.LocationOn, "Permanent Address", student?.permanentAddress ?: "Campus Resident")
                }

                Spacer(modifier = Modifier.height(28.dp))

                AppButton(
                    text = "Sign Out",
                    onClick = onLogout,
                    variant = ButtonVariant.DANGER
                )
            }
        }
    }
}

@Composable
private fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
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
