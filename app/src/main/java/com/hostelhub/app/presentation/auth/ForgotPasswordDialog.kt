package com.hostelhub.app.presentation.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.theme.PrimaryContainer
import com.hostelhub.app.presentation.theme.PrimaryNavy

@Composable
fun ForgotPasswordDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(1) }
    var identifier by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var generatedOtpPreview by remember { mutableStateOf<String?>(null) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(PrimaryContainer, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (step == 1) "Forgot Password" else "Reset Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (step == 1)
                        "Enter your registered mobile number, Gmail, or Student ID to verify your identity."
                    else
                        "Enter the 6-digit verification code sent to your account and choose a new password.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (step == 1) {
                    AppTextField(
                        value = identifier,
                        onValueChange = {
                            identifier = it
                            errorMessage = null
                        },
                        label = "Mobile Number / Gmail / Student ID",
                        placeholder = "e.g. 9876543210 or name@gmail.com",
                        leadingIcon = Icons.Default.AccountCircle,
                        errorMessage = errorMessage,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AppButton(
                        text = if (isLoading) "Verifying..." else "Send Verification Code",
                        onClick = {
                            if (identifier.isBlank()) {
                                errorMessage = "Please enter your registered mobile, email, or Student ID."
                                return@AppButton
                            }
                            isLoading = true
                            errorMessage = null
                            authViewModel.forgotPassword(
                                identifier = identifier.trim(),
                                onSuccess = { dto ->
                                    isLoading = false
                                    generatedOtpPreview = dto.otpPreview
                                    if (!dto.otpPreview.isNullOrBlank()) {
                                        otpCode = dto.otpPreview
                                    }
                                    step = 2
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                }
                            )
                        },
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    generatedOtpPreview?.let { preview ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            color = PrimaryContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Verification Code: $preview",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryNavy,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    AppTextField(
                        value = otpCode,
                        onValueChange = {
                            otpCode = it
                            errorMessage = null
                        },
                        label = "6-Digit Verification Code",
                        placeholder = "e.g. 123456",
                        leadingIcon = Icons.Default.Pin,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            errorMessage = null
                        },
                        label = "New Password",
                        placeholder = "Minimum 6 characters",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = "Confirm New Password",
                        placeholder = "Re-enter new password",
                        isPassword = true,
                        leadingIcon = Icons.Default.LockClock,
                        errorMessage = errorMessage,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AppButton(
                        text = if (isLoading) "Updating Password..." else "Update Password",
                        onClick = {
                            if (newPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters long."
                                return@AppButton
                            }
                            if (newPassword != confirmPassword) {
                                errorMessage = "Passwords do not match."
                                return@AppButton
                            }
                            isLoading = true
                            errorMessage = null
                            authViewModel.resetPassword(
                                identifier = identifier.trim(),
                                otp = otpCode.trim(),
                                newPassword = newPassword,
                                onSuccess = {
                                    isLoading = false
                                    Toast.makeText(context, "Password updated! You can now log in.", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                },
                                onError = { err ->
                                    isLoading = false
                                    errorMessage = err
                                }
                            )
                        },
                        isLoading = isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
