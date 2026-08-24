package com.hostelhub.app.domain.model

data class AppNotification(
    val notificationId: String = "",
    val recipientUserId: String = "",
    val title: String = "",
    val body: String = "",
    val type: NotificationType = NotificationType.ANNOUNCEMENT,
    val relatedEntityId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class NotificationType {
    PAYMENT_DUE,
    PAYMENT_CONFIRMED,
    COMPLAINT_UPDATE,
    ATTENDANCE_ALERT,
    ANNOUNCEMENT,
    LEAVE_APPROVED
}
