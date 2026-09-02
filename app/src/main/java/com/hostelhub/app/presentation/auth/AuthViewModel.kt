package com.hostelhub.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.app.data.remote.ConnectionTestResult
import com.hostelhub.app.data.remote.NetworkConfig
import com.hostelhub.app.data.remote.dto.ForgotPasswordResponseDto
import com.hostelhub.app.data.remote.dto.ValidateStudentIdResponseDto
import com.hostelhub.app.domain.model.Host
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.domain.model.User
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.domain.repository.AuthRepository
import com.hostelhub.app.utils.Resource
import com.hostelhub.app.utils.UiState
import com.hostelhub.app.data.remote.datasource.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val hostelRepository: com.hostelhub.app.domain.repository.HostelRepository,
    private val networkConfig: NetworkConfig,
    private val tokenManager: TokenManager
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val deallocationNoticeFlow = tokenManager.deallocationNoticeFlow

    private val _availableHostels = MutableStateFlow<List<com.hostelhub.app.domain.model.Hostel>>(emptyList())
    val availableHostels: StateFlow<List<com.hostelhub.app.domain.model.Hostel>> = _availableHostels.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

    private val _studentValidationState = MutableStateFlow<UiState<ValidateStudentIdResponseDto>>(UiState.Idle)
    val studentValidationState: StateFlow<UiState<ValidateStudentIdResponseDto>> = _studentValidationState.asStateFlow()

    private val _activationState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val activationState: StateFlow<UiState<User>> = _activationState.asStateFlow()

    init {
        loadHostels()
    }

    fun loadHostels() {
        viewModelScope.launch {
            hostelRepository.getHostels().collect { res ->
                if (res is Resource.Success) {
                    _availableHostels.value = res.data
                }
            }
        }
    }

    fun login(identifier: String, password: String, role: UserRole, onSuccess: (UserRole) -> Unit = {}) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(identifier.trim(), password, role)) {
                is Resource.Success -> {
                    _loginState.value = UiState.Success(result.data)
                    onSuccess(result.data.role)
                }
                is Resource.Error -> {
                    _loginState.value = UiState.Error(result.message)
                }
                is Resource.Loading -> {
                    _loginState.value = UiState.Loading
                }
            }
        }
    }

    fun validateStudentId(
        studentId: String,
        onSuccess: (ValidateStudentIdResponseDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _studentValidationState.value = UiState.Loading
            when (val result = authRepository.validateStudentId(studentId.trim())) {
                is Resource.Success -> {
                    _studentValidationState.value = UiState.Success(result.data)
                    onSuccess(result.data)
                }
                is Resource.Error -> {
                    _studentValidationState.value = UiState.Error(result.message)
                    onError(result.message)
                }
                is Resource.Loading -> {
                    _studentValidationState.value = UiState.Loading
                }
            }
        }
    }

    fun activateStudent(
        studentId: String,
        emailOrPhone: String,
        password: String,
        onSuccess: (User) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            _activationState.value = UiState.Loading
            when (val result = authRepository.activateStudent(studentId.trim(), emailOrPhone.trim(), password)) {
                is Resource.Success -> {
                    _activationState.value = UiState.Success(result.data)
                    _loginState.value = UiState.Success(result.data)
                    onSuccess(result.data)
                }
                is Resource.Error -> {
                    _activationState.value = UiState.Error(result.message)
                    onError(result.message)
                }
                is Resource.Loading -> {
                    _activationState.value = UiState.Loading
                }
            }
        }
    }

    fun resetStudentActivationFlow() {
        _studentValidationState.value = UiState.Idle
        _activationState.value = UiState.Idle
    }

    fun forgotPassword(
        identifier: String,
        onSuccess: (ForgotPasswordResponseDto) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = authRepository.forgotPassword(identifier.trim())) {
                is Resource.Success -> onSuccess(result.data)
                is Resource.Error -> onError(result.message)
                else -> {}
            }
        }
    }

    fun resetPassword(
        identifier: String,
        otp: String,
        newPassword: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = authRepository.resetPassword(identifier.trim(), otp.trim(), newPassword)) {
                is Resource.Success -> onSuccess()
                is Resource.Error -> onError(result.message)
                else -> {}
            }
        }
    }

    fun registerStudent(student: Student, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.registerStudent(student, password)) {
                is Resource.Success -> {
                    _registerState.value = UiState.Success(result.data)
                    onSuccess()
                }
                is Resource.Error -> {
                    _registerState.value = UiState.Error(result.message)
                }
                is Resource.Loading -> {
                    _registerState.value = UiState.Loading
                }
            }
        }
    }

    fun registerHost(host: Host, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.registerHost(host, password)) {
                is Resource.Success -> {
                    _registerState.value = UiState.Success(result.data)
                    onSuccess()
                }
                is Resource.Error -> {
                    _registerState.value = UiState.Error(result.message)
                }
                is Resource.Loading -> {
                    _registerState.value = UiState.Loading
                }
            }
        }
    }

    fun registerAdmin(admin: com.hostelhub.app.domain.model.Admin, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            when (val result = authRepository.registerAdmin(admin, password)) {
                is Resource.Success -> {
                    _registerState.value = UiState.Success(result.data)
                    onSuccess()
                }
                is Resource.Error -> {
                    _registerState.value = UiState.Error(result.message)
                }
                is Resource.Loading -> {
                    _registerState.value = UiState.Loading
                }
            }
        }
    }

    fun logout(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = UiState.Idle
            _registerState.value = UiState.Idle
            onSuccess()
        }
    }
}
