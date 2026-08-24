package com.hostelhub.app.domain.model

data class Announcement(
    val announcementId: String = "",
    val hostelId: String = "GLOBAL_CAMPUS",
    val senderId: String = "",
    val senderRole: UserRole = UserRole.HOST,
    val senderName: String = "",
    val title: String = "",
    val message: String = "",
    val priority: AnnouncementPriority = AnnouncementPriority.NORMAL,
    val targetAudience: String = "ALL",
    val attachmentUrls: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null
)

enum class AnnouncementPriority {
    NORMAL,
    IMPORTANT,
    URGENT
}
