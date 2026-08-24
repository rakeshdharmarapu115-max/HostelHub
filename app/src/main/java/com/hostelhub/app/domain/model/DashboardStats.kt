package com.hostelhub.app.domain.model

data class StudentDashboardStats(
    val roomNumber: String = "A-204",
    val bedNumber: String = "Bed-A",
    val hostelName: String = "Green Valley Residencies",
    val pendingFees: Double = 0.0,
    val activeComplaints: Int = 0,
    val attendanceRate: Int = 95
)

data class HostDashboardStats(
    val totalRooms: Int = 0,
    val totalBeds: Int = 0,
    val occupiedBeds: Int = 0,
    val availableBeds: Int = 0,
    val pendingFeeCount: Int = 0,
    val pendingFeeAmount: Double = 0.0,
    val pendingComplaints: Int = 0,
    val todayPresent: Int = 0
)

data class AdminDashboardStats(
    val totalHostels: Int = 0,
    val totalStudents: Int = 0,
    val totalRooms: Int = 0,
    val totalBeds: Int = 0,
    val occupiedBeds: Int = 0,
    val totalRevenue: Double = 0.0,
    val pendingComplaints: Int = 0
)
