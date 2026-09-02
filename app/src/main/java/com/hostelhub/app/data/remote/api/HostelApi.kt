package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface HostelApi {
    @GET("hostels/search")
    suspend fun searchNearbyHostels(
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radius") radius: Double? = null,
        @Query("city") city: String? = null,
        @Query("gender") gender: String? = null,
        @Query("query") query: String? = null,
        @Query("minRent") minRent: Double? = null,
        @Query("maxRent") maxRent: Double? = null
    ): Response<ApiResponse<List<HostelDto>>>

    @PUT("hostels/{id}/location")
    suspend fun updateHostelLocation(
        @Path("id") id: String,
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Response<ApiResponse<HostelDto>>

    @GET("hostels")
    suspend fun getHostels(
        @Query("city") city: String? = null,
        @Query("gender") gender: String? = null,
        @Query("minRent") minRent: Double? = null,
        @Query("maxRent") maxRent: Double? = null
    ): Response<ApiResponse<List<HostelDto>>>

    @GET("hostels/{id}")
    suspend fun getHostelById(@Path("id") id: String): Response<ApiResponse<HostelDto>>

    @POST("hostels")
    suspend fun createHostel(@Body hostel: HostelDto): Response<ApiResponse<HostelDto>>

    @PATCH("hostels/{id}")
    suspend fun updateHostel(@Path("id") id: String, @Body hostel: HostelDto): Response<ApiResponse<HostelDto>>

    @POST("hostels/{id}/images")
    suspend fun addHostelImages(@Path("id") id: String, @Body request: AddHostelImagesRequestDto): Response<ApiResponse<HostelDto>>

    @POST("hostels/{id}/reviews")
    suspend fun addReview(@Path("id") id: String, @Body request: CreateReviewRequestDto): Response<ApiResponse<HostelReviewDto>>

    @GET("hostels/{id}/reviews")
    suspend fun getReviews(@Path("id") id: String): Response<ApiResponse<List<HostelReviewDto>>>

    @DELETE("hostels/{id}")
    suspend fun deleteHostel(@Path("id") id: String): Response<ApiResponse<Unit>>

    @GET("dashboard/host")
    suspend fun getHostDashboardStats(@Query("hostelId") hostelId: String): Response<ApiResponse<HostDashboardStatsDto>>

    @GET("dashboard/admin")
    suspend fun getAdminDashboardStats(): Response<ApiResponse<AdminDashboardStatsDto>>
}
