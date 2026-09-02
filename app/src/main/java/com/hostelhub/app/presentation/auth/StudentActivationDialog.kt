package com.hostelhub.app.presentation.auth

import android.widget.Toast
import androidx.compose.animation.*
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
import com.hostelhub.app.data.remote.dto.ValidateStudentIdResponseDto
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.theme.*

@Composable
fun StudentActivationDialog(
    authViewModel: AuthViewModel,
    onDismiss: () -> Unit,
    onActivationSuccess: (UserRole) -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(1) }
    var studentIdInput by remember { mutableStateOf("") }
    var validatedStudent by remember { mutableStateOf<ValidateStudentIdResponseDto?>(null) }
    var isValidating by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Step 2 Fields
    var emailOrPhoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isActivating by remember { mutableStateOf(false) }
    var activationError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            if (!isActivating && !isValidating) {
                authViewModel.resetStudentActivationFlow()
                onDismiss()
            }
        },
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
                // Header
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(PrimaryContainer, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (currentStep == 1) Icons.Default.Badge else Icons.Default.LockReset,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (currentStep == 1) "Activate Student Account" else "Create Personal Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (currentStep == 1)
                        "Enter the Student ID issued by your hostel management to verify your room allocation."
                    else
                        "Set up your personal credentials to log in securely anytime.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                // Step 1: Validate Student ID
                if (currentStep == 1) {
                    AppTextField(
                        value = studentIdInput,
                        onValueChange = {
                            studentIdInput = it.uppercase()
                            validationError = null
                        },
                        label = "Hostel Student ID",
                        placeholder = "e.g. STU-2026-0001",
                        leadingIcon = Icons.Default.Pin,
                        errorMessage = validationError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AppButton(
                        text = if (isValidating) "Verifying ID..." else "Verify Student ID",
                        onClick = {
                            if (studentIdInput.isBlank()) {
                                validationError = "Please enter your Student ID."
                                return@AppButton
                            }
                            isValidating = true
                            validationError = null
                            authViewModel.validateStudentId(
                                studentId = studentIdInput.trim(),
                                onSuccess = { dto ->
                                    isValidating = false
                                    validatedStudent = dto
                                    currentStep = 2
                                },
                                onError = { errorMsg ->
                                    isValidating = false
                                    validationError = errorMsg
                                }
                            )
                        },
                        isLoading = isValidating,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Step 2: Show Verified Badge & Set Password
                    validatedStudent?.let { stu ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = StatusSuccessBg.copy(alpha = 0.7f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = StatusSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Verified: ${stu.fullName}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusSuccess
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Hostel: ${stu.hostelName ?: "Campus Hostel"}${if (!stu.roomNumber.isNullOrBlank()) " • Room: ${stu.roomNumber}" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "College: ${stu.collegeName} (${stu.course})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    AppTextField(
                        value = emailOrPhoneInput,
                        onValueChange = {
                            emailOrPhoneInput = it
                            activationError = null
                        },
                        label = "Mobile Number or Gmail / Email",
                        placeholder = "e.g. 9876543210 or yourname@gmail.com",
                        leadingIcon = Icons.Default.ContactPhone,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = passwordInput,
                        onValueChange = {
                            passwordInput = it
                            activationError = null
                        },
                        label = "Create Personal Password",
                        placeholder = "At least 6 characters",
                        isPassword = true,
                        leadingIcon = Icons.Default.Lock,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            activationError = null
                        },
                        label = "Confirm Password",
                        placeholder = "Re-type personal password",
                        isPassword = true,
                        leadingIcon = Icons.Default.LockClock,
                        errorMessage = activationError,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AppButton(
                        text = if (isActivating) "Activating Account..." else "Activate & Sign In",
                        onClick = {
                            if (emailOrPhoneInput.isBlank()) {
                                activationError = "Please enter your mobile number or Gmail."
                                return@AppButton
                            }
                            if (passwordInput.length < 6) {
                                activationError = "Password must be at least 6 characters long."
                                return@AppButton
                            }
                            if (passwordInput != confirmPasswordInput) {
                                activationError = "Passwords do not match."
                                return@AppButton
                            }

                            isActivating = true
                            activationError = null
                            authViewModel.activateStudent(
                                studentId = validatedStudent?.rollNumber ?: studentIdInput,
                                emailOrPhone = emailOrPhoneInput.trim(),
                                password = passwordInput,
                                onSuccess = { user ->
                                    isActivating = false
                                    Toast.makeText(context, "Account activated successfully! Welcome ${user.fullName}", Toast.LENGTH_LONG).show()
                                    onActivationSuccess(UserRole.STUDENT)
                                },
                                onError = { err ->
                                    isActivating = false
                                    activationError = err
                                }
                            )
                        },
                        isLoading = isActivating,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { currentStep = 1 },
                        enabled = !isActivating
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Change Student ID", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = {
                        authViewModel.resetStudentActivationFlow()
                        onDismiss()
                    },
                    enabled = !isValidating && !isActivating
                ) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
