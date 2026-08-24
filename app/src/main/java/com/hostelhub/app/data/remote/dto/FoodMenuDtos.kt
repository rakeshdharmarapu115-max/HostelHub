package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.DailyMeals
import com.hostelhub.app.domain.model.FoodMenu

data class FoodMenuDto(
    @SerializedName("menuId") val menuId: String = "",
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("weekStartDate") val weekStartDate: String = "",
    @SerializedName("schedule") val schedule: Map<String, DailyMealsDto>? = null,
    @SerializedName("specialNotice") val specialNotice: String? = null,
    @SerializedName("isPublished") val isPublished: Boolean = true,
    @SerializedName("updatedAt") val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): FoodMenu {
        val convertedSchedule = schedule?.mapValues { entry ->
            DailyMeals(
                breakfast = entry.value.breakfast ?: emptyList(),
                lunch = entry.value.lunch ?: emptyList(),
                snacks = entry.value.snacks ?: emptyList(),
                dinner = entry.value.dinner ?: emptyList()
            )
        } ?: emptyMap()

        return FoodMenu(
            menuId = menuId,
            hostelId = hostelId,
            weekStartDate = weekStartDate,
            schedule = convertedSchedule,
            specialNotice = specialNotice,
            isPublished = isPublished,
            updatedAt = updatedAt
        )
    }
}

data class DailyMealsDto(
    @SerializedName("breakfast") val breakfast: List<String>? = emptyList(),
    @SerializedName("lunch") val lunch: List<String>? = emptyList(),
    @SerializedName("snacks") val snacks: List<String>? = emptyList(),
    @SerializedName("dinner") val dinner: List<String>? = emptyList()
)

data class UpdateFoodMenuRequestDto(
    @SerializedName("hostelId") val hostelId: String,
    @SerializedName("weekStartDate") val weekStartDate: String,
    @SerializedName("schedule") val schedule: Map<String, DailyMeals>,
    @SerializedName("specialNotice") val specialNotice: String? = null,
    @SerializedName("isPublished") val isPublished: Boolean = true
)
