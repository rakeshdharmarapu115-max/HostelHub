package com.hostelhub.app.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.ComplaintCategory
import com.hostelhub.app.domain.model.ComplaintUrgency
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.FilterChipRow
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.utils.FormValidators

@Composable
fun NewComplaintScreen(
    studentViewModel: StudentViewModel? = null,
    onSubmitSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val categories = ComplaintCategory.entries.map { it.name.replace("_", " ") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedUrgency by remember { mutableStateOf(ComplaintUrgency.MEDIUM) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var roomNumber by remember { mutableStateOf("A-204") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "New Maintenance Request",
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
            AppCard(padding = 20.dp) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterChipRow(
                    items = categories,
                    selectedItem = selectedCategory,
                    onItemSelected = { selectedCategory = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = null
                    },
                    label = "Complaint Title",
                    placeholder = "e.g. Electrical outlet sparking",
                    errorMessage = titleError
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = "Room & Wing",
                    placeholder = "e.g. A-204"
                )

                Spacer(modifier = Modifier.height(14.dp))

                AppTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        descriptionError = null
                    },
                    label = "Description of Problem",
                    placeholder = "Provide details about the issue, location, and when it started occurring.",
                    singleLine = false,
                    errorMessage = descriptionError
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Urgency Level",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ComplaintUrgency.entries.forEach { urgency ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedUrgency == urgency,
                                onClick = { selectedUrgency = urgency },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryNavy)
                            )
                            Text(
                                text = urgency.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppButton(
                    text = "Submit Complaint",
                    onClick = {
                        val titleVal = FormValidators.validateRequired(title, "Title")
                        val descVal = FormValidators.validateRequired(description, "Description")

                        if (!titleVal.isValid) titleError = titleVal.errorMessage
                        if (!descVal.isValid) descriptionError = descVal.errorMessage

                        if (titleVal.isValid && descVal.isValid) {
                            val categoryEnum = try {
                                ComplaintCategory.valueOf(selectedCategory.replace(" ", "_"))
                            } catch (e: Exception) {
                                ComplaintCategory.OTHER
                            }
                            if (studentViewModel != null) {
                                studentViewModel.submitComplaint(
                                    title = title,
                                    category = categoryEnum,
                                    description = description,
                                    urgency = selectedUrgency,
                                    onSuccess = onSubmitSuccess
                                )
                            } else {
                                onSubmitSuccess()
                            }
                        }
                    }
                )
            }
        }
    }
}
