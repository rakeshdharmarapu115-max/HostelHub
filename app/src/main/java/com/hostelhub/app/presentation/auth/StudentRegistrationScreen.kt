package com.hostelhub.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Hostel
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.domain.model.StudentStatus
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryContainer
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.presentation.theme.SurfaceWhite
import com.hostelhub.app.utils.FormValidators
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegistrationScreen(
    authViewModel: AuthViewModel? = null,
    onRegistrationSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var studentId by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var college by remember { mutableStateOf("Engineering") }
    var course by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("1st Year") }
    var gender by remember { mutableStateOf("Male") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val availableHostels by authViewModel?.availableHostels?.collectAsState() ?: remember { mutableStateOf(emptyList()) }
    var selectedHostel by remember { mutableStateOf<Hostel?>(null) }

    LaunchedEffect(availableHostels) {
        if (selectedHostel == null && availableHostels.isNotEmpty()) {
            selectedHostel = availableHostels.first()
        }
    }

    var fullNameError by remember { mutableStateOf<String?>(null) }
    var studentIdError by remember { mutableStateOf<String?>(null) }
    var mobileError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val registerState by authViewModel?.registerState?.collectAsState() ?: remember { mutableStateOf(UiState.Idle) }
    val isLoading = registerState is UiState.Loading
    val generalError = (registerState as? UiState.Error)?.message

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Student Account", style = MaterialTheme.typography.titleLarge) },
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
                    text = "1. Target Hostel Selection",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryNavy
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select which campus hostel you are enrolling in:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                if (availableHostels.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        availableHostels.forEach { hostel ->
                            val isSelected = selectedHostel?.hostelId == hostel.hostelId
                            Surface(
                                onClick = { selectedHostel = hostel },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) SecondaryTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SecondaryTeal) else null,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Apartment,
                                        contentDescription = null,
                                        tint = if (isSelected) SecondaryTeal else PrimaryNavy,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = hostel.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (isSelected) PrimaryNavy else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${hostel.genderType.name} • ${hostel.address}, ${hostel.city} • From ${Formatters.formatCurrencyNoDecimals(hostel.baseMonthlyRent)}/mo",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = SecondaryTeal, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "🏨 Campus General Residencies (Default)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryNavy,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "2. Personal & Academic Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryNavy
                )
                Spacer(modifier = Modifier.height(14.dp))

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
                    value = studentId,
                    onValueChange = {
                        studentId = it
                        studentIdError = null
                    },
                    label = "Roll / Student ID",
                    placeholder = "e.g. 24248-cs-093",
                    errorMessage = studentIdError
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = "Email Address",
                    placeholder = "e.g. name@campus.edu",
                    leadingIcon = Icons.Default.Email,
                    errorMessage = emailError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = mobile,
                    onValueChange = {
                        mobile = it
                        mobileError = null
                    },
                    label = "Mobile Phone",
                    placeholder = "Enter 10-digit mobile number",
                    leadingIcon = Icons.Default.Phone,
                    errorMessage = mobileError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = course,
                    onValueChange = { course = it },
                    label = "Course & Branch",
                    placeholder = "e.g. Diploma Computer Science"
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = "Permanent Address",
                    placeholder = "Enter home town / address",
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = "Create Account Password",
                    placeholder = "Minimum 8 characters",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    errorMessage = passwordError
                )

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = "Create Student Account",
                    onClick = {
                        val nameVal = FormValidators.validateFullName(fullName)
                        val idVal = FormValidators.validateStudentId(studentId)
                        val mobVal = FormValidators.validatePhone(mobile)
                        val emailVal = FormValidators.validateEmail(email)
                        val passVal = FormValidators.validatePassword(password)

                        if (!nameVal.isValid) fullNameError = nameVal.errorMessage
                        if (!idVal.isValid) studentIdError = idVal.errorMessage
                        if (!mobVal.isValid) mobileError = mobVal.errorMessage
                        if (!emailVal.isValid) emailError = emailVal.errorMessage
                        if (!passVal.isValid) passwordError = passVal.errorMessage

                        if (nameVal.isValid && idVal.isValid && mobVal.isValid && emailVal.isValid && passVal.isValid) {
                            val chosenHostel = selectedHostel ?: availableHostels.firstOrNull()
                            val newStudent = Student(
                                studentId = "",
                                userId = "",
                                email = email,
                                fullName = fullName,
                                rollNumber = studentId,
                                collegeName = college,
                                course = if (course.isNotBlank()) course else "General Studies",
                                yearOfStudy = year,
                                gender = gender,
                                permanentAddress = if (address.isNotBlank()) address else "Campus Resident",
                                emergencyContactName = fullName + " Guardian",
                                emergencyContactPhone = mobile,
                                hostelId = chosenHostel?.hostelId,
                                hostelName = chosenHostel?.name,
                                roomId = null,
                                roomNumber = null,
                                bedNumber = null,
                                admissionDate = System.currentTimeMillis(),
                                status = StudentStatus.ACTIVE
                            )
                            if (authViewModel != null) {
                                authViewModel.registerStudent(newStudent, password, onSuccess = onRegistrationSuccess)
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
                        text = "Already have an account?",
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
