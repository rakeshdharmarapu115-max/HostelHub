package com.hostelhub.app.domain.model

data class FoodMenu(
    val menuId: String = "",
    val hostelId: String = "",
    val weekStartDate: String = "", // YYYY-MM-DD
    val schedule: Map<String, DailyMeals> = emptyMap(),
    val specialNotice: String? = null,
    val isPublished: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

data class DailyMeals(
    val breakfast: List<String> = emptyList(),
    val lunch: List<String> = emptyList(),
    val snacks: List<String> = emptyList(),
    val dinner: List<String> = emptyList()
)
