package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.User
import com.hostelhub.app.domain.model.UserRole

data class LoginRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterStudentRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("rollNumber") val rollNumber: String,
    @SerializedName("collegeName") val collegeName: String,
    @SerializedName("course") val course: String,
    @SerializedName("yearOfStudy") val yearOfStudy: String = "1",
    @SerializedName("gender") val gender: String = "male",
    @SerializedName("permanentAddress") val permanentAddress: String,
    @SerializedName("emergencyContactName") val emergencyContactName: String,
    @SerializedName("emergencyContactPhone") val emergencyContactPhone: String,
    @SerializedName("hostelId") val hostelId: String? = null,
    @SerializedName("roomId") val roomId: String? = null,
    @SerializedName("bedNumber") val bedNumber: String? = null
)

data class RegisterHostRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("businessName") val businessName: String,
    @SerializedName("contactPhone") val contactPhone: String,
    @SerializedName("contactEmail") val contactEmail: String
)

data class RegisterAdminRequestDto(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("associationName") val associationName: String,
    @SerializedName("designation") val designation: String,
    @SerializedName("contactPhone") val contactPhone: String? = null
)

data class RefreshTokenRequestDto(
    @SerializedName("refreshToken") val refreshToken: String
)

data class AuthResponseDataDto(
    @SerializedName("user") val user: UserDto,
    @SerializedName("tokens") val tokens: AuthTokensDto
)

data class AuthTokensDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenResponseDto(
    @SerializedName("accessToken") val accessToken: String
)

data class UserDto(
    @SerializedName("userId") val userId: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("role") val role: String = "STUDENT",
    @SerializedName("fullName") val fullName: String = "",
    @SerializedName("phoneNumber") val phoneNumber: String = "",
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("fcmToken") val fcmToken: String? = null,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("hostId") val hostId: String? = null,
    @SerializedName("adminId") val adminId: String? = null,
    @SerializedName("hostelId") val hostelId: String? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): User {
        val userRole = try {
            UserRole.valueOf(role.uppercase())
        } catch (e: Exception) {
            UserRole.STUDENT
        }
        return User(
            userId = userId,
            email = email,
            role = userRole,
            fullName = fullName,
            phoneNumber = phoneNumber,
            avatarUrl = avatarUrl,
            isActive = isActive,
            fcmToken = fcmToken,
            studentId = studentId,
            hostId = hostId,
            adminId = adminId,
            hostelId = hostelId,
            createdAt = createdAt
        )
    }
}
