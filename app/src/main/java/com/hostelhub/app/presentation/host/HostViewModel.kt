package com.hostelhub.app.presentation.host

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
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HostViewModel @Inject constructor(
    private val hostelRepository: HostelRepository,
    private val roomRepository: RoomRepository,
    private val studentRepository: StudentRepository,
    private val feePaymentRepository: FeePaymentRepository,
    private val complaintRepository: ComplaintRepository,
    private val attendanceRepository: AttendanceRepository,
    private val foodMenuRepository: FoodMenuRepository,
    private val announcementRepository: AnnouncementRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentHostelId = MutableStateFlow("")
    val currentHostelId: StateFlow<String> = _currentHostelId.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _dashboardStats = MutableStateFlow<UiState<HostDashboardStats>>(UiState.Loading)
    val dashboardStats: StateFlow<UiState<HostDashboardStats>> = _dashboardStats.asStateFlow()

    private val _hostelInfo = MutableStateFlow<UiState<Hostel>>(UiState.Loading)
    val hostelInfo: StateFlow<UiState<Hostel>> = _hostelInfo.asStateFlow()

    private val _rooms = MutableStateFlow<UiState<List<Room>>>(UiState.Loading)
    val rooms: StateFlow<UiState<List<Room>>> = _rooms.asStateFlow()

    private val _residents = MutableStateFlow<UiState<List<Student>>>(UiState.Loading)
    val residents: StateFlow<UiState<List<Student>>> = _residents.asStateFlow()

    private val _fees = MutableStateFlow<UiState<List<Fee>>>(UiState.Loading)
    val fees: StateFlow<UiState<List<Fee>>> = _fees.asStateFlow()

    private val _payments = MutableStateFlow<UiState<List<Payment>>>(UiState.Loading)
    val payments: StateFlow<UiState<List<Payment>>> = _payments.asStateFlow()

    private val _complaints = MutableStateFlow<UiState<List<Complaint>>>(UiState.Loading)
    val complaints: StateFlow<UiState<List<Complaint>>> = _complaints.asStateFlow()

    private val _todayAttendance = MutableStateFlow<UiState<List<AttendanceRecord>>>(UiState.Loading)
    val todayAttendance: StateFlow<UiState<List<AttendanceRecord>>> = _todayAttendance.asStateFlow()

    private val _announcements = MutableStateFlow<UiState<List<Announcement>>>(UiState.Loading)
    val announcements: StateFlow<UiState<List<Announcement>>> = _announcements.asStateFlow()

    private val _foodMenu = MutableStateFlow<UiState<FoodMenu>>(UiState.Loading)
    val foodMenu: StateFlow<UiState<FoodMenu>> = _foodMenu.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _currentUser.value = user
                if (user != null && user.role == UserRole.HOST) {
                    val resolvedHostelId = user.hostelId ?: ""
                    if (_currentHostelId.value != resolvedHostelId || _dashboardStats.value is UiState.Loading) {
                        _currentHostelId.value = resolvedHostelId
                        if (resolvedHostelId.isNotBlank()) {
                            loadHostData(resolvedHostelId)
                        } else {
                            resolveHostelForHost(user)
                        }
                    }
                }
            }
        }
    }

    private fun resolveHostelForHost(user: User) {
        viewModelScope.launch {
            hostelRepository.getHostels().collect { res ->
                if (res is Resource.Success && res.data.isNotEmpty()) {
                    val hostHostel = res.data.find { it.hostId == user.hostId || it.hostId == user.userId } ?: res.data.first()
                    _currentHostelId.value = hostHostel.hostelId
                    loadHostData(hostHostel.hostelId)
                }
            }
        }
    }

    fun setHostelId(hostelId: String) {
        _currentHostelId.value = hostelId
        loadHostData(hostelId)
    }

    fun loadHostData(hostelId: String) {
        val targetHostelId = if (hostelId.isBlank()) "hostel_001" else hostelId
        loadDashboardStats(targetHostelId)
        loadHostelInfo(targetHostelId)
        loadRooms(targetHostelId)
        loadResidents(targetHostelId)
        loadFees(targetHostelId)
        loadPayments(targetHostelId)
        loadComplaints(targetHostelId)
        loadTodayAttendance(targetHostelId)
        loadAnnouncements(targetHostelId)
        loadFoodMenu(targetHostelId)
    }

    fun loadDashboardStats(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            hostelRepository.getHostDashboardStats(hostelId).collect { res ->
                _dashboardStats.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadHostelInfo(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            hostelRepository.getHostelById(hostelId).collect { res ->
                _hostelInfo.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadRooms(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            roomRepository.getRoomsByHostel(hostelId).collect { res ->
                _rooms.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadResidents(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            studentRepository.getResidentsByHostel(hostelId).collect { res ->
                _residents.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadFees(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            feePaymentRepository.getFeesForHostel(hostelId).collect { res ->
                _fees.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadPayments(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            feePaymentRepository.getPaymentsForHostel(hostelId).collect { res ->
                _payments.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadComplaints(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            complaintRepository.getComplaintsForHostel(hostelId).collect { res ->
                _complaints.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadTodayAttendance(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            attendanceRepository.getAttendanceForHostel(hostelId, today).collect { res ->
                _todayAttendance.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadAnnouncements(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            announcementRepository.getAnnouncements(hostelId).collect { res ->
                _announcements.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadFoodMenu(hostelId: String, weekStartDate: String = "2026-10-19") {
        val targetHostelId = if (hostelId.isBlank()) "hostel_001" else hostelId
        viewModelScope.launch {
            foodMenuRepository.getWeeklyMenu(targetHostelId, weekStartDate).collect { res ->
                _foodMenu.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun addRoom(
        roomNumber: String,
        floor: Int,
        block: String,
        roomType: RoomType,
        capacity: Int,
        monthlyRent: Double,
        amenities: List<String>,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
            val room = Room(
                roomId = "",
                hostelId = currentHId,
                roomNumber = roomNumber,
                floor = floor,
                block = block,
                roomType = roomType,
                totalCapacity = capacity,
                occupiedCount = 0,
                monthlyRent = monthlyRent,
                amenities = amenities,
                status = RoomStatus.AVAILABLE
            )
            roomRepository.addRoom(room)
            loadRooms(currentHId)
            loadDashboardStats(currentHId)
            onSuccess()
        }
    }

    fun createFeeInvoice(
        studentId: String,
        title: String,
        amount: Double,
        feeType: FeeType = FeeType.RENT,
        roomId: String = "",
        dueDate: Long = System.currentTimeMillis() + 15L * 24 * 3600 * 1000,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
            val fee = Fee(
                feeId = "",
                hostelId = currentHId,
                studentId = studentId,
                roomId = roomId,
                title = title,
                feeType = feeType,
                amount = amount,
                amountPaid = 0.0,
                dueDate = dueDate,
                billingMonth = Calendar.getInstance().get(Calendar.MONTH) + 1,
                billingYear = Calendar.getInstance().get(Calendar.YEAR),
                status = FeeStatus.PENDING
            )
            val result = feePaymentRepository.createFee(fee)
            when (result) {
                is Resource.Success -> {
                    loadFees(currentHId)
                    loadDashboardStats(currentHId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        notes: String?,
        assignedStaff: String? = null,
        resolutionSummary: String? = null,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = complaintRepository.updateComplaintStatus(
                complaintId = complaintId,
                status = status,
                notes = notes,
                assignedStaff = assignedStaff,
                resolutionSummary = resolutionSummary
            )
            when (result) {
                is Resource.Success -> {
                    val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
                    loadComplaints(currentHId)
                    loadDashboardStats(currentHId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun assignBed(
        roomId: String,
        bedId: String,
        studentId: String,
        studentName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = if (studentId.isBlank()) {
                roomRepository.vacateBed(bedId, roomId)
            } else {
                roomRepository.assignBed(roomId, bedId, studentId, studentName)
            }
            when (result) {
                is Resource.Success -> {
                    val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
                    loadRooms(currentHId)
                    loadResidents(currentHId)
                    loadDashboardStats(currentHId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun publishAnnouncement(
        title: String,
        message: String,
        priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
        targetAudience: String = "ALL",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
            val announcement = Announcement(
                announcementId = "",
                hostelId = currentHId,
                senderId = user?.userId ?: "host",
                senderRole = user?.role ?: UserRole.HOST,
                senderName = user?.fullName ?: "Hostel Warden",
                title = title,
                message = message,
                priority = priority,
                targetAudience = targetAudience
            )
            announcementRepository.createAnnouncement(announcement)
            loadAnnouncements(currentHId)
            onSuccess()
        }
    }

    fun updateFoodMenu(
        menu: FoodMenu,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = foodMenuRepository.updateWeeklyMenu(menu)
            when (result) {
                is Resource.Success -> {
                    val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
                    loadFoodMenu(currentHId, menu.weekStartDate)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun markBatchAttendance(records: List<AttendanceRecord>, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            attendanceRepository.markBatchAttendance(records)
            val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
            loadTodayAttendance(currentHId)
            loadDashboardStats(currentHId)
            onSuccess()
        }
    }

    fun uploadHostelImages(
        images: List<String>,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
            val result = hostelRepository.addHostelImages(currentHId, images)
            when (result) {
                is Resource.Success -> {
                    loadHostelInfo(currentHId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun generateStudentId(onResult: (String) -> Unit) {
        viewModelScope.launch {
            when (val res = studentRepository.generateStudentId()) {
                is Resource.Success -> onResult(res.data)
                is Resource.Error -> {
                    val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                    val rand = (1000..9999).random()
                    onResult("STU-$year-$rand")
                }
                else -> {}
            }
        }
    }

    fun addStudentByOwner(
        student: Student,
        onSuccess: (Student) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val resolvedStudent = if (student.hostelId.isNullOrBlank()) {
                student.copy(hostelId = _currentHostelId.value)
            } else {
                student
            }
            when (val res = studentRepository.createStudentByAdmin(resolvedStudent, "HostelResident@2026")) {
                is Resource.Success -> {
                    val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
                    loadResidents(currentHId)
                    loadRooms(currentHId)
                    loadDashboardStats(currentHId)
                    onSuccess(res.data)
                }
                is Resource.Error -> {
                    onError(res.message)
                }
                else -> {}
            }
        }
    }

    fun deallocateStudent(
        studentId: String,
        remarks: String = "",
        onSuccess: (Student) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = studentRepository.deallocateStudent(studentId, remarks)
            when (result) {
                is Resource.Success -> {
                    val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
                    loadResidents(currentHId)
                    loadRooms(currentHId)
                    loadDashboardStats(currentHId)
                    onSuccess(result.data)
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun updateHostelLocation(
        latitude: Double,
        longitude: Double,
        address: String,
        city: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val currentHId = _currentHostelId.value.ifBlank { "hostel_001" }
            val result = hostelRepository.updateHostelLocation(currentHId, latitude, longitude, address, city)
            when (result) {
                is Resource.Success -> {
                    loadHostelInfo(currentHId)
                    loadDashboardStats(currentHId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }
}
