package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.FeePaymentApi
import com.hostelhub.app.data.remote.dto.*
import com.hostelhub.app.domain.model.Fee
import com.hostelhub.app.domain.model.Payment
import com.hostelhub.app.domain.repository.FeePaymentRepository
import com.hostelhub.app.utils.ErrorParser
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteFeePaymentRepositoryImpl @Inject constructor(
    private val feePaymentApi: FeePaymentApi
) : FeePaymentRepository {

    override fun getFeesForStudent(studentId: String): Flow<Resource<List<Fee>>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getFeesForStudent(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch student fees"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching fees"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getFeesForHostel(hostelId: String): Flow<Resource<List<Fee>>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getFeesForHostel(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch hostel fees"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching hostel fees"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllFees(): Flow<Resource<List<Fee>>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getAllFees()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch all fees"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching fees"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPaymentsForStudent(studentId: String): Flow<Resource<List<Payment>>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getPaymentsForStudent(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch payments"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching payments"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getPaymentsForHostel(hostelId: String): Flow<Resource<List<Payment>>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getPaymentsForHostel(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch hostel payment history"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching hostel payment history"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getTransactionHistory(): Flow<Resource<List<Payment>>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getTransactionHistory()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch transaction history"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching transaction history"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createRazorpayOrder(feeId: String, amount: Double?): Resource<RazorpayOrderResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = feePaymentApi.createRazorpayOrder(CreateRazorpayOrderRequestDto(feeId, amount))
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to initiate Razorpay order")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error creating Razorpay order"))
        }
    }

    override suspend fun verifyRazorpayPayment(
        feeId: String,
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String?,
        amountPaid: Double?
    ): Resource<Payment> = withContext(Dispatchers.IO) {
        try {
            val request = VerifyRazorpayPaymentRequestDto(
                feeId = feeId,
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = razorpayPaymentId,
                razorpaySignature = razorpaySignature,
                amountPaid = amountPaid
            )
            val response = feePaymentApi.verifyRazorpayPayment(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Payment verification failed")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error verifying payment with server"))
        }
    }

    override suspend fun recordPaymentFailure(
        feeId: String,
        razorpayOrderId: String?,
        razorpayPaymentId: String?,
        errorMessage: String?
    ): Resource<Payment> = withContext(Dispatchers.IO) {
        try {
            val request = RecordPaymentFailureRequestDto(
                feeId = feeId,
                razorpayOrderId = razorpayOrderId,
                razorpayPaymentId = razorpayPaymentId,
                errorMessage = errorMessage
            )
            val response = feePaymentApi.recordPaymentFailure(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error("Failed to record payment cancellation")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error recording failure")
        }
    }

    override suspend fun recordPayment(payment: Payment): Resource<Payment> = withContext(Dispatchers.IO) {
        try {
            val request = RecordPaymentRequestDto(
                feeId = payment.feeId,
                studentId = payment.studentId,
                hostelId = payment.hostelId,
                amountPaid = payment.amountPaid,
                paymentMethod = payment.paymentMethod.name,
                transactionReference = payment.transactionReference,
                remarks = payment.remarks
            )
            val response = feePaymentApi.recordPayment(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to process payment")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error recording payment")
        }
    }

    override suspend fun createFee(fee: Fee): Resource<Fee> = withContext(Dispatchers.IO) {
        try {
            val request = CreateFeeRequestDto(
                hostelId = fee.hostelId,
                studentId = fee.studentId,
                roomId = fee.roomId,
                title = fee.title,
                feeType = fee.feeType.name,
                amount = fee.amount,
                dueDate = fee.dueDate,
                billingMonth = fee.billingMonth,
                billingYear = fee.billingYear
            )
            val response = feePaymentApi.createFee(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to create fee")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error creating fee")
        }
    }

    override fun getMyHostelPaymentConfig(): Flow<Resource<com.hostelhub.app.domain.model.HostelPaymentConfig>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getMyHostelPaymentConfig()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to retrieve hostel payment QR")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ErrorParser.parseExceptionMessage(e, "Network error fetching hostel payment config")))
        }
    }.flowOn(Dispatchers.IO)

    override fun getHostelPaymentConfig(hostelId: String): Flow<Resource<com.hostelhub.app.domain.model.HostelPaymentConfig>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getHostelPaymentConfig(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to retrieve hostel payment config")
                emit(Resource.Error(errorMsg))
            }
        } catch (e: Exception) {
            emit(Resource.Error(ErrorParser.parseExceptionMessage(e, "Network error fetching hostel payment config")))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateHostelPaymentConfig(
        hostelId: String,
        paymentAccountId: String?,
        paymentAccountStatus: String?,
        paymentQrUrl: String?,
        qrPaymentEnabled: Boolean?,
        upiId: String?,
        merchantName: String?
    ): Resource<com.hostelhub.app.domain.model.HostelPaymentConfig> = withContext(Dispatchers.IO) {
        try {
            val request = UpdatePaymentConfigRequestDto(
                paymentAccountId = paymentAccountId,
                paymentAccountStatus = paymentAccountStatus,
                paymentQrUrl = paymentQrUrl,
                qrPaymentEnabled = qrPaymentEnabled,
                upiId = upiId,
                merchantName = merchantName
            )
            val response = feePaymentApi.updateHostelPaymentConfig(hostelId, request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to update hostel payment config")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Network error updating payment config"))
        }
    }

    override suspend fun submitManualQrPayment(
        feeId: String,
        amountPaid: Double,
        transactionReference: String,
        remarks: String?,
        receiptUrl: String?
    ): Resource<Payment> = withContext(Dispatchers.IO) {
        try {
            val request = ManualPaymentSubmissionRequestDto(
                feeId = feeId,
                amountPaid = amountPaid,
                transactionReference = transactionReference,
                remarks = remarks,
                receiptUrl = receiptUrl
            )
            val response = feePaymentApi.submitManualQrPayment(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to submit manual payment reference")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error submitting payment reference"))
        }
    }

    override suspend fun verifyManualPayment(
        paymentId: String,
        approved: Boolean,
        remarks: String?
    ): Resource<Payment> = withContext(Dispatchers.IO) {
        try {
            val request = VerifyManualPaymentRequestDto(
                approved = approved,
                remarks = remarks
            )
            val response = feePaymentApi.verifyManualPayment(paymentId, request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to verify manual payment")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error verifying payment"))
        }
    }

    override fun getAdminPaymentOverview(): Flow<Resource<List<com.hostelhub.app.domain.model.AdminPaymentOverviewItem>>> = flow {
        emit(Resource.Loading)
        try {
            val response = feePaymentApi.getAdminPaymentOverview()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to retrieve admin payment overview"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching payment overview"))
        }
    }.flowOn(Dispatchers.IO)
}
