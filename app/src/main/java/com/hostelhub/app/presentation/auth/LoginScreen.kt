package com.hostelhub.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.FormValidators
import com.hostelhub.app.utils.UiState

@Composable
fun LoginScreen(
    selectedRole: UserRole,
    authViewModel: AuthViewModel? = null,
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToRegister: (UserRole) -> Unit,
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showServerDialog by remember { mutableStateOf(false) }
    var serverUrlInput by remember { mutableStateOf(authViewModel?.getServerUrl() ?: "https://hostelhub-backend.onrender.com/api/") }

    val loginState by authViewModel?.loginState?.collectAsState() ?: remember { mutableStateOf(UiState.Idle) }
    val isLoading = loginState is UiState.Loading
    val generalError = (loginState as? UiState.Error)?.message

    val roleTitle = when (selectedRole) {
        UserRole.STUDENT -> "Student Portal"
        UserRole.HOST -> "Hostel Warden / Host"
        UserRole.ADMIN -> "Campus Administration"
    }

    val isCloud = authViewModel?.isCloudOrTunnel() ?: true
    val displayHost = authViewModel?.getDisplayHost() ?: "Cloud Service"

    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isCloud) Icons.Default.Dns else Icons.Default.Settings,
                        contentDescription = null,
                        tint = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cloud & Server Connection", style = MaterialTheme.typography.titleMedium)
                }
            },
            text = {
                Column {
                    Text(
                        "Connected to Cloud Services (Worldwide 4G/5G/Wi-Fi):",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = SurfaceContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "☁️ Cloud Server & Database:",
                                style = MaterialTheme.typography.labelSmall,
                                color = SecondaryDark
                            )
                            Text(
                                "https://hostelhub-backend.onrender.com/api/",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "🌐 Custom Cloud / Gateway:",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryIndigo
                            )
                            Text(
                                "e.g. https://your-domain.com/api/",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = serverUrlInput,
                        onValueChange = { serverUrlInput = it },
                        label = { Text("Cloud API Base URL") },
                        placeholder = { Text("https://hostelhub-backend.onrender.com/api/") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Presets:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = { serverUrlInput = "http://192.168.1.2:5000/api/" },
                            label = { Text("Local Wi-Fi", style = MaterialTheme.typography.labelSmall) }
                        )
                        SuggestionChip(
                            onClick = { serverUrlInput = "http://10.0.2.2:5000/api/" },
                            label = { Text("Emulator", style = MaterialTheme.typography.labelSmall) }
                        )
                        SuggestionChip(
                            onClick = { serverUrlInput = "https://hostelhub-backend.onrender.com/api/" },
                            label = { Text("Cloud", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        authViewModel?.setServerUrl(serverUrlInput)
                        showServerDialog = false
                    }
                ) {
                    Text("Save & Apply", color = SecondaryTeal)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        authViewModel?.resetServerUrl()
                        serverUrlInput = authViewModel?.getServerUrl() ?: "https://hostelhub-backend.onrender.com/api/"
                        showServerDialog = false
                    }
                ) {
                    Text("Reset Default")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundCool)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(56.dp)
                .background(PrimaryContainer, shape = MaterialTheme.shapes.medium),
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
            text = "Welcome Back",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = roleTitle,
            style = MaterialTheme.typography.titleMedium,
            color = SecondaryTeal
        )

        Spacer(modifier = Modifier.height(24.dp))

        AppCard(padding = 24.dp) {
            if (generalError != null) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clickable {
                            serverUrlInput = authViewModel?.getServerUrl() ?: "https://hostelhub-backend.onrender.com/api/"
                            showServerDialog = true
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Connection Notice",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = generalError,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚙️ Tap here to configure Global Server URL or fix connection",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryIndigo
                        )
                    }
                }
            }

            AppTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Email Address",
                placeholder = "Enter your registered email",
                leadingIcon = Icons.Default.Email,
                errorMessage = emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Password",
                placeholder = "Enter your password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = passwordError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
            )

            Spacer(modifier = Modifier.height(24.dp))

            AppButton(
                text = "Sign In",
                onClick = {
                    val emailVal = FormValidators.validateEmail(email)
                    val passVal = FormValidators.validatePassword(password)
                    if (!emailVal.isValid) {
                        emailError = emailVal.errorMessage
                    }
                    if (!passVal.isValid) {
                        passwordError = passVal.errorMessage
                    }
                    if (emailVal.isValid && passVal.isValid) {
                        if (authViewModel != null) {
                            authViewModel.login(email, password, selectedRole, onSuccess = onLoginSuccess)
                        } else {
                            onLoginSuccess(selectedRole)
                        }
                    }
                },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = { onNavigateToRegister(selectedRole) }) {
                    Text(
                        text = if (selectedRole == UserRole.ADMIN) "Register as Association Head" else "Register here",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedRole == UserRole.ADMIN) AdminAccent else SecondaryTeal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateBack) {
            Text(
                text = "Change Role",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
