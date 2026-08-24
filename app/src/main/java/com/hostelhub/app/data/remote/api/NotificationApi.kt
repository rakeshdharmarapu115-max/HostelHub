package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.ApiResponse
import com.hostelhub.app.data.remote.dto.AppNotificationDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationApi {
    @GET("notifications")
    suspend fun getNotifications(@Query("userId") userId: String): Response<ApiResponse<List<AppNotificationDto>>>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<ApiResponse<Unit>>
}
