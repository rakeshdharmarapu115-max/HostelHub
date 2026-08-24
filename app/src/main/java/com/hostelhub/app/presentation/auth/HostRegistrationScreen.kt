package com.hostelhub.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Host
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.presentation.theme.SurfaceWhite
import com.hostelhub.app.utils.FormValidators
import com.hostelhub.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostRegistrationScreen(
    authViewModel: AuthViewModel? = null,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var businessNameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val registerState by authViewModel?.registerState?.collectAsState() ?: remember { mutableStateOf(UiState.Idle) }
    val isLoading = registerState is UiState.Loading
    val generalError = (registerState as? UiState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register as Hostel Host", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
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
                    text = "Property Owner Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryNavy
                )
                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        fullNameError = null
                    },
                    label = "Full Name",
                    placeholder = "Enter your full name",
                    leadingIcon = Icons.Default.Person,
                    errorMessage = fullNameError
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = businessName,
                    onValueChange = {
                        businessName = it
                        businessNameError = null
                    },
                    label = "Business / Property Name",
                    placeholder = "e.g. Green Valley Residencies Inc",
                    leadingIcon = Icons.Default.Business,
                    errorMessage = businessNameError
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = "Business Email",
                    placeholder = "e.g. warden@greenvalley.edu",
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
                    placeholder = "Enter phone number",
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
                    text = "Register as Host",
                    onClick = {
                        val nameVal = FormValidators.validateFullName(fullName)
                        val busVal = FormValidators.validateRequired(businessName, "Business name")
                        val emailVal = FormValidators.validateEmail(email)
                        val phoneVal = FormValidators.validatePhone(phone)
                        val passVal = FormValidators.validatePassword(password)

                        if (!nameVal.isValid) fullNameError = nameVal.errorMessage
                        if (!busVal.isValid) businessNameError = busVal.errorMessage
                        if (!emailVal.isValid) emailError = emailVal.errorMessage
                        if (!phoneVal.isValid) phoneError = phoneVal.errorMessage
                        if (!passVal.isValid) passwordError = passVal.errorMessage

                        if (nameVal.isValid && busVal.isValid && emailVal.isValid && phoneVal.isValid && passVal.isValid) {
                            val newHost = Host(
                                hostId = "",
                                userId = "",
                                fullName = fullName,
                                businessName = businessName,
                                contactPhone = phone,
                                contactEmail = email,
                                verifiedStatus = true
                            )
                            if (authViewModel != null) {
                                authViewModel.registerHost(newHost, password, onSuccess = onRegistrationSuccess)
                            } else {
                                onRegistrationSuccess()
                            }
                        }
                    },
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already registered?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Log in here",
                            style = MaterialTheme.typography.labelLarge,
                            color = SecondaryTeal
                        )
                    }
                }
            }
        }
    }
}
