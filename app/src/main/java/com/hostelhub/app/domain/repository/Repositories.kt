package com.hostelhub.app.domain.repository

import com.hostelhub.app.domain.model.*
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun login(email: String, password: String, role: UserRole): Resource<User>
    suspend fun registerStudent(student: Student, password: String): Resource<User>
    suspend fun registerHost(host: Host, password: String): Resource<User>
    suspend fun registerAdmin(admin: Admin, password: String): Resource<User>
    suspend fun logout(): Resource<Unit>
    fun getAllUsers(): Flow<Resource<List<User>>>
    suspend fun toggleUserStatus(userId: String, isActive: Boolean): Resource<Unit>
}

interface StudentRepository {
    fun getStudentProfile(studentId: String): Flow<Resource<Student>>
    suspend fun updateStudentProfile(student: Student): Resource<Unit>
    fun getResidentsByHostel(hostelId: String): Flow<Resource<List<Student>>>
    fun getAllStudents(): Flow<Resource<List<Student>>>
    suspend fun deleteStudent(studentId: String): Resource<Unit>
    fun getStudentDashboardStats(studentId: String): Flow<Resource<StudentDashboardStats>>
}

interface HostelRepository {
    fun getHostels(): Flow<Resource<List<Hostel>>>
    fun getHostelById(hostelId: String): Flow<Resource<Hostel>>
    suspend fun updateHostel(hostel: Hostel): Resource<Unit>
    fun getHostDashboardStats(hostelId: String): Flow<Resource<HostDashboardStats>>
    fun getAdminDashboardStats(): Flow<Resource<AdminDashboardStats>>
    fun getHostelReviews(hostelId: String): Flow<Resource<List<HostelReview>>>
    suspend fun submitReview(
        hostelId: String,
        studentId: String? = null,
        studentName: String? = null,
        rating: Double,
        comment: String,
        cleanliness: Double = 5.0,
        foodQuality: Double = 5.0,
        amenitiesRating: Double = 5.0
    ): Resource<HostelReview>
    suspend fun addHostelImages(hostelId: String, images: List<String>): Resource<Hostel>
}

interface RoomRepository {
    fun getRoomsByHostel(hostelId: String): Flow<Resource<List<Room>>>
    fun getRoomById(roomId: String): Flow<Resource<Room>>
    suspend fun addRoom(room: Room): Resource<Room>
    suspend fun updateRoom(room: Room): Resource<Unit>
    suspend fun deleteRoom(roomId: String): Resource<Unit>
    suspend fun assignBed(roomId: String, bedId: String, studentId: String, studentName: String): Resource<Unit>
    suspend fun vacateBed(bedId: String, roomId: String): Resource<Unit>
}

interface FeePaymentRepository {
    fun getFeesForStudent(studentId: String): Flow<Resource<List<Fee>>>
    fun getFeesForHostel(hostelId: String): Flow<Resource<List<Fee>>>
    fun getAllFees(): Flow<Resource<List<Fee>>>
    fun getPaymentsForStudent(studentId: String): Flow<Resource<List<Payment>>>
    fun getPaymentsForHostel(hostelId: String): Flow<Resource<List<Payment>>>
    suspend fun recordPayment(payment: Payment): Resource<Payment>
    suspend fun createFee(fee: Fee): Resource<Fee>
}

interface ComplaintRepository {
    fun getComplaintsForStudent(studentId: String): Flow<Resource<List<Complaint>>>
    fun getComplaintsForHostel(hostelId: String): Flow<Resource<List<Complaint>>>
    fun getAllComplaints(): Flow<Resource<List<Complaint>>>
    fun getComplaintById(complaintId: String): Flow<Resource<Complaint>>
    suspend fun submitComplaint(complaint: Complaint): Resource<Complaint>
    suspend fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        notes: String?,
        assignedStaff: String? = null,
        resolutionSummary: String? = null
    ): Resource<Unit>
    suspend fun deleteComplaint(complaintId: String): Resource<Unit>
}

interface AttendanceRepository {
    fun getAttendanceForStudent(studentId: String, month: Int, year: Int): Flow<Resource<List<AttendanceRecord>>>
    fun getAttendanceForHostel(hostelId: String, date: String): Flow<Resource<List<AttendanceRecord>>>
    suspend fun markAttendance(record: AttendanceRecord): Resource<Unit>
    suspend fun markBatchAttendance(records: List<AttendanceRecord>): Resource<Unit>
}

interface FoodMenuRepository {
    fun getWeeklyMenu(hostelId: String, weekStartDate: String): Flow<Resource<FoodMenu>>
    suspend fun updateWeeklyMenu(menu: FoodMenu): Resource<Unit>
}

interface AnnouncementRepository {
    fun getAnnouncements(hostelId: String): Flow<Resource<List<Announcement>>>
    suspend fun createAnnouncement(announcement: Announcement): Resource<Announcement>
    suspend fun deleteAnnouncement(announcementId: String): Resource<Unit>
}

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<Resource<List<AppNotification>>>
    suspend fun markAsRead(notificationId: String): Resource<Unit>
}
