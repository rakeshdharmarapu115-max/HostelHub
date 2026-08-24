package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.FeePaymentApi
import com.hostelhub.app.data.remote.dto.CreateFeeRequestDto
import com.hostelhub.app.data.remote.dto.RecordPaymentRequestDto
import com.hostelhub.app.domain.model.Fee
import com.hostelhub.app.domain.model.Payment
import com.hostelhub.app.domain.repository.FeePaymentRepository
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
}
