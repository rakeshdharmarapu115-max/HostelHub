package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RoomApi {
    @GET("rooms/hostel/{hostelId}")
    suspend fun getRoomsByHostel(@Path("hostelId") hostelId: String): Response<ApiResponse<List<RoomDto>>>

    @GET("rooms/{id}")
    suspend fun getRoomById(@Path("id") id: String): Response<ApiResponse<RoomDto>>

    @POST("rooms/hostel/{hostelId}")
    suspend fun addRoom(@Path("hostelId") hostelId: String, @Body room: RoomDto): Response<ApiResponse<RoomDto>>

    @PATCH("rooms/{id}")
    suspend fun updateRoom(@Path("id") id: String, @Body room: RoomDto): Response<ApiResponse<RoomDto>>

    @DELETE("rooms/{id}")
    suspend fun deleteRoom(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("allocations")
    suspend fun assignBed(@Body request: AllocateBedRequestDto): Response<ApiResponse<Any>>

    @POST("allocations/vacate")
    suspend fun vacateBed(@Body request: VacateBedRequestDto): Response<ApiResponse<Unit>>
}
