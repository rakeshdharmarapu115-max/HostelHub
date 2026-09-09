package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeePaymentApi {
    @GET("fees/student/{studentId}")
    suspend fun getFeesForStudent(@Path("studentId") studentId: String): Response<ApiResponse<List<FeeDto>>>

    @GET("fees/hostel/{hostelId}")
    suspend fun getFeesForHostel(@Path("hostelId") hostelId: String): Response<ApiResponse<List<FeeDto>>>

    @GET("fees")
    suspend fun getAllFees(): Response<ApiResponse<List<FeeDto>>>

    @POST("fees")
    suspend fun createFee(@Body request: CreateFeeRequestDto): Response<ApiResponse<FeeDto>>

    @GET("payments/student/{studentId}")
    suspend fun getPaymentsForStudent(@Path("studentId") studentId: String): Response<ApiResponse<List<PaymentDto>>>

    @GET("payments/hostel/{hostelId}")
    suspend fun getPaymentsForHostel(@Path("hostelId") hostelId: String): Response<ApiResponse<List<PaymentDto>>>

    @GET("payments/history")
    suspend fun getTransactionHistory(): Response<ApiResponse<List<PaymentDto>>>

    @POST("payments/razorpay/create-order")
    suspend fun createRazorpayOrder(@Body request: CreateRazorpayOrderRequestDto): Response<ApiResponse<RazorpayOrderResponseDto>>

    @POST("payments/razorpay/verify")
    suspend fun verifyRazorpayPayment(@Body request: VerifyRazorpayPaymentRequestDto): Response<ApiResponse<PaymentDto>>

    @POST("payments/razorpay/failed")
    suspend fun recordPaymentFailure(@Body request: RecordPaymentFailureRequestDto): Response<ApiResponse<PaymentDto>>

    @POST("payments")
    suspend fun recordPayment(@Body request: RecordPaymentRequestDto): Response<ApiResponse<PaymentDto>>

    // Hostel Owner QR Payment Configuration Endpoints
    @GET("payments/config/my-hostel")
    suspend fun getMyHostelPaymentConfig(): Response<ApiResponse<HostelPaymentConfigDto>>

    @GET("payments/config/hostel/{hostelId}")
    suspend fun getHostelPaymentConfig(@Path("hostelId") hostelId: String): Response<ApiResponse<HostelPaymentConfigDto>>

    @retrofit2.http.PUT("payments/config/hostel/{hostelId}")
    suspend fun updateHostelPaymentConfig(
        @Path("hostelId") hostelId: String,
        @Body body: UpdatePaymentConfigRequestDto
    ): Response<ApiResponse<HostelPaymentConfigDto>>

    // Manual Static QR Submission & Verification Endpoints
    @POST("payments/manual-qr")
    suspend fun submitManualQrPayment(@Body request: ManualPaymentSubmissionRequestDto): Response<ApiResponse<PaymentDto>>

    @retrofit2.http.PATCH("payments/{id}/verify-manual")
    suspend fun verifyManualPayment(
        @Path("id") paymentId: String,
        @Body request: VerifyManualPaymentRequestDto
    ): Response<ApiResponse<PaymentDto>>

    // Admin Overview
    @GET("payments/admin/overview")
    suspend fun getAdminPaymentOverview(): Response<ApiResponse<List<AdminPaymentOverviewDto>>>
}
