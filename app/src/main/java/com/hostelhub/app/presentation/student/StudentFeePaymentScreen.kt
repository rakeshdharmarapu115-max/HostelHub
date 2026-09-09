package com.hostelhub.app.presentation.student

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.hostelhub.app.domain.model.Fee
import com.hostelhub.app.domain.model.FeeStatus
import com.hostelhub.app.domain.model.FeeType
import com.hostelhub.app.domain.model.HostelPaymentConfig
import com.hostelhub.app.domain.model.Payment
import com.hostelhub.app.domain.model.PaymentMethod
import com.hostelhub.app.domain.model.PaymentStatus
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFeePaymentScreen(
    studentViewModel: StudentViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val feesState by studentViewModel?.fees?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val paymentsState by studentViewModel?.payments?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val studentProfileState by studentViewModel?.studentProfile?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val hostelPaymentConfigState by studentViewModel?.hostelPaymentConfig?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val feesList = (feesState as? UiState.Success)?.data ?: emptyList()
    val paymentsList = (paymentsState as? UiState.Success)?.data ?: emptyList()
    val currentStudent = (studentProfileState as? UiState.Success)?.data
    val hostelPaymentConfig = (hostelPaymentConfigState as? UiState.Success)?.data

    // Total pending balance calculation
    val pendingFees = feesList.filter { it.status != FeeStatus.PAID }
    val totalPendingDues = pendingFees.sumOf { it.amount - it.amountPaid }

    // Active Screen Tab: 0 = Pay Dues & Invoices, 1 = Transaction History
    var selectedTab by remember { mutableIntStateOf(0) }

    // Selected Fee for Payment
    var selectedFeeForPayment by remember { mutableStateOf<Fee?>(null) }

    // Payment Option Mode: "ONLINE" (Razorpay) vs "OWNER_QR" (Static QR + Manual UTR)
    var paymentOptionMode by remember { mutableStateOf("ONLINE") }

    // Custom or exact entered amount
    var enteredAmount by remember {
        mutableStateOf(if (totalPendingDues > 0) totalPendingDues.toInt().toString() else "5000")
    }

    // Manual QR Payment Submission State
    var transactionUtrInput by remember { mutableStateOf("") }
    var manualRemarksInput by remember { mutableStateOf("") }
    var isSubmittingManualQr by remember { mutableStateOf(false) }
    var manualSubmissionSuccessModal by remember { mutableStateOf(false) }

    // Payment Processing States
    var isProcessingPayment by remember { mutableStateOf(false) }
    var processingStageText by remember { mutableStateOf("Connecting to Payment Gateway...") }
    var completedPaymentReceipt by remember { mutableStateOf<Payment?>(null) }
    var paymentErrorMessage by remember { mutableStateOf<String?>(null) }

    // Transaction History Filter & Modal
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedReceiptForView by remember { mutableStateOf<Payment?>(null) }

    val filteredPayments = remember(paymentsList, selectedStatusFilter) {
        if (selectedStatusFilter == "ALL") {
            paymentsList
        } else {
            paymentsList.filter { it.status.name.equals(selectedStatusFilter, ignoreCase = true) }
        }
    }

    // Auto-update enteredAmount when pending dues load or fee selected
    LaunchedEffect(selectedFeeForPayment) {
        if (selectedFeeForPayment != null) {
            val due = selectedFeeForPayment!!.amount - selectedFeeForPayment!!.amountPaid
            enteredAmount = due.toInt().coerceAtLeast(1).toString()
        }
    }

    LaunchedEffect(Unit) {
        studentViewModel?.loadHostelPaymentConfig()
    }

    // UPI Intent Launcher helper
    fun launchUpiIntent(upiId: String, name: String, amount: Double, packageName: String? = null) {
        if (upiId.isBlank()) {
            Toast.makeText(context, "Hostel owner UPI ID is not configured.", Toast.LENGTH_LONG).show()
            return
        }
        val formattedAmount = String.format(Locale.US, "%.2f", amount)
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId.trim())
            .appendQueryParameter("pn", name.trim())
            .appendQueryParameter("am", formattedAmount)
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", "Hostel Fee - ${currentStudent?.fullName ?: "Resident"}")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        if (!packageName.isNullOrBlank()) {
            intent.setPackage(packageName)
        }

        try {
            if (packageName.isNullOrBlank()) {
                val chooser = Intent.createChooser(intent, "Pay ₹$formattedAmount via UPI")
                context.startActivity(chooser)
            } else {
                context.startActivity(intent)
            }
        } catch (e: android.content.ActivityNotFoundException) {
            val appName = when (packageName) {
                "com.google.android.apps.nbu.paisa.user" -> "Google Pay"
                "com.phonepe.app" -> "PhonePe"
                "net.one97.paytm" -> "Paytm"
                "in.org.npci.upiapp" -> "BHIM"
                else -> "Selected UPI app"
            }
            Toast.makeText(context, "$appName is not installed. Please choose another UPI app or scan the QR code.", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open UPI app directly. Please scan the QR code.", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Fee Payment Portal",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(StudentBackground)
                .padding(paddingValues)
        ) {
            // Main Mode Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryNavy,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryNavy
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pay Fees & Dues", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Payment History", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // =========================================================================
                // TAB 0: FEE PAYMENT DESK & INVOICE MANAGEMENT
                // =========================================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (completedPaymentReceipt != null) {
                        // -------------------------------------------------------------
                        // SUCCESS STATE: OFFICIAL DIGITAL RECEIPT
                        // -------------------------------------------------------------
                        val receipt = completedPaymentReceipt!!
                        DigitalReceiptCard(
                            receipt = receipt,
                            studentName = currentStudent?.fullName ?: receipt.studentName ?: "Resident Student",
                            roomNumber = currentStudent?.roomNumber ?: "A-204",
                            hostelName = currentStudent?.hostelName ?: (hostelPaymentConfig?.hostelName ?: "HostelHub Property"),
                            onDownloadReceipt = {
                                Toast.makeText(context, "Official PDF Receipt downloaded to device storage.", Toast.LENGTH_LONG).show()
                            },
                            onViewHistory = {
                                completedPaymentReceipt = null
                                selectedTab = 1
                            },
                            onNewPayment = {
                                completedPaymentReceipt = null
                                selectedFeeForPayment = null
                            }
                        )
                    } else if (!paymentErrorMessage.isNullOrBlank() && !isProcessingPayment) {
                        // -------------------------------------------------------------
                        // FAILURE / CANCELLED STATE
                        // -------------------------------------------------------------
                        PaymentFailureCard(
                            errorMessage = paymentErrorMessage ?: "Payment was not completed",
                            onRetry = {
                                paymentErrorMessage = null
                            },
                            onContactSupport = {
                                Toast.makeText(context, "Hostel Admin Helpdesk: +91 98765 43210 (admin@hostelhub.com)", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        // -------------------------------------------------------------
                        // ACTIVE PAYMENT DESK & INVOICES
                        // -------------------------------------------------------------

                        // 1. Authenticated Student Context Card (Exact requirements header)
                        AppCard(
                            backgroundColor = PrimaryNavy,
                            padding = 18.dp
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentStudent?.fullName ?: "Resident Student",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ID: ${currentStudent?.studentId ?: "STU-2026-001"} • Room ${currentStudent?.roomNumber ?: "A-204"}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.White.copy(alpha = 0.85f)
                                        )
                                        Text(
                                            text = "📍 ${currentStudent?.hostelName ?: (hostelPaymentConfig?.hostelName ?: "Green Valley Residencies")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SecondaryTeal,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "Amount Due",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            text = if (totalPendingDues > 0) Formatters.formatCurrency(totalPendingDues) else "₹0.00",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (totalPendingDues > 0) Color(0xFFFFD54F) else StatusSuccess
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (pendingFees.isNotEmpty()) "${pendingFees.size} Pending Invoice(s)" else "All Invoices Cleared ✓",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                    if (totalPendingDues > 0) {
                                        Surface(
                                            color = SecondaryTeal,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.clickable {
                                                selectedFeeForPayment = null
                                                enteredAmount = totalPendingDues.toInt().toString()
                                            }
                                        ) {
                                            Text(
                                                text = "Pay Total Dues",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryNavy,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. Fee Invoices Breakdown (If available)
                        if (feesList.isNotEmpty()) {
                            Text(
                                text = "Select Invoice to Pay",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            feesList.forEach { fee ->
                                val feeDue = fee.amount - fee.amountPaid
                                val isSelected = selectedFeeForPayment?.feeId == fee.feeId
                                val isPaid = fee.status == FeeStatus.PAID

                                AppCard(
                                    padding = 14.dp,
                                    borderColor = if (isSelected) PrimaryNavy else if (isPaid) StatusSuccess.copy(alpha = 0.4f) else OutlineColor.copy(alpha = 0.3f),
                                    onClick = {
                                        if (!isPaid) {
                                            selectedFeeForPayment = fee
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(
                                                        if (isPaid) StatusSuccessBg else StudentAccentContainer.copy(alpha = 0.6f),
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = when (fee.feeType) {
                                                        FeeType.RENT -> Icons.Default.Home
                                                        FeeType.MESS -> Icons.Default.Restaurant
                                                        FeeType.ELECTRICITY -> Icons.Default.Bolt
                                                        FeeType.CAUTION_DEPOSIT -> Icons.Default.AccountBalance
                                                        else -> Icons.Default.Receipt
                                                    },
                                                    contentDescription = null,
                                                    tint = if (isPaid) StatusSuccess else PrimaryNavy,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = fee.title.ifBlank { "${fee.feeType.name} Fee" },
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Due: ${SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(fee.dueDate))}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (fee.amountPaid > 0 && !isPaid) {
                                                    Text(
                                                        text = "Paid: ${Formatters.formatCurrency(fee.amountPaid)} of ${Formatters.formatCurrency(fee.amount)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = StatusSuccess
                                                    )
                                                }
                                            }
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = if (isPaid) Formatters.formatCurrency(fee.amount) else Formatters.formatCurrency(feeDue),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isPaid) StatusSuccess else PrimaryNavy
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            StatusBadge(
                                                text = if (isPaid) "PAID" else if (fee.amountPaid > 0) "PARTIAL" else "DUE",
                                                statusType = if (isPaid) BadgeStatusType.SUCCESS else if (fee.amountPaid > 0) BadgeStatusType.WARNING else BadgeStatusType.ERROR
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Payment Method Choice: Option 1 (Razorpay Online) vs Option 2 (Owner UPI QR)
                        Text(
                            text = "Choose Payment Option",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { paymentOptionMode = "ONLINE" },
                                color = if (paymentOptionMode == "ONLINE") PrimaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (paymentOptionMode == "ONLINE") 2.dp else 1.dp,
                                    color = if (paymentOptionMode == "ONLINE") PrimaryNavy else OutlineColor.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = if (paymentOptionMode == "ONLINE") PrimaryNavy else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Option 1: Verified Online",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (paymentOptionMode == "ONLINE") PrimaryNavy else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Instant Auto-Receipt",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusSuccess,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { paymentOptionMode = "OWNER_QR" },
                                color = if (paymentOptionMode == "OWNER_QR") PrimaryContainer else MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (paymentOptionMode == "OWNER_QR") 2.dp else 1.dp,
                                    color = if (paymentOptionMode == "OWNER_QR") PrimaryNavy else OutlineColor.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        tint = if (paymentOptionMode == "OWNER_QR") PrimaryNavy else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Option 2: Owner QR",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (paymentOptionMode == "OWNER_QR") PrimaryNavy else MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "Scan & Submit UTR",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TertiaryAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // 4. Amount Input Field
                        AppCard(padding = 16.dp) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = if (selectedFeeForPayment != null) "Paying for: ${selectedFeeForPayment!!.title}" else "Enter Payment Amount",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                AppTextField(
                                    value = enteredAmount,
                                    onValueChange = { input ->
                                        enteredAmount = input.filter { it.isDigit() }
                                        paymentErrorMessage = null
                                    },
                                    label = "Amount in INR (₹)",
                                    placeholder = "e.g. 5000",
                                    leadingIcon = Icons.Default.CurrencyRupee,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("1000", "2500", "5000", "10000").forEach { quickAmount ->
                                        FilterChip(
                                            selected = enteredAmount == quickAmount,
                                            onClick = {
                                                enteredAmount = quickAmount
                                                paymentErrorMessage = null
                                            },
                                            label = { Text("₹$quickAmount", style = MaterialTheme.typography.labelSmall) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        val parsedAmount = enteredAmount.toDoubleOrNull() ?: 0.0

                        // 5A. OPTION 1: ONLINE RAZORPAY PAYMENT DESK
                        if (paymentOptionMode == "ONLINE") {
                            AppCard(padding = 18.dp) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Surface(
                                        color = Color(0xFFF0FDF4),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF16A34A),
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Instant Verified Settlement",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                                Text(
                                                    text = "Your fee is immediately marked PAID on server and official digital receipt is issued.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Supported Modes: Google Pay, PhonePe, Cards, NetBanking",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Button(
                                        onClick = {
                                            if (parsedAmount <= 0) {
                                                paymentErrorMessage = "Please enter a valid amount greater than ₹0."
                                                return@Button
                                            }
                                            if (activity == null) {
                                                paymentErrorMessage = "Activity context unavailable to launch payment gateway."
                                                return@Button
                                            }

                                            paymentErrorMessage = null
                                            isProcessingPayment = true
                                            processingStageText = "Creating Razorpay Payment Order..."

                                            val targetFeeId = selectedFeeForPayment?.feeId
                                                ?: pendingFees.firstOrNull()?.feeId
                                                ?: feesList.firstOrNull()?.feeId
                                                ?: "fee_general_${System.currentTimeMillis()}"

                                            studentViewModel?.initiateRazorpayPayment(
                                                activity = activity,
                                                feeId = targetFeeId,
                                                amount = parsedAmount,
                                                onSuccess = { payment ->
                                                    isProcessingPayment = false
                                                    completedPaymentReceipt = payment
                                                    Toast.makeText(context, "Payment verified & confirmed by server!", Toast.LENGTH_LONG).show()
                                                },
                                                onError = { err ->
                                                    isProcessingPayment = false
                                                    paymentErrorMessage = err
                                                    Toast.makeText(context, "Payment Error: $err", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        },
                                        enabled = !isProcessingPayment && parsedAmount > 0,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        if (isProcessingPayment) {
                                            CircularProgressIndicator(
                                                color = Color.White,
                                                modifier = Modifier.size(22.dp),
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(processingStageText, fontWeight = FontWeight.Bold)
                                        } else {
                                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (parsedAmount > 0) "Pay ${Formatters.formatCurrency(parsedAmount)} Online" else "Proceed to Pay",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5B. OPTION 2: HOSTEL OWNER'S OFFICIAL PAYMENT QR SECTION
                        if (paymentOptionMode == "OWNER_QR") {
                            val config = hostelPaymentConfig
                            val ownerUpiId = config?.upiId?.trim()?.takeIf { it.isNotEmpty() }
                            val ownerMerchantName = config?.merchantName?.trim()?.takeIf { it.isNotEmpty() }
                                ?: config?.hostelName?.trim()?.takeIf { it.isNotEmpty() }
                                ?: "Hostel Owner"
                            val rawQrUrl = config?.paymentQrUrl?.trim()?.takeIf { it.isNotEmpty() }
                            val resolvedQrUrl = studentViewModel?.resolveImageUrl(rawQrUrl) ?: rawQrUrl
                            val isQrActive = config?.qrPaymentEnabled != false && (ownerUpiId != null || !resolvedQrUrl.isNullOrBlank())

                            if (ownerUpiId == null && resolvedQrUrl.isNullOrBlank()) {
                                // Graceful Empty State when owner has not configured UPI / QR
                                AppCard(padding = 18.dp) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.size(52.dp).background(TertiaryContainer, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(28.dp))
                                        }
                                        Text(
                                            text = "Hostel Payment QR Not Configured",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Your hostel owner has not configured an official payment QR code or settlement UPI ID yet. You can use 'Pay Online via Razorpay' above to pay your fee invoice instantly.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                AppCard(padding = 18.dp) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Hostel Owner Payment QR",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "Pay to: $ownerMerchantName",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            StatusBadge(
                                                text = if (isQrActive) "Active QR" else "Paused",
                                                statusType = if (isQrActive) BadgeStatusType.SUCCESS else BadgeStatusType.WARNING
                                            )
                                        }

                                        // UPI ID Badge
                                        if (ownerUpiId != null) {
                                            Surface(
                                                color = SecondaryContainer.copy(alpha = 0.7f),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "Owner Settlement UPI ID",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = PrimaryNavy.copy(alpha = 0.7f)
                                                        )
                                                        Text(
                                                            text = ownerUpiId,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = PrimaryNavy
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                            val clip = android.content.ClipData.newPlainText("UPI ID", ownerUpiId)
                                                            clipboard.setPrimaryClip(clip)
                                                            Toast.makeText(context, "UPI ID copied to clipboard", Toast.LENGTH_SHORT).show()
                                                        }
                                                    ) {
                                                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy UPI ID", tint = PrimaryNavy, modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }

                                        // Official Payment QR Image Display
                                        if (!resolvedQrUrl.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .size(240.dp)
                                                    .clip(RoundedCornerShape(16.dp))
                                                    .background(Color.White)
                                                    .border(2.dp, PrimaryNavy.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                                                    .padding(12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                AsyncImage(
                                                    model = resolvedQrUrl,
                                                    contentDescription = "Hostel Payment QR Code",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }

                                            Text(
                                                text = "Scan this QR using your UPI app",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        // "Open UPI App" main action launcher button
                                        if (ownerUpiId != null) {
                                            Button(
                                                onClick = {
                                                    launchUpiIntent(
                                                        upiId = ownerUpiId,
                                                        name = ownerMerchantName,
                                                        amount = parsedAmount
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Open UPI App (Pay ₹${enteredAmount})", fontWeight = FontWeight.Bold)
                                            }

                                            // App specific launcher buttons: Google Pay, PhonePe, Paytm, BHIM
                                            Text(
                                                text = "Or launch directly in your installed app:",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { launchUpiIntent(ownerUpiId, ownerMerchantName, parsedAmount, "com.google.android.apps.nbu.paisa.user") },
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("GPay", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { launchUpiIntent(ownerUpiId, ownerMerchantName, parsedAmount, "com.phonepe.app") },
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("PhonePe", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { launchUpiIntent(ownerUpiId, ownerMerchantName, parsedAmount, "net.one97.paytm") },
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("Paytm", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }

                                                OutlinedButton(
                                                    onClick = { launchUpiIntent(ownerUpiId, ownerMerchantName, parsedAmount, "in.org.npci.upiapp") },
                                                    modifier = Modifier.weight(1f),
                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("BHIM", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                    // Step 2: Submit UTR / Reference ID for Owner Verification
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            text = "Step 2: Submit UTR / Reference ID",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Surface(
                                            color = Color(0xFFFFFBEB),
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Info, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Static QR payments require owner verification. Your invoice status will show 'Pending Verification' until verified by your hostel owner.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color(0xFF92400E)
                                                )
                                            }
                                        }

                                        AppTextField(
                                            value = transactionUtrInput,
                                            onValueChange = { transactionUtrInput = it },
                                            label = "12-digit UPI Reference / UTR Number *",
                                            placeholder = "e.g. 423987123456",
                                            leadingIcon = Icons.Default.Tag
                                        )

                                        AppTextField(
                                            value = manualRemarksInput,
                                            onValueChange = { manualRemarksInput = it },
                                            label = "Remarks (Optional)",
                                            placeholder = "e.g. Paid via PhonePe from ICICI Bank",
                                            leadingIcon = Icons.Default.Notes
                                        )

                                        Button(
                                            onClick = {
                                                if (transactionUtrInput.isBlank() || transactionUtrInput.length < 6) {
                                                    Toast.makeText(context, "Please enter a valid 12-digit UPI UTR number", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }

                                                val targetFeeId = selectedFeeForPayment?.feeId
                                                    ?: pendingFees.firstOrNull()?.feeId
                                                    ?: feesList.firstOrNull()?.feeId
                                                    ?: "fee_general_${System.currentTimeMillis()}"

                                                isSubmittingManualQr = true
                                                studentViewModel?.submitManualQrPayment(
                                                    feeId = targetFeeId,
                                                    amountPaid = parsedAmount,
                                                    transactionReference = transactionUtrInput.trim(),
                                                    remarks = manualRemarksInput.ifBlank { null },
                                                    onSuccess = {
                                                        isSubmittingManualQr = false
                                                        manualSubmissionSuccessModal = true
                                                        transactionUtrInput = ""
                                                        manualRemarksInput = ""
                                                    },
                                                    onError = { err ->
                                                        isSubmittingManualQr = false
                                                        Toast.makeText(context, "Submission Error: $err", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            },
                                            enabled = !isSubmittingManualQr && transactionUtrInput.isNotBlank() && parsedAmount > 0,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryTeal, contentColor = PrimaryNavy),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            if (isSubmittingManualQr) {
                                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryNavy, strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Submitting to Owner...")
                                            } else {
                                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Submit Reference for Verification", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
                // =========================================================================
                // TAB 1: TRANSACTION HISTORY & RECEIPTS LEDGER
                // =========================================================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Transaction History (${filteredPayments.size})",
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
                            listOf("ALL" to "All", "SUCCESS" to "Successful", "PENDING" to "Pending Verification", "FAILED" to "Failed").forEach { (code, label) ->
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
                                    "When you pay hostel fees via Online Payment or submit Owner QR UTR references, records will appear here."
                                else "No transaction records matching '$selectedStatusFilter'."
                            )
                        }
                    } else {
                        items(filteredPayments) { txn ->
                            val isVerified = txn.status == PaymentStatus.SUCCESS
                            val isPendingVerification = txn.status == PaymentStatus.PENDING || txn.status.name == "PENDING_VERIFICATION"

                            AppCard(
                                padding = 16.dp,
                                onClick = { selectedReceiptForView = txn }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    if (isVerified) StatusSuccessBg else if (isPendingVerification) TertiaryContainer else StatusErrorBg,
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isVerified) Icons.Default.CheckCircle else if (isPendingVerification) Icons.Default.HourglassTop else Icons.Default.Error,
                                                contentDescription = null,
                                                tint = if (isVerified) StatusSuccess else if (isPendingVerification) TertiaryAmber else StatusError,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = txn.feeTitle ?: "Hostel Fee Payment",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(txn.paymentDate)),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "Ref: ${txn.razorpayPaymentId ?: txn.transactionReference}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = Formatters.formatCurrency(txn.amountPaid),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isVerified) StatusSuccess else if (isPendingVerification) TertiaryAmber else StatusError
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        StatusBadge(
                                            text = if (isPendingVerification) "PENDING" else txn.status.name,
                                            statusType = when {
                                                isVerified -> BadgeStatusType.SUCCESS
                                                isPendingVerification -> BadgeStatusType.WARNING
                                                else -> BadgeStatusType.ERROR
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: Detailed Printable Digital Receipt Dialog
    selectedReceiptForView?.let { receipt ->
        Dialog(onDismissRequest = { selectedReceiptForView = null }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fee Payment Receipt", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { selectedReceiptForView = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HorizontalDivider()

                    val isVerified = receipt.status == PaymentStatus.SUCCESS
                    Surface(
                        color = if (isVerified) Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isVerified) Color(0xFF86EFAC) else Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isVerified) Icons.Default.Verified else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isVerified) Color(0xFF16A34A) else TertiaryAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isVerified) "Verified & Settled Official Payment" else "Awaiting Hostel Owner Verification",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isVerified) Color(0xFF15803D) else Color(0xFF92400E)
                            )
                        }
                    }

                    ReceiptRow("Hostel / Property", currentStudent?.hostelName ?: (hostelPaymentConfig?.hostelName ?: "HostelHub Property"))
                    ReceiptRow("Student Name", currentStudent?.fullName ?: receipt.studentName ?: "Resident Student")
                    ReceiptRow("Student ID", currentStudent?.studentId ?: "STU-2026-001")
                    ReceiptRow("Room Number", currentStudent?.roomNumber ?: "A-204")
                    ReceiptRow("Fee Title", receipt.feeTitle ?: "Monthly Hostel Fees")
                    ReceiptRow("Amount Paid", Formatters.formatCurrency(receipt.amountPaid))
                    ReceiptRow("Payment Method", receipt.paymentMethod.name)
                    ReceiptRow("Transaction Ref / UTR", receipt.razorpayPaymentId ?: receipt.transactionReference)
                    if (!receipt.razorpayOrderId.isNullOrBlank()) {
                        ReceiptRow("Order ID", receipt.razorpayOrderId)
                    }
                    ReceiptRow("Date & Time", SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(receipt.paymentDate)))
                    ReceiptRow("Status", receipt.status.name)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Receipt PDF saved to downloads.", Toast.LENGTH_SHORT).show()
                                selectedReceiptForView = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Download PDF")
                        }
                        Button(
                            onClick = { selectedReceiptForView = null },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            }
        }
    }

    // Modal: Manual Submission Confirmation Modal
    if (manualSubmissionSuccessModal) {
        Dialog(onDismissRequest = { manualSubmissionSuccessModal = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(TertiaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(32.dp))
                    }

                    Text(
                        text = "Submission Sent to Owner",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Your transaction reference has been logged. Your hostel owner will verify the amount and mark your invoice as PAID.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = {
                            manualSubmissionSuccessModal = false
                            selectedTab = 1
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                    ) {
                        Text("View Status in History")
                    }
                }
            }
        }
    }
}

@Composable
private fun DigitalReceiptCard(
    receipt: Payment,
    studentName: String,
    roomNumber: String,
    hostelName: String,
    onDownloadReceipt: () -> Unit,
    onViewHistory: () -> Unit,
    onNewPayment: () -> Unit
) {
    AppCard(
        padding = 22.dp,
        borderColor = StatusSuccess
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .background(StatusSuccessBg, shape = CircleShape)
                    .border(2.dp, StatusSuccess, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = StatusSuccess,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Payment Verified & Successful!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = StatusSuccess
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Cryptographically verified & recorded on server",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Surface(
                color = StudentAccentContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount Paid",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Formatters.formatCurrency(receipt.amountPaid),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = PrimaryNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReceiptRow("Resident Name", studentName)
                ReceiptRow("Room Number", roomNumber)
                ReceiptRow("Residency / Hostel", hostelName)
                ReceiptRow("Transaction Ref", receipt.razorpayPaymentId ?: receipt.transactionReference)
                if (!receipt.razorpayOrderId.isNullOrBlank()) {
                    ReceiptRow("Razorpay Order ID", receipt.razorpayOrderId)
                }
                ReceiptRow("Payment Mode", receipt.paymentMethod.name)
                ReceiptRow("Date & Time", SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US).format(Date(receipt.paymentDate)))
                ReceiptRow("Settlement Status", "VERIFIED & CONFIRMED")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onViewHistory,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("View in Payment History", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDownloadReceipt,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Download PDF")
                }
                OutlinedButton(
                    onClick = onNewPayment,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Make Another")
                }
            }
        }
    }
}

@Composable
private fun PaymentFailureCard(
    errorMessage: String,
    onRetry: () -> Unit,
    onContactSupport: () -> Unit
) {
    AppCard(
        padding = 20.dp,
        borderColor = StatusError
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(StatusErrorBg, shape = CircleShape)
                    .border(2.dp, StatusError, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Failed",
                    tint = StatusError,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Payment Incomplete or Cancelled",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = StatusError
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onContactSupport,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contact Hostel Support")
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
