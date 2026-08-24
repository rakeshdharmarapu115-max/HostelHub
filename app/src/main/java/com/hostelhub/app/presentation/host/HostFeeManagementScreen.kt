package com.hostelhub.app.presentation.host

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hostelhub.app.domain.model.Fee
import com.hostelhub.app.domain.model.FeeStatus
import com.hostelhub.app.domain.model.FeeType
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostFeeManagementScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Invoices, 1: Payment Transactions
    val filters = listOf("All Dues", "Pending Only", "Settled")
    var selectedFilter by remember { mutableStateOf("All Dues") }
    var showCreateFeeModal by remember { mutableStateOf(false) }

    val feesState by hostViewModel?.fees?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val paymentsState by hostViewModel?.payments?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val residentsState by hostViewModel?.residents?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val feesList = (feesState as? UiState.Success)?.data ?: emptyList()
    val paymentsList = (paymentsState as? UiState.Success)?.data ?: emptyList()
    val residentsList = (residentsState as? UiState.Success)?.data ?: emptyList()

    val totalCollected = paymentsList.sumOf { it.amountPaid }.let { if (it > 0) it else feesList.sumOf { f -> f.amountPaid } }
    val totalPending = feesList.filter { it.status != FeeStatus.PAID }.sumOf { it.amount - it.amountPaid }

    val filteredFees = feesList.filter { fee ->
        when (selectedFilter) {
            "Pending Only" -> fee.status != FeeStatus.PAID
            "Settled" -> fee.status == FeeStatus.PAID
            else -> true
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Fee Management & Ledger",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateFeeModal = true },
                containerColor = PrimaryNavy,
                contentColor = Color.White
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = "Create Invoice")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Issue Invoice", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Collection Summary Cards in Indian Rupees (₹)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricStatCard(
                    title = "Total Collected (₹)",
                    value = Formatters.formatCurrencyNoDecimals(totalCollected),
                    icon = Icons.Default.Payment,
                    subtitle = "Fee Collections to Date",
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Pending Dues (₹)",
                    value = Formatters.formatCurrencyNoDecimals(totalPending),
                    icon = Icons.Default.Receipt,
                    subtitle = "${feesList.count { it.status != FeeStatus.PAID }} Invoices Due",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Navigation Tabs: Invoices vs Payment History
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryNavy
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Fee Invoices (${feesList.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Payment Receipts (${paymentsList.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                FilterChipRow(
                    items = filters,
                    selectedItem = selectedFilter,
                    onItemSelected = { selectedFilter = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (filteredFees.isEmpty()) {
                    EmptyStateView(
                        title = "No Invoices in this Category",
                        message = "Tap 'Issue Invoice' below to bill a student for room rent or mess charges."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredFees) { fee ->
                            AppCard(padding = 16.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = fee.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (fee.roomId.isNotBlank()) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                StatusBadge(
                                                    text = "Room ${fee.roomId}",
                                                    statusType = BadgeStatusType.INFO
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Resident: ${fee.studentId} • Due: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(fee.dueDate))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "${Formatters.formatCurrency(fee.amount)} (Paid: ${Formatters.formatCurrency(fee.amountPaid)})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryNavy
                                        )
                                    }

                                    StatusBadge(
                                        text = fee.status.name.replace("_", " "),
                                        statusType = when (fee.status) {
                                            FeeStatus.PAID -> BadgeStatusType.SUCCESS
                                            FeeStatus.PENDING -> BadgeStatusType.WARNING
                                            FeeStatus.OVERDUE -> BadgeStatusType.ERROR
                                            FeeStatus.PARTIALLY_PAID -> BadgeStatusType.INFO
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Payment Transactions History Tab
                if (paymentsList.isEmpty()) {
                    EmptyStateView(
                        title = "No Payments Received Yet",
                        message = "When students make payments via UPI or Card, receipts will appear here automatically."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(paymentsList) { payment ->
                            AppCard(padding = 16.dp) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Fee Settlement (${payment.paymentMethod.name})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "Ref: ${payment.transactionReference} • ${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US).format(Date(payment.paymentDate))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = Formatters.formatCurrency(payment.amountPaid),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = StatusSuccess
                                        )
                                    }

                                    StatusBadge(
                                        text = payment.status.name,
                                        statusType = BadgeStatusType.SUCCESS
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to Issue New Fee Invoice to Selected Student
    if (showCreateFeeModal) {
        var selectedStudentId by remember { mutableStateOf(residentsList.firstOrNull()?.studentId ?: "") }
        var invoiceTitle by remember { mutableStateOf("Monthly Room Rent & Mess Fee") }
        var invoiceAmount by remember { mutableStateOf("5500") }
        var selectedFeeType by remember { mutableStateOf(FeeType.RENT) }

        AlertDialog(
            onDismissRequest = { showCreateFeeModal = false },
            title = {
                Text(
                    text = "Issue Fee Invoice to Student",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Resident Student:", style = MaterialTheme.typography.labelMedium)
                    if (residentsList.isNotEmpty()) {
                        residentsList.forEach { student ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedStudentId = student.studentId }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedStudentId == student.studentId,
                                    onClick = { selectedStudentId = student.studentId }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = student.fullName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${student.rollNumber} • Room ${student.roomNumber ?: "Unassigned"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        AppTextField(
                            value = selectedStudentId,
                            onValueChange = { selectedStudentId = it },
                            label = "Student Roll / ID",
                            placeholder = "e.g. 24248-cs-093"
                        )
                    }

                    AppTextField(
                        value = invoiceTitle,
                        onValueChange = { invoiceTitle = it },
                        label = "Invoice Title",
                        placeholder = "e.g. Monthly Room & Mess Fee"
                    )

                    AppTextField(
                        value = invoiceAmount,
                        onValueChange = { invoiceAmount = it },
                        label = "Fee Amount in Rupees (₹)",
                        placeholder = "e.g. 5500"
                    )

                    Text("Fee Category:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(FeeType.RENT, FeeType.MESS, FeeType.ELECTRICITY, FeeType.OTHER).forEach { type ->
                            FilterChip(
                                selected = selectedFeeType == type,
                                onClick = { selectedFeeType = type },
                                label = { Text(type.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = invoiceAmount.toDoubleOrNull() ?: 0.0
                        if (selectedStudentId.isBlank() || amt <= 0) {
                            Toast.makeText(context, "Please select student and enter a valid fee amount", Toast.LENGTH_SHORT).show()
                        } else {
                            hostViewModel?.createFeeInvoice(
                                studentId = selectedStudentId,
                                title = invoiceTitle,
                                amount = amt,
                                feeType = selectedFeeType,
                                onSuccess = {
                                    Toast.makeText(context, "Fee Invoice issued successfully to student!", Toast.LENGTH_LONG).show()
                                    showCreateFeeModal = false
                                },
                                onError = { err ->
                                    Toast.makeText(context, "Error: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Issue Invoice (₹)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateFeeModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
