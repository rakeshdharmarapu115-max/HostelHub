package com.hostelhub.app.presentation.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.UiState

@Composable
fun LoginScreen(
    selectedRole: UserRole = UserRole.STUDENT,
    authViewModel: AuthViewModel? = null,
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToRegister: (UserRole) -> Unit,
    onNavigateToDiscovery: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Active Role Selection (Student, Hostel Owner, Association Head)
    var currentRole by remember { mutableStateOf(selectedRole) }

    // Credentials
    var identifierInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val loginState by authViewModel?.loginState?.collectAsState() ?: remember { mutableStateOf(UiState.Idle) }
    val isLoading = loginState is UiState.Loading
    val generalError = (loginState as? UiState.Error)?.message

    val roleColor = when (currentRole) {
        UserRole.STUDENT -> Color(0xFF00D2FF)
        UserRole.HOST -> Color(0xFF00E676)
        UserRole.ADMIN -> Color(0xFFFFB74D)
    }

    fun performLogin() {
        focusManager.clearFocus()
        var hasError = false

        if (identifierInput.isBlank()) {
            identifierError = when (currentRole) {
                UserRole.STUDENT -> "Please enter your Student ID or Email"
                UserRole.HOST -> "Please enter your registered Email or Phone"
                UserRole.ADMIN -> "Please enter your Admin Email or ID"
            }
            hasError = true
        } else {
            identifierError = null
        }

        if (passwordInput.isBlank()) {
            passwordError = "Please enter your password"
            hasError = true
        } else {
            passwordError = null
        }

        if (!hasError) {
            authViewModel?.login(
                identifier = identifierInput.trim(),
                password = passwordInput,
                role = currentRole,
                onSuccess = { role ->
                    Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(role)
                }
            )
        }
    }

    // Rich colorful background gradient with deep indigo, teal, and navy
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF07111E),
            Color(0xFF0B1E36),
            Color(0xFF0F3057),
            Color(0xFF002B49)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // ==================== DASHBOARD SWITCHER TABS ====================
            // Allows instant toggling between "Login Dashboard" and "Discover Hostels"
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF132A4A).copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF204570)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Active: Login Dashboard Tab
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = PrimaryNavy,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Login Dashboard",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Inactive: Discover Hostels Tab (Clickable)
                    Surface(
                        onClick = onNavigateToDiscovery,
                        shape = RoundedCornerShape(20.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Explore, contentDescription = null, tint = Color(0xFF8BB5E8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Discover Hostels",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF8BB5E8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Branding Emblem
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFF1E3A5F), shape = CircleShape)
                    .border(2.dp, roleColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (currentRole) {
                        UserRole.STUDENT -> Icons.Default.School
                        UserRole.HOST -> Icons.Default.Apartment
                        UserRole.ADMIN -> Icons.Default.AccountBalance
                    },
                    contentDescription = "Role Emblem",
                    tint = roleColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "HOSTEL MANAGEMENT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = roleColor
            )

            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(18.dp))

            // ==================== ICON-BASED ROLE SELECTOR ====================
            // Minimal, intuitive icons with zero clutter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RoleIconTab(
                    title = "Student",
                    icon = Icons.Default.School,
                    isSelected = currentRole == UserRole.STUDENT,
                    activeColor = Color(0xFF00D2FF),
                    modifier = Modifier.weight(1f),
                    onClick = { currentRole = UserRole.STUDENT }
                )
                RoleIconTab(
                    title = "Owner",
                    icon = Icons.Default.Apartment,
                    isSelected = currentRole == UserRole.HOST,
                    activeColor = Color(0xFF00E676),
                    modifier = Modifier.weight(1f),
                    onClick = { currentRole = UserRole.HOST }
                )
                RoleIconTab(
                    title = "Association",
                    icon = Icons.Default.AccountBalance,
                    isSelected = currentRole == UserRole.ADMIN,
                    activeColor = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f),
                    onClick = { currentRole = UserRole.ADMIN }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ==================== GLASSMORPHIC LOGIN CARD ====================
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF132238).copy(alpha = 0.92f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF284469))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    // Dynamic label based on selected role
                    val inputLabel = when (currentRole) {
                        UserRole.STUDENT -> "Student ID or Email"
                        UserRole.HOST -> "Owner Email / Phone"
                        UserRole.ADMIN -> "Association Head Email"
                    }
                    val inputPlaceholder = when (currentRole) {
                        UserRole.STUDENT -> "e.g. STU-2026-0001 or email"
                        UserRole.HOST -> "e.g. owner@hostel.com or 9876543210"
                        UserRole.ADMIN -> "e.g. admin@campus.edu"
                    }
                    val inputIcon = when (currentRole) {
                        UserRole.STUDENT -> Icons.Default.Badge
                        UserRole.HOST -> Icons.Default.Business
                        UserRole.ADMIN -> Icons.Default.AccountBalance
                    }

                    Text(
                        text = inputLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCCD6F6)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = identifierInput,
                        onValueChange = {
                            identifierInput = it
                            identifierError = null
                        },
                        placeholder = { Text(inputPlaceholder, color = Color(0xFF64748B), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(inputIcon, contentDescription = null, tint = roleColor)
                        },
                        isError = identifierError != null,
                        supportingText = identifierError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = roleColor,
                            unfocusedBorderColor = Color(0xFF284469),
                            focusedContainerColor = Color(0xFF0F1B2C),
                            unfocusedContainerColor = Color(0xFF0F1B2C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCCD6F6)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            passwordError = null
                        },
                        placeholder = { Text("Enter your password", color = Color(0xFF64748B), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = roleColor)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF8BB5E8)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError != null,
                        supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { performLogin() }),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = roleColor,
                            unfocusedBorderColor = Color(0xFF284469),
                            focusedContainerColor = Color(0xFF0F1B2C),
                            unfocusedContainerColor = Color(0xFF0F1B2C),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Forgot Password
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = roleColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { showForgotPasswordDialog = true }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                        )
                    }

                    // Error Message
                    AnimatedVisibility(visible = !generalError.isNullOrBlank(), enter = fadeIn(), exit = fadeOut()) {
                        generalError?.let { msg ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF3F161C),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = msg, style = MaterialTheme.typography.bodySmall, color = Color(0xFFFCA5A5))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary LOGIN Button
                    Button(
                        onClick = { performLogin() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = roleColor,
                            contentColor = Color(0xFF07111E)
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF07111E), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "LOGIN",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Minimal Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF284469))
                        Text(text = "   New to HostelHub?   ", style = MaterialTheme.typography.bodySmall, color = Color(0xFF8BB5E8))
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF284469))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Register Button
                    OutlinedButton(
                        onClick = { onNavigateToRegister(currentRole) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, roleColor.copy(alpha = 0.8f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = roleColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REGISTER NEW ACCOUNT",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Discover Hostels CTA
            TextButton(
                onClick = onNavigateToDiscovery,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Explore, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Browse Public Hostel Catalog & Map",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SecondaryTeal
                )
            }
        }
    }

    if (showForgotPasswordDialog && authViewModel != null) {
        ForgotPasswordDialog(
            authViewModel = authViewModel,
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}

@Composable
private fun RoleIconTab(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.2f) else Color(0xFF132238).copy(alpha = 0.7f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) activeColor else Color(0xFF284469)
        ),
        modifier = modifier.height(64.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isSelected) activeColor else Color(0xFF8BB5E8),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFF8BB5E8),
                fontSize = 11.sp
            )
        }
    }
}
