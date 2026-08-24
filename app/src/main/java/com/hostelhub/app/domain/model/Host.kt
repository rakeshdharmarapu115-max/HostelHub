package com.hostelhub.app.domain.model

data class Host(
    val hostId: String = "",
    val userId: String = "",
    val fullName: String = "",
    val businessName: String = "",
    val contactPhone: String = "",
    val contactEmail: String = "",
    val hostelIds: List<String> = emptyList(),
    val verifiedStatus: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
