package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.*

data class FeeDto(
    @SerializedName("feeId") val feeId: String = "",
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("studentId") val studentId: String = "",
    @SerializedName("roomId") val roomId: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("feeType") val feeType: String = "RENT",
    @SerializedName("amount") val amount: Double = 0.0,
    @SerializedName("amountPaid") val amountPaid: Double = 0.0,
    @SerializedName("dueDate") val dueDate: Long = System.currentTimeMillis(),
    @SerializedName("billingMonth") val billingMonth: Int = 1,
    @SerializedName("billingYear") val billingYear: Int = 2026,
    @SerializedName("status") val status: String = "PENDING",
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Fee {
        val fType = try {
            FeeType.valueOf(feeType.uppercase())
        } catch (e: Exception) {
            FeeType.RENT
        }
        val fStatus = try {
            FeeStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            FeeStatus.PENDING
        }
        return Fee(
            feeId = feeId,
            hostelId = hostelId,
            studentId = studentId,
            roomId = roomId,
            title = title,
            feeType = fType,
            amount = amount,
            amountPaid = amountPaid,
            dueDate = dueDate,
            billingMonth = billingMonth,
            billingYear = billingYear,
            status = fStatus,
            createdAt = createdAt
        )
    }
}

data class PaymentDto(
    @SerializedName("paymentId") val paymentId: String = "",
    @SerializedName("feeId") val feeId: String = "",
    @SerializedName("studentId") val studentId: String = "",
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("amountPaid") val amountPaid: Double = 0.0,
    @SerializedName("paymentMethod") val paymentMethod: String = "UPI",
    @SerializedName("transactionReference") val transactionReference: String = "",
    @SerializedName("paymentDate") val paymentDate: Long = System.currentTimeMillis(),
    @SerializedName("receiptUrl") val receiptUrl: String? = null,
    @SerializedName("status") val status: String = "SUCCESS",
    @SerializedName("verifiedByHostId") val verifiedByHostId: String? = null,
    @SerializedName("remarks") val remarks: String? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Payment {
        val pMethod = try {
            PaymentMethod.valueOf(paymentMethod.uppercase())
        } catch (e: Exception) {
            PaymentMethod.UPI
        }
        val pStatus = try {
            PaymentStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            PaymentStatus.SUCCESS
        }
        return Payment(
            paymentId = paymentId,
            feeId = feeId,
            studentId = studentId,
            hostelId = hostelId,
            amountPaid = amountPaid,
            paymentMethod = pMethod,
            transactionReference = transactionReference,
            paymentDate = paymentDate,
            receiptUrl = receiptUrl,
            status = pStatus,
            verifiedByHostId = verifiedByHostId,
            remarks = remarks,
            createdAt = createdAt
        )
    }
}

data class CreateFeeRequestDto(
    @SerializedName("hostelId") val hostelId: String,
    @SerializedName("studentId") val studentId: String,
    @SerializedName("roomId") val roomId: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("feeType") val feeType: String = "RENT",
    @SerializedName("amount") val amount: Double,
    @SerializedName("dueDate") val dueDate: Long,
    @SerializedName("billingMonth") val billingMonth: Int,
    @SerializedName("billingYear") val billingYear: Int
)

data class RecordPaymentRequestDto(
    @SerializedName("feeId") val feeId: String,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("hostelId") val hostelId: String? = null,
    @SerializedName("amountPaid") val amountPaid: Double,
    @SerializedName("paymentMethod") val paymentMethod: String = "UPI",
    @SerializedName("transactionReference") val transactionReference: String? = null,
    @SerializedName("remarks") val remarks: String? = null
)
