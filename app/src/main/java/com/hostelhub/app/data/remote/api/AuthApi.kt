package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): Response<ApiResponse<AuthResponseDataDto>>

    @POST("auth/validate-student-id")
    suspend fun validateStudentId(@Body request: ValidateStudentIdRequestDto): Response<ApiResponse<ValidateStudentIdResponseDto>>

    @POST("auth/activate-student")
    suspend fun activateStudent(@Body request: ActivateStudentRequestDto): Response<ApiResponse<AuthResponseDataDto>>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<ApiResponse<ForgotPasswordResponseDto>>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<ApiResponse<Map<String, String>>>

    @POST("auth/register/student")
    suspend fun registerStudent(@Body request: RegisterStudentRequestDto): Response<ApiResponse<AuthResponseDataDto>>

    @POST("auth/register/host")
    suspend fun registerHost(@Body request: RegisterHostRequestDto): Response<ApiResponse<AuthResponseDataDto>>

    @POST("auth/register/admin")
    suspend fun registerAdmin(@Body request: RegisterAdminRequestDto): Response<ApiResponse<AuthResponseDataDto>>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): Response<ApiResponse<RefreshTokenResponseDto>>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequestDto): Response<ApiResponse<Unit>>

    @GET("auth/me")
    suspend fun getMe(): Response<ApiResponse<UserDto>>
}

interface UsersApi {
    @GET("users")
    suspend fun getAllUsers(): Response<ApiResponse<List<UserDto>>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<ApiResponse<UserDto>>

    @PATCH("users/{id}/status")
    suspend fun toggleUserStatus(@Path("id") id: String, @Body body: Map<String, Boolean>): Response<ApiResponse<Unit>>
}
