package com.hostelhub.app.domain.model

data class Complaint(
    val complaintId: String = "",
    val hostelId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val roomNumber: String = "",
    val category: ComplaintCategory = ComplaintCategory.OTHER,
    val title: String = "",
    val description: String = "",
    val attachments: List<String> = emptyList(),
    val urgency: ComplaintUrgency = ComplaintUrgency.MEDIUM,
    val status: ComplaintStatus = ComplaintStatus.OPEN,
    val assignedStaffName: String? = null,
    val hostNotes: String? = null,
    val resolutionSummary: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

enum class ComplaintCategory {
    ELECTRICAL,
    PLUMBING,
    WIFI,
    CLEANING,
    FOOD,
    FURNITURE,
    SECURITY,
    OTHER
}

enum class ComplaintUrgency {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class ComplaintStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    REJECTED
}
