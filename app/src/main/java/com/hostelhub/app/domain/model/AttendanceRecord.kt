package com.hostelhub.app.domain.model

data class AttendanceRecord(
    val attendanceId: String = "",
    val hostelId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val roomNumber: String = "",
    val date: String = "", // YYYY-MM-DD
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val checkInTime: Long? = null,
    val remarks: String? = null,
    val markedBy: String = "STUDENT_SELF",
    val leaveRequestId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    ON_LEAVE,
    LATE
}
