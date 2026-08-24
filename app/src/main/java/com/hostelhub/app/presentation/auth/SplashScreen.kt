package com.hostelhub.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryContainer
import com.hostelhub.app.presentation.theme.SecondaryTeal
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authViewModel: AuthViewModel? = null,
    onNavigateToDashboard: (UserRole) -> Unit = {},
    onNavigateToRoleSelection: () -> Unit
) {
    val currentUser by authViewModel?.currentUser?.collectAsState() ?: androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(null)
    }

    LaunchedEffect(Unit) {
        delay(900)
        val user = authViewModel?.currentUser?.value
        if (user != null && user.userId.isNotBlank()) {
            onNavigateToDashboard(user.role)
        } else {
            onNavigateToRoleSelection()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Apartment,
                contentDescription = null,
                tint = SecondaryContainer,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HostelHub",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Next-Gen Hostel Logistics & Living",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(48.dp))
            CircularProgressIndicator(
                color = SecondaryTeal,
                modifier = Modifier.size(28.dp),
                strokeWidth = 2.5.dp
            )
        }
    }
}
