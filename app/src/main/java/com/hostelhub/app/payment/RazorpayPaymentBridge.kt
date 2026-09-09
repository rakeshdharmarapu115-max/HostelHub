package com.hostelhub.app.payment

import android.app.Activity
import android.util.Log
import com.hostelhub.app.data.remote.dto.RazorpayOrderResponseDto
import com.razorpay.Checkout
import com.razorpay.PaymentData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

sealed class RazorpayResult {
    data class Success(
        val feeId: String,
        val razorpayPaymentId: String,
        val razorpayOrderId: String,
        val razorpaySignature: String?,
        val amountPaid: Double
    ) : RazorpayResult()

    data class Error(
        val feeId: String,
        val code: Int,
        val message: String,
        val razorpayOrderId: String? = null,
        val razorpayPaymentId: String? = null
    ) : RazorpayResult()

    data class Cancelled(
        val feeId: String,
        val message: String = "Payment was cancelled by user"
    ) : RazorpayResult()
}

@Singleton
class RazorpayPaymentBridge @Inject constructor() {

    private val _paymentResultFlow = MutableSharedFlow<RazorpayResult>(extraBufferCapacity = 1)
    val paymentResultFlow: SharedFlow<RazorpayResult> = _paymentResultFlow.asSharedFlow()

    private var activeFeeId: String = ""
    private var activeOrderId: String = ""
    private var activeAmount: Double = 0.0

    /**
     * Launch Razorpay native checkout for given order details.
     */
    fun startCheckout(
        activity: Activity,
        order: RazorpayOrderResponseDto,
        prefillEmail: String? = null,
        prefillPhone: String? = null
    ) {
        activeFeeId = order.feeId
        activeOrderId = order.orderId
        activeAmount = order.amount

        val checkout = Checkout()
        val keyToUse = order.keyId
        if (keyToUse.isBlank()) {
            Log.e("RazorpayPaymentBridge", "Razorpay Key ID is missing from order response.")
            _paymentResultFlow.tryEmit(
                RazorpayResult.Error(
                    feeId = activeFeeId,
                    code = -1,
                    message = "Razorpay payment gateway is not properly configured. Missing Key ID.",
                    razorpayOrderId = activeOrderId
                )
            )
            return
        }
        checkout.setKeyID(keyToUse)

        try {
            val options = JSONObject()
            options.put("name", order.hostelName.ifBlank { "HostelHub Campus Residency" })
            options.put("description", order.feeTitle.ifBlank { "Hostel Fee Settlement" })
            options.put("currency", order.currency.ifBlank { "INR" })
            val paise = if (order.amountInPaise > 0) order.amountInPaise else (order.amount * 100).toLong()
            options.put("amount", paise)

            if (order.orderId.isNotBlank()) {
                options.put("order_id", order.orderId)
            }

            // Theme Customization matching HostelHub branding
            val theme = JSONObject()
            theme.put("color", "#1B365D") // PrimaryNavy
            theme.put("backdrop_color", "#0B192C")
            options.put("theme", theme)

            // Prefill Student Information
            val prefill = JSONObject()
            val email = prefillEmail?.ifBlank { null } ?: order.studentEmail
            if (email.isNotBlank()) prefill.put("email", email)
            val phone = prefillPhone?.ifBlank { null } ?: order.studentPhone
            if (phone.isNotBlank()) prefill.put("contact", phone)
            if (order.studentName.isNotBlank()) prefill.put("name", order.studentName)
            options.put("prefill", prefill)

            // Retry options
            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 3)
            options.put("retry", retryObj)

            // Auto-trigger native Razorpay modal
            checkout.open(activity, options)
            Log.d("RazorpayPaymentBridge", "Razorpay Checkout opened successfully for Order ID: ${order.orderId}")
        } catch (e: Exception) {
            Log.e("RazorpayPaymentBridge", "Error starting Razorpay checkout", e)
            _paymentResultFlow.tryEmit(
                RazorpayResult.Error(
                    feeId = activeFeeId,
                    code = -1,
                    message = e.localizedMessage ?: "Failed to initialize payment gateway",
                    razorpayOrderId = activeOrderId
                )
            )
        }
    }

    /**
     * Called by MainActivity when PaymentResultWithDataListener receives payment success.
     */
    fun onPaymentSuccess(razorpayPaymentId: String?, paymentData: PaymentData?) {
        val resolvedPaymentId = razorpayPaymentId
            ?: paymentData?.paymentId
            ?: "pay_${System.currentTimeMillis()}"
        val resolvedOrderId = paymentData?.orderId
            ?: activeOrderId
        val signature = paymentData?.signature

        Log.d("RazorpayPaymentBridge", "Payment successful! ID: $resolvedPaymentId, Order: $resolvedOrderId")
        _paymentResultFlow.tryEmit(
            RazorpayResult.Success(
                feeId = activeFeeId,
                razorpayPaymentId = resolvedPaymentId,
                razorpayOrderId = resolvedOrderId,
                razorpaySignature = signature,
                amountPaid = activeAmount
            )
        )
    }

    /**
     * Called by MainActivity when PaymentResultWithDataListener receives payment error / cancellation.
     */
    fun onPaymentError(errorCode: Int, response: String?, paymentData: PaymentData?) {
        val errorMsg = when (errorCode) {
            Checkout.NETWORK_ERROR -> "Network error during payment. Please check your connection."
            Checkout.INVALID_OPTIONS -> "Invalid payment options provided."
            Checkout.PAYMENT_CANCELED -> "Payment was cancelled."
            Checkout.TLS_ERROR -> "Device does not support TLS 1.2+."
            else -> response ?: "Payment failed with error code: $errorCode"
        }

        Log.e("RazorpayPaymentBridge", "Payment failed. Code: $errorCode, Msg: $errorMsg")

        if (errorCode == Checkout.PAYMENT_CANCELED) {
            _paymentResultFlow.tryEmit(
                RazorpayResult.Cancelled(
                    feeId = activeFeeId,
                    message = errorMsg
                )
            )
        } else {
            _paymentResultFlow.tryEmit(
                RazorpayResult.Error(
                    feeId = activeFeeId,
                    code = errorCode,
                    message = errorMsg,
                    razorpayOrderId = paymentData?.orderId ?: activeOrderId,
                    razorpayPaymentId = paymentData?.paymentId
                )
            )
        }
    }
}
