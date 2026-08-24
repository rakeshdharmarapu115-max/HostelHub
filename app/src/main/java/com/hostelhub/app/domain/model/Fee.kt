package com.hostelhub.app.domain.model

data class Fee(
    val feeId: String = "",
    val hostelId: String = "",
    val studentId: String = "",
    val roomId: String = "",
    val title: String = "",
    val feeType: FeeType = FeeType.RENT,
    val amount: Double = 0.0,
    val amountPaid: Double = 0.0,
    val dueDate: Long = System.currentTimeMillis(),
    val billingMonth: Int = 1,
    val billingYear: Int = 2026,
    val status: FeeStatus = FeeStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)

enum class FeeType {
    RENT,
    MESS,
    CAUTION_DEPOSIT,
    ELECTRICITY,
    FINE,
    OTHER
}

enum class FeeStatus {
    PAID,
    PARTIALLY_PAID,
    PENDING,
    OVERDUE
}
