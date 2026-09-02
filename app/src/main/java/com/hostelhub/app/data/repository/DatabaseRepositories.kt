package com.hostelhub.app.data.repository

import com.hostelhub.app.data.local.db.DatabaseDaos
import com.hostelhub.app.domain.model.*
import com.hostelhub.app.domain.repository.*
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseAuthRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : AuthRepository {

    private val currentUserState = MutableStateFlow<User?>(null)

    init {
        // Initialize with default demo active student session if available
        val defaultUser = daos.getUserById("std_001")
        currentUserState.value = defaultUser
    }

    override fun getCurrentUser(): Flow<User?> = currentUserState

    override suspend fun login(email: String, password: String, role: UserRole): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val user = daos.getUserByEmail(email) ?: run {
                val fallbackId = when (role) {
                    UserRole.STUDENT -> "std_001"
                    UserRole.HOST -> "host_001"
                    UserRole.ADMIN -> "admin_001"
                }
                daos.getUserById(fallbackId) ?: User(
                    userId = fallbackId,
                    email = email,
                    role = role,
                    fullName = if (role == UserRole.STUDENT) "Alex Mercer" else if (role == UserRole.HOST) "Robert Vance" else "Dean Henderson"
                )
            }
            currentUserState.value = user
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Authentication failed")
        }
    }

    override suspend fun validateStudentId(studentId: String): Resource<com.hostelhub.app.data.remote.dto.ValidateStudentIdResponseDto> = withContext(Dispatchers.IO) {
        Resource.Success(
            com.hostelhub.app.data.remote.dto.ValidateStudentIdResponseDto(
                valid = true,
                studentId = studentId,
                rollNumber = studentId,
                fullName = "Resident Student",
                collegeName = "Engineering Campus",
                course = "B.Tech",
                hostelName = "Green Valley Residencies",
                roomNumber = "A-204"
            )
        )
    }

    override suspend fun activateStudent(studentId: String, emailOrPhone: String, password: String): Resource<User> = withContext(Dispatchers.IO) {
        val user = User(
            userId = "std_act_${System.currentTimeMillis() % 1000}",
            email = if (emailOrPhone.contains("@")) emailOrPhone else "$emailOrPhone@campus.edu",
            phoneNumber = if (!emailOrPhone.contains("@")) emailOrPhone else "",
            role = UserRole.STUDENT,
            fullName = "Resident Student",
            studentId = studentId
        )
        currentUserState.value = user
        Resource.Success(user)
    }

    override suspend fun forgotPassword(identifier: String): Resource<com.hostelhub.app.data.remote.dto.ForgotPasswordResponseDto> = withContext(Dispatchers.IO) {
        Resource.Success(
            com.hostelhub.app.data.remote.dto.ForgotPasswordResponseDto(
                success = true,
                message = "Verification code generated",
                otpPreview = "123456",
                identifier = identifier
            )
        )
    }

    override suspend fun resetPassword(identifier: String, otp: String, newPassword: String): Resource<Unit> = withContext(Dispatchers.IO) {
        Resource.Success(Unit)
    }

    override suspend fun registerStudent(student: Student, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val userId = student.studentId.ifBlank { "std_" + System.currentTimeMillis() }
            val user = User(
                userId = userId,
                email = student.rollNumber.lowercase() + "@campus.edu",
                role = UserRole.STUDENT,
                fullName = student.fullName,
                phoneNumber = student.emergencyContactPhone
            )
            daos.insertUser(user, password)
            daos.saveStudent(student.copy(studentId = userId, userId = userId))
            currentUserState.value = user
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Student registration failed")
        }
    }

    override suspend fun registerHost(host: Host, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val userId = host.hostId.ifBlank { "host_" + System.currentTimeMillis() }
            val user = User(
                userId = userId,
                email = host.contactEmail,
                role = UserRole.HOST,
                fullName = host.fullName,
                phoneNumber = host.contactPhone
            )
            daos.insertUser(user, password)
            daos.saveHost(host.copy(hostId = userId, userId = userId))
            currentUserState.value = user
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Host registration failed")
        }
    }

    override suspend fun registerAdmin(admin: Admin, password: String): Resource<User> = withContext(Dispatchers.IO) {
        try {
            val userId = admin.adminId.ifBlank { "admin_" + System.currentTimeMillis() }
            val user = User(
                userId = userId,
                email = if (admin.userId.isNotBlank()) admin.userId else "admin@campus.edu",
                role = UserRole.ADMIN,
                fullName = admin.fullName,
                phoneNumber = admin.contactPhone,
                adminId = userId
            )
            daos.insertUser(user, password)
            currentUserState.value = user
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Association Head registration failed")
        }
    }

    override suspend fun logout(): Resource<Unit> = withContext(Dispatchers.IO) {
        currentUserState.value = null
        Resource.Success(Unit)
    }

    override fun getAllUsers(): Flow<Resource<List<User>>> = flow {
        emit(Resource.Loading)
        try {
            val list = daos.getAllUsers()
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch users"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun toggleUserStatus(userId: String, isActive: Boolean): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.toggleUserStatus(userId, isActive)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user status")
        }
    }
}

@Singleton
class DatabaseStudentRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : StudentRepository {

    override fun getStudentProfile(studentId: String): Flow<Resource<Student>> = flow {
        emit(Resource.Loading)
        try {
            val student = daos.getStudentById(studentId)
            if (student != null) {
                emit(Resource.Success(student))
            } else {
                emit(Resource.Error("Student profile not found for id: $studentId"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch student profile"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateStudentProfile(student: Student): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.saveStudent(student)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }

    override fun getResidentsByHostel(hostelId: String): Flow<Resource<List<Student>>> = flow {
        emit(Resource.Loading)
        try {
            val residents = daos.getStudentsByHostel(hostelId)
            emit(Resource.Success(residents))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch hostel residents"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllStudents(): Flow<Resource<List<Student>>> = flow {
        emit(Resource.Loading)
        try {
            val students = daos.getAllStudents()
            emit(Resource.Success(students))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load all students"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun deleteStudent(studentId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.deleteStudent(studentId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete student")
        }
    }

    override fun getStudentDashboardStats(studentId: String): Flow<Resource<StudentDashboardStats>> = flow {
        emit(Resource.Loading)
        try {
            val stats = daos.getStudentDashboardStats(studentId)
            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load dashboard statistics"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun generateStudentId(): Resource<String> = withContext(Dispatchers.IO) {
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val randomSeq = String.format("%04d", (1..9999).random())
        Resource.Success("STU-$year-$randomSeq")
    }

    override suspend fun createStudentByAdmin(
        student: Student,
        password: String
    ): Resource<Student> = withContext(Dispatchers.IO) {
        val year = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        val randomSeq = String.format("%04d", (1..9999).random())
        val studentId = student.rollNumber.ifBlank { "STU-$year-$randomSeq" }
        val newStudent = student.copy(
            studentId = studentId,
            userId = student.userId.ifBlank { "usr_local_${System.currentTimeMillis()}" },
            rollNumber = studentId
        )
        daos.saveStudent(newStudent)
        Resource.Success(newStudent)
    }

    override suspend fun deallocateStudent(studentId: String, remarks: String): Resource<Student> = withContext(Dispatchers.IO) {
        val student = daos.getStudentById(studentId)
        if (student != null) {
            val updated = student.copy(
                status = com.hostelhub.app.domain.model.StudentStatus.DEALLOCATED,
                roomId = null,
                roomNumber = null,
                bedNumber = null
            )
            daos.saveStudent(updated)
            Resource.Success(updated)
        } else {
            Resource.Error("Student not found")
        }
    }
}

@Singleton
class DatabaseHostelRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : HostelRepository {

    override fun getHostels(): Flow<Resource<List<Hostel>>> = flow {
        emit(Resource.Loading)
        try {
            val list = daos.getAllHostels()
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load hostels"))
        }
    }.flowOn(Dispatchers.IO)

    override fun searchNearbyHostels(
        lat: Double?,
        lng: Double?,
        radius: Double?,
        city: String?,
        query: String?
    ): Flow<Resource<List<Hostel>>> = flow {
        emit(Resource.Loading)
        try {
            val list = daos.getAllHostels()
            val filtered = list.filter { hostel ->
                val matchCity = city.isNullOrBlank() || hostel.city.contains(city, ignoreCase = true)
                val matchQuery = query.isNullOrBlank() || hostel.name.contains(query, ignoreCase = true) || hostel.address.contains(query, ignoreCase = true)
                matchCity && matchQuery
            }
            emit(Resource.Success(filtered))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to search nearby hostels"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getHostelById(hostelId: String): Flow<Resource<Hostel>> = flow {
        emit(Resource.Loading)
        try {
            val hostel = daos.getHostelById(hostelId)
            if (hostel != null) {
                emit(Resource.Success(hostel))
            } else {
                emit(Resource.Error("Hostel not found: $hostelId"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load hostel details"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createHostel(hostel: Hostel): Resource<Hostel> = withContext(Dispatchers.IO) {
        try {
            val newHostel = hostel.copy(
                hostelId = hostel.hostelId.ifBlank { "hostel_${System.currentTimeMillis()}" }
            )
            daos.saveHostel(newHostel)
            Resource.Success(newHostel)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save hostel")
        }
    }

    override suspend fun updateHostel(hostel: Hostel): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.saveHostel(hostel)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update hostel")
        }
    }

    override fun getHostDashboardStats(hostelId: String): Flow<Resource<HostDashboardStats>> = flow {
        emit(Resource.Loading)
        try {
            val stats = daos.getHostDashboardStats(hostelId)
            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load host dashboard metrics"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAdminDashboardStats(): Flow<Resource<AdminDashboardStats>> = flow {
        emit(Resource.Loading)
        try {
            val stats = daos.getAdminDashboardStats()
            emit(Resource.Success(stats))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load admin metrics"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getHostelReviews(hostelId: String): Flow<Resource<List<com.hostelhub.app.domain.model.HostelReview>>> = flow {
        emit(Resource.Loading)
        try {
            val empty: List<com.hostelhub.app.domain.model.HostelReview> = emptyList()
            emit(Resource.Success(empty))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch reviews"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun submitReview(
        hostelId: String,
        studentId: String?,
        studentName: String?,
        rating: Double,
        comment: String,
        cleanliness: Double,
        foodQuality: Double,
        amenitiesRating: Double
    ): Resource<com.hostelhub.app.domain.model.HostelReview> = withContext(Dispatchers.IO) {
        Resource.Success(
            com.hostelhub.app.domain.model.HostelReview(
                reviewId = "rev_local",
                hostelId = hostelId,
                studentId = studentId ?: "std_001",
                studentName = studentName ?: "Student",
                rating = rating,
                comment = comment
            )
        )
    }

    override suspend fun addHostelImages(hostelId: String, images: List<String>): Resource<Hostel> = withContext(Dispatchers.IO) {
        val hostel = daos.getHostelById(hostelId)
        if (hostel != null) {
            val updated = hostel.copy(images = hostel.images + images)
            daos.saveHostel(updated)
            Resource.Success(updated)
        } else {
            Resource.Error("Hostel not found")
        }
    }

    override suspend fun updateHostelLocation(
        hostelId: String,
        latitude: Double,
        longitude: Double,
        address: String,
        city: String
    ): Resource<Hostel> = withContext(Dispatchers.IO) {
        val hostel = daos.getHostelById(hostelId)
        if (hostel != null) {
            val updated = hostel.copy(
                latitude = latitude,
                longitude = longitude,
                address = address,
                city = city
            )
            daos.saveHostel(updated)
            Resource.Success(updated)
        } else {
            Resource.Error("Hostel not found")
        }
    }
}

@Singleton
class DatabaseRoomRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : RoomRepository {

    override fun getRoomsByHostel(hostelId: String): Flow<Resource<List<Room>>> = flow {
        emit(Resource.Loading)
        try {
            val rooms = daos.getRoomsByHostel(hostelId)
            emit(Resource.Success(rooms))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch rooms"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getRoomById(roomId: String): Flow<Resource<Room>> = flow {
        emit(Resource.Loading)
        try {
            val room = daos.getRoomById(roomId)
            if (room != null) {
                emit(Resource.Success(room))
            } else {
                emit(Resource.Error("Room not found: $roomId"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load room details"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addRoom(room: Room): Resource<Room> = withContext(Dispatchers.IO) {
        try {
            val created = daos.addRoom(room)
            Resource.Success(created)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add room")
        }
    }

    override suspend fun updateRoom(room: Room): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.updateRoom(room)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update room")
        }
    }

    override suspend fun deleteRoom(roomId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.deleteRoom(roomId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete room")
        }
    }

    override suspend fun assignBed(roomId: String, bedId: String, studentId: String, studentName: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.assignBed(roomId, bedId, studentId, studentName)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to assign bed")
        }
    }

    override suspend fun vacateBed(bedId: String, roomId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.vacateBed(bedId, roomId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to vacate bed")
        }
    }
}

@Singleton
class DatabaseFeePaymentRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : FeePaymentRepository {

    override fun getFeesForStudent(studentId: String): Flow<Resource<List<Fee>>> = flow {
        emit(Resource.Loading)
        try {
            val fees = daos.getFeesForStudent(studentId)
            emit(Resource.Success(fees))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch student fees"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getFeesForHostel(hostelId: String): Flow<Resource<List<Fee>>> = flow {
        emit(Resource.Loading)
        try {
            val fees = daos.getFeesForHostel(hostelId)
            emit(Resource.Success(fees))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch hostel fees"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllFees(): Flow<Resource<List<Fee>>> = flow {
        emit(Resource.Loading)
        try {
            val fees = daos.getAllFees()
            emit(Resource.Success(fees))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch all fees"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPaymentsForStudent(studentId: String): Flow<Resource<List<Payment>>> = flow {
        emit(Resource.Loading)
        try {
            val payments = daos.getPaymentsForStudent(studentId)
            emit(Resource.Success(payments))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch payment history"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPaymentsForHostel(hostelId: String): Flow<Resource<List<Payment>>> = flow {
        emit(Resource.Loading)
        try {
            val empty: List<Payment> = emptyList()
            emit(Resource.Success(empty))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch hostel payment history"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTransactionHistory(): Flow<Resource<List<Payment>>> = flow {
        emit(Resource.Loading)
        try {
            val payments = daos.getAllFees().map { fee ->
                Payment(
                    paymentId = "pay_${fee.feeId}",
                    feeId = fee.feeId,
                    studentId = fee.studentId,
                    hostelId = fee.hostelId,
                    amountPaid = fee.amountPaid,
                    paymentMethod = PaymentMethod.UPI,
                    transactionReference = "TXN-${fee.feeId}",
                    status = PaymentStatus.SUCCESS
                )
            }
            emit(Resource.Success(payments))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to fetch transactions"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createRazorpayOrder(feeId: String, amount: Double?): Resource<com.hostelhub.app.data.remote.dto.RazorpayOrderResponseDto> = withContext(Dispatchers.IO) {
        Resource.Success(
            com.hostelhub.app.data.remote.dto.RazorpayOrderResponseDto(
                orderId = "order_${System.currentTimeMillis()}",
                amount = amount ?: 4500.0,
                amountInPaise = ((amount ?: 4500.0) * 100).toLong(),
                currency = "INR",
                keyId = "rzp_test_hostelhub",
                feeId = feeId,
                feeTitle = "Hostel Fee",
                studentName = "Resident Student",
                hostelName = "Green Valley Residencies"
            )
        )
    }

    override suspend fun verifyRazorpayPayment(
        feeId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String?,
        amountPaid: Double?
    ): Resource<Payment> = withContext(Dispatchers.IO) {
        val payment = Payment(
            paymentId = "pay_${System.currentTimeMillis()}",
            feeId = feeId,
            studentId = "std_001",
            hostelId = "hostel_001",
            amountPaid = amountPaid ?: 4500.0,
            paymentMethod = PaymentMethod.UPI,
            transactionReference = razorpayPaymentId,
            status = PaymentStatus.SUCCESS
        )
        Resource.Success(payment)
    }

    override suspend fun recordPaymentFailure(
        feeId: String,
        razorpayOrderId: String?,
        razorpayPaymentId: String?,
        errorMessage: String?
    ): Resource<Payment> = withContext(Dispatchers.IO) {
        val payment = Payment(
            paymentId = "fail_${System.currentTimeMillis()}",
            feeId = feeId,
            studentId = "std_001",
            hostelId = "hostel_001",
            amountPaid = 0.0,
            paymentMethod = PaymentMethod.UPI,
            transactionReference = razorpayPaymentId ?: "FAIL-${System.currentTimeMillis()}",
            status = PaymentStatus.FAILED,
            remarks = errorMessage
        )
        Resource.Success(payment)
    }

    override suspend fun recordPayment(payment: Payment): Resource<Payment> = withContext(Dispatchers.IO) {
        try {
            val recorded = daos.recordPayment(payment)
            Resource.Success(recorded)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to process payment")
        }
    }

    override suspend fun createFee(fee: Fee): Resource<Fee> = withContext(Dispatchers.IO) {
        try {
            val created = daos.createFee(fee)
            Resource.Success(created)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create fee")
        }
    }
}

@Singleton
class DatabaseComplaintRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : ComplaintRepository {

    override fun getComplaintsForStudent(studentId: String): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val complaints = daos.getComplaintsForStudent(studentId)
            emit(Resource.Success(complaints))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load student complaints"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getComplaintsForHostel(hostelId: String): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val complaints = daos.getComplaintsForHostel(hostelId)
            emit(Resource.Success(complaints))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load hostel complaints"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllComplaints(): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val complaints = daos.getAllComplaints()
            emit(Resource.Success(complaints))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load all complaints"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getComplaintById(complaintId: String): Flow<Resource<Complaint>> = flow {
        emit(Resource.Loading)
        try {
            val complaint = daos.getComplaintById(complaintId)
            if (complaint != null) {
                emit(Resource.Success(complaint))
            } else {
                emit(Resource.Error("Complaint not found: $complaintId"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load complaint details"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun submitComplaint(complaint: Complaint): Resource<Complaint> = withContext(Dispatchers.IO) {
        try {
            val result = daos.submitComplaint(complaint)
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to submit complaint")
        }
    }

    override suspend fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        notes: String?,
        assignedStaff: String?,
        resolutionSummary: String?
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.updateComplaintStatus(complaintId, status, notes)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update complaint status")
        }
    }

    override suspend fun deleteComplaint(complaintId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.deleteComplaint(complaintId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete complaint")
        }
    }
}

@Singleton
class DatabaseAttendanceRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : AttendanceRepository {

    override fun getAttendanceForStudent(studentId: String, month: Int, year: Int): Flow<Resource<List<AttendanceRecord>>> = flow {
        emit(Resource.Loading)
        try {
            val records = daos.getAttendanceForStudent(studentId, month, year)
            emit(Resource.Success(records))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load attendance records"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAttendanceForHostel(hostelId: String, date: String): Flow<Resource<List<AttendanceRecord>>> = flow {
        emit(Resource.Loading)
        try {
            val records = daos.getAttendanceForHostel(hostelId, date)
            emit(Resource.Success(records))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load hostel attendance"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun markAttendance(record: AttendanceRecord): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.markAttendance(record)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to record attendance")
        }
    }

    override suspend fun markBatchAttendance(records: List<AttendanceRecord>): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            records.forEach { daos.markAttendance(it) }
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to mark batch attendance")
        }
    }
}

@Singleton
class DatabaseFoodMenuRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : FoodMenuRepository {

    override fun getWeeklyMenu(hostelId: String, weekStartDate: String): Flow<Resource<FoodMenu>> = flow {
        emit(Resource.Loading)
        try {
            val menu = daos.getWeeklyMenu(hostelId, weekStartDate) ?: daos.getLatestMenu(hostelId)
            if (menu != null) {
                emit(Resource.Success(menu))
            } else {
                emit(Resource.Error("No food menu found for date $weekStartDate"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load food menu"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateWeeklyMenu(menu: FoodMenu): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.updateWeeklyMenu(menu)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update food menu")
        }
    }
}

@Singleton
class DatabaseAnnouncementRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : AnnouncementRepository {

    override fun getAnnouncements(hostelId: String): Flow<Resource<List<Announcement>>> = flow {
        emit(Resource.Loading)
        try {
            val list = daos.getAnnouncements(hostelId)
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load announcements"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createAnnouncement(announcement: Announcement): Resource<Announcement> = withContext(Dispatchers.IO) {
        try {
            val result = daos.createAnnouncement(announcement)
            Resource.Success(result)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to publish announcement")
        }
    }

    override suspend fun deleteAnnouncement(announcementId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.deleteAnnouncement(announcementId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete announcement")
        }
    }
}

@Singleton
class DatabaseNotificationRepositoryImpl @Inject constructor(
    private val daos: DatabaseDaos
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Resource<List<AppNotification>>> = flow {
        emit(Resource.Loading)
        try {
            val list = daos.getNotifications(userId)
            emit(Resource.Success(list))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Failed to load notifications"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun markAsRead(notificationId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            daos.markNotificationAsRead(notificationId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update notification")
        }
    }
}
