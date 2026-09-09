package com.hostelhub.app.presentation.host

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.hostelhub.app.domain.model.Payment
import com.hostelhub.app.domain.model.PaymentStatus
import com.hostelhub.app.presentation.components.*
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.utils.FormValidators
import com.hostelhub.app.utils.Formatters
import com.hostelhub.app.utils.ImageUtils
import com.hostelhub.app.utils.UiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostPaymentSettingsScreen(
    hostViewModel: HostViewModel? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val currentHostelId by hostViewModel?.currentHostelId?.collectAsState() ?: remember {
        mutableStateOf("")
    }
    val paymentConfigState by hostViewModel?.paymentConfig?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }
    val paymentsState by hostViewModel?.payments?.collectAsState() ?: remember {
        mutableStateOf(UiState.Idle)
    }

    val config = (paymentConfigState as? UiState.Success)?.data
    val allPayments = (paymentsState as? UiState.Success)?.data ?: emptyList()
    val pendingManualPayments = allPayments.filter { it.status == PaymentStatus.PENDING || it.status.name == "PENDING_VERIFICATION" }

    // Form inputs (No hardcoded sample data)
    var upiIdInput by remember(config) { mutableStateOf(config?.upiId ?: "") }
    var merchantNameInput by remember(config) { mutableStateOf(config?.merchantName ?: (config?.hostelName ?: "")) }
    var paymentAccountIdInput by remember(config) { mutableStateOf(config?.paymentAccountId ?: "") }
    var qrPaymentEnabled by remember(config) { mutableStateOf(config?.qrPaymentEnabled ?: true) }
    var currentQrUrl by remember(config) { mutableStateOf(config?.paymentQrUrl ?: "") }

    var isUpdating by remember { mutableStateOf(false) }
    var isUploadingQr by remember { mutableStateOf(false) }
    var showQrUrlDialog by remember { mutableStateOf(false) }
    var manualQrUrlInput by remember { mutableStateOf("") }
    var selectedPaymentForAction by remember { mutableStateOf<Payment?>(null) }
    var actionDialogType by remember { mutableStateOf<String?>(null) } // "APPROVE" or "REJECT"
    var rejectionReasonInput by remember { mutableStateOf("") }

    // Image Picker for QR Code Upload (Real Multipart Upload to Server)
    val qrImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingQr = true
            hostViewModel?.uploadPaymentQr(
                uri = uri,
                context = context,
                onSuccess = { uploadedUrl ->
                    isUploadingQr = false
                    currentQrUrl = uploadedUrl
                    Toast.makeText(context, "Official Payment QR uploaded and saved!", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    isUploadingQr = false
                    Toast.makeText(context, "QR Upload Failed: $err", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    LaunchedEffect(currentHostelId) {
        if (currentHostelId.isNotBlank()) {
            hostViewModel?.loadPaymentConfig(currentHostelId)
            hostViewModel?.loadPayments(currentHostelId)
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Payment Settings & QR",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(HostBackground)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Account Status Banner
            AppCard(
                backgroundColor = Color(0xFF0F291E),
                padding = 18.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(HostAccent.copy(alpha = 0.2f), CircleShape)
                                    .border(1.dp, HostAccent.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = HostAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = config?.hostelName?.ifBlank { "Hostel Hub Property" } ?: "Green Valley Residencies",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Account ID: ${config?.paymentAccountId ?: paymentAccountIdInput}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        StatusBadge(
                            text = if (qrPaymentEnabled) "Active ✓" else "Disabled",
                            statusType = if (qrPaymentEnabled) BadgeStatusType.SUCCESS else BadgeStatusType.ERROR
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Settlement UPI ID",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (!config?.upiId.isNullOrBlank()) config!!.upiId else if (upiIdInput.isNotBlank()) upiIdInput else "Not configured",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Switch(
                            checked = qrPaymentEnabled,
                            onCheckedChange = { checked ->
                                qrPaymentEnabled = checked
                                hostViewModel?.updatePaymentConfig(
                                    hostelId = currentHostelId,
                                    paymentAccountId = paymentAccountIdInput.trim(),
                                    paymentAccountStatus = if (checked) "ACTIVE" else "DISABLED",
                                    paymentQrUrl = currentQrUrl.trim(),
                                    qrPaymentEnabled = checked,
                                    upiId = upiIdInput.trim(),
                                    merchantName = merchantNameInput.trim(),
                                    onSuccess = {
                                        Toast.makeText(context, if (checked) "QR Payments Enabled" else "QR Payments Disabled", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { Toast.makeText(context, "Error: $it", Toast.LENGTH_SHORT).show() }
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HostAccent,
                                checkedTrackColor = HostAccentContainer
                            )
                        )
                    }
                }
            }

            // 2. Official Payment QR Box
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = HostAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Official Payment QR Code",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (currentQrUrl.isNotBlank()) {
                            StatusBadge(
                                text = if (qrPaymentEnabled) "Active ✓" else "Inactive",
                                statusType = if (qrPaymentEnabled) BadgeStatusType.SUCCESS else BadgeStatusType.WARNING
                            )
                        }
                    }

                    if (currentQrUrl.isNotBlank()) {
                        // QR Preview Box with server URL resolution & loading state
                        val resolvedUrl = hostViewModel?.resolveImageUrl(currentQrUrl) ?: currentQrUrl
                        Box(
                            modifier = Modifier
                                .size(220.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(2.dp, HostAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUploadingQr) {
                                CircularProgressIndicator(color = HostAccent, modifier = Modifier.size(36.dp))
                            } else {
                                AsyncImage(
                                    model = resolvedUrl,
                                    contentDescription = "Hostel Payment QR Code",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Text(
                            text = "Scan with any UPI app to pay hostel fees directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Action Buttons: Update QR / Replace
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { qrImagePickerLauncher.launch("image/*") },
                                enabled = !isUploadingQr,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = HostAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                if (isUploadingQr) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Uploading...")
                                } else {
                                    Icon(Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Replace QR", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }

                            OutlinedButton(
                                onClick = { showQrUrlDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("QR URL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    } else {
                        // Empty State: No QR configured
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(Color.White, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(32.dp))
                                }
                                Text(
                                    text = "Payment QR Not Configured",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Upload your official UPI QR code image from your gallery so residents can scan and pay dues seamlessly.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                Button(
                                    onClick = { qrImagePickerLauncher.launch("image/*") },
                                    enabled = !isUploadingQr,
                                    colors = ButtonDefaults.buttonColors(containerColor = HostAccent),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    if (isUploadingQr) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Uploading...")
                                    } else {
                                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Upload Payment QR", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Payment Account Details & UPI Configuration Form
            AppCard(padding = 18.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Merchant & UPI Account Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    AppTextField(
                        value = upiIdInput,
                        onValueChange = { upiIdInput = it },
                        label = "Official UPI ID (VPA)",
                        placeholder = "e.g. name@okhdfcbank, 9876543210@upi",
                        leadingIcon = Icons.Default.CurrencyRupee
                    )

                    AppTextField(
                        value = merchantNameInput,
                        onValueChange = { merchantNameInput = it },
                        label = "Business / Merchant Display Name",
                        placeholder = "e.g. Green Valley Residencies",
                        leadingIcon = Icons.Default.Business
                    )

                    AppTextField(
                        value = paymentAccountIdInput,
                        onValueChange = { paymentAccountIdInput = it },
                        label = "Payment Account Identifier",
                        placeholder = "e.g. acc_gv_987654",
                        leadingIcon = Icons.Default.Badge
                    )

                    Button(
                        onClick = {
                            val cleanUpi = upiIdInput.trim()
                            if (cleanUpi.isNotBlank()) {
                                val validation = FormValidators.validateUpiId(cleanUpi)
                                if (!validation.isValid) {
                                    Toast.makeText(context, validation.errorMessage ?: "Invalid UPI ID format", Toast.LENGTH_LONG).show()
                                    return@Button
                                }
                            }

                            isUpdating = true
                            hostViewModel?.updatePaymentConfig(
                                hostelId = currentHostelId,
                                paymentAccountId = paymentAccountIdInput.trim(),
                                paymentAccountStatus = if (qrPaymentEnabled) "ACTIVE" else "DISABLED",
                                paymentQrUrl = currentQrUrl.trim(),
                                qrPaymentEnabled = qrPaymentEnabled,
                                upiId = cleanUpi,
                                merchantName = merchantNameInput.trim(),
                                onSuccess = {
                                    isUpdating = false
                                    Toast.makeText(context, "Payment configuration saved successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onError = { err ->
                                    isUpdating = false
                                    Toast.makeText(context, "Save Error: $err", Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isUpdating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = HostAccent),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isUpdating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...")
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Payment Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Pending Student Manual QR Payments Queue
            Text(
                text = "Manual QR Verification Queue (${pendingManualPayments.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (pendingManualPayments.isEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "All Payment Submissions Cleared",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "When students pay by scanning your static QR and submit their UTR/Ref ID, they will appear here for verification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                pendingManualPayments.forEach { payment ->
                    AppCard(padding = 16.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(TertiaryContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = TertiaryAmber, modifier = Modifier.size(22.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = payment.studentName ?: "Resident Student",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Ref/UTR: ${payment.transactionReference}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = HostAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = Formatters.formatCurrency(payment.amountPaid),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = HostAccent
                                    )
                                    StatusBadge(text = "Awaiting Verification", statusType = BadgeStatusType.WARNING)
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date: ${SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(payment.paymentDate))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = payment.feeTitle ?: "Hostel Fee",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Verification Buttons: Approve vs Reject
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedPaymentForAction = payment
                                        actionDialogType = "REJECT"
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reject", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        selectedPaymentForAction = payment
                                        actionDialogType = "APPROVE"
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve & Mark Paid", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal: QR Image URL Input
    if (showQrUrlDialog) {
        Dialog(onDismissRequest = { showQrUrlDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Enter QR Image URL", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    AppTextField(
                        value = manualQrUrlInput,
                        onValueChange = { manualQrUrlInput = it },
                        label = "Image URL (HTTPS)",
                        placeholder = "https://example.com/my-upi-qr.png"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showQrUrlDialog = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualQrUrlInput.isNotBlank()) {
                                    val cleanUrl = manualQrUrlInput.trim()
                                    currentQrUrl = cleanUrl
                                    hostViewModel?.setDirectPaymentQrUrl(
                                        qrUrl = cleanUrl,
                                        onSuccess = { Toast.makeText(context, "Payment QR URL updated!", Toast.LENGTH_SHORT).show() },
                                        onError = { err -> Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show() }
                                    )
                                }
                                showQrUrlDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HostAccent)
                        ) {
                            Text("Set QR")
                        }
                    }
                }
            }
        }
    }

    // Modal: Approval Confirmation / Rejection Dialog
    selectedPaymentForAction?.let { payment ->
        if (actionDialogType != null) {
            Dialog(onDismissRequest = { selectedPaymentForAction = null; actionDialogType = null }) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = if (actionDialogType == "APPROVE") "Approve Payment of ${Formatters.formatCurrency(payment.amountPaid)}?" else "Reject Payment Submission",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (actionDialogType == "APPROVE")
                                "This will mark the student's fee invoice as PAID and send an official digital receipt notification to ${payment.studentName}."
                            else "Please specify why this payment reference could not be verified (e.g., UTR not received in bank account).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (actionDialogType == "REJECT") {
                            AppTextField(
                                value = rejectionReasonInput,
                                onValueChange = { rejectionReasonInput = it },
                                label = "Reason for Rejection",
                                placeholder = "e.g. Transaction reference not found in bank statement"
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { selectedPaymentForAction = null; actionDialogType = null }) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val isApproved = actionDialogType == "APPROVE"
                                    hostViewModel?.verifyManualPayment(
                                        paymentId = payment.paymentId,
                                        approved = isApproved,
                                        remarks = if (isApproved) "Approved by hostel owner" else rejectionReasonInput.ifBlank { "Invalid transaction reference" },
                                        onSuccess = {
                                            selectedPaymentForAction = null
                                            actionDialogType = null
                                            rejectionReasonInput = ""
                                            Toast.makeText(context, if (isApproved) "Payment approved & verified!" else "Payment rejected.", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, "Error: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (actionDialogType == "APPROVE") StatusSuccess else MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text(if (actionDialogType == "APPROVE") "Confirm & Approve" else "Confirm Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}
