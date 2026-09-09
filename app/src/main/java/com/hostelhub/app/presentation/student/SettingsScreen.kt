package com.hostelhub.app.presentation.student

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.hostelhub.app.data.local.AppSettingsManager
import com.hostelhub.app.domain.model.FeeStatus
import com.hostelhub.app.domain.model.User
import com.hostelhub.app.notifications.HostelNotificationManager
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.theme.*
import com.hostelhub.app.security.AppLockManager
import com.hostelhub.app.utils.UiState
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    currentUser: User? = null,
    studentViewModel: StudentViewModel? = null,
    onNavigateToProfile: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appSettingsManager = remember { AppSettingsManager(context.applicationContext) }
    val appLockManager = remember { AppLockManager(context.applicationContext) }
    val hostelNotificationManager = remember { HostelNotificationManager(context.applicationContext, appSettingsManager) }

    LaunchedEffect(currentUser?.userId) {
        appSettingsManager.setUserScope(currentUser?.userId)
    }

    val pushNotifications by appSettingsManager.pushNotifications.collectAsState()
    val feeReminders by appSettingsManager.feeReminders.collectAsState()
    val menuUpdates by appSettingsManager.menuUpdates.collectAsState()
    val emergencyAlerts by appSettingsManager.emergencyAlerts.collectAsState()

    val isAppLockActive by appLockManager.isAppLockEnabled.collectAsState()
    val hasPinSet by appLockManager.hasPinSet.collectAsState()

    var showEditDetailsDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showEmergencyReportDialog by remember { mutableStateOf(false) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var showDisablePinDialog by remember { mutableStateOf(false) }

    // Collapsible Help Centre state (collapsed by default)
    var isFaqSectionExpanded by remember { mutableStateOf(false) }
    var selectedFaqIndex by remember { mutableStateOf<Int?>(null) }

    // User details editable state
    var userFullName by remember { mutableStateOf(currentUser?.fullName ?: "Resident Account") }
    var userPhone by remember { mutableStateOf(currentUser?.phoneNumber ?: "") }
    var userCollege by remember { mutableStateOf("TKR College of Engineering") }
    var userCourse by remember { mutableStateOf("Diploma in Engineering") }
    var userYear by remember { mutableStateOf("1st Year") }
    var userEmergencyPhone by remember { mutableStateOf(currentUser?.phoneNumber ?: "6303299506") }
    var userAddress by remember { mutableStateOf("Campus Hostel Resident, Block A") }

    // Permission state checking
    fun hasDeviceNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    var isNotificationPermissionGranted by remember { mutableStateOf(hasDeviceNotificationPermission()) }
    var pendingNotificationAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Runtime Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificationPermissionGranted = isGranted
        if (isGranted) {
            pendingNotificationAction?.invoke()
            pendingNotificationAction = null
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        } else {
            pendingNotificationAction = null
            Toast.makeText(context, "Notification permission denied. Alerts remain muted.", Toast.LENGTH_LONG).show()
        }
    }

    fun requestNotificationPermissionOrExecute(onGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                isNotificationPermissionGranted = true
                onGranted()
            } else {
                pendingNotificationAction = onGranted
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                isNotificationPermissionGranted = true
                onGranted()
            } else {
                Toast.makeText(context, "Notifications are disabled on your device. Opening system settings...", Toast.LENGTH_LONG).show()
                val intent = Intent(AndroidSettings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(AndroidSettings.EXTRA_APP_PACKAGE, context.packageName)
                }
                context.startActivity(intent)
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings & Preferences",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // 1. Profile & Account Settings Card
            Text(
                text = "Profile & Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            AppCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(PrimaryContainer, shape = CircleShape)
                            .border(1.5.dp, PrimaryNavy.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = PrimaryNavy,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userFullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser?.email ?: "student@campus.edu",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$userCourse • $userYear",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                // Action buttons: Edit Details and Change Password
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showEditDetailsDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Details", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = { showPasswordDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Password", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. Push Notifications & Alerts (REAL FUNCTIONALITY)
            Text(
                text = "Push Notifications & Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            AppCard(padding = 16.dp) {
                val isPushActive = isNotificationPermissionGranted && pushNotifications

                SettingSwitchRow(
                    title = "Push Notifications",
                    subtitle = if (isPushActive) "Active • Receiving live notices and circulars" else "Muted • Normal notifications are silenced",
                    icon = Icons.Default.Notifications,
                    checked = isPushActive,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestNotificationPermissionOrExecute {
                                appSettingsManager.setPushNotifications(true)
                                hostelNotificationManager.showAnnouncement(
                                    title = "HostelHub Alerts Active",
                                    message = "You will now receive official hostel notices and circulars."
                                )
                                Toast.makeText(context, "Push notifications enabled!", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            appSettingsManager.setPushNotifications(false)
                            Toast.makeText(context, "Push notifications muted.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                val isFeeReminderActive = isPushActive && feeReminders

                SettingSwitchRow(
                    title = "Fee Payment Reminders",
                    subtitle = if (isFeeReminderActive) "Active • Pending invoice alerts will notify you in ₹" else "Muted • Fee reminder alerts are disabled",
                    icon = Icons.Default.Payment,
                    checked = isFeeReminderActive,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestNotificationPermissionOrExecute {
                                appSettingsManager.setPushNotifications(true)
                                appSettingsManager.setFeeReminders(true)
                                val feesList = (studentViewModel?.fees?.value as? UiState.Success)?.data
                                val pendingFee = feesList?.firstOrNull { it.status == FeeStatus.PENDING || it.status == FeeStatus.OVERDUE }
                                if (pendingFee != null) {
                                    val due = pendingFee.amount - pendingFee.amountPaid
                                    hostelNotificationManager.showFeeReminder(pendingFee.title, due, feeId = pendingFee.feeId)
                                    Toast.makeText(context, "Fee reminder active: ${pendingFee.title} (₹${due.toInt()})", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Fee reminders enabled for upcoming dues.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            appSettingsManager.setFeeReminders(false)
                            Toast.makeText(context, "Fee reminders disabled.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                val isMenuAlertActive = isPushActive && menuUpdates

                SettingSwitchRow(
                    title = "Weekly Mess Menu Alerts",
                    subtitle = if (isMenuAlertActive) "Active • Meal schedule publications will alert you" else "Muted • Meal menu alerts disabled",
                    icon = Icons.Default.Restaurant,
                    checked = isMenuAlertActive,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestNotificationPermissionOrExecute {
                                appSettingsManager.setPushNotifications(true)
                                appSettingsManager.setMenuUpdates(true)
                                hostelNotificationManager.showMessMenuAlert("Weekly Campus Dining Menu", "Fresh 7-day meal schedule is published and active.")
                                Toast.makeText(context, "Mess menu alerts enabled.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            appSettingsManager.setMenuUpdates(false)
                            Toast.makeText(context, "Mess menu alerts disabled.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                val isEmergencyAlertActive = isNotificationPermissionGranted && emergencyAlerts

                SettingSwitchRow(
                    title = "Emergency / Safety Broadcasts",
                    subtitle = if (isEmergencyAlertActive) "Active • High-priority campus safety & urgent hazard alerts" else "Muted • Emergency safety broadcasts disabled",
                    icon = Icons.Default.Warning,
                    checked = isEmergencyAlertActive,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestNotificationPermissionOrExecute {
                                appSettingsManager.setEmergencyAlerts(true)
                                Toast.makeText(context, "Emergency & safety broadcasts enabled.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            appSettingsManager.setEmergencyAlerts(false)
                            Toast.makeText(context, "Emergency safety alerts muted.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Security & Privacy
            Text(
                text = "Security & Privacy",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            AppCard(padding = 16.dp) {
                SettingSwitchRow(
                    title = "Biometric & App Lock",
                    subtitle = if (isAppLockActive) "Protected • Fingerprint & 4-digit PIN lock active" else "Disabled • Tap to setup PIN & Biometric lock",
                    icon = Icons.Default.Fingerprint,
                    checked = isAppLockActive,
                    onCheckedChange = { shouldEnable ->
                        if (shouldEnable) {
                            showSetPinDialog = true
                        } else {
                            showDisablePinDialog = true
                        }
                    }
                )

                if (isAppLockActive) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSetPinDialog = true }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pin, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Change Security PIN", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text("Update your 4-digit master lock PIN", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Emergency & Incident Reporting
            Text(
                text = "Emergency & Incident Reporting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(10.dp))

            AppCard(
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                padding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.error, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Emergency SOS & Incident Report",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Immediate assistance for medical, fire, or security issues.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call 112 / SOS", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = { showEmergencyReportDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Report Incident", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. Compact Collapsible Help Centre & FAQs (Collapsed by Default)
            val faqs = listOf(
                Pair("How do I pay hostel fees in Indian Rupees (₹)?", "Navigate to 'Fees' from your dashboard or bottom navigation. You will see your live balance in ₹. Tap 'Pay Pending Dues' to select full or partial settlement."),
                Pair("How are rooms and beds allocated by the hostel owner?", "The hostel owner assigns rooms directly via the Host Portal. Once assigned, your Room Number and Bed ID will update automatically on your dashboard."),
                Pair("How do I submit and track a maintenance complaint?", "Tap 'Complaints' from the dashboard quick actions or bottom navigation. Fill in the title, category, and issue description. The warden will be notified immediately."),
                Pair("When is the weekly food menu updated?", "The catering team and hostel owner publish the 7-day meal plan every week. You can see today's breakfast, lunch, and dinner directly on your dashboard.")
            )

            AppCard(
                padding = 16.dp,
                onClick = { isFaqSectionExpanded = !isFaqSectionExpanded }
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
                                .size(38.dp)
                                .background(SecondaryContainer, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                contentDescription = null,
                                tint = SecondaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Help Centre & FAQs",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isFaqSectionExpanded) "Tap to collapse FAQ answers" else "Tap to view frequently asked questions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = if (isFaqSectionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isFaqSectionExpanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AnimatedVisibility(visible = isFaqSectionExpanded) {
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Spacer(modifier = Modifier.height(10.dp))

                        faqs.forEachIndexed { index, faq ->
                            val isQuestionExpanded = selectedFaqIndex == index
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { selectedFaqIndex = if (isQuestionExpanded) null else index }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = faq.first,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = if (isQuestionExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            tint = SecondaryTeal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    AnimatedVisibility(visible = isQuestionExpanded) {
                                        Column(modifier = Modifier.padding(top = 8.dp)) {
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = faq.second,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@hostelhub.edu"))
                                intent.putExtra(Intent.EXTRA_SUBJECT, "HostelHub App Support Inquiry")
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Contact support at support@hostelhub.edu", Toast.LENGTH_LONG).show()
                                }
                            }) {
                                Text("Contact Support ✉", color = SecondaryTeal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Version Footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HostelHub v1.0.0 (Production)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Secure Campus Housing & Accommodation Suite",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Edit Details Dialog
    if (showEditDetailsDialog) {
        var editName by remember { mutableStateOf(userFullName) }
        var editPhone by remember { mutableStateOf(userPhone) }
        var editCollege by remember { mutableStateOf(userCollege) }
        var editCourse by remember { mutableStateOf(userCourse) }
        var editYear by remember { mutableStateOf(userYear) }
        var editEmergencyPhone by remember { mutableStateOf(userEmergencyPhone) }
        var editAddress by remember { mutableStateOf(userAddress) }

        AlertDialog(
            onDismissRequest = { showEditDetailsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Profile Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AppTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = "Full Name",
                        placeholder = "e.g. Rakesh Kumar"
                    )
                    AppTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = "Mobile Phone Number",
                        placeholder = "e.g. 9876543210"
                    )
                    AppTextField(
                        value = editCollege,
                        onValueChange = { editCollege = it },
                        label = "College / University Name",
                        placeholder = "e.g. TKR College"
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            AppTextField(
                                value = editCourse,
                                onValueChange = { editCourse = it },
                                label = "Course",
                                placeholder = "e.g. Diploma / B.Tech"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            AppTextField(
                                value = editYear,
                                onValueChange = { editYear = it },
                                label = "Year",
                                placeholder = "e.g. 1st Year"
                            )
                        }
                    }
                    AppTextField(
                        value = editEmergencyPhone,
                        onValueChange = { editEmergencyPhone = it },
                        label = "Emergency Contact Phone",
                        placeholder = "Parent / Guardian Contact"
                    )
                    AppTextField(
                        value = editAddress,
                        onValueChange = { editAddress = it },
                        label = "Permanent Address",
                        placeholder = "Home address...",
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isBlank()) {
                            Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show()
                        } else {
                            userFullName = editName.trim()
                            userPhone = editPhone.trim()
                            userCollege = editCollege.trim()
                            userCourse = editCourse.trim()
                            userYear = editYear.trim()
                            userEmergencyPhone = editEmergencyPhone.trim()
                            userAddress = editAddress.trim()
                            Toast.makeText(context, "Details updated successfully!", Toast.LENGTH_LONG).show()
                            showEditDetailsDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDetailsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Change Password Dialog
    if (showPasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var isUpdating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { if (!isUpdating) showPasswordDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockReset, contentDescription = null, tint = PrimaryNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Change Password & Security", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your current password and choose a strong new password.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = "Current Password",
                        isPassword = true
                    )
                    AppTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = "New Password (Min 8 chars)",
                        isPassword = true
                    )
                    AppTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = "Confirm New Password",
                        isPassword = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPassword.isBlank()) {
                            Toast.makeText(context, "Please enter your current password", Toast.LENGTH_SHORT).show()
                        } else if (newPassword.length < 8) {
                            Toast.makeText(context, "New password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                        } else if (newPassword != confirmPassword) {
                            Toast.makeText(context, "New passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            isUpdating = true
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(600)
                                isUpdating = false
                                Toast.makeText(context, "Password updated successfully! Please use new password next time.", Toast.LENGTH_LONG).show()
                                showPasswordDialog = false
                            }
                        }
                    },
                    enabled = !isUpdating,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Updating...")
                    } else {
                        Text("Update Password")
                    }
                }
            },
            dismissButton = {
                if (!isUpdating) {
                    TextButton(onClick = { showPasswordDialog = false }) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

    // Set 4-Digit Security PIN Dialog
    if (showSetPinDialog) {
        var p1 by remember { mutableStateOf("") }
        var p2 by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showSetPinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Pin, contentDescription = null, tint = PrimaryNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (hasPinSet) "Change 4-Digit PIN" else "Set 4-Digit Security PIN",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose a 4-digit PIN to secure HostelHub when opening the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppTextField(
                        value = p1,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) p1 = it },
                        label = "New 4-Digit PIN",
                        placeholder = "••••",
                        isPassword = true
                    )
                    AppTextField(
                        value = p2,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) p2 = it },
                        label = "Confirm 4-Digit PIN",
                        placeholder = "••••",
                        isPassword = true
                    )
                    if (pinError != null) {
                        Text(
                            text = pinError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (p1.length != 4) {
                            pinError = "PIN must be exactly 4 digits."
                        } else if (p1 != p2) {
                            pinError = "PINs do not match."
                        } else {
                            val saved = appLockManager.setPin(p1)
                            if (saved) {
                                appSettingsManager.setBiometricLock(true)
                                Toast.makeText(context, "App Lock & PIN enabled successfully!", Toast.LENGTH_LONG).show()
                                showSetPinDialog = false
                            } else {
                                pinError = "Failed to save PIN."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Save & Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetPinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Disable App Lock Dialog
    if (showDisablePinDialog) {
        var disablePinInput by remember { mutableStateOf("") }
        var disableError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showDisablePinDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LockOpen, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disable App Lock", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter your current 4-digit PIN to turn off app lock protection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppTextField(
                        value = disablePinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) disablePinInput = it },
                        label = "Current 4-Digit PIN",
                        placeholder = "••••",
                        isPassword = true
                    )
                    if (disableError != null) {
                        Text(
                            text = disableError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val disabled = appLockManager.disableLock(disablePinInput)
                        if (disabled) {
                            appSettingsManager.setBiometricLock(false)
                            Toast.makeText(context, "App lock disabled.", Toast.LENGTH_SHORT).show()
                            showDisablePinDialog = false
                        } else {
                            disableError = "Incorrect PIN. Cannot disable app lock."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Turn Off")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisablePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report Emergency Incident Dialog
    if (showEmergencyReportDialog) {
        var incidentType by remember { mutableStateOf("Medical Emergency") }
        var incidentDetails by remember { mutableStateOf("") }
        var roomLocation by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showEmergencyReportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report Urgent Hazard", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Incident Category:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Medical", "Fire", "Electrical", "Security").forEach { type ->
                            FilterChip(
                                selected = incidentType.startsWith(type),
                                onClick = { incidentType = type },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    AppTextField(
                        value = roomLocation,
                        onValueChange = { roomLocation = it },
                        label = "Location / Room Number",
                        placeholder = "e.g. Block B, Room 204"
                    )
                    AppTextField(
                        value = incidentDetails,
                        onValueChange = { incidentDetails = it },
                        label = "Incident Description",
                        placeholder = "Describe the emergency...",
                        singleLine = false
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "Emergency report dispatched to warden & campus security!", Toast.LENGTH_LONG).show()
                        showEmergencyReportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Dispatch Alert")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmergencyReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(PrimaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryNavy,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PrimaryNavy,
                checkedTrackColor = SecondaryTeal.copy(alpha = 0.5f)
            )
        )
    }
}
