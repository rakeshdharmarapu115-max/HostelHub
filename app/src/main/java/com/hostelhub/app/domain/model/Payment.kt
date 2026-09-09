package com.hostelhub.app.domain.model

data class Payment(
    val paymentId: String = "",
    val feeId: String = "",
    val studentId: String = "",
    val studentName: String? = null,
    val feeTitle: String? = null,
    val hostelId: String = "",
    val amountPaid: Double = 0.0,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val paymentGateway: String = "RAZORPAY",
    val orderId: String? = null,
    val transactionId: String? = null,
    val transactionReference: String = "",
    val razorpayOrderId: String? = null,
    val razorpayPaymentId: String? = null,
    val paymentDate: Long = System.currentTimeMillis(),
    val receiptUrl: String? = null,
    val status: PaymentStatus = PaymentStatus.SUCCESS,
    val verifiedByHostId: String? = null,
    val remarks: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class AdminPaymentOverviewItem(
    val hostelId: String = "",
    val hostelName: String = "",
    val city: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerContact: String = "",
    val ownerEmail: String = "",
    val paymentAccountId: String? = null,
    val paymentAccountStatus: String = "ACTIVE",
    val qrConfigured: Boolean = false,
    val qrPaymentEnabled: Boolean = true,
    val paymentQrUrl: String? = null,
    val upiId: String? = null,
    val merchantName: String = "",
    val totalCollections: Double = 0.0,
    val successfulPaymentCount: Int = 0,
    val pendingVerificationCount: Int = 0
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
    PENDING_VERIFICATION,
    FAILED,
    CANCELLED
}
