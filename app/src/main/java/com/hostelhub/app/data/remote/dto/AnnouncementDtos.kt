package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.Announcement
import com.hostelhub.app.domain.model.AnnouncementPriority
import com.hostelhub.app.domain.model.UserRole

data class AnnouncementDto(
    @SerializedName("announcementId") val announcementId: String = "",
    @SerializedName("hostelId") val hostelId: String = "GLOBAL_CAMPUS",
    @SerializedName("senderId") val senderId: String = "",
    @SerializedName("senderRole") val senderRole: String = "HOST",
    @SerializedName("senderName") val senderName: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("message") val message: String = "",
    @SerializedName("priority") val priority: String = "NORMAL",
    @SerializedName("targetAudience") val targetAudience: String = "ALL",
    @SerializedName("attachmentUrls") val attachmentUrls: List<String> = emptyList(),
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("expiresAt") val expiresAt: Long? = null
) {
    fun toDomain(): Announcement {
        val sRole = try {
            UserRole.valueOf(senderRole.uppercase())
        } catch (e: Exception) {
            UserRole.HOST
        }
        val prio = try {
            AnnouncementPriority.valueOf(priority.uppercase())
        } catch (e: Exception) {
            AnnouncementPriority.NORMAL
        }
        return Announcement(
            announcementId = announcementId,
            hostelId = hostelId,
            senderId = senderId,
            senderRole = sRole,
            senderName = senderName,
            title = title,
            message = message,
            priority = prio,
            targetAudience = targetAudience,
            attachmentUrls = attachmentUrls,
            createdAt = createdAt,
            expiresAt = expiresAt
        )
    }
}

data class CreateAnnouncementRequestDto(
    @SerializedName("hostelId") val hostelId: String? = null,
    @SerializedName("senderId") val senderId: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("priority") val priority: String = "NORMAL",
    @SerializedName("targetAudience") val targetAudience: String = "ALL",
    @SerializedName("attachmentUrls") val attachmentUrls: List<String> = emptyList()
)
