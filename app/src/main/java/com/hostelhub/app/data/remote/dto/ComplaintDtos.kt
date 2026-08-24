package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.*

data class ComplaintDto(
    @SerializedName("complaintId") val complaintId: String = "",
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("studentId") val studentId: String = "",
    @SerializedName("studentName") val studentName: String = "",
    @SerializedName("roomNumber") val roomNumber: String = "",
    @SerializedName("category") val category: String = "OTHER",
    @SerializedName("title") val title: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName("urgency") val urgency: String = "MEDIUM",
    @SerializedName("status") val status: String = "OPEN",
    @SerializedName("assignedStaffName") val assignedStaffName: String? = null,
    @SerializedName("hostNotes") val hostNotes: String? = null,
    @SerializedName("resolutionSummary") val resolutionSummary: String? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("resolvedAt") val resolvedAt: Long? = null
) {
    fun toDomain(): Complaint {
        val cat = try {
            ComplaintCategory.valueOf(category.uppercase())
        } catch (e: Exception) {
            ComplaintCategory.OTHER
        }
        val urg = try {
            ComplaintUrgency.valueOf(urgency.uppercase())
        } catch (e: Exception) {
            ComplaintUrgency.MEDIUM
        }
        val stat = try {
            ComplaintStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            ComplaintStatus.OPEN
        }
        return Complaint(
            complaintId = complaintId,
            hostelId = hostelId,
            studentId = studentId,
            studentName = studentName,
            roomNumber = roomNumber,
            category = cat,
            title = title,
            description = description,
            attachments = attachments,
            urgency = urg,
            status = stat,
            assignedStaffName = assignedStaffName,
            hostNotes = hostNotes,
            resolutionSummary = resolutionSummary,
            createdAt = createdAt,
            resolvedAt = resolvedAt
        )
    }
}

data class CreateComplaintRequestDto(
    @SerializedName("hostelId") val hostelId: String? = null,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("roomNumber") val roomNumber: String? = null,
    @SerializedName("category") val category: String = "OTHER",
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("attachments") val attachments: List<String> = emptyList(),
    @SerializedName("urgency") val urgency: String = "MEDIUM"
)

data class UpdateComplaintStatusRequestDto(
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("assignedStaffName") val assignedStaffName: String? = null,
    @SerializedName("resolutionSummary") val resolutionSummary: String? = null
)
