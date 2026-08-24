package com.hostelhub.app.domain.model

data class Student(
    val studentId: String = "",
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val rollNumber: String = "",
    val collegeName: String = "",
    val course: String = "",
    val yearOfStudy: String = "1",
    val gender: String = "male",
    val permanentAddress: String = "",
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val hostelId: String? = null,
    val hostelName: String? = null,
    val roomId: String? = null,
    val roomNumber: String? = null,
    val bedNumber: String? = null,
    val admissionDate: Long? = null,
    val status: StudentStatus = StudentStatus.ACTIVE
)

enum class StudentStatus {
    ACTIVE,
    VACATED,
    PENDING_APPROVAL
}
