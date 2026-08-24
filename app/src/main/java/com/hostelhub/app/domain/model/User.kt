package com.hostelhub.app.domain.model

data class User(
    val userId: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STUDENT,
    val fullName: String = "",
    val phoneNumber: String = "",
    val avatarUrl: String? = null,
    val isActive: Boolean = true,
    val fcmToken: String? = null,
    val studentId: String? = null,
    val hostId: String? = null,
    val adminId: String? = null,
    val hostelId: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
