package com.hostelhub.app.data.local.db

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.hostelhub.app.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseDaos @Inject constructor(
    private val dbHelper: HostelDatabaseHelper
) {
    private val readableDb: SQLiteDatabase get() = dbHelper.readableDatabase
    private val writableDb: SQLiteDatabase get() = dbHelper.writableDatabase

    // ========================================================================
    // 1. User DAO
    // ========================================================================
    fun getUserById(userId: String): User? {
        val cursor = readableDb.rawQuery("SELECT * FROM users WHERE user_id = ? AND is_active = 1", arrayOf(userId))
        return cursor.use {
            if (it.moveToFirst()) parseUser(it) else null
        }
    }

    fun getUserByEmail(email: String): User? {
        val cursor = readableDb.rawQuery("SELECT * FROM users WHERE email = ? AND is_active = 1", arrayOf(email))
        return cursor.use {
            if (it.moveToFirst()) parseUser(it) else null
        }
    }

    fun getAllUsers(): List<User> {
        val cursor = readableDb.rawQuery("SELECT * FROM users ORDER BY created_at DESC", null)
        return cursor.use {
            val list = mutableListOf<User>()
            while (it.moveToNext()) {
                list.add(parseUser(it))
            }
            list
        }
    }

    fun insertUser(user: User, passwordHash: String = "hash_default"): Long {
        val values = ContentValues().apply {
            put("user_id", user.userId)
            put("email", user.email)
            put("password_hash", passwordHash)
            put("role", user.role.name)
            put("full_name", user.fullName)
            put("phone_number", user.phoneNumber)
            put("avatar_url", user.avatarUrl)
            put("is_active", if (user.isActive) 1 else 0)
            put("fcm_token", user.fcmToken)
            put("created_at", user.createdAt)
            put("updated_at", System.currentTimeMillis())
        }
        return writableDb.insertWithOnConflict("users", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun toggleUserStatus(userId: String, isActive: Boolean): Int {
        val values = ContentValues().apply {
            put("is_active", if (isActive) 1 else 0)
            put("updated_at", System.currentTimeMillis())
        }
        return writableDb.update("users", values, "user_id = ?", arrayOf(userId))
    }

    // ========================================================================
    // 2. Student DAO
    // ========================================================================
    fun getStudentById(studentId: String): Student? {
        val cursor = readableDb.rawQuery("SELECT * FROM students WHERE student_id = ? OR user_id = ?", arrayOf(studentId, studentId))
        return cursor.use {
            if (it.moveToFirst()) parseStudent(it) else null
        }
    }

    fun getStudentsByHostel(hostelId: String): List<Student> {
        val cursor = readableDb.rawQuery("SELECT * FROM students WHERE hostel_id = ? ORDER BY room_number ASC", arrayOf(hostelId))
        return cursor.use {
            val list = mutableListOf<Student>()
            while (it.moveToNext()) {
                list.add(parseStudent(it))
            }
            list
        }
    }

    fun getAllStudents(): List<Student> {
        val cursor = readableDb.rawQuery("SELECT * FROM students ORDER BY full_name ASC", null)
        return cursor.use {
            val list = mutableListOf<Student>()
            while (it.moveToNext()) {
                list.add(parseStudent(it))
            }
            list
        }
    }

    fun saveStudent(student: Student): Long {
        val values = ContentValues().apply {
            put("student_id", student.studentId)
            put("user_id", student.userId)
            put("full_name", student.fullName)
            put("roll_number", student.rollNumber)
            put("college_name", student.collegeName)
            put("course", student.course)
            put("year_of_study", student.yearOfStudy)
            put("gender", student.gender)
            put("permanent_address", student.permanentAddress)
            put("emergency_contact_name", student.emergencyContactName)
            put("emergency_contact_phone", student.emergencyContactPhone)
            put("hostel_id", student.hostelId)
            put("hostel_name", student.hostelName)
            put("room_id", student.roomId)
            put("room_number", student.roomNumber)
            put("bed_number", student.bedNumber)
            put("admission_date", student.admissionDate)
            put("status", student.status.name)
            put("created_at", System.currentTimeMillis())
            put("updated_at", System.currentTimeMillis())
        }
        return writableDb.insertWithOnConflict("students", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun deleteStudent(studentId: String): Int {
        return writableDb.delete("students", "student_id = ? OR user_id = ?", arrayOf(studentId, studentId))
    }

    // ========================================================================
    // 3. Host & Admin DAO
    // ========================================================================
    fun getHostById(hostId: String): Host? {
        val cursor = readableDb.rawQuery("SELECT * FROM hosts WHERE host_id = ? OR user_id = ?", arrayOf(hostId, hostId))
        return cursor.use {
            if (it.moveToFirst()) {
                Host(
                    hostId = it.getString(it.getColumnIndexOrThrow("host_id")),
                    userId = it.getString(it.getColumnIndexOrThrow("user_id")),
                    fullName = it.getString(it.getColumnIndexOrThrow("full_name")),
                    businessName = it.getString(it.getColumnIndexOrThrow("business_name")),
                    contactPhone = it.getString(it.getColumnIndexOrThrow("contact_phone")),
                    contactEmail = it.getString(it.getColumnIndexOrThrow("contact_email")),
                    verifiedStatus = it.getInt(it.getColumnIndexOrThrow("verified_status")) == 1,
                    createdAt = it.getLong(it.getColumnIndexOrThrow("created_at"))
                )
            } else null
        }
    }

    fun saveHost(host: Host): Long {
        val values = ContentValues().apply {
            put("host_id", host.hostId)
            put("user_id", host.userId)
            put("full_name", host.fullName)
            put("business_name", host.businessName)
            put("contact_phone", host.contactPhone)
            put("contact_email", host.contactEmail)
            put("verified_status", if (host.verifiedStatus) 1 else 0)
            put("created_at", host.createdAt)
            put("updated_at", System.currentTimeMillis())
        }
        return writableDb.insertWithOnConflict("hosts", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getAdminById(adminId: String): Admin? {
        val cursor = readableDb.rawQuery("SELECT * FROM admins WHERE admin_id = ? OR user_id = ?", arrayOf(adminId, adminId))
        return cursor.use {
            if (it.moveToFirst()) {
                Admin(
                    adminId = it.getString(it.getColumnIndexOrThrow("admin_id")),
                    userId = it.getString(it.getColumnIndexOrThrow("user_id")),
                    fullName = it.getString(it.getColumnIndexOrThrow("full_name")),
                    associationName = it.getString(it.getColumnIndexOrThrow("association_name")),
                    designation = it.getString(it.getColumnIndexOrThrow("designation")),
                    contactPhone = it.getString(it.getColumnIndexOrThrow("contact_phone")) ?: ""
                )
            } else null
        }
    }

    // ========================================================================
    // 4. Hostel DAO
    // ========================================================================
    fun getAllHostels(): List<Hostel> {
        val cursor = readableDb.rawQuery("SELECT * FROM hostels ORDER BY rating DESC", null)
        return cursor.use {
            val list = mutableListOf<Hostel>()
            while (it.moveToNext()) {
                list.add(parseHostel(it))
            }
            list
        }
    }

    fun getHostelById(hostelId: String): Hostel? {
        val cursor = readableDb.rawQuery("SELECT * FROM hostels WHERE hostel_id = ?", arrayOf(hostelId))
        return cursor.use {
            if (it.moveToFirst()) parseHostel(it) else null
        }
    }

    fun saveHostel(hostel: Hostel): Long {
        val values = ContentValues().apply {
            put("hostel_id", hostel.hostelId)
            put("host_id", hostel.hostId)
            put("name", hostel.name)
            put("address", hostel.address)
            put("city", hostel.city)
            put("state", hostel.state)
            put("postal_code", hostel.postalCode)
            put("latitude", hostel.latitude)
            put("longitude", hostel.longitude)
            put("description", hostel.description)
            put("gender_type", hostel.genderType.name)
            put("amenities", JSONArray(hostel.amenities).toString())
            put("rules", JSONArray(hostel.rules).toString())
            put("images", JSONArray(hostel.images).toString())
            put("total_rooms", hostel.totalRooms)
            put("total_beds", hostel.totalBeds)
            put("occupied_beds", hostel.occupiedBeds)
            put("base_monthly_rent", hostel.baseMonthlyRent)
            put("caution_deposit", hostel.cautionDeposit)
            put("rating", hostel.rating)
            put("rating_count", hostel.ratingCount)
            put("contact_email", hostel.contactEmail)
            put("contact_phone", hostel.contactPhone)
            put("created_at", hostel.createdAt)
            put("updated_at", System.currentTimeMillis())
        }
        return writableDb.insertWithOnConflict("hostels", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ========================================================================
    // 5. Room & Bed DAO
    // ========================================================================
    fun getRoomsByHostel(hostelId: String): List<Room> {
        val cursor = readableDb.rawQuery("SELECT * FROM rooms WHERE hostel_id = ? ORDER BY room_number ASC", arrayOf(hostelId))
        return cursor.use {
            val list = mutableListOf<Room>()
            while (it.moveToNext()) {
                val room = parseRoom(it)
                val beds = getBedsForRoom(room.roomId)
                list.add(room.copy(beds = beds))
            }
            list
        }
    }

    fun getRoomById(roomId: String): Room? {
        val cursor = readableDb.rawQuery("SELECT * FROM rooms WHERE room_id = ?", arrayOf(roomId))
        return cursor.use {
            if (it.moveToFirst()) {
                val room = parseRoom(it)
                val beds = getBedsForRoom(room.roomId)
                room.copy(beds = beds)
            } else null
        }
    }

    fun getBedsForRoom(roomId: String): List<Bed> {
        val sql = """
            SELECT b.bed_id, b.bed_number, b.is_occupied, s.student_id, s.full_name as student_name
            FROM beds b
            LEFT JOIN room_allocations a ON b.bed_id = a.bed_id AND a.status = 'ACTIVE'
            LEFT JOIN students s ON a.student_id = s.student_id
            WHERE b.room_id = ?
            ORDER BY b.bed_number ASC
        """.trimIndent()
        val cursor = readableDb.rawQuery(sql, arrayOf(roomId))
        return cursor.use {
            val list = mutableListOf<Bed>()
            while (it.moveToNext()) {
                val bedId = it.getString(it.getColumnIndexOrThrow("bed_id"))
                val bedNumber = it.getString(it.getColumnIndexOrThrow("bed_number"))
                val isOccupied = it.getInt(it.getColumnIndexOrThrow("is_occupied")) == 1
                val studentId = if (it.isNull(it.getColumnIndexOrThrow("student_id"))) null else it.getString(it.getColumnIndexOrThrow("student_id"))
                val studentName = if (it.isNull(it.getColumnIndexOrThrow("student_name"))) null else it.getString(it.getColumnIndexOrThrow("student_name"))
                list.add(Bed(bedId, bedNumber, studentId, studentName, isOccupied))
            }
            list
        }
    }

    fun addRoom(room: Room): Room {
        writableDb.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("room_id", room.roomId)
                put("hostel_id", room.hostelId)
                put("room_number", room.roomNumber)
                put("floor", room.floor)
                put("block", room.block)
                put("room_type", room.roomType.name)
                put("total_capacity", room.totalCapacity)
                put("occupied_count", room.occupiedCount)
                put("monthly_rent", room.monthlyRent)
                put("amenities", JSONArray(room.amenities).toString())
                put("status", room.status.name)
                put("created_at", room.createdAt)
                put("updated_at", System.currentTimeMillis())
            }
            writableDb.insertWithOnConflict("rooms", null, values, SQLiteDatabase.CONFLICT_REPLACE)

            // Insert beds
            val generatedBeds = if (room.beds.isNotEmpty()) room.beds else {
                (1..room.totalCapacity).map { idx ->
                    val suffix = ('A'.code + idx - 1).toChar()
                    Bed("bed_${room.roomId}_$suffix", "Bed-$suffix", null, null, false)
                }
            }

            generatedBeds.forEach { bed ->
                val bedValues = ContentValues().apply {
                    put("bed_id", bed.bedId)
                    put("room_id", room.roomId)
                    put("bed_number", bed.bedNumber)
                    put("is_occupied", if (bed.isOccupied) 1 else 0)
                    put("created_at", System.currentTimeMillis())
                }
                writableDb.insertWithOnConflict("beds", null, bedValues, SQLiteDatabase.CONFLICT_REPLACE)
            }
            writableDb.setTransactionSuccessful()
            return room.copy(beds = generatedBeds)
        } finally {
            writableDb.endTransaction()
        }
    }

    fun updateRoom(room: Room): Int {
        val values = ContentValues().apply {
            put("room_number", room.roomNumber)
            put("floor", room.floor)
            put("block", room.block)
            put("room_type", room.roomType.name)
            put("total_capacity", room.totalCapacity)
            put("occupied_count", room.occupiedCount)
            put("monthly_rent", room.monthlyRent)
            put("amenities", JSONArray(room.amenities).toString())
            put("status", room.status.name)
            put("updated_at", System.currentTimeMillis())
        }
        return writableDb.update("rooms", values, "room_id = ?", arrayOf(room.roomId))
    }

    fun deleteRoom(roomId: String): Int {
        return writableDb.delete("rooms", "room_id = ?", arrayOf(roomId))
    }

    fun assignBed(roomId: String, bedId: String, studentId: String, studentName: String) {
        writableDb.beginTransaction()
        try {
            val now = System.currentTimeMillis()

            // 1. Update Bed
            val bedValues = ContentValues().apply { put("is_occupied", 1) }
            writableDb.update("beds", bedValues, "bed_id = ?", arrayOf(bedId))

            // 2. Add Room Allocation
            val allocId = "alloc_" + System.currentTimeMillis()
            val allocValues = ContentValues().apply {
                put("allocation_id", allocId)
                put("bed_id", bedId)
                put("room_id", roomId)
                put("hostel_id", "hostel_001")
                put("student_id", studentId)
                put("allocation_date", now)
                put("check_in_date", now)
                put("status", "ACTIVE")
                put("created_at", now)
                put("updated_at", now)
            }
            writableDb.insertWithOnConflict("room_allocations", null, allocValues, SQLiteDatabase.CONFLICT_REPLACE)

            // 3. Update Room count
            writableDb.execSQL("UPDATE rooms SET occupied_count = occupied_count + 1 WHERE room_id = ?", arrayOf(roomId))
            writableDb.execSQL("UPDATE rooms SET status = CASE WHEN occupied_count >= total_capacity THEN 'FULL' ELSE 'AVAILABLE' END WHERE room_id = ?", arrayOf(roomId))

            // 4. Update Student
            val studentValues = ContentValues().apply {
                put("room_id", roomId)
                put("status", "ACTIVE")
            }
            writableDb.update("students", studentValues, "student_id = ?", arrayOf(studentId))

            writableDb.setTransactionSuccessful()
        } finally {
            writableDb.endTransaction()
        }
    }

    fun vacateBed(bedId: String, roomId: String) {
        writableDb.beginTransaction()
        try {
            writableDb.execSQL("UPDATE beds SET is_occupied = 0 WHERE bed_id = ?", arrayOf(bedId))
            writableDb.execSQL("UPDATE room_allocations SET status = 'VACATED', check_out_date = ?, updated_at = ? WHERE bed_id = ? AND status = 'ACTIVE'", arrayOf(System.currentTimeMillis(), System.currentTimeMillis(), bedId))
            writableDb.execSQL("UPDATE rooms SET occupied_count = MAX(0, occupied_count - 1) WHERE room_id = ?", arrayOf(roomId))
            writableDb.execSQL("UPDATE rooms SET status = CASE WHEN occupied_count >= total_capacity THEN 'FULL' ELSE 'AVAILABLE' END WHERE room_id = ?", arrayOf(roomId))
            writableDb.setTransactionSuccessful()
        } finally {
            writableDb.endTransaction()
        }
    }

    // ========================================================================
    // 6. Fees & Payments DAO
    // ========================================================================
    fun getFeesForStudent(studentId: String): List<Fee> {
        val cursor = readableDb.rawQuery("SELECT * FROM fees WHERE student_id = ? ORDER BY due_date DESC", arrayOf(studentId))
        return cursor.use {
            val list = mutableListOf<Fee>()
            while (it.moveToNext()) {
                list.add(parseFee(it))
            }
            list
        }
    }

    fun getFeesForHostel(hostelId: String): List<Fee> {
        val cursor = readableDb.rawQuery("SELECT * FROM fees WHERE hostel_id = ? ORDER BY due_date DESC", arrayOf(hostelId))
        return cursor.use {
            val list = mutableListOf<Fee>()
            while (it.moveToNext()) {
                list.add(parseFee(it))
            }
            list
        }
    }

    fun getAllFees(): List<Fee> {
        val cursor = readableDb.rawQuery("SELECT * FROM fees ORDER BY due_date DESC", null)
        return cursor.use {
            val list = mutableListOf<Fee>()
            while (it.moveToNext()) {
                list.add(parseFee(it))
            }
            list
        }
    }

    fun createFee(fee: Fee): Fee {
        val values = ContentValues().apply {
            put("fee_id", fee.feeId)
            put("hostel_id", fee.hostelId)
            put("student_id", fee.studentId)
            put("room_id", fee.roomId)
            put("title", fee.title)
            put("fee_type", fee.feeType.name)
            put("amount", fee.amount)
            put("amount_paid", fee.amountPaid)
            put("due_date", fee.dueDate)
            put("billing_month", fee.billingMonth)
            put("billing_year", fee.billingYear)
            put("status", fee.status.name)
            put("created_at", fee.createdAt)
            put("updated_at", System.currentTimeMillis())
        }
        writableDb.insertWithOnConflict("fees", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return fee
    }

    fun getPaymentsForStudent(studentId: String): List<Payment> {
        val cursor = readableDb.rawQuery("SELECT * FROM payments WHERE student_id = ? ORDER BY payment_date DESC", arrayOf(studentId))
        return cursor.use {
            val list = mutableListOf<Payment>()
            while (it.moveToNext()) {
                list.add(parsePayment(it))
            }
            list
        }
    }

    fun recordPayment(payment: Payment): Payment {
        writableDb.beginTransaction()
        try {
            val values = ContentValues().apply {
                put("payment_id", payment.paymentId)
                put("fee_id", payment.feeId)
                put("student_id", payment.studentId)
                put("hostel_id", payment.hostelId)
                put("amount_paid", payment.amountPaid)
                put("payment_method", payment.paymentMethod.name)
                put("transaction_reference", payment.transactionReference)
                put("payment_date", payment.paymentDate)
                put("receipt_url", payment.receiptUrl)
                put("status", payment.status.name)
                put("verified_by_host_id", payment.verifiedByHostId)
                put("remarks", payment.remarks)
                put("created_at", payment.createdAt)
            }
            writableDb.insertWithOnConflict("payments", null, values, SQLiteDatabase.CONFLICT_REPLACE)

            // Update Fee status
            writableDb.execSQL("""
                UPDATE fees 
                SET amount_paid = amount_paid + ?, 
                    status = CASE WHEN (amount_paid + ?) >= amount THEN 'PAID' ELSE 'PARTIALLY_PAID' END,
                    updated_at = ?
                WHERE fee_id = ?
            """.trimIndent(), arrayOf(payment.amountPaid, payment.amountPaid, System.currentTimeMillis(), payment.feeId))

            writableDb.setTransactionSuccessful()
            return payment
        } finally {
            writableDb.endTransaction()
        }
    }

    // ========================================================================
    // 7. Complaints DAO
    // ========================================================================
    fun getComplaintsForStudent(studentId: String): List<Complaint> {
        val cursor = readableDb.rawQuery("SELECT * FROM complaints WHERE student_id = ? ORDER BY created_at DESC", arrayOf(studentId))
        return cursor.use {
            val list = mutableListOf<Complaint>()
            while (it.moveToNext()) {
                list.add(parseComplaint(it))
            }
            list
        }
    }

    fun getComplaintsForHostel(hostelId: String): List<Complaint> {
        val cursor = readableDb.rawQuery("SELECT * FROM complaints WHERE hostel_id = ? ORDER BY created_at DESC", arrayOf(hostelId))
        return cursor.use {
            val list = mutableListOf<Complaint>()
            while (it.moveToNext()) {
                list.add(parseComplaint(it))
            }
            list
        }
    }

    fun getAllComplaints(): List<Complaint> {
        val cursor = readableDb.rawQuery("SELECT * FROM complaints ORDER BY created_at DESC", null)
        return cursor.use {
            val list = mutableListOf<Complaint>()
            while (it.moveToNext()) {
                list.add(parseComplaint(it))
            }
            list
        }
    }

    fun getComplaintById(complaintId: String): Complaint? {
        val cursor = readableDb.rawQuery("SELECT * FROM complaints WHERE complaint_id = ?", arrayOf(complaintId))
        return cursor.use {
            if (it.moveToFirst()) parseComplaint(it) else null
        }
    }

    fun submitComplaint(complaint: Complaint): Complaint {
        val values = ContentValues().apply {
            put("complaint_id", complaint.complaintId)
            put("hostel_id", complaint.hostelId)
            put("student_id", complaint.studentId)
            put("student_name", complaint.studentName)
            put("room_number", complaint.roomNumber)
            put("category", complaint.category.name)
            put("title", complaint.title)
            put("description", complaint.description)
            put("attachments", JSONArray(complaint.attachments).toString())
            put("urgency", complaint.urgency.name)
            put("status", complaint.status.name)
            put("assigned_staff_name", complaint.assignedStaffName)
            put("host_notes", complaint.hostNotes)
            put("resolution_summary", complaint.resolutionSummary)
            put("created_at", complaint.createdAt)
            put("resolved_at", complaint.resolvedAt)
            put("updated_at", System.currentTimeMillis())
        }
        writableDb.insertWithOnConflict("complaints", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return complaint
    }

    fun updateComplaintStatus(complaintId: String, status: ComplaintStatus, notes: String?) {
        val values = ContentValues().apply {
            put("status", status.name)
            if (notes != null) {
                put("host_notes", notes)
            }
            if (status == ComplaintStatus.RESOLVED) {
                put("resolved_at", System.currentTimeMillis())
            }
            put("updated_at", System.currentTimeMillis())
        }
        writableDb.update("complaints", values, "complaint_id = ?", arrayOf(complaintId))
    }

    fun deleteComplaint(complaintId: String): Int {
        return writableDb.delete("complaints", "complaint_id = ?", arrayOf(complaintId))
    }

    // ========================================================================
    // 8. Attendance DAO
    // ========================================================================
    fun getAttendanceForStudent(studentId: String, month: Int, year: Int): List<AttendanceRecord> {
        val prefix = "%04d-%02d".format(year, month)
        val cursor = readableDb.rawQuery("SELECT * FROM attendance_records WHERE student_id = ? AND date LIKE ? ORDER BY date DESC", arrayOf(studentId, "$prefix%"))
        return cursor.use {
            val list = mutableListOf<AttendanceRecord>()
            while (it.moveToNext()) {
                list.add(parseAttendance(it))
            }
            list
        }
    }

    fun getAttendanceForHostel(hostelId: String, date: String): List<AttendanceRecord> {
        val cursor = readableDb.rawQuery("SELECT * FROM attendance_records WHERE hostel_id = ? AND date = ? ORDER BY room_number ASC", arrayOf(hostelId, date))
        return cursor.use {
            val list = mutableListOf<AttendanceRecord>()
            while (it.moveToNext()) {
                list.add(parseAttendance(it))
            }
            list
        }
    }

    fun markAttendance(record: AttendanceRecord) {
        val values = ContentValues().apply {
            put("attendance_id", record.attendanceId)
            put("hostel_id", record.hostelId)
            put("student_id", record.studentId)
            put("student_name", record.studentName)
            put("room_number", record.roomNumber)
            put("date", record.date)
            put("status", record.status.name)
            put("check_in_time", record.checkInTime)
            put("remarks", record.remarks)
            put("marked_by", record.markedBy)
            put("leave_request_id", record.leaveRequestId)
            put("created_at", record.createdAt)
        }
        writableDb.insertWithOnConflict("attendance_records", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ========================================================================
    // 9. Food Menu DAO
    // ========================================================================
    fun getWeeklyMenu(hostelId: String, weekStartDate: String): FoodMenu? {
        val cursor = readableDb.rawQuery("SELECT * FROM food_menus WHERE hostel_id = ? AND week_start_date = ?", arrayOf(hostelId, weekStartDate))
        return cursor.use {
            if (it.moveToFirst()) parseFoodMenu(it) else null
        }
    }

    fun getLatestMenu(hostelId: String): FoodMenu? {
        val cursor = readableDb.rawQuery("SELECT * FROM food_menus WHERE hostel_id = ? ORDER BY week_start_date DESC LIMIT 1", arrayOf(hostelId))
        return cursor.use {
            if (it.moveToFirst()) parseFoodMenu(it) else null
        }
    }

    fun updateWeeklyMenu(menu: FoodMenu) {
        val scheduleJson = serializeSchedule(menu.schedule)
        val values = ContentValues().apply {
            put("menu_id", menu.menuId)
            put("hostel_id", menu.hostelId)
            put("week_start_date", menu.weekStartDate)
            put("schedule_json", scheduleJson)
            put("special_notice", menu.specialNotice)
            put("is_published", if (menu.isPublished) 1 else 0)
            put("updated_at", System.currentTimeMillis())
            put("created_at", System.currentTimeMillis())
        }
        writableDb.insertWithOnConflict("food_menus", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    // ========================================================================
    // 10. Announcements DAO
    // ========================================================================
    fun getAnnouncements(hostelId: String): List<Announcement> {
        val cursor = readableDb.rawQuery("SELECT * FROM announcements WHERE hostel_id = ? OR hostel_id = 'GLOBAL_CAMPUS' OR sender_role = 'ADMIN' ORDER BY created_at DESC", arrayOf(hostelId))
        return cursor.use {
            val list = mutableListOf<Announcement>()
            while (it.moveToNext()) {
                list.add(parseAnnouncement(it))
            }
            list
        }
    }

    fun createAnnouncement(announcement: Announcement): Announcement {
        val values = ContentValues().apply {
            put("announcement_id", announcement.announcementId)
            put("hostel_id", announcement.hostelId)
            put("sender_id", announcement.senderId)
            put("sender_role", announcement.senderRole.name)
            put("sender_name", announcement.senderName)
            put("title", announcement.title)
            put("message", announcement.message)
            put("priority", announcement.priority.name)
            put("target_audience", announcement.targetAudience)
            put("attachment_urls", JSONArray(announcement.attachmentUrls).toString())
            put("created_at", announcement.createdAt)
            put("expires_at", announcement.expiresAt)
        }
        writableDb.insertWithOnConflict("announcements", null, values, SQLiteDatabase.CONFLICT_REPLACE)
        return announcement
    }

    fun deleteAnnouncement(announcementId: String): Int {
        return writableDb.delete("announcements", "announcement_id = ?", arrayOf(announcementId))
    }

    // ========================================================================
    // 11. Notifications DAO
    // ========================================================================
    fun getNotifications(userId: String): List<AppNotification> {
        val cursor = readableDb.rawQuery("SELECT * FROM notifications WHERE recipient_user_id = ? ORDER BY created_at DESC", arrayOf(userId))
        return cursor.use {
            val list = mutableListOf<AppNotification>()
            while (it.moveToNext()) {
                list.add(parseNotification(it))
            }
            list
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        val values = ContentValues().apply { put("is_read", 1) }
        writableDb.update("notifications", values, "notification_id = ?", arrayOf(notificationId))
    }

    // ========================================================================
    // 12. Dashboard Aggregations DAO
    // ========================================================================
    fun getStudentDashboardStats(studentId: String): StudentDashboardStats {
        val student = getStudentById(studentId)
        val feeCursor = readableDb.rawQuery("SELECT SUM(amount - amount_paid) FROM fees WHERE student_id = ? AND status != 'PAID'", arrayOf(studentId))
        val pendingFees = feeCursor.use { if (it.moveToFirst()) it.getDouble(0) else 0.0 }

        val compCursor = readableDb.rawQuery("SELECT COUNT(*) FROM complaints WHERE student_id = ? AND status != 'RESOLVED'", arrayOf(studentId))
        val activeComplaints = compCursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }

        val attCursor = readableDb.rawQuery("SELECT COUNT(*), SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) FROM attendance_records WHERE student_id = ?", arrayOf(studentId))
        var totalAtt = 0
        var presentAtt = 0
        attCursor.use {
            if (it.moveToFirst()) {
                totalAtt = it.getInt(0)
                presentAtt = it.getInt(1)
            }
        }
        val attRate = if (totalAtt > 0) ((presentAtt.toDouble() / totalAtt) * 100).toInt() else 95

        return StudentDashboardStats(
            roomNumber = student?.roomNumber ?: "A-204",
            bedNumber = student?.bedNumber ?: "Bed-A",
            hostelName = student?.hostelName ?: "Green Valley Residencies",
            pendingFees = pendingFees,
            activeComplaints = activeComplaints,
            attendanceRate = attRate
        )
    }

    fun getHostDashboardStats(hostelId: String): HostDashboardStats {
        val roomCursor = readableDb.rawQuery("SELECT COUNT(*), SUM(total_capacity), SUM(occupied_count) FROM rooms WHERE hostel_id = ?", arrayOf(hostelId))
        var totalRooms = 0
        var totalBeds = 0
        var occupiedBeds = 0
        roomCursor.use {
            if (it.moveToFirst()) {
                totalRooms = it.getInt(0)
                totalBeds = it.getInt(1)
                occupiedBeds = it.getInt(2)
            }
        }
        val availableBeds = maxOf(0, totalBeds - occupiedBeds)

        val feeCursor = readableDb.rawQuery("SELECT COUNT(*), SUM(amount - amount_paid) FROM fees WHERE hostel_id = ? AND status != 'PAID'", arrayOf(hostelId))
        var pendingFeeCount = 0
        var pendingFeeAmount = 0.0
        feeCursor.use {
            if (it.moveToFirst()) {
                pendingFeeCount = it.getInt(0)
                pendingFeeAmount = it.getDouble(1)
            }
        }

        val compCursor = readableDb.rawQuery("SELECT COUNT(*) FROM complaints WHERE hostel_id = ? AND status != 'RESOLVED'", arrayOf(hostelId))
        val pendingComplaints = compCursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }

        val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val attCursor = readableDb.rawQuery("SELECT COUNT(*) FROM attendance_records WHERE hostel_id = ? AND date = ? AND status = 'PRESENT'", arrayOf(hostelId, todayDate))
        val todayPresent = attCursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }

        return HostDashboardStats(
            totalRooms = totalRooms,
            totalBeds = totalBeds,
            occupiedBeds = occupiedBeds,
            availableBeds = availableBeds,
            pendingFeeCount = pendingFeeCount,
            pendingFeeAmount = pendingFeeAmount,
            pendingComplaints = pendingComplaints,
            todayPresent = todayPresent
        )
    }

    fun getAdminDashboardStats(): AdminDashboardStats {
        val hostelCursor = readableDb.rawQuery("SELECT COUNT(*), SUM(total_rooms), SUM(total_beds), SUM(occupied_beds) FROM hostels", null)
        var totalHostels = 0
        var totalRooms = 0
        var totalBeds = 0
        var occupiedBeds = 0
        hostelCursor.use {
            if (it.moveToFirst()) {
                totalHostels = it.getInt(0)
                totalRooms = it.getInt(1)
                totalBeds = it.getInt(2)
                occupiedBeds = it.getInt(3)
            }
        }

        val studentCursor = readableDb.rawQuery("SELECT COUNT(*) FROM students", null)
        val totalStudents = studentCursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }

        val revenueCursor = readableDb.rawQuery("SELECT SUM(amount_paid) FROM payments WHERE status = 'SUCCESS'", null)
        val totalRevenue = revenueCursor.use { if (it.moveToFirst()) it.getDouble(0) else 0.0 }

        val compCursor = readableDb.rawQuery("SELECT COUNT(*) FROM complaints WHERE status != 'RESOLVED'", null)
        val pendingComplaints = compCursor.use { if (it.moveToFirst()) it.getInt(0) else 0 }

        return AdminDashboardStats(
            totalHostels = totalHostels,
            totalStudents = totalStudents,
            totalRooms = totalRooms,
            totalBeds = totalBeds,
            occupiedBeds = occupiedBeds,
            totalRevenue = totalRevenue,
            pendingComplaints = pendingComplaints
        )
    }

    // ========================================================================
    // Helpers & Parsers
    // ========================================================================
    private fun parseUser(cursor: Cursor): User {
        return User(
            userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
            email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
            role = try { UserRole.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("role"))) } catch (e: Exception) { UserRole.STUDENT },
            fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
            phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("phone_number")) ?: "",
            avatarUrl = cursor.getString(cursor.getColumnIndexOrThrow("avatar_url")),
            isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1,
            fcmToken = cursor.getString(cursor.getColumnIndexOrThrow("fcm_token")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun parseStudent(cursor: Cursor): Student {
        return Student(
            studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id")),
            userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id")),
            fullName = cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
            rollNumber = cursor.getString(cursor.getColumnIndexOrThrow("roll_number")),
            collegeName = cursor.getString(cursor.getColumnIndexOrThrow("college_name")),
            course = cursor.getString(cursor.getColumnIndexOrThrow("course")),
            yearOfStudy = cursor.getString(cursor.getColumnIndexOrThrow("year_of_study")),
            gender = cursor.getString(cursor.getColumnIndexOrThrow("gender")),
            permanentAddress = cursor.getString(cursor.getColumnIndexOrThrow("permanent_address")),
            emergencyContactName = cursor.getString(cursor.getColumnIndexOrThrow("emergency_contact_name")),
            emergencyContactPhone = cursor.getString(cursor.getColumnIndexOrThrow("emergency_contact_phone")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            hostelName = cursor.getString(cursor.getColumnIndexOrThrow("hostel_name")),
            roomId = cursor.getString(cursor.getColumnIndexOrThrow("room_id")),
            roomNumber = cursor.getString(cursor.getColumnIndexOrThrow("room_number")),
            bedNumber = cursor.getString(cursor.getColumnIndexOrThrow("bed_number")),
            admissionDate = if (cursor.isNull(cursor.getColumnIndexOrThrow("admission_date"))) null else cursor.getLong(cursor.getColumnIndexOrThrow("admission_date")),
            status = try { StudentStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))) } catch (e: Exception) { StudentStatus.ACTIVE }
        )
    }

    private fun parseHostel(cursor: Cursor): Hostel {
        val amenitiesJson = cursor.getString(cursor.getColumnIndexOrThrow("amenities")) ?: "[]"
        val rulesJson = cursor.getString(cursor.getColumnIndexOrThrow("rules")) ?: "[]"
        val imagesJson = cursor.getString(cursor.getColumnIndexOrThrow("images")) ?: "[]"

        return Hostel(
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            hostId = cursor.getString(cursor.getColumnIndexOrThrow("host_id")),
            name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
            address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
            city = cursor.getString(cursor.getColumnIndexOrThrow("city")),
            state = cursor.getString(cursor.getColumnIndexOrThrow("state")) ?: "",
            postalCode = cursor.getString(cursor.getColumnIndexOrThrow("postal_code")) ?: "",
            latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude")),
            longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")) ?: "",
            genderType = try { HostelGenderType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("gender_type"))) } catch (e: Exception) { HostelGenderType.COED },
            amenities = parseJsonStringList(amenitiesJson),
            rules = parseJsonStringList(rulesJson),
            images = parseJsonStringList(imagesJson),
            totalRooms = cursor.getInt(cursor.getColumnIndexOrThrow("total_rooms")),
            totalBeds = cursor.getInt(cursor.getColumnIndexOrThrow("total_beds")),
            occupiedBeds = cursor.getInt(cursor.getColumnIndexOrThrow("occupied_beds")),
            baseMonthlyRent = cursor.getDouble(cursor.getColumnIndexOrThrow("base_monthly_rent")),
            cautionDeposit = cursor.getDouble(cursor.getColumnIndexOrThrow("caution_deposit")),
            rating = cursor.getDouble(cursor.getColumnIndexOrThrow("rating")),
            ratingCount = cursor.getInt(cursor.getColumnIndexOrThrow("rating_count")),
            contactEmail = cursor.getString(cursor.getColumnIndexOrThrow("contact_email")) ?: "",
            contactPhone = cursor.getString(cursor.getColumnIndexOrThrow("contact_phone")) ?: "",
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun parseRoom(cursor: Cursor): Room {
        val amenitiesJson = cursor.getString(cursor.getColumnIndexOrThrow("amenities")) ?: "[]"
        return Room(
            roomId = cursor.getString(cursor.getColumnIndexOrThrow("room_id")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            roomNumber = cursor.getString(cursor.getColumnIndexOrThrow("room_number")),
            floor = cursor.getInt(cursor.getColumnIndexOrThrow("floor")),
            block = cursor.getString(cursor.getColumnIndexOrThrow("block")),
            roomType = try { RoomType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("room_type"))) } catch (e: Exception) { RoomType.DOUBLE },
            totalCapacity = cursor.getInt(cursor.getColumnIndexOrThrow("total_capacity")),
            occupiedCount = cursor.getInt(cursor.getColumnIndexOrThrow("occupied_count")),
            monthlyRent = cursor.getDouble(cursor.getColumnIndexOrThrow("monthly_rent")),
            amenities = parseJsonStringList(amenitiesJson),
            status = try { RoomStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))) } catch (e: Exception) { RoomStatus.AVAILABLE },
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun parseFee(cursor: Cursor): Fee {
        return Fee(
            feeId = cursor.getString(cursor.getColumnIndexOrThrow("fee_id")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id")),
            roomId = cursor.getString(cursor.getColumnIndexOrThrow("room_id")) ?: "",
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            feeType = try { FeeType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("fee_type"))) } catch (e: Exception) { FeeType.RENT },
            amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
            amountPaid = cursor.getDouble(cursor.getColumnIndexOrThrow("amount_paid")),
            dueDate = cursor.getLong(cursor.getColumnIndexOrThrow("due_date")),
            billingMonth = cursor.getInt(cursor.getColumnIndexOrThrow("billing_month")),
            billingYear = cursor.getInt(cursor.getColumnIndexOrThrow("billing_year")),
            status = try { FeeStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))) } catch (e: Exception) { FeeStatus.PENDING },
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun parsePayment(cursor: Cursor): Payment {
        return Payment(
            paymentId = cursor.getString(cursor.getColumnIndexOrThrow("payment_id")),
            feeId = cursor.getString(cursor.getColumnIndexOrThrow("fee_id")),
            studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            amountPaid = cursor.getDouble(cursor.getColumnIndexOrThrow("amount_paid")),
            paymentMethod = try { PaymentMethod.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("payment_method"))) } catch (e: Exception) { PaymentMethod.UPI },
            transactionReference = cursor.getString(cursor.getColumnIndexOrThrow("transaction_reference")),
            paymentDate = cursor.getLong(cursor.getColumnIndexOrThrow("payment_date")),
            receiptUrl = cursor.getString(cursor.getColumnIndexOrThrow("receipt_url")),
            status = try { PaymentStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))) } catch (e: Exception) { PaymentStatus.SUCCESS },
            verifiedByHostId = cursor.getString(cursor.getColumnIndexOrThrow("verified_by_host_id")),
            remarks = cursor.getString(cursor.getColumnIndexOrThrow("remarks")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun parseComplaint(cursor: Cursor): Complaint {
        val attachmentsJson = cursor.getString(cursor.getColumnIndexOrThrow("attachments")) ?: "[]"
        return Complaint(
            complaintId = cursor.getString(cursor.getColumnIndexOrThrow("complaint_id")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id")),
            studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name")),
            roomNumber = cursor.getString(cursor.getColumnIndexOrThrow("room_number")),
            category = try { ComplaintCategory.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("category"))) } catch (e: Exception) { ComplaintCategory.OTHER },
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            description = cursor.getString(cursor.getColumnIndexOrThrow("description")),
            attachments = parseJsonStringList(attachmentsJson),
            urgency = try { ComplaintUrgency.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("urgency"))) } catch (e: Exception) { ComplaintUrgency.MEDIUM },
            status = try { ComplaintStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))) } catch (e: Exception) { ComplaintStatus.OPEN },
            assignedStaffName = cursor.getString(cursor.getColumnIndexOrThrow("assigned_staff_name")),
            hostNotes = cursor.getString(cursor.getColumnIndexOrThrow("host_notes")),
            resolutionSummary = cursor.getString(cursor.getColumnIndexOrThrow("resolution_summary")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
            resolvedAt = if (cursor.isNull(cursor.getColumnIndexOrThrow("resolved_at"))) null else cursor.getLong(cursor.getColumnIndexOrThrow("resolved_at"))
        )
    }

    private fun parseAttendance(cursor: Cursor): AttendanceRecord {
        return AttendanceRecord(
            attendanceId = cursor.getString(cursor.getColumnIndexOrThrow("attendance_id")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            studentId = cursor.getString(cursor.getColumnIndexOrThrow("student_id")),
            studentName = cursor.getString(cursor.getColumnIndexOrThrow("student_name")),
            roomNumber = cursor.getString(cursor.getColumnIndexOrThrow("room_number")),
            date = cursor.getString(cursor.getColumnIndexOrThrow("date")),
            status = try { AttendanceStatus.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("status"))) } catch (e: Exception) { AttendanceStatus.PRESENT },
            checkInTime = if (cursor.isNull(cursor.getColumnIndexOrThrow("check_in_time"))) null else cursor.getLong(cursor.getColumnIndexOrThrow("check_in_time")),
            remarks = cursor.getString(cursor.getColumnIndexOrThrow("remarks")),
            markedBy = cursor.getString(cursor.getColumnIndexOrThrow("marked_by")),
            leaveRequestId = cursor.getString(cursor.getColumnIndexOrThrow("leave_request_id")),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun parseFoodMenu(cursor: Cursor): FoodMenu {
        val scheduleJson = cursor.getString(cursor.getColumnIndexOrThrow("schedule_json")) ?: "{}"
        val scheduleMap = mutableMapOf<String, DailyMeals>()
        try {
            val jsonObj = JSONObject(scheduleJson)
            jsonObj.keys().forEach { day ->
                val dayObj = jsonObj.getJSONObject(day)
                val breakfast = parseJsonStringList(dayObj.optJSONArray("breakfast")?.toString() ?: "[]")
                val lunch = parseJsonStringList(dayObj.optJSONArray("lunch")?.toString() ?: "[]")
                val snacks = parseJsonStringList(dayObj.optJSONArray("snacks")?.toString() ?: "[]")
                val dinner = parseJsonStringList(dayObj.optJSONArray("dinner")?.toString() ?: "[]")
                scheduleMap[day] = DailyMeals(breakfast, lunch, snacks, dinner)
            }
        } catch (e: Exception) {
            // fallback
        }

        return FoodMenu(
            menuId = cursor.getString(cursor.getColumnIndexOrThrow("menu_id")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            weekStartDate = cursor.getString(cursor.getColumnIndexOrThrow("week_start_date")),
            schedule = scheduleMap,
            specialNotice = cursor.getString(cursor.getColumnIndexOrThrow("special_notice")),
            isPublished = cursor.getInt(cursor.getColumnIndexOrThrow("is_published")) == 1,
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow("updated_at"))
        )
    }

    private fun serializeSchedule(schedule: Map<String, DailyMeals>): String {
        val obj = JSONObject()
        schedule.forEach { (day, meals) ->
            val dayObj = JSONObject().apply {
                put("breakfast", JSONArray(meals.breakfast))
                put("lunch", JSONArray(meals.lunch))
                put("snacks", JSONArray(meals.snacks))
                put("dinner", JSONArray(meals.dinner))
            }
            obj.put(day, dayObj)
        }
        return obj.toString()
    }

    private fun parseAnnouncement(cursor: Cursor): Announcement {
        val urlsJson = cursor.getString(cursor.getColumnIndexOrThrow("attachment_urls")) ?: "[]"
        return Announcement(
            announcementId = cursor.getString(cursor.getColumnIndexOrThrow("announcement_id")),
            hostelId = cursor.getString(cursor.getColumnIndexOrThrow("hostel_id")),
            senderId = cursor.getString(cursor.getColumnIndexOrThrow("sender_id")),
            senderRole = try { UserRole.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("sender_role"))) } catch (e: Exception) { UserRole.HOST },
            senderName = cursor.getString(cursor.getColumnIndexOrThrow("sender_name")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            message = cursor.getString(cursor.getColumnIndexOrThrow("message")),
            priority = try { AnnouncementPriority.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("priority"))) } catch (e: Exception) { AnnouncementPriority.NORMAL },
            targetAudience = cursor.getString(cursor.getColumnIndexOrThrow("target_audience")),
            attachmentUrls = parseJsonStringList(urlsJson),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at")),
            expiresAt = if (cursor.isNull(cursor.getColumnIndexOrThrow("expires_at"))) null else cursor.getLong(cursor.getColumnIndexOrThrow("expires_at"))
        )
    }

    private fun parseNotification(cursor: Cursor): AppNotification {
        return AppNotification(
            notificationId = cursor.getString(cursor.getColumnIndexOrThrow("notification_id")),
            recipientUserId = cursor.getString(cursor.getColumnIndexOrThrow("recipient_user_id")),
            title = cursor.getString(cursor.getColumnIndexOrThrow("title")),
            body = cursor.getString(cursor.getColumnIndexOrThrow("body")),
            type = try { NotificationType.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("type"))) } catch (e: Exception) { NotificationType.ANNOUNCEMENT },
            relatedEntityId = cursor.getString(cursor.getColumnIndexOrThrow("related_entity_id")),
            isRead = cursor.getInt(cursor.getColumnIndexOrThrow("is_read")) == 1,
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"))
        )
    }

    private fun parseJsonStringList(jsonStr: String): List<String> {
        val list = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                list.add(jsonArray.getString(i))
            }
        } catch (e: Exception) {
            // ignore
        }
        return list
    }
}
