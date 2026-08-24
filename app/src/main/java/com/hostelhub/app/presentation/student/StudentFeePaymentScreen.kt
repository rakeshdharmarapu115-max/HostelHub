package com.hostelhub.app.presentation.student

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Payment
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
import com.hostelhub.app.domain.model.Payment
import com.hostelhub.app.domain.model.PaymentMethod
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StudentFeePaymentScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val feesState by studentViewModel?.fees?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val paymentsState by studentViewModel?.payments?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    var selectedFeeForPayment by remember { mutableStateOf<Fee?>(null) }

    val feesList = (feesState as? UiState.Success)?.data ?: emptyList()
    val paymentsList = (paymentsState as? UiState.Success)?.data ?: emptyList()

    val pendingDues = feesList.filter { it.status != FeeStatus.PAID }.sumOf { it.amount - it.amountPaid }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Fee Payment Desk (₹)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundCool)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Outstanding Balance Banner Card
                AppCard(
                    backgroundColor = PrimaryNavy,
                    padding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Pending Dues",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (pendingDues > 0) Formatters.formatCurrency(pendingDues) else "₹0.00",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (pendingDues > 0) "Immediate payment required before invoice due date." else "All hostel & mess dues are fully settled.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadge(
                            text = if (pendingDues > 0) "Payment Due" else "All Cleared",
                            statusType = if (pendingDues > 0) BadgeStatusType.WARNING else BadgeStatusType.SUCCESS,
                            customBgColor = SecondaryContainer,
                            customTextColor = PrimaryNavy
                        )
                    }
                }
            }

            if (pendingDues > 0) {
                item {
                    Text(
                        text = "Pending Invoices Issued by Warden",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(feesList.filter { it.status != FeeStatus.PAID }) { fee ->
                    AppCard(padding = 16.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fee.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Due: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(fee.dueDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatCurrency(fee.amount - fee.amountPaid),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                            }
                            AppButton(
                                text = "Pay (₹)",
                                onClick = { selectedFeeForPayment = fee },
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }
                }
            }

            item {
                // Monthly Billing Breakdown
                Text(
                    text = "Standard Tariff & Charges",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            item {
                AppCard(padding = 16.dp) {
                    FeeBreakdownRow("Room Rent (Double AC Sharing)", "₹4,500.00 / mo")
                    FeeBreakdownRow("Full 4-Meal Mess Plan", "₹2,500.00 / mo")
                    FeeBreakdownRow("Wi-Fi & Electricity Allowance", "Included")
                    FeeBreakdownRow("Refundable Caution Deposit", "₹3,000.00 (One-Time)")
                }
            }

            item {
                Text(
                    text = "Payment Receipts & Transaction History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (paymentsList.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Payment Receipts Yet",
                        message = "When you settle fee dues, receipts with UPI transaction IDs will be saved here."
                    )
                }
            } else {
                items(paymentsList) { txn ->
                    AppCard(padding = 16.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Hostel Fee Payment (${txn.paymentMethod.name})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US).format(Date(txn.paymentDate))} • ${txn.transactionReference}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatCurrency(txn.amountPaid),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = StatusSuccess
                                )
                            }
                            StatusBadge(
                                text = txn.status.name,
                                statusType = BadgeStatusType.SUCCESS
                            )
                        }
                    }
                }
            }
        }
    }

    // Interactive Payment Processing Dialog
    selectedFeeForPayment?.let { fee ->
        var paymentMethod by remember { mutableStateOf(PaymentMethod.UPI) }
        var upiId by remember { mutableStateOf("student@okhdfcbank") }
        var isProcessing by remember { mutableStateOf(false) }

        val amountToPay = fee.amount - fee.amountPaid

        AlertDialog(
            onDismissRequest = { if (!isProcessing) selectedFeeForPayment = null },
            title = {
                Text(
                    text = "Complete Fee Payment",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Invoice: ${fee.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryNavy
                    )
                    Text(
                        text = "Total Payable Amount: ${Formatters.formatCurrency(amountToPay)}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = StatusSuccess
                    )

                    Text("Select Payment Gateway:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(PaymentMethod.UPI, PaymentMethod.CARD, PaymentMethod.BANK_TRANSFER, PaymentMethod.CASH).forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method.name.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    if (paymentMethod == PaymentMethod.UPI) {
                        AppTextField(
                            value = upiId,
                            onValueChange = { upiId = it },
                            label = "Virtual Payment Address (VPA)",
                            placeholder = "username@bank"
                        )
                    }

                    Surface(
                        color = PrimaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Payment, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Instant transaction receipt will be generated and saved in both Student and Hostel Owner portals.",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryNavy
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isProcessing = true
                        studentViewModel?.payFee(
                            feeId = fee.feeId,
                            amount = amountToPay,
                            paymentMethod = paymentMethod,
                            onSuccess = {
                                isProcessing = false
                                Toast.makeText(context, "Payment of ${Formatters.formatCurrency(amountToPay)} successful! Receipt saved.", Toast.LENGTH_LONG).show()
                                selectedFeeForPayment = null
                            },
                            onError = { err ->
                                isProcessing = false
                                Toast.makeText(context, "Payment Failed: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isProcessing,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Processing...")
                    } else {
                        Text("Pay ${Formatters.formatCurrency(amountToPay)}")
                    }
                }
            },
            dismissButton = {
                if (!isProcessing) {
                    TextButton(onClick = { selectedFeeForPayment = null }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }
}

@Composable
private fun FeeBreakdownRow(item: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = item,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
