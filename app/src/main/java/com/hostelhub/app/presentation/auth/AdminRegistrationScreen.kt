package com.hostelhub.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Admin
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.FormValidators
import com.hostelhub.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRegistrationScreen(
    authViewModel: AuthViewModel? = null,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var associationName by remember { mutableStateOf("Campus Hostel Housing Association") }
    var designation by remember { mutableStateOf("Chief Association Head") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var associationError by remember { mutableStateOf<String?>(null) }
    var designationError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val registerState by authViewModel?.registerState?.collectAsState() ?: remember { mutableStateOf(UiState.Idle) }
    val isLoading = registerState is UiState.Loading
    val generalError = (registerState as? UiState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Association Head Registration",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AdminAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AdminBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(AdminHeroBg, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SupervisorAccount,
                    contentDescription = null,
                    tint = AdminAccentContainer,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Housing Council & Governance Portal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = AdminOnAccentContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Register as an authorized campus association administrator or council head to monitor multi-hostel logistics and broadcast directives.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            AppCard(padding = 20.dp) {
                if (generalError != null) {
                    Text(
                        text = generalError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = "Administrator Credentials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = AdminOnAccentContainer
                )
                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        fullNameError = null
                    },
                    label = "Full Name",
                    placeholder = "e.g. Dean Henderson",
                    leadingIcon = Icons.Default.Person,
                    errorMessage = fullNameError
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = associationName,
                    onValueChange = {
                        associationName = it
                        associationError = null
                    },
                    label = "Association / Department Name",
                    placeholder = "e.g. Central Campus Housing Council",
                    leadingIcon = Icons.Default.AccountBalance,
                    errorMessage = associationError
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = designation,
                    onValueChange = {
                        designation = it
                        designationError = null
                    },
                    label = "Council Designation",
                    placeholder = "e.g. President / Association Head",
                    leadingIcon = Icons.Default.Badge,
                    errorMessage = designationError
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = "Official Email Address",
                    placeholder = "e.g. admin@campus.edu",
                    leadingIcon = Icons.Default.Email,
                    errorMessage = emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                        phoneError = null
                    },
                    label = "Contact Phone",
                    placeholder = "e.g. +91 98765 43210",
                    leadingIcon = Icons.Default.Phone,
                    errorMessage = phoneError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = "Create Password",
                    placeholder = "Minimum 8 characters",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    errorMessage = passwordError
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = "Register Association Head Account",
                    onClick = {
                        val nameVal = FormValidators.validateFullName(fullName)
                        val assocVal = FormValidators.validateRequired(associationName, "Association name")
                        val desigVal = FormValidators.validateRequired(designation, "Designation")
                        val emailVal = FormValidators.validateEmail(email)
                        val phoneVal = FormValidators.validatePhone(phone)
                        val passVal = FormValidators.validatePassword(password)

                        if (!nameVal.isValid) fullNameError = nameVal.errorMessage
                        if (!assocVal.isValid) associationError = assocVal.errorMessage
                        if (!desigVal.isValid) designationError = desigVal.errorMessage
                        if (!emailVal.isValid) emailError = emailVal.errorMessage
                        if (!phoneVal.isValid) phoneError = phoneVal.errorMessage
                        if (!passVal.isValid) passwordError = passVal.errorMessage

                        if (nameVal.isValid && assocVal.isValid && desigVal.isValid && emailVal.isValid && phoneVal.isValid && passVal.isValid) {
                            val newAdmin = Admin(
                                adminId = "",
                                userId = email,
                                fullName = fullName,
                                associationName = associationName,
                                designation = designation,
                                contactPhone = phone,
                                permissions = listOf("ALL")
                            )
                            if (authViewModel != null) {
                                authViewModel.registerAdmin(newAdmin, password, onSuccess = onRegistrationSuccess)
                            } else {
                                onRegistrationSuccess()
                            }
                        }
                    },
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an Association account?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Sign in here",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = AdminAccent
                        )
                    }
                }
            }
        }
    }
}
