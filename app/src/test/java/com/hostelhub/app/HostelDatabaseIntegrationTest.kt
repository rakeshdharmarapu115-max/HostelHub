package com.hostelhub.app

import com.hostelhub.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class HostelDatabaseIntegrationTest {

    @Test
    fun testDomainModelsAndEnumIntegrity() {
        // 1. Roles & Users
        val user = User(
            userId = "std_001",
            email = "student@campus.edu",
            role = UserRole.STUDENT,
            fullName = "Alex Mercer",
            phoneNumber = "+1 555-0199"
        )
        assertEquals("std_001", user.userId)
        assertEquals(UserRole.STUDENT, user.role)
        assertTrue(user.isActive)

        // 2. Student entity
        val student = Student(
            studentId = "std_001",
            userId = "std_001",
            fullName = "Alex Mercer",
            rollNumber = "STD-2024-0042",
            collegeName = "College of Engineering",
            course = "B.Tech Computer Science",
            yearOfStudy = "3",
            gender = "male",
            permanentAddress = "42 Silicon Avenue, Metro City",
            emergencyContactName = "Sarah Mercer (Mother)",
            emergencyContactPhone = "+1 555-0144",
            hostelId = "hostel_001",
            hostelName = "Green Valley Residencies",
            roomId = "room_204",
            roomNumber = "A-204",
            bedNumber = "Bed-A",
            status = StudentStatus.ACTIVE
        )
        assertEquals("STD-2024-0042", student.rollNumber)
        assertEquals(StudentStatus.ACTIVE, student.status)

        // 3. Hostel entity
        val hostel = Hostel(
            hostelId = "hostel_001",
            hostId = "host_001",
            name = "Green Valley Residencies",
            address = "12 North Campus Road, University District",
            city = "Academic City",
            genderType = HostelGenderType.COED,
            totalRooms = 30,
            totalBeds = 60,
            occupiedBeds = 52,
            baseMonthlyRent = 450.0,
            cautionDeposit = 200.0,
            rating = 4.8
        )
        assertEquals(30, hostel.totalRooms)
        assertEquals(HostelGenderType.COED, hostel.genderType)

        // 4. Room & Bed entity
        val bed1 = Bed(bedId = "bed_1", bedNumber = "Bed-A", studentId = "std_001", studentName = "Alex Mercer", isOccupied = true)
        val bed2 = Bed(bedId = "bed_2", bedNumber = "Bed-B", studentId = null, studentName = null, isOccupied = false)
        val room = Room(
            roomId = "room_204",
            hostelId = "hostel_001",
            roomNumber = "A-204",
            floor = 2,
            block = "A",
            roomType = RoomType.DOUBLE,
            totalCapacity = 2,
            occupiedCount = 1,
            monthlyRent = 450.0,
            beds = listOf(bed1, bed2),
            status = RoomStatus.AVAILABLE
        )
        assertEquals(2, room.totalCapacity)
        assertEquals(1, room.occupiedCount)
        assertEquals(RoomStatus.AVAILABLE, room.status)
        assertEquals(2, room.beds.size)

        // 5. Fee & Payment
        val fee = Fee(
            feeId = "fee_001",
            hostelId = "hostel_001",
            studentId = "std_001",
            roomId = "room_204",
            title = "October 2026 Accommodation & Mess",
            feeType = FeeType.RENT,
            amount = 450.0,
            amountPaid = 450.0,
            status = FeeStatus.PAID
        )
        val payment = Payment(
            paymentId = "pay_101",
            feeId = "fee_001",
            studentId = "std_001",
            hostelId = "hostel_001",
            amountPaid = 450.0,
            paymentMethod = PaymentMethod.UPI,
            transactionReference = "TXN-98421049-OCT",
            status = PaymentStatus.SUCCESS
        )
        assertEquals(FeeStatus.PAID, fee.status)
        assertEquals(PaymentMethod.UPI, payment.paymentMethod)
        assertEquals(PaymentStatus.SUCCESS, payment.status)

        // 6. Complaint
        val complaint = Complaint(
            complaintId = "comp_001",
            hostelId = "hostel_001",
            studentId = "std_001",
            studentName = "Alex Mercer",
            roomNumber = "A-204",
            category = ComplaintCategory.ELECTRICAL,
            title = "Study lamp socket sparking",
            description = "Sparks on plugging charger",
            urgency = ComplaintUrgency.HIGH,
            status = ComplaintStatus.IN_PROGRESS,
            assignedStaffName = "Carl Johnson"
        )
        assertEquals(ComplaintCategory.ELECTRICAL, complaint.category)
        assertEquals(ComplaintUrgency.HIGH, complaint.urgency)
        assertEquals(ComplaintStatus.IN_PROGRESS, complaint.status)

        // 7. Attendance
        val attendance = AttendanceRecord(
            attendanceId = "att_1",
            hostelId = "hostel_001",
            studentId = "std_001",
            studentName = "Alex Mercer",
            roomNumber = "A-204",
            date = "2026-10-20",
            status = AttendanceStatus.PRESENT
        )
        assertEquals(AttendanceStatus.PRESENT, attendance.status)

        // 8. Food Menu
        val meals = DailyMeals(
            breakfast = listOf("Poha", "Tea"),
            lunch = listOf("Rice", "Dal", "Paneer"),
            snacks = listOf("Samosa", "Chai"),
            dinner = listOf("Roti", "Curry", "Kheer")
        )
        val menu = FoodMenu(
            menuId = "menu_1",
            hostelId = "hostel_001",
            weekStartDate = "2026-10-19",
            schedule = mapOf("monday" to meals),
            isPublished = true
        )
        assertEquals(1, menu.schedule.size)
        assertTrue(menu.isPublished)

        // 9. Announcement
        val announcement = Announcement(
            announcementId = "anc_1",
            hostelId = "hostel_001",
            senderId = "host_001",
            senderRole = UserRole.HOST,
            senderName = "Hostel Warden",
            title = "Wi-Fi Maintenance",
            message = "Maintenance on Saturday night",
            priority = AnnouncementPriority.NORMAL
        )
        assertEquals(AnnouncementPriority.NORMAL, announcement.priority)

        // 10. Notification
        val notif = AppNotification(
            notificationId = "notif_1",
            recipientUserId = "std_001",
            title = "Rent Reminder",
            body = "November rent is due",
            type = NotificationType.PAYMENT_DUE,
            isRead = false
        )
        assertEquals(NotificationType.PAYMENT_DUE, notif.type)
        assertFalse(notif.isRead)
    }

    @Test
    fun testDashboardStatisticsAggregations() {
        val studentStats = StudentDashboardStats(
            roomNumber = "A-204",
            bedNumber = "Bed-A",
            hostelName = "Green Valley Residencies",
            pendingFees = 0.0,
            activeComplaints = 1,
            attendanceRate = 95
        )
        assertEquals("A-204", studentStats.roomNumber)
        assertEquals(0.0, studentStats.pendingFees, 0.001)
        assertEquals(95, studentStats.attendanceRate)

        val hostStats = HostDashboardStats(
            totalRooms = 30,
            totalBeds = 60,
            occupiedBeds = 52,
            availableBeds = 8,
            pendingFeeCount = 4,
            pendingFeeAmount = 1800.0,
            pendingComplaints = 3,
            todayPresent = 50
        )
        assertEquals(30, hostStats.totalRooms)
        assertEquals(8, hostStats.availableBeds)
        assertEquals(1800.0, hostStats.pendingFeeAmount, 0.001)

        val adminStats = AdminDashboardStats(
            totalHostels = 3,
            totalStudents = 120,
            totalRooms = 60,
            totalBeds = 120,
            occupiedBeds = 104,
            totalRevenue = 54000.0,
            pendingComplaints = 3
        )
        assertEquals(3, adminStats.totalHostels)
        assertEquals(120, adminStats.totalStudents)
        assertEquals(54000.0, adminStats.totalRevenue, 0.001)
    }
}
