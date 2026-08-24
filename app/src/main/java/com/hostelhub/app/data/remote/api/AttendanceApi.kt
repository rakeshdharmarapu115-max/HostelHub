package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.ApiResponse
import com.hostelhub.app.data.remote.dto.AttendanceRecordDto
import com.hostelhub.app.data.remote.dto.BatchAttendanceRequestDto
import com.hostelhub.app.data.remote.dto.MarkAttendanceRequestDto
import retrofit2.Response
import retrofit2.http.*

interface AttendanceApi {
    @GET("attendance/student/{studentId}")
    suspend fun getAttendanceForStudent(
        @Path("studentId") studentId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<ApiResponse<List<AttendanceRecordDto>>>

    @GET("attendance/hostel/{hostelId}")
    suspend fun getAttendanceForHostel(
        @Path("hostelId") hostelId: String,
        @Query("date") date: String
    ): Response<ApiResponse<List<AttendanceRecordDto>>>

    @POST("attendance")
    suspend fun markAttendance(@Body request: MarkAttendanceRequestDto): Response<ApiResponse<AttendanceRecordDto>>

    @POST("attendance/batch")
    suspend fun markBatchAttendance(@Body request: BatchAttendanceRequestDto): Response<ApiResponse<List<AttendanceRecordDto>>>
}
