package com.hostelhub.app.data.remote.api

import com.hostelhub.app.data.remote.dto.ApiResponse
import com.hostelhub.app.data.remote.dto.FoodMenuDto
import com.hostelhub.app.data.remote.dto.UpdateFoodMenuRequestDto
import retrofit2.Response
import retrofit2.http.*

interface FoodMenuApi {
    @GET("food-menu")
    suspend fun getWeeklyMenu(
        @Query("hostelId") hostelId: String,
        @Query("weekStartDate") weekStartDate: String
    ): Response<ApiResponse<FoodMenuDto>>

    @POST("food-menu")
    suspend fun updateWeeklyMenu(@Body request: UpdateFoodMenuRequestDto): Response<ApiResponse<FoodMenuDto>>
}
