package com.hostelhub.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.app.data.remote.NetworkConfig
import com.hostelhub.app.domain.model.Host
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.domain.model.User
import com.hostelhub.app.domain.model.UserRole
import com.hostelhub.app.domain.repository.AuthRepository
import com.hostelhub.app.utils.Resource
import com.hostelhub.app.utils.UiState
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
    private val networkConfig: NetworkConfig
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _availableHostels = MutableStateFlow<List<com.hostelhub.app.domain.model.Hostel>>(emptyList())
    val availableHostels: StateFlow<List<com.hostelhub.app.domain.model.Hostel>> = _availableHostels.asStateFlow()

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val registerState: StateFlow<UiState<User>> = _registerState.asStateFlow()

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

    fun getServerUrl(): String = networkConfig.getBaseUrl()

    fun setServerUrl(url: String) {
        networkConfig.setCustomBaseUrl(url)
    }

    fun resetServerUrl() {
        networkConfig.resetToDefault()
    }

    fun isCloudOrTunnel(): Boolean = networkConfig.isCloudOrTunnel()

    fun getDisplayHost(): String = networkConfig.getDisplayHost()

    fun login(email: String, password: String, role: UserRole, onSuccess: (UserRole) -> Unit = {}) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            when (val result = authRepository.login(email, password, role)) {
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
