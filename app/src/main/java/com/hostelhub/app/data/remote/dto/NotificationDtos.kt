package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.AppNotification
import com.hostelhub.app.domain.model.NotificationType

data class AppNotificationDto(
    @SerializedName("notificationId") val notificationId: String = "",
    @SerializedName("recipientUserId") val recipientUserId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("body") val body: String = "",
    @SerializedName("type") val type: String = "ANNOUNCEMENT",
    @SerializedName("relatedEntityId") val relatedEntityId: String? = null,
    @SerializedName("isRead") val isRead: Boolean = false,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): AppNotification {
        val nType = try {
            NotificationType.valueOf(type.uppercase())
        } catch (e: Exception) {
            NotificationType.ANNOUNCEMENT
        }
        return AppNotification(
            notificationId = notificationId,
            recipientUserId = recipientUserId,
            title = title,
            body = body,
            type = nType,
            relatedEntityId = relatedEntityId,
            isRead = isRead,
            createdAt = createdAt
        )
    }
}
