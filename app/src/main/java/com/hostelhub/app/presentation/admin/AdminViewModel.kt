package com.hostelhub.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hostelhub.app.domain.model.*
import com.hostelhub.app.domain.repository.*
import com.hostelhub.app.utils.Resource
import com.hostelhub.app.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val authRepository: AuthRepository,
    private val studentRepository: StudentRepository,
    private val announcementRepository: AnnouncementRepository,
    private val complaintRepository: ComplaintRepository,
    private val feePaymentRepository: FeePaymentRepository
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _dashboardStats = MutableStateFlow<UiState<AdminDashboardStats>>(UiState.Loading)
    val dashboardStats: StateFlow<UiState<AdminDashboardStats>> = _dashboardStats.asStateFlow()

    private val _hostels = MutableStateFlow<UiState<List<Hostel>>>(UiState.Loading)
    val hostels: StateFlow<UiState<List<Hostel>>> = _hostels.asStateFlow()

    private val _users = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val users: StateFlow<UiState<List<User>>> = _users.asStateFlow()

    private val _generatedStudentId = MutableStateFlow<String>("")
    val generatedStudentId: StateFlow<String> = _generatedStudentId.asStateFlow()

    private val _announcements = MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading)
    val announcements: StateFlow<UiState<List<Announcement>>> = _announcements.asStateFlow()

    private val _complaints = MutableStateFlow<UiState<List<Complaint>>>(UiState.Loading)
    val complaints: StateFlow<UiState<List<Complaint>>> = _complaints.asStateFlow()

    private val _fees = MutableStateFlow<UiState<List<Fee>>>(UiState.Loading)
    val fees: StateFlow<UiState<List<Fee>>> = _fees.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _currentUser.value = user
                if (user != null && user.role == UserRole.ADMIN && (_dashboardStats.value is UiState.Loading || _dashboardStats.value is UiState.Idle)) {
                    loadAdminData()
                }
            }
        }
    }

    fun loadAdminData() {
        loadDashboardStats()
        loadHostels()
        loadUsers()
        loadAnnouncements()
        loadComplaints()
        loadFees()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            hostelRepository.getAdminDashboardStats().collect { res ->
                _dashboardStats.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadHostels() {
        viewModelScope.launch {
            hostelRepository.getHostels().collect { res ->
                _hostels.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadUsers() {
        viewModelScope.launch {
            authRepository.getAllUsers().collect { res ->
                _users.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadAnnouncements() {
        viewModelScope.launch {
            announcementRepository.getAnnouncements("GLOBAL_CAMPUS").collect { res ->
                _announcements.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadComplaints() {
        viewModelScope.launch {
            complaintRepository.getAllComplaints().collect { res ->
                _complaints.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadFees() {
        viewModelScope.launch {
            feePaymentRepository.getAllFees().collect { res ->
                _fees.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun toggleUserStatus(userId: String, isActive: Boolean, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.toggleUserStatus(userId, isActive)
            loadUsers()
            onSuccess()
        }
    }

    fun broadcastAnnouncement(
        title: String,
        message: String,
        priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
        targetAudience: String = "ALL",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val announcement = Announcement(
                announcementId = "",
                hostelId = "GLOBAL_CAMPUS",
                senderId = user?.userId ?: "admin",
                senderRole = UserRole.ADMIN,
                senderName = user?.fullName ?: "Campus Administration",
                title = title,
                message = message,
                priority = priority,
                targetAudience = targetAudience
            )
            announcementRepository.createAnnouncement(announcement)
            loadAnnouncements()
            onSuccess()
        }
    }

    fun fetchGeneratedStudentId() {
        viewModelScope.launch {
            when (val res = studentRepository.generateStudentId()) {
                is Resource.Success -> {
                    _generatedStudentId.value = res.data
                }
                is Resource.Error -> {
                    val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val rand = (1000..9999).random()
                    _generatedStudentId.value = "STU-$year-$rand"
                }
                else -> {}
            }
        }
    }

    fun createStudentByAdmin(
        student: Student,
        password: String,
        onSuccess: (Student) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val res = studentRepository.createStudentByAdmin(student, password)) {
                is Resource.Success -> {
                    loadUsers()
                    onSuccess(res.data)
                }
                is Resource.Error -> {
                    onError(res.message)
                }
                else -> {}
            }
        }
    }

    fun updateHostel(hostel: Hostel, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            hostelRepository.updateHostel(hostel)
            loadHostels()
            loadDashboardStats()
            onSuccess()
        }
    }
}
