package com.hostelhub.app.presentation.host

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.DailyMeals
import com.hostelhub.app.domain.model.FoodMenu
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.FilterChipRow
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.utils.UiState

@Composable
fun HostFoodMenuAdminScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDay by remember { mutableStateOf("Monday") }

    val currentHostelId by hostViewModel?.currentHostelId?.collectAsState() ?: remember { mutableStateOf("") }

    LaunchedEffect(currentHostelId) {
        if (currentHostelId.isNotBlank()) {
            hostViewModel?.loadFoodMenu(currentHostelId)
        } else {
            hostViewModel?.loadFoodMenu("hostel_001")
        }
    }

    val menuState by hostViewModel?.foodMenu?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val foodMenu = (menuState as? UiState.Success)?.data

    var breakfastItems by remember { mutableStateOf("Poha, Boiled Eggs / Sprouts, Masala Chai, Filter Coffee") }
    var lunchItems by remember { mutableStateOf("Steamed Basmati Rice, Dal Tadka, Paneer Butter Masala, Curd, Salad") }
    var snacksItems by remember { mutableStateOf("Vegetable Samosa, Masala Chai, Biscuits") }
    var dinnerItems by remember { mutableStateOf("Butter Roti, Mixed Veg Curry, Jeera Rice, Gulab Jamun") }
    var specialNotice by remember { mutableStateOf("Sunday Special Feast served between 12:30 PM and 3:00 PM.") }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(foodMenu, selectedDay) {
        val dayMeals = foodMenu?.schedule?.get(selectedDay.lowercase())
        if (dayMeals != null) {
            breakfastItems = dayMeals.breakfast.joinToString(", ")
            lunchItems = dayMeals.lunch.joinToString(", ")
            snacksItems = dayMeals.snacks.joinToString(", ")
            dinnerItems = dayMeals.dinner.joinToString(", ")
        }
        if (foodMenu?.specialNotice != null) {
            specialNotice = foodMenu.specialNotice
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Weekly Food Menu Editor",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            FilterChipRow(
                items = days,
                selectedItem = selectedDay,
                onItemSelected = { selectedDay = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppCard(padding = 18.dp) {
                Text(
                    text = "Edit $selectedDay Meals",
                    style = MaterialTheme.typography.titleMedium,
                    color = PrimaryNavy
                )
                Spacer(modifier = Modifier.height(14.dp))

                AppTextField(
                    value = breakfastItems,
                    onValueChange = { breakfastItems = it },
                    label = "Breakfast Menu (7:30 AM - 9:30 AM)",
                    placeholder = "Comma separated items",
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = lunchItems,
                    onValueChange = { lunchItems = it },
                    label = "Lunch Menu (12:30 PM - 2:30 PM)",
                    placeholder = "Comma separated items",
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = snacksItems,
                    onValueChange = { snacksItems = it },
                    label = "Evening Snacks (5:00 PM - 6:00 PM)",
                    placeholder = "Comma separated items",
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = dinnerItems,
                    onValueChange = { dinnerItems = it },
                    label = "Dinner Menu (7:30 PM - 9:30 PM)",
                    placeholder = "Comma separated items",
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppTextField(
                    value = specialNotice,
                    onValueChange = { specialNotice = it },
                    label = "Special Catering Notice",
                    placeholder = "e.g. Sunday feast timings",
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppButton(
                        text = "Publish Menu to Residents",
                        isLoading = isSaving,
                        onClick = {
                            isSaving = true
                            val currentSchedule = foodMenu?.schedule?.toMutableMap() ?: mutableMapOf()
                            currentSchedule[selectedDay.lowercase()] = DailyMeals(
                                breakfast = breakfastItems.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                lunch = lunchItems.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                snacks = snacksItems.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                dinner = dinnerItems.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            )
                            val targetHostelId = if (currentHostelId.isNotBlank()) currentHostelId else (foodMenu?.hostelId?.ifBlank { "hostel_001" } ?: "hostel_001")
                            val updatedMenu = FoodMenu(
                                menuId = foodMenu?.menuId ?: "",
                                hostelId = targetHostelId,
                                weekStartDate = foodMenu?.weekStartDate ?: "2026-10-19",
                                schedule = currentSchedule,
                                specialNotice = specialNotice,
                                isPublished = true
                            )
                            hostViewModel?.updateFoodMenu(
                                menu = updatedMenu,
                                onSuccess = {
                                    isSaving = false
                                    Toast.makeText(context, "$selectedDay Menu saved and published!", Toast.LENGTH_SHORT).show()
                                    onNavigateBack()
                                },
                                onError = { errorMsg ->
                                    isSaving = false
                                    Toast.makeText(context, "Error saving menu: $errorMsg", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
