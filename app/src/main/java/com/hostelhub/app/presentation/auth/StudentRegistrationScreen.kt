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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.hostelhub.app.data.remote.dto.ValidateStudentIdResponseDto
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegistrationScreen(
    authViewModel: AuthViewModel? = null,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    // Step state: 1 = Enter & Validate Student ID, 2 = Set Password & Confirm Details
    var currentStep by remember { mutableIntStateOf(1) }

    // Step 1: Student ID
    var studentIdInput by remember { mutableStateOf("") }
    var isValidatingId by remember { mutableStateOf(false) }
    var studentIdError by remember { mutableStateOf<String?>(null) }
    var validatedStudent by remember { mutableStateOf<ValidateStudentIdResponseDto?>(null) }

    // Step 2: Personal Credentials
    var mobileInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var mobileError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    var isActivating by remember { mutableStateOf(false) }
    var activationGeneralError by remember { mutableStateOf<String?>(null) }

    fun validateStudentId() {
        focusManager.clearFocus()
        val rawId = studentIdInput.trim()
        if (rawId.isBlank()) {
            studentIdError = "Please enter your unique Student ID."
            return
        }

        isValidatingId = true
        studentIdError = null

        authViewModel?.validateStudentId(
            studentId = rawId,
            onSuccess = { dto ->
                isValidatingId = false
                validatedStudent = dto
                currentStep = 2
            },
            onError = { errorMsg ->
                isValidatingId = false
                studentIdError = errorMsg
            }
        )
    }

    fun completeRegistration() {
        focusManager.clearFocus()
        var hasError = false

        if (mobileInput.isBlank() && emailInput.isBlank()) {
            mobileError = "Please enter your mobile number or email."
            hasError = true
        }

        if (passwordInput.isBlank() || passwordInput.length < 6) {
            passwordError = "Password must be at least 6 characters."
            hasError = true
        }

        if (passwordInput != confirmPasswordInput) {
            confirmPasswordError = "Passwords do not match."
            hasError = true
        }

        if (!hasError) {
            isActivating = true
            activationGeneralError = null

            val primaryIdentifier = if (emailInput.isNotBlank()) emailInput.trim() else mobileInput.trim()

            authViewModel?.activateStudent(
                studentId = studentIdInput.trim(),
                emailOrPhone = primaryIdentifier,
                password = passwordInput,
                onSuccess = {
                    isActivating = false
                    Toast.makeText(context, "Registration successful! Welcome to HostelHub.", Toast.LENGTH_LONG).show()
                    onRegistrationSuccess()
                },
                onError = { err ->
                    isActivating = false
                    activationGeneralError = err
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentStep == 1) "Student Registration" else "Create Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep == 2) {
                            currentStep = 1
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Progress Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                StepIndicatorItem(stepNumber = 1, title = "Student ID", isActive = currentStep >= 1, isCompleted = currentStep > 1)
                HorizontalDivider(
                    modifier = Modifier
                        .width(48.dp)
                        .padding(horizontal = 8.dp),
                    color = if (currentStep > 1) PrimaryNavy else MaterialTheme.colorScheme.outlineVariant
                )
                StepIndicatorItem(stepNumber = 2, title = "Password", isActive = currentStep == 2, isCompleted = false)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (currentStep == 1) {
                // ==================== STEP 1: VALIDATE STUDENT ID ====================
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "Enter Owner-Issued Student ID",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Every hostel resident receives a unique Student ID (e.g. STU-2026-0001) generated by the hostel owner upon room allocation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        OutlinedTextField(
                            value = studentIdInput,
                            onValueChange = {
                                studentIdInput = it.uppercase()
                                studentIdError = null
                            },
                            label = { Text("Unique Student ID") },
                            placeholder = { Text("e.g. STU-2026-0001") },
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = PrimaryNavy)
                            },
                            trailingIcon = {
                                if (studentIdInput.isNotBlank()) {
                                    IconButton(onClick = { studentIdInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            isError = studentIdError != null,
                            supportingText = studentIdError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { validateStudentId() }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryNavy,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = { validateStudentId() },
                            enabled = !isValidatingId,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryNavy,
                                contentColor = Color.White
                            )
                        ) {
                            if (isValidatingId) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "VALIDATE STUDENT ID",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PrimaryNavy.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Don't have a Student ID yet? Please ask your hostel owner to add you to their resident directory.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // ==================== STEP 2: COMPLETE REGISTRATION ====================
                validatedStudent?.let { stu ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Verified Resident Banner
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SecondaryTeal.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SecondaryTeal.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(SecondaryTeal, shape = CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Verified Resident: ${stu.fullName}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy
                                        )
                                        Text(
                                            text = "Hostel: ${stu.hostelName ?: "Campus Hostel"} • ID: ${stu.rollNumber}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!stu.roomNumber.isNullOrBlank()) {
                                            Text(
                                                text = "Assigned Room: Room ${stu.roomNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = SecondaryTeal
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Set Up Your Personal Login Credentials",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Mobile Number
                            OutlinedTextField(
                                value = mobileInput,
                                onValueChange = {
                                    mobileInput = it
                                    mobileError = null
                                },
                                label = { Text("Mobile Phone Number") },
                                placeholder = { Text("e.g. 9876543210") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryNavy) },
                                isError = mobileError != null,
                                supportingText = mobileError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Email Address
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    emailError = null
                                },
                                label = { Text("Email Address") },
                                placeholder = { Text("e.g. student@gmail.com") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryNavy) },
                                isError = emailError != null,
                                supportingText = emailError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Password
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    passwordError = null
                                },
                                label = { Text("Personal Password") },
                                placeholder = { Text("Minimum 6 characters") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryNavy) },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = passwordError != null,
                                supportingText = passwordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Confirm Password
                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = {
                                    confirmPasswordInput = it
                                    confirmPasswordError = null
                                },
                                label = { Text("Confirm Password") },
                                placeholder = { Text("Re-enter password") },
                                leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = PrimaryNavy) },
                                trailingIcon = {
                                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                isError = confirmPasswordError != null,
                                supportingText = confirmPasswordError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { completeRegistration() }),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            AnimatedVisibility(visible = !activationGeneralError.isNullOrBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp)
                                ) {
                                    Text(
                                        text = activationGeneralError ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = { completeRegistration() },
                                enabled = !isActivating,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryNavy,
                                    contentColor = Color.White
                                )
                            ) {
                                if (isActivating) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "COMPLETE REGISTRATION",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Back to Login Prompt
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Already registered? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNavy,
                    modifier = Modifier
                        .clickable { onNavigateToLogin() }
                        .padding(4.dp)
                )
            }
        }
    }
}

@Composable
private fun StepIndicatorItem(
    stepNumber: Int,
    title: String,
    isActive: Boolean,
    isCompleted: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = when {
                        isCompleted -> SecondaryTeal
                        isActive -> PrimaryNavy
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            } else {
                Text(
                    text = "$stepNumber",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) PrimaryNavy else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
