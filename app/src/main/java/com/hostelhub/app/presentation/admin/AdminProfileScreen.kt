package com.hostelhub.app.presentation.admin

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

@Composable
fun AdminProfileScreen(
    adminViewModel: AdminViewModel? = null,
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by adminViewModel?.currentUser?.collectAsState() ?: remember {
        mutableStateOf(null)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Association Head Profile",
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
                    imageVector = Icons.Default.SupervisorAccount,
                    contentDescription = null,
                    tint = PrimaryNavy,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentUser?.fullName ?: "Campus Administrator",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Association Head • Campus Housing Council",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppCard(padding = 16.dp) {
                AdminInfoRow(Icons.Default.SupervisorAccount, "Designation", "Chief Housing Administrator")
                AdminInfoRow(Icons.Default.Email, "Campus Email", currentUser?.email ?: "admin@campus.edu")
                AdminInfoRow(Icons.Default.Phone, "Direct Line", currentUser?.phoneNumber?.ifBlank { "+1 555-ADMIN-01" } ?: "+1 555-ADMIN-01")
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

@Composable
private fun AdminInfoRow(icon: ImageVector, label: String, value: String) {
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
