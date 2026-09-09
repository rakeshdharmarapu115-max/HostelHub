package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.ApiResponse
import com.hostelhub.app.data.remote.dto.StorageUploadResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface StorageApi {
    @Multipart
    @POST("storage/payment-qr")
    suspend fun uploadPaymentQr(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<StorageUploadResponseDto>>

    @Multipart
    @POST("storage/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<ApiResponse<StorageUploadResponseDto>>
}
