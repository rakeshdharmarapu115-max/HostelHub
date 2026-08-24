package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.ApiResponse
import com.hostelhub.app.data.remote.dto.StudentDashboardStatsDto
import com.hostelhub.app.data.remote.dto.StudentDto
import retrofit2.Response
import retrofit2.http.*

interface StudentApi {
    @GET("students")
    suspend fun getAllStudents(): Response<ApiResponse<List<StudentDto>>>

    @GET("students/{id}")
    suspend fun getStudentById(@Path("id") id: String): Response<ApiResponse<StudentDto>>

    @GET("students/hostel/{hostelId}")
    suspend fun getResidentsByHostel(@Path("hostelId") hostelId: String): Response<ApiResponse<List<StudentDto>>>

    @PATCH("students/{id}")
    suspend fun updateStudentProfile(@Path("id") id: String, @Body student: StudentDto): Response<ApiResponse<StudentDto>>

    @DELETE("students/{id}")
    suspend fun deleteStudent(@Path("id") id: String): Response<ApiResponse<Unit>>

    @GET("dashboard/student")
    suspend fun getStudentDashboardStats(@Query("studentId") studentId: String): Response<ApiResponse<StudentDashboardStatsDto>>
}
