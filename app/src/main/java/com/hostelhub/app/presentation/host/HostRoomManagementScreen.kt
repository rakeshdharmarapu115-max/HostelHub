package com.hostelhub.app.presentation.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Room
import com.hostelhub.app.domain.model.RoomStatus
import com.hostelhub.app.domain.model.RoomType
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.BackgroundCool
import com.hostelhub.app.presentation.theme.PrimaryNavy
import com.hostelhub.app.presentation.theme.SecondaryTeal
import com.hostelhub.app.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostRoomManagementScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateToRoomDetail: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val filters = listOf("All Rooms", "Available Beds", "Full Rooms", "Maintenance")
    var selectedFilter by remember { mutableStateOf("All Rooms") }
    var showAddRoomModal by remember { mutableStateOf(false) }

    var newRoomNumber by remember { mutableStateOf("") }
    var newFloor by remember { mutableStateOf("1") }
    var newBlock by remember { mutableStateOf("A") }
    var newCapacity by remember { mutableStateOf("2") }
    var newRent by remember { mutableStateOf("450") }

    val roomsState by hostViewModel?.rooms?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val roomsList = (roomsState as? UiState.Success)?.data ?: emptyList()

    val filteredRooms = roomsList.filter {
        when (selectedFilter) {
            "Available Beds" -> it.status == RoomStatus.AVAILABLE
            "Full Rooms" -> it.status == RoomStatus.FULL
            "Maintenance" -> it.status == RoomStatus.MAINTENANCE
            else -> true
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Room Management",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddRoomModal = true },
                containerColor = PrimaryNavy,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Add Room")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Room", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            FilterChipRow(
                items = filters,
                selectedItem = selectedFilter,
                onItemSelected = { selectedFilter = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredRooms.isEmpty()) {
                EmptyStateView(
                    title = "No Rooms Found",
                    message = "No rooms match the selected criteria. You can add a new room using the button below."
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRooms) { room ->
                        AppCard(
                            padding = 14.dp,
                            onClick = { onNavigateToRoomDetail(room.roomId) }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Room ${room.roomNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = PrimaryNavy
                                    )
                                    StatusBadge(
                                        text = room.status.name,
                                        statusType = when (room.status) {
                                            RoomStatus.AVAILABLE -> BadgeStatusType.SUCCESS
                                            RoomStatus.FULL -> BadgeStatusType.INFO
                                            RoomStatus.MAINTENANCE -> BadgeStatusType.ERROR
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Block ${room.block} • Floor ${room.floor} • ${room.roomType.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                val occupancyRatio = if (room.totalCapacity > 0) room.occupiedCount.toFloat() / room.totalCapacity else 0f
                                LinearProgressIndicator(
                                    progress = { occupancyRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp),
                                    color = if (occupancyRatio >= 1f) PrimaryNavy else SecondaryTeal,
                                    trackColor = Color.LightGray.copy(alpha = 0.4f),
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${room.occupiedCount}/${room.totalCapacity} Beds",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${com.hostelhub.app.utils.Formatters.formatCurrencyNoDecimals(room.monthlyRent)}/mo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SecondaryTeal
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Room Bottom Sheet
        if (showAddRoomModal) {
            ModalBottomSheet(
                onDismissRequest = { showAddRoomModal = false },
                sheetState = rememberModalBottomSheetState()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Add New Room",
                        style = MaterialTheme.typography.titleLarge,
                        color = PrimaryNavy
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = newRoomNumber,
                        onValueChange = { newRoomNumber = it },
                        label = "Room Number",
                        placeholder = "e.g. B-205"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppTextField(
                            value = newFloor,
                            onValueChange = { newFloor = it },
                            label = "Floor",
                            placeholder = "1",
                            modifier = Modifier.weight(1f)
                        )
                        AppTextField(
                            value = newBlock,
                            onValueChange = { newBlock = it },
                            label = "Block",
                            placeholder = "A",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AppTextField(
                            value = newCapacity,
                            onValueChange = { newCapacity = it },
                            label = "Bed Capacity",
                            placeholder = "2",
                            modifier = Modifier.weight(1f)
                        )
                        AppTextField(
                            value = newRent,
                            onValueChange = { newRent = it },
                            label = "Monthly Rent ($)",
                            placeholder = "450",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AppButton(
                        text = "Create Room",
                        onClick = {
                            if (newRoomNumber.isNotBlank()) {
                                val cap = newCapacity.toIntOrNull() ?: 2
                                val rent = newRent.toDoubleOrNull() ?: 450.0
                                val fl = newFloor.toIntOrNull() ?: 1
                                val rType = when (cap) {
                                    1 -> RoomType.SINGLE
                                    2 -> RoomType.DOUBLE
                                    3 -> RoomType.TRIPLE
                                    else -> RoomType.DORMITORY
                                }
                                hostViewModel?.addRoom(
                                    roomNumber = newRoomNumber,
                                    floor = fl,
                                    block = newBlock,
                                    roomType = rType,
                                    capacity = cap,
                                    monthlyRent = rent,
                                    amenities = listOf("High-Speed Wi-Fi", "Air Conditioned", "Attached Bath")
                                )
                                showAddRoomModal = false
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}
