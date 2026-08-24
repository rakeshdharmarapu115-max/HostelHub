package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ComplaintApi {
    @GET("complaints/student/{studentId}")
    suspend fun getComplaintsForStudent(@Path("studentId") studentId: String): Response<ApiResponse<List<ComplaintDto>>>

    @GET("complaints/hostel/{hostelId}")
    suspend fun getComplaintsForHostel(@Path("hostelId") hostelId: String): Response<ApiResponse<List<ComplaintDto>>>

    @GET("complaints")
    suspend fun getAllComplaints(): Response<ApiResponse<List<ComplaintDto>>>

    @GET("complaints/{id}")
    suspend fun getComplaintById(@Path("id") id: String): Response<ApiResponse<ComplaintDto>>

    @POST("complaints")
    suspend fun submitComplaint(@Body request: CreateComplaintRequestDto): Response<ApiResponse<ComplaintDto>>

    @PATCH("complaints/{id}")
    suspend fun updateComplaintStatus(@Path("id") id: String, @Body request: UpdateComplaintStatusRequestDto): Response<ApiResponse<ComplaintDto>>

    @DELETE("complaints/{id}")
    suspend fun deleteComplaint(@Path("id") id: String): Response<ApiResponse<Unit>>
}
