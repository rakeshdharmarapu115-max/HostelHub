package com.hostelhub.app.presentation.student

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import com.hostelhub.app.data.local.AppSettingsManager
import com.hostelhub.app.data.remote.NetworkConfig
import com.hostelhub.app.domain.model.User
import com.hostelhub.app.presentation.components.AppButton
import com.hostelhub.app.presentation.components.AppCard
import com.hostelhub.app.presentation.components.AppTextField
import com.hostelhub.app.presentation.components.AppTopBar
import com.hostelhub.app.presentation.components.ButtonVariant
import com.hostelhub.app.presentation.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun SettingsScreen(
    currentUser: User? = null,
    onNavigateToProfile: () -> Unit = {},
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appSettingsManager = remember { AppSettingsManager(context.applicationContext) }
    val networkConfig = remember { NetworkConfig(context.applicationContext) }

    var currentBaseUrl by remember { mutableStateOf(networkConfig.getBaseUrl()) }
    var showServerDialog by remember { mutableStateOf(false) }
    var customUrlInput by remember { mutableStateOf(currentBaseUrl) }
    var pingStatus by remember { mutableStateOf<String?>(null) }
    var isPinging by remember { mutableStateOf(false) }

    val isDarkMode by appSettingsManager.isDarkMode.collectAsState()
    val pushNotifications by appSettingsManager.pushNotifications.collectAsState()
    val feeReminders by appSettingsManager.feeReminders.collectAsState()
    val menuUpdates by appSettingsManager.menuUpdates.collectAsState()
    val emergencyAlerts by appSettingsManager.emergencyAlerts.collectAsState()

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showEmergencyReportDialog by remember { mutableStateOf(false) }
    var selectedFaqIndex by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Settings & Support",
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
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            // 1. Profiles & Profile Settings Header Card
            Text(
                text = "Profile & Account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            AppCard(
                padding = 16.dp,
                onClick = onNavigateToProfile
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(PrimaryContainer, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = PrimaryNavy,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.fullName ?: "Resident Account",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser?.email ?: "Campus Verified Account",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to view profile details & student credentials",
                            style = MaterialTheme.typography.labelSmall,
                            color = SecondaryTeal
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = SecondaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.clickable { showPasswordDialog = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Change Password & Security", style = MaterialTheme.typography.bodyMedium, color = PrimaryNavy)
                    }
                    Text(
                        text = "Currency: ₹ INR",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 2. Appearance & Dark Mode
            Text(
                text = "Appearance & Display",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            AppCard(padding = 16.dp) {
                SettingSwitchRow(
                    title = "Dark Mode Theme",
                    subtitle = "Sleek dark interface with high contrast",
                    icon = Icons.Default.DarkMode,
                    checked = isDarkMode,
                    onCheckedChange = { appSettingsManager.setDarkMode(it) }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Worldwide Server & Network Connectivity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Server & Cross-Network Connection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = {
                    customUrlInput = currentBaseUrl
                    showServerDialog = true
                }) {
                    Text("Change URL ✏️", color = SecondaryTeal, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            AppCard(padding = 16.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (networkConfig.isCloudOrTunnel()) SecondaryTeal.copy(alpha = 0.15f) else PrimaryNavy.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (networkConfig.isCloudOrTunnel()) Icons.Default.CloudDone else Icons.Default.Wifi,
                            contentDescription = null,
                            tint = if (networkConfig.isCloudOrTunnel()) SecondaryTeal else PrimaryNavy,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (networkConfig.isCloudOrTunnel()) "Worldwide Cloud / Tunnel" else "Local LAN Wi-Fi",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (networkConfig.isCloudOrTunnel()) SecondaryTeal.copy(alpha = 0.2f) else PrimaryNavy.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (networkConfig.isCloudOrTunnel()) "Any Network / 4G / 5G" else "Same Wi-Fi",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (networkConfig.isCloudOrTunnel()) SecondaryTeal else PrimaryNavy
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentBaseUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }

                if (pingStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = pingStatus ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (pingStatus?.contains("Online") == true) SecondaryTeal else MaterialTheme.colorScheme.error
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isPinging = true
                            pingStatus = "Testing connection..."
                            coroutineScope.launch(Dispatchers.IO) {
                                val startTime = System.currentTimeMillis()
                                try {
                                    val url = URL(currentBaseUrl + "hostels")
                                    val conn = url.openConnection() as HttpURLConnection
                                    conn.connectTimeout = 5000
                                    conn.readTimeout = 5000
                                    conn.setRequestProperty("Bypass-Tunnel-Reminder", "true")
                                    conn.requestMethod = "GET"
                                    val code = conn.responseCode
                                    val elapsed = System.currentTimeMillis() - startTime
                                    withContext(Dispatchers.Main) {
                                        isPinging = false
                                        if (code in 200..299) {
                                            pingStatus = "✓ Online & Connected (${elapsed}ms latency)"
                                        } else {
                                            pingStatus = "⚠️ Server responded with HTTP $code (${elapsed}ms)"
                                        }
                                    }
                                    conn.disconnect()
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isPinging = false
                                        pingStatus = "❌ Connection Failed: ${e.localizedMessage ?: "Unreachable"}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isPinging
                    ) {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isPinging) "Pinging..." else "⚡ Ping Test", style = MaterialTheme.typography.labelMedium)
                    }

                    Button(
                        onClick = {
                            val newUrl = if (networkConfig.isCloudOrTunnel()) {
                                NetworkConfig.DEFAULT_LOCAL_URL
                            } else {
                                NetworkConfig.DEFAULT_GLOBAL_URL
                            }
                            networkConfig.setCustomBaseUrl(newUrl)
                            currentBaseUrl = networkConfig.getBaseUrl()
                            pingStatus = null
                            Toast.makeText(context, "Switched to: $currentBaseUrl", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (networkConfig.isCloudOrTunnel()) PrimaryNavy else SecondaryTeal
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (networkConfig.isCloudOrTunnel()) "Switch to LAN" else "Switch to Cloud",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Push Notifications & Alerts
            Text(
                text = "Push Notifications & Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            AppCard(padding = 16.dp) {
                SettingSwitchRow(
                    title = "Push Notifications",
                    subtitle = "Receive live hostel announcements and notices",
                    icon = Icons.Default.Notifications,
                    checked = pushNotifications,
                    onCheckedChange = { appSettingsManager.setPushNotifications(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                SettingSwitchRow(
                    title = "Fee Payment Reminders",
                    subtitle = "Upcoming invoice due alerts in ₹",
                    icon = Icons.Default.Payment,
                    checked = feeReminders,
                    onCheckedChange = { appSettingsManager.setFeeReminders(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                SettingSwitchRow(
                    title = "Weekly Mess Menu Alerts",
                    subtitle = "Notifies when fresh daily meals are published",
                    icon = Icons.Default.Restaurant,
                    checked = menuUpdates,
                    onCheckedChange = { appSettingsManager.setMenuUpdates(it) }
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                SettingSwitchRow(
                    title = "Emergency & Safety Broadcasts",
                    subtitle = "High priority urgent hostel safety warnings",
                    icon = Icons.Default.Warning,
                    checked = emergencyAlerts,
                    onCheckedChange = { appSettingsManager.setEmergencyAlerts(it) }
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Report & Emergency SOS
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

            // 5. Help Centre & FAQs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Help Centre & FAQs",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@hostelhub.edu"))
                    intent.putExtra(Intent.EXTRA_SUBJECT, "HostelHub App Support Inquiry")
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Contact support at support@hostelhub.edu", Toast.LENGTH_LONG).show()
                    }
                }) {
                    Text("Contact Support ✉", color = SecondaryTeal)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val faqs = listOf(
                Pair("How do I pay hostel fees in Indian Rupees (₹)?", "Navigate to 'Pay Fees' from your dashboard or bottom navigation. You will see your live balance in ₹. Tap 'Pay Pending Dues' to select full or partial settlement."),
                Pair("How are rooms and beds allocated by the hostel owner?", "The hostel owner assigns rooms directly via the Host Portal. Once assigned, your Room Number and Bed ID will update automatically on your dashboard."),
                Pair("How do I submit and track a maintenance complaint?", "Tap 'Complaints' from the dashboard quick actions or bottom navigation. Fill in the title, category, and issue description. The warden will be notified immediately."),
                Pair("When is the weekly food menu updated?", "The catering team and hostel owner publish the 7-day meal plan every week. You can see today's breakfast, lunch, and dinner directly on your dashboard.")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                faqs.forEachIndexed { index, faq ->
                    val isExpanded = selectedFaqIndex == index
                    AppCard(
                        padding = 14.dp,
                        onClick = { selectedFaqIndex = if (isExpanded) null else index }
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
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                                    contentDescription = null,
                                    tint = SecondaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = faq.first,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(8.dp))
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

    // Change Password Dialog
    if (showPasswordDialog) {
        var currentPassword by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Account Password", style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        if (newPassword.length < 8) {
                            Toast.makeText(context, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                        } else if (newPassword != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                            showPasswordDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Update Password")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) {
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

    // Change Server URL Dialog
    if (showServerDialog) {
        AlertDialog(
            onDismissRequest = { showServerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Dns, contentDescription = null, tint = PrimaryNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Server Connection URL", style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter any Worldwide Cloud / Tunnel URL (HTTPS) or local Wi-Fi IP address (HTTP):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AppTextField(
                        value = customUrlInput,
                        onValueChange = { customUrlInput = it },
                        label = "API Base URL",
                        placeholder = "https://your-tunnel.loca.lt/api/"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedButton(
                            onClick = { customUrlInput = NetworkConfig.DEFAULT_GLOBAL_URL },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Default Cloud", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(
                            onClick = { customUrlInput = NetworkConfig.DEFAULT_LOCAL_URL },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Default LAN", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (customUrlInput.isNotBlank()) {
                            networkConfig.setCustomBaseUrl(customUrlInput)
                            currentBaseUrl = networkConfig.getBaseUrl()
                            pingStatus = null
                            Toast.makeText(context, "Server URL updated!", Toast.LENGTH_SHORT).show()
                            showServerDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServerDialog = false }) {
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
