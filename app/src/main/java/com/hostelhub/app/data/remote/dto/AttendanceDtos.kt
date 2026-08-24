package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.AttendanceRecord
import com.hostelhub.app.domain.model.AttendanceStatus

data class AttendanceRecordDto(
    @SerializedName("attendanceId") val attendanceId: String = "",
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("studentId") val studentId: String = "",
    @SerializedName("studentName") val studentName: String = "",
    @SerializedName("roomNumber") val roomNumber: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("status") val status: String = "PRESENT",
    @SerializedName("checkInTime") val checkInTime: Long? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("markedBy") val markedBy: String = "STUDENT_SELF",
    @SerializedName("leaveRequestId") val leaveRequestId: String? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): AttendanceRecord {
        val aStatus = try {
            AttendanceStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            AttendanceStatus.PRESENT
        }
        return AttendanceRecord(
            attendanceId = attendanceId,
            hostelId = hostelId,
            studentId = studentId,
            studentName = studentName,
            roomNumber = roomNumber,
            date = date,
            status = aStatus,
            checkInTime = checkInTime,
            remarks = remarks,
            markedBy = markedBy,
            leaveRequestId = leaveRequestId,
            createdAt = createdAt
        )
    }
}

data class MarkAttendanceRequestDto(
    @SerializedName("hostelId") val hostelId: String? = null,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("roomNumber") val roomNumber: String? = null,
    @SerializedName("date") val date: String,
    @SerializedName("status") val status: String = "PRESENT",
    @SerializedName("checkInTime") val checkInTime: Long? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("markedBy") val markedBy: String? = null
)

data class BatchAttendanceRequestDto(
    @SerializedName("records") val records: List<MarkAttendanceRequestDto>
)
