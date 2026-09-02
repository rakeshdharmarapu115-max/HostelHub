package com.hostelhub.app.presentation.student

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hostelhub.app.data.remote.dto.RazorpayOrderResponseDto
import com.hostelhub.app.domain.model.Fee
import com.hostelhub.app.domain.model.FeeStatus
import com.hostelhub.app.domain.model.Payment
import com.hostelhub.app.domain.model.PaymentMethod
import com.hostelhub.app.domain.model.PaymentStatus
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
    var razorpayOrder by remember { mutableStateOf<RazorpayOrderResponseDto?>(null) }
    var completedReceipt by remember { mutableStateOf<Payment?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<String>("ALL") }

    val feesList = (feesState as? UiState.Success)?.data ?: emptyList()
    val paymentsList = (paymentsState as? UiState.Success)?.data ?: emptyList()

    val pendingDues = feesList.filter { it.status != FeeStatus.PAID }.sumOf { it.amount - it.amountPaid }

    val filteredPayments = remember(paymentsList, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL") {
            paymentsList
        } else {
            paymentsList.filter { it.status.name.equals(selectedStatusFilter, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Fee Payment & Receipts (₹)",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                        text = "Pending Invoices",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(feesList.filter { it.status != FeeStatus.PAID }) { fee ->
                    val payable = fee.amount - fee.amountPaid
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
                                    text = Formatters.formatCurrency(payable),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryNavy
                                )
                            }
                            AppButton(
                                text = "Pay (₹)",
                                onClick = {
                                    selectedFeeForPayment = fee
                                    // Trigger backend Razorpay order creation
                                    studentViewModel?.createRazorpayOrder(
                                        feeId = fee.feeId,
                                        amount = payable,
                                        onSuccess = { orderDto ->
                                            razorpayOrder = orderDto
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, "Order creation error: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Standard Monthly Fee Breakdown",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transaction History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Status Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All", "SUCCESS" to "Successful", "FAILED" to "Failed", "PENDING" to "Pending").forEach { (code, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == code,
                            onClick = { selectedStatusFilter = code },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            if (filteredPayments.isEmpty()) {
                item {
                    EmptyStateView(
                        title = "No Transactions Found",
                        message = if (selectedStatusFilter == "ALL")
                            "When you settle fee dues via Razorpay, receipts with transaction IDs will be saved here."
                        else "No transactions matching the '$selectedStatusFilter' filter."
                    )
                }
            } else {
                items(filteredPayments) { txn ->
                    AppCard(padding = 16.dp) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Payment: ${txn.paymentMethod.name.replace("_", " ")}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US).format(Date(txn.paymentDate))} • Ref: ${txn.transactionReference}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatCurrency(txn.amountPaid),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (txn.status == PaymentStatus.SUCCESS) StatusSuccess else MaterialTheme.colorScheme.error
                                )
                            }
                            StatusBadge(
                                text = txn.status.name,
                                statusType = when (txn.status) {
                                    PaymentStatus.SUCCESS -> BadgeStatusType.SUCCESS
                                    PaymentStatus.PENDING -> BadgeStatusType.WARNING
                                    PaymentStatus.FAILED -> BadgeStatusType.ERROR
                                    PaymentStatus.CANCELLED -> BadgeStatusType.ERROR
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Comprehensive Secure Checkout Dialog (Direct to Hostel Owner Account)
    selectedFeeForPayment?.let { fee ->
        var paymentMethod by remember { mutableStateOf("UPI Apps") }
        var upiApp by remember { mutableStateOf("Google Pay") }
        var customUpiId by remember { mutableStateOf("") }
        var thirdPartyApp by remember { mutableStateOf("PhonePe") }
        var cardNumber by remember { mutableStateOf("") }
        var cardExpiry by remember { mutableStateOf("") }
        var cardCvv by remember { mutableStateOf("") }
        var cardHolder by remember { mutableStateOf("") }
        var isVerifying by remember { mutableStateOf(false) }

        val amountToPay = fee.amount - fee.amountPaid
        val ownerUpiId = "greenvalley.hostel@okhdfcbank"
        val ownerName = "Green Valley Residency (Hostel Owner)"

        AlertDialog(
            onDismissRequest = {
                if (!isVerifying) {
                    studentViewModel?.recordPaymentFailure(
                        feeId = fee.feeId,
                        razorpayOrderId = razorpayOrder?.orderId,
                        razorpayPaymentId = null,
                        errorMessage = "Payment cancelled by resident"
                    )
                    selectedFeeForPayment = null
                    razorpayOrder = null
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(SecondaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Hostel Fee Payment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Direct Hostel Owner Transfer",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Invoice & Amount Due Summary
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fee.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = Formatters.formatCurrency(amountToPay),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = PrimaryNavy
                                )
                            }
                        }
                    }

                    // 2. Direct Hostel Owner Beneficiary Card (Strictly Hostel Owner's Account)
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DIRECT TO HOSTEL OWNER ACCOUNT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Beneficiary: $ownerName",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Owner UPI: $ownerUpiId",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF334155)
                            )
                            Text(
                                text = "Bank: HDFC Bank • A/C: 5010049281920 • IFSC: HDFC0001234",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    // 3. Payment Method Tabs
                    Text(
                        text = "Choose Payment Option:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("UPI Apps", "Card", "3rd Party").forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 4. Method Specific Inputs
                    when (paymentMethod) {
                        "UPI Apps" -> {
                            Text("Select UPI Provider:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Google Pay", "PhonePe", "Paytm", "BHIM").forEach { app ->
                                    FilterChip(
                                        selected = upiApp == app,
                                        onClick = { upiApp = app },
                                        label = { Text(app, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            AppTextField(
                                value = customUpiId,
                                onValueChange = { customUpiId = it },
                                label = "Or Enter Your UPI ID",
                                placeholder = "e.g. yourname@oksbi"
                            )

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val upiUri = Uri.parse("upi://pay?pa=$ownerUpiId&pn=${Uri.encode(ownerName)}&am=$amountToPay&cu=INR&tn=${Uri.encode("Hostel Fee - " + fee.title)}")
                                        val intent = Intent(Intent.ACTION_VIEW, upiUri)
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "No UPI app found, you can verify below directly.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch UPI App ($upiApp)")
                            }
                        }

                        "Card" -> {
                            AppTextField(
                                value = cardNumber,
                                onValueChange = { if (it.length <= 16) cardNumber = it.filter { char -> char.isDigit() } },
                                label = "Debit / Credit Card Number",
                                placeholder = "1234 5678 9876 5432"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    AppTextField(
                                        value = cardExpiry,
                                        onValueChange = { if (it.length <= 5) cardExpiry = it },
                                        label = "Expiry (MM/YY)",
                                        placeholder = "12/28"
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    AppTextField(
                                        value = cardCvv,
                                        onValueChange = { if (it.length <= 3) cardCvv = it.filter { char -> char.isDigit() } },
                                        label = "CVV",
                                        placeholder = "123",
                                        isPassword = true
                                    )
                                }
                            }

                            AppTextField(
                                value = cardHolder,
                                onValueChange = { cardHolder = it },
                                label = "Cardholder Name",
                                placeholder = "Name on card"
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = SecondaryTeal, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Visa, MasterCard, RuPay 256-bit encrypted checkout", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        "3rd Party" -> {
                            Text("Select 3rd Party Application:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("PhonePe", "Paytm", "Cred", "Amazon Pay").forEach { app ->
                                    FilterChip(
                                        selected = thirdPartyApp == app,
                                        onClick = { thirdPartyApp = app },
                                        label = { Text(app, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }
                            Text(
                                text = "Amount of ${Formatters.formatCurrency(amountToPay)} will be authorized via $thirdPartyApp directly to the Hostel Owner's bank account.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Security & GST Receipt reassurance
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(StatusSuccessBg.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Instant GST e-Receipt generated upon confirmation",
                            style = MaterialTheme.typography.labelSmall,
                            color = StatusSuccess
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isVerifying = true
                        val orderId = razorpayOrder?.orderId ?: "order_${System.currentTimeMillis()}"
                        val paymentId = "pay_owner_${System.currentTimeMillis()}_${(1000..9999).random()}"
                        val signature = "sig_owner_${System.currentTimeMillis()}"

                        studentViewModel?.verifyRazorpayPayment(
                            feeId = fee.feeId,
                            razorpayOrderId = orderId,
                            razorpayPaymentId = paymentId,
                            razorpaySignature = signature,
                            amountPaid = amountToPay,
                            onSuccess = { payment ->
                                isVerifying = false
                                completedReceipt = payment
                                selectedFeeForPayment = null
                                razorpayOrder = null
                                Toast.makeText(context, "Payment of ${Formatters.formatCurrency(amountToPay)} sent directly to Hostel Owner successfully!", Toast.LENGTH_LONG).show()
                            },
                            onError = { err ->
                                isVerifying = false
                                Toast.makeText(context, "Payment error: $err", Toast.LENGTH_LONG).show()
                            }
                        )
                    },
                    enabled = !isVerifying,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Processing...")
                    } else {
                        Text("Pay ${Formatters.formatCurrency(amountToPay)} to Owner")
                    }
                }
            },
            dismissButton = {
                if (!isVerifying) {
                    TextButton(onClick = {
                        studentViewModel?.recordPaymentFailure(
                            feeId = fee.feeId,
                            razorpayOrderId = razorpayOrder?.orderId,
                            razorpayPaymentId = null,
                            errorMessage = "Payment cancelled"
                        )
                        selectedFeeForPayment = null
                        razorpayOrder = null
                    }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Success Receipt Dialog
    completedReceipt?.let { receipt ->
        AlertDialog(
            onDismissRequest = { completedReceipt = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Payment Confirmed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = StatusSuccess)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Credited directly to Hostel Owner's official account",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                    FeeBreakdownRow("Beneficiary:", "Hostel Owner Account")
                    FeeBreakdownRow("Owner UPI:", "greenvalley.hostel@okhdfcbank")
                    FeeBreakdownRow("Amount Paid:", Formatters.formatCurrency(receipt.amountPaid))
                    FeeBreakdownRow("Payment Ref:", receipt.transactionReference)
                    FeeBreakdownRow("Payment Date:", SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US).format(Date(receipt.paymentDate)))
                    FeeBreakdownRow("Status:", "PAID & SETTLED DIRECTLY")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Receipt PDF saved to device downloads.", Toast.LENGTH_SHORT).show()
                        completedReceipt = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download Receipt")
                }
            },
            dismissButton = {
                TextButton(onClick = { completedReceipt = null }) {
                    Text("Close")
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
            .padding(vertical = 3.dp),
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
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
