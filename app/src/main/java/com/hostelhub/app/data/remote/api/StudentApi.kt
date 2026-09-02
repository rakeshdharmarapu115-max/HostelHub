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

    @GET("students/generate-id")
    suspend fun generateStudentId(): Response<ApiResponse<Map<String, String>>>

    @POST("students/admin-create")
    suspend fun createStudentByAdmin(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse<Map<String, Any>>>

    @POST("students")
    suspend fun createStudentDirect(@Body request: Map<String, @JvmSuppressWildcards Any>): Response<ApiResponse<Map<String, Any>>>

    @POST("students/{id}/deallocate")
    suspend fun deallocateStudent(@Path("id") id: String, @Body body: Map<String, String> = emptyMap()): Response<ApiResponse<StudentDto>>

    @GET("dashboard/student")
    suspend fun getStudentDashboardStats(@Query("studentId") studentId: String): Response<ApiResponse<StudentDashboardStatsDto>>
}
