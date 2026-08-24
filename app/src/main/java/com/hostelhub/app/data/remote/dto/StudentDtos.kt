package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.Student
import com.hostelhub.app.domain.model.StudentStatus

data class StudentDto(
    @SerializedName("studentId") val studentId: String = "",
    @SerializedName("userId") val userId: String = "",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("rollNumber") val rollNumber: String = "",
    @SerializedName("collegeName") val collegeName: String = "",
    @SerializedName("course") val course: String = "",
    @SerializedName("yearOfStudy") val yearOfStudy: String = "1",
    @SerializedName("gender") val gender: String = "male",
    @SerializedName("permanentAddress") val permanentAddress: String = "",
    @SerializedName("emergencyContactName") val emergencyContactName: String = "",
    @SerializedName("emergencyContactPhone") val emergencyContactPhone: String = "",
    @SerializedName("hostelId") val hostelId: String? = null,
    @SerializedName("hostelName") val hostelName: String? = null,
    @SerializedName("roomId") val roomId: String? = null,
    @SerializedName("roomNumber") val roomNumber: String? = null,
    @SerializedName("bedNumber") val bedNumber: String? = null,
    @SerializedName("admissionDate") val admissionDate: Long? = null,
    @SerializedName("status") val status: String = "ACTIVE"
) {
    fun toDomain(): Student {
        val studentStatus = try {
            StudentStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            StudentStatus.ACTIVE
        }
        return Student(
            studentId = studentId,
            userId = userId,
            fullName = fullName,
            rollNumber = rollNumber,
            collegeName = collegeName,
            course = course,
            yearOfStudy = yearOfStudy,
            gender = gender,
            permanentAddress = permanentAddress,
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone,
            hostelId = hostelId,
            hostelName = hostelName,
            roomId = roomId,
            roomNumber = roomNumber,
            bedNumber = bedNumber,
            admissionDate = admissionDate,
            status = studentStatus
        )
    }
}
