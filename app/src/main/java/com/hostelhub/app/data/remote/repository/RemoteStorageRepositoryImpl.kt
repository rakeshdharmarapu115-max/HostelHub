package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.StorageApi
import com.hostelhub.app.data.remote.dto.StorageUploadResponseDto
import com.hostelhub.app.domain.repository.StorageRepository
import com.hostelhub.app.utils.ErrorParser
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteStorageRepositoryImpl @Inject constructor(
    private val storageApi: StorageApi
) : StorageRepository {

    override suspend fun uploadPaymentQr(filePart: MultipartBody.Part): Resource<StorageUploadResponseDto> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("QR_UPLOAD_DIAGNOSTIC", "QR_UPLOAD_ENDPOINT = storage/payment-qr")
            android.util.Log.d("QR_UPLOAD_DIAGNOSTIC", "QR_UPLOAD_FINAL_URL = https://hostelhub-yp73.onrender.com/api/storage/payment-qr")
            android.util.Log.d("QR_UPLOAD_REQUEST", "Uploading QR code via storage/payment-qr (Method: POST, Part: ${filePart.headers})")
            val response = storageApi.uploadPaymentQr(filePart)
            if (response.isSuccessful && response.body()?.data != null) {
                android.util.Log.d("QR_UPLOAD_REQUEST", "QR code upload successful via storage/payment-qr: ${response.body()!!.data!!.url}")
                Resource.Success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to upload payment QR image")
                android.util.Log.e("QR_UPLOAD_REQUEST", "QR upload failed: $errorMsg (HTTP ${response.code()})")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            android.util.Log.e("QR_UPLOAD_REQUEST", "QR upload network exception: ${e.message}", e)
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error uploading payment QR image"))
        }
    }

    override suspend fun uploadFile(filePart: MultipartBody.Part): Resource<StorageUploadResponseDto> = withContext(Dispatchers.IO) {
        try {
            val response = storageApi.uploadFile(filePart)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!)
            } else {
                val errorMsg = response.body()?.message ?: ErrorParser.parseErrorMessage(response, "Failed to upload file")
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(ErrorParser.parseExceptionMessage(e, "Error uploading file"))
        }
    }
}
