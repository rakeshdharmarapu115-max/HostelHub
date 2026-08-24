package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.DailyMeals
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.BadgeStatusType
import com.hostelhub.app.presentation.components.FilterChipRow
import com.hostelhub.app.presentation.components.StatusBadge
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryContainer
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.utils.UiState

@Composable
fun StudentFoodMenuScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    var selectedDay by remember { mutableStateOf("Monday") }

    val menuState by studentViewModel?.foodMenu?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val foodMenu = (menuState as? UiState.Success)?.data
    val currentDayMeals = foodMenu?.schedule?.get(selectedDay.lowercase()) ?: DailyMeals(
        breakfast = listOf("Poha", "Boiled Eggs / Sprouts", "Masala Chai"),
        lunch = listOf("Steamed Rice", "Dal Tadka", "Paneer Butter Masala", "Curd"),
        snacks = listOf("Veg Samosa", "Ginger Tea"),
        dinner = listOf("Butter Roti", "Mixed Vegetable Curry", "Jeera Rice", "Gulab Jamun")
    )

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Weekly Food Menu",
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
            // Day selection filter chips
            FilterChipRow(
                items = days,
                selectedItem = selectedDay,
                onItemSelected = { selectedDay = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notice Banner
            AppCard(
                backgroundColor = SecondaryContainer.copy(alpha = 0.3f),
                borderColor = SecondaryTeal.copy(alpha = 0.3f),
                padding = 14.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = foodMenu?.specialNotice ?: "Sunday Special Feast: Served between 12:30 PM and 3:00 PM.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Meals Schedule for Selected Day
            Text(
                text = "$selectedDay's Menu Schedule",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            MealCard(
                mealName = "Breakfast",
                timing = "07:30 AM - 09:30 AM",
                items = currentDayMeals.breakfast,
                isVeg = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            MealCard(
                mealName = "Lunch",
                timing = "12:30 PM - 02:30 PM",
                items = currentDayMeals.lunch,
                isVeg = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            MealCard(
                mealName = "Evening Snacks & Tea",
                timing = "05:00 PM - 06:30 PM",
                items = currentDayMeals.snacks,
                isVeg = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            MealCard(
                mealName = "Dinner",
                timing = "07:30 PM - 09:30 PM",
                items = currentDayMeals.dinner,
                isVeg = true
            )
        }
    }
}

@Composable
private fun MealCard(
    mealName: String,
    timing: String,
    items: List<String>,
    isVeg: Boolean
) {
    AppCard(padding = 16.dp) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = PrimaryNavy,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = mealName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                StatusBadge(
                    text = timing,
                    statusType = BadgeStatusType.INFO
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items.forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.titleMedium,
                            color = SecondaryTeal,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
