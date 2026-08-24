package com.hostelhub.app.domain.model

data class Payment(
    val paymentId: String = "",
    val feeId: String = "",
    val studentId: String = "",
    val hostelId: String = "",
    val amountPaid: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val transactionReference: String = "",
    val paymentDate: Long = System.currentTimeMillis(),
    val receiptUrl: String? = null,
    val status: PaymentStatus = PaymentStatus.SUCCESS,
    val verifiedByHostId: String? = null,
    val remarks: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class PaymentMethod {
    ONLINE,
    UPI,
    CARD,
    CASH,
    BANK_TRANSFER
}

enum class PaymentStatus {
    SUCCESS,
    PENDING,
    FAILED
}
