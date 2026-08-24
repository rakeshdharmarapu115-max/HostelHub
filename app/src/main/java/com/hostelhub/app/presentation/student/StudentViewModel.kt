package com.hostelhub.app.presentation.student

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
class StudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val roomRepository: RoomRepository,
    private val feePaymentRepository: FeePaymentRepository,
    private val complaintRepository: ComplaintRepository,
    private val attendanceRepository: AttendanceRepository,
    private val foodMenuRepository: FoodMenuRepository,
    private val hostelRepository: HostelRepository,
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _currentStudentId = MutableStateFlow("")
    val currentStudentId: StateFlow<String> = _currentStudentId.asStateFlow()

    private val _studentProfile = MutableStateFlow<UiState<Student>>(UiState.Loading)
    val studentProfile: StateFlow<UiState<Student>> = _studentProfile.asStateFlow()

    private val _dashboardStats = MutableStateFlow<UiState<StudentDashboardStats>>(UiState.Loading)
    val dashboardStats: StateFlow<UiState<StudentDashboardStats>> = _dashboardStats.asStateFlow()

    private val _room = MutableStateFlow<UiState<Room>>(UiState.Idle)
    val room: StateFlow<UiState<Room>> = _room.asStateFlow()
    val roomDetails: StateFlow<UiState<Room>> = _room.asStateFlow()

    private val _fees = MutableStateFlow<UiState<List<Fee>>>(UiState.Loading)
    val fees: StateFlow<UiState<List<Fee>>> = _fees.asStateFlow()

    private val _payments = MutableStateFlow<UiState<List<Payment>>>(UiState.Loading)
    val payments: StateFlow<UiState<List<Payment>>> = _payments.asStateFlow()

    private val _complaints = MutableStateFlow<UiState<List<Complaint>>>(UiState.Loading)
    val complaints: StateFlow<UiState<List<Complaint>>> = _complaints.asStateFlow()

    private val _attendance = MutableStateFlow<UiState<List<AttendanceRecord>>>(UiState.Loading)
    val attendance: StateFlow<UiState<List<AttendanceRecord>>> = _attendance.asStateFlow()

    private val _foodMenu = MutableStateFlow<UiState<FoodMenu>>(UiState.Loading)
    val foodMenu: StateFlow<UiState<FoodMenu>> = _foodMenu.asStateFlow()

    private val _hostels = MutableStateFlow<UiState<List<Hostel>>>(UiState.Loading)
    val hostels: StateFlow<UiState<List<Hostel>>> = _hostels.asStateFlow()

    private val _hostelReviews = MutableStateFlow<UiState<List<HostelReview>>>(UiState.Loading)
    val hostelReviews: StateFlow<UiState<List<HostelReview>>> = _hostelReviews.asStateFlow()

    private val _notifications = MutableStateFlow<UiState<List<AppNotification>>>(UiState.Loading)
    val notifications: StateFlow<UiState<List<AppNotification>>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                if (user != null && user.role == UserRole.STUDENT) {
                    val resolvedStudentId = user.studentId ?: user.userId
                    if (_currentStudentId.value != resolvedStudentId || _studentProfile.value is UiState.Idle || _studentProfile.value is UiState.Error) {
                        _currentStudentId.value = resolvedStudentId
                        loadStudentData(resolvedStudentId)
                    }
                }
            }
        }
    }

    fun setStudentId(id: String) {
        _currentStudentId.value = id
        loadStudentData(id)
    }

    fun loadStudentData(studentId: String) {
        if (studentId.isBlank()) return
        loadProfile(studentId)
        loadDashboardStats(studentId)
        loadFees(studentId)
        loadPayments(studentId)
        loadComplaints(studentId)
        loadFoodMenu("hostel_001")
        loadHostels()
        loadNotifications(studentId)
    }

    fun loadProfile(studentId: String) {
        if (studentId.isBlank()) return
        viewModelScope.launch {
            studentRepository.getStudentProfile(studentId).collect { res ->
                _studentProfile.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> {
                        val student = res.data
                        val targetRoomId = if (!student.roomId.isNullOrBlank()) {
                            student.roomId
                        } else if (!student.roomNumber.isNullOrBlank()) {
                            student.roomNumber
                        } else null

                        if (!targetRoomId.isNullOrBlank()) {
                            loadRoom(targetRoomId)
                        } else {
                            _room.value = UiState.Idle
                        }

                        val hostelId = if (!student.hostelId.isNullOrBlank()) student.hostelId else "hostel_001"
                        loadFoodMenu(hostelId)
                        UiState.Success(student)
                    }
                    is Resource.Error -> {
                        _room.value = UiState.Idle
                        UiState.Error(res.message)
                    }
                }
            }
        }
    }

    fun loadDashboardStats(studentId: String) {
        if (studentId.isBlank()) return
        viewModelScope.launch {
            studentRepository.getStudentDashboardStats(studentId).collect { res ->
                _dashboardStats.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadRoom(roomId: String) {
        if (roomId.isBlank()) {
            _room.value = UiState.Idle
            return
        }
        viewModelScope.launch {
            roomRepository.getRoomById(roomId).collect { res ->
                _room.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Idle
                }
            }
        }
    }

    fun loadFees(studentId: String) {
        if (studentId.isBlank()) return
        viewModelScope.launch {
            feePaymentRepository.getFeesForStudent(studentId).collect { res ->
                _fees.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadPayments(studentId: String) {
        if (studentId.isBlank()) return
        viewModelScope.launch {
            feePaymentRepository.getPaymentsForStudent(studentId).collect { res ->
                _payments.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadComplaints(studentId: String) {
        if (studentId.isBlank()) return
        viewModelScope.launch {
            complaintRepository.getComplaintsForStudent(studentId).collect { res ->
                _complaints.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun loadAttendance(studentId: String, month: Int = 10, year: Int = 2026) {
        if (studentId.isBlank()) return
        viewModelScope.launch {
            attendanceRepository.getAttendanceForStudent(studentId, month, year).collect { res ->
                _attendance.value = when (res) {
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

    fun loadHostelReviews(hostelId: String) {
        if (hostelId.isBlank()) return
        viewModelScope.launch {
            hostelRepository.getHostelReviews(hostelId).collect { res ->
                _hostelReviews.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun submitHostelReview(
        hostelId: String,
        rating: Double,
        comment: String,
        cleanliness: Double = 5.0,
        foodQuality: Double = 5.0,
        amenitiesRating: Double = 5.0,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val student = (_studentProfile.value as? UiState.Success)?.data
            val result = hostelRepository.submitReview(
                hostelId = hostelId,
                studentId = _currentStudentId.value,
                studentName = student?.fullName ?: "Resident Student",
                rating = rating,
                comment = comment,
                cleanliness = cleanliness,
                foodQuality = foodQuality,
                amenitiesRating = amenitiesRating
            )
            when (result) {
                is Resource.Success -> {
                    loadHostels()
                    loadHostelReviews(hostelId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun loadNotifications(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            notificationRepository.getNotifications(userId).collect { res ->
                _notifications.value = when (res) {
                    is Resource.Loading -> UiState.Loading
                    is Resource.Success -> UiState.Success(res.data)
                    is Resource.Error -> UiState.Error(res.message)
                }
            }
        }
    }

    fun submitComplaint(
        title: String,
        description: String,
        category: ComplaintCategory,
        roomNumber: String = "",
        urgency: ComplaintUrgency = ComplaintUrgency.MEDIUM,
        attachments: List<String> = emptyList(),
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val student = (_studentProfile.value as? UiState.Success)?.data
            val currentSId = _currentStudentId.value
            val complaint = Complaint(
                complaintId = "",
                hostelId = student?.hostelId?.ifBlank { "hostel_001" } ?: "hostel_001",
                studentId = currentSId,
                studentName = student?.fullName ?: "Resident Student",
                roomNumber = roomNumber.ifBlank { student?.roomNumber ?: "A-204" },
                category = category,
                title = title,
                description = description,
                attachments = attachments,
                urgency = urgency,
                status = ComplaintStatus.OPEN
            )
            val result = complaintRepository.submitComplaint(complaint)
            when (result) {
                is Resource.Success -> {
                    loadComplaints(currentSId)
                    loadDashboardStats(currentSId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun payFee(
        feeId: String,
        amount: Double,
        paymentMethod: PaymentMethod = PaymentMethod.UPI,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            val student = (_studentProfile.value as? UiState.Success)?.data
            val currentSId = _currentStudentId.value
            val payment = Payment(
                paymentId = "",
                feeId = feeId,
                studentId = currentSId,
                hostelId = student?.hostelId?.ifBlank { "hostel_001" } ?: "hostel_001",
                amountPaid = amount,
                paymentMethod = paymentMethod,
                transactionReference = "TXN-" + System.currentTimeMillis(),
                status = PaymentStatus.SUCCESS
            )
            val result = feePaymentRepository.recordPayment(payment)
            when (result) {
                is Resource.Success -> {
                    loadFees(currentSId)
                    loadPayments(currentSId)
                    loadDashboardStats(currentSId)
                    onSuccess()
                }
                is Resource.Error -> {
                    onError(result.message)
                }
                is Resource.Loading -> {}
            }
        }
    }

    fun markSelfAttendance(status: AttendanceStatus = AttendanceStatus.PRESENT, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val student = (_studentProfile.value as? UiState.Success)?.data
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            val record = AttendanceRecord(
                attendanceId = "",
                hostelId = student?.hostelId?.ifBlank { "hostel_001" } ?: "hostel_001",
                studentId = _currentStudentId.value,
                studentName = student?.fullName ?: "",
                roomNumber = student?.roomNumber ?: "",
                date = today,
                status = status,
                checkInTime = System.currentTimeMillis(),
                markedBy = "STUDENT_SELF"
            )
            attendanceRepository.markAttendance(record)
            loadAttendance(_currentStudentId.value)
            loadDashboardStats(_currentStudentId.value)
            onSuccess()
        }
    }

    fun updateProfile(student: Student, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            studentRepository.updateStudentProfile(student)
            loadProfile(student.studentId.ifBlank { _currentStudentId.value })
            onSuccess()
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
            loadNotifications(_currentStudentId.value)
        }
    }
}
