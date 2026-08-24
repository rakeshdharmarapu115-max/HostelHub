package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.AdminDashboardStats
import com.hostelhub.app.domain.model.HostDashboardStats
import com.hostelhub.app.domain.model.StudentDashboardStats

data class StudentDashboardStatsDto(
    @SerializedName("roomNumber") val roomNumber: String = "",
    @SerializedName("bedNumber") val bedNumber: String = "",
    @SerializedName("hostelName") val hostelName: String = "",
    @SerializedName("pendingFees") val pendingFees: Double = 0.0,
    @SerializedName("activeComplaints") val activeComplaints: Int = 0,
    @SerializedName("attendanceRate") val attendanceRate: Int = 100
) {
    fun toDomain(): StudentDashboardStats {
        return StudentDashboardStats(
            roomNumber = roomNumber,
            bedNumber = bedNumber,
            hostelName = hostelName,
            pendingFees = pendingFees,
            activeComplaints = activeComplaints,
            attendanceRate = attendanceRate
        )
    }
}

data class HostDashboardStatsDto(
    @SerializedName("totalRooms") val totalRooms: Int = 0,
    @SerializedName("totalBeds") val totalBeds: Int = 0,
    @SerializedName("occupiedBeds") val occupiedBeds: Int = 0,
    @SerializedName("availableBeds") val availableBeds: Int = 0,
    @SerializedName("pendingFeeCount") val pendingFeeCount: Int = 0,
    @SerializedName("pendingFeeAmount") val pendingFeeAmount: Double = 0.0,
    @SerializedName("pendingComplaints") val pendingComplaints: Int = 0,
    @SerializedName("todayPresent") val todayPresent: Int = 0
) {
    fun toDomain(): HostDashboardStats {
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
}

data class AdminDashboardStatsDto(
    @SerializedName("totalHostels") val totalHostels: Int = 0,
    @SerializedName("totalStudents") val totalStudents: Int = 0,
    @SerializedName("totalRooms") val totalRooms: Int = 0,
    @SerializedName("totalBeds") val totalBeds: Int = 0,
    @SerializedName("occupiedBeds") val occupiedBeds: Int = 0,
    @SerializedName("totalRevenue") val totalRevenue: Double = 0.0,
    @SerializedName("pendingComplaints") val pendingComplaints: Int = 0
) {
    fun toDomain(): AdminDashboardStats {
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
}
