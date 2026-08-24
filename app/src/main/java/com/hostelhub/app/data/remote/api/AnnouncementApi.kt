package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.AnnouncementDto
import com.hostelhub.app.data.remote.dto.ApiResponse
import com.hostelhub.app.data.remote.dto.CreateAnnouncementRequestDto
import retrofit2.Response
import retrofit2.http.*

interface AnnouncementApi {
    @GET("announcements")
    suspend fun getAnnouncements(@Query("hostelId") hostelId: String): Response<ApiResponse<List<AnnouncementDto>>>

    @POST("announcements")
    suspend fun createAnnouncement(@Body request: CreateAnnouncementRequestDto): Response<ApiResponse<AnnouncementDto>>

    @DELETE("announcements/{id}")
    suspend fun deleteAnnouncement(@Path("id") id: String): Response<ApiResponse<Unit>>
}
