package com.hostelhub.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM_DEFAULT
}

@Singleton
class AppSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hostelhub_app_settings", Context.MODE_PRIVATE)

    private val initialThemeMode = try {
        val saved = prefs.getString(KEY_THEME_MODE, null)
        if (saved != null) {
            ThemeMode.valueOf(saved)
        } else if (prefs.contains(KEY_DARK_MODE)) {
            if (prefs.getBoolean(KEY_DARK_MODE, false)) ThemeMode.DARK else ThemeMode.LIGHT
        } else {
            ThemeMode.SYSTEM_DEFAULT
        }
    } catch (e: Exception) {
        ThemeMode.SYSTEM_DEFAULT
    }

    private val _themeMode = MutableStateFlow(initialThemeMode)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(initialThemeMode == ThemeMode.DARK)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _pushNotifications = MutableStateFlow(prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true))
    val pushNotifications: StateFlow<Boolean> = _pushNotifications.asStateFlow()

    private val _feeReminders = MutableStateFlow(prefs.getBoolean(KEY_FEE_REMINDERS, true))
    val feeReminders: StateFlow<Boolean> = _feeReminders.asStateFlow()

    private val _menuUpdates = MutableStateFlow(prefs.getBoolean(KEY_MENU_UPDATES, true))
    val menuUpdates: StateFlow<Boolean> = _menuUpdates.asStateFlow()

    private val _emergencyAlerts = MutableStateFlow(prefs.getBoolean(KEY_EMERGENCY_ALERTS, true))
    val emergencyAlerts: StateFlow<Boolean> = _emergencyAlerts.asStateFlow()

    private val _biometricLock = MutableStateFlow(prefs.getBoolean(KEY_BIOMETRIC_LOCK, false))
    val biometricLock: StateFlow<Boolean> = _biometricLock.asStateFlow()

    private var activeUserId: String? = null

    fun setUserScope(userId: String?) {
        activeUserId = userId
        val userPrefix = if (!userId.isNullOrBlank()) "${userId}_" else ""

        _pushNotifications.value = prefs.getBoolean("${userPrefix}$KEY_PUSH_NOTIFICATIONS", true)
        _feeReminders.value = prefs.getBoolean("${userPrefix}$KEY_FEE_REMINDERS", true)
        _menuUpdates.value = prefs.getBoolean("${userPrefix}$KEY_MENU_UPDATES", true)
        _emergencyAlerts.value = prefs.getBoolean("${userPrefix}$KEY_EMERGENCY_ALERTS", true)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
        _isDarkMode.value = (mode == ThemeMode.DARK)
    }

    fun setDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    fun setBiometricLock(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, enabled).apply()
        _biometricLock.value = enabled
    }

    fun setPushNotifications(enabled: Boolean) {
        val userPrefix = if (!activeUserId.isNullOrBlank()) "${activeUserId}_" else ""
        prefs.edit().putBoolean("${userPrefix}$KEY_PUSH_NOTIFICATIONS", enabled).apply()
        _pushNotifications.value = enabled
    }

    fun setFeeReminders(enabled: Boolean) {
        val userPrefix = if (!activeUserId.isNullOrBlank()) "${activeUserId}_" else ""
        prefs.edit().putBoolean("${userPrefix}$KEY_FEE_REMINDERS", enabled).apply()
        _feeReminders.value = enabled
    }

    fun setMenuUpdates(enabled: Boolean) {
        val userPrefix = if (!activeUserId.isNullOrBlank()) "${activeUserId}_" else ""
        prefs.edit().putBoolean("${userPrefix}$KEY_MENU_UPDATES", enabled).apply()
        _menuUpdates.value = enabled
    }

    fun setEmergencyAlerts(enabled: Boolean) {
        val userPrefix = if (!activeUserId.isNullOrBlank()) "${activeUserId}_" else ""
        prefs.edit().putBoolean("${userPrefix}$KEY_EMERGENCY_ALERTS", enabled).apply()
        _emergencyAlerts.value = enabled
    }

    fun clearCache(): Long {
        return try {
            val cacheDir = context.cacheDir
            val size = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            cacheDir.deleteRecursively()
            size
        } catch (e: Exception) {
            0L
        }
    }

    companion object {
        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_PUSH_NOTIFICATIONS = "pref_push_notifications"
        private const val KEY_FEE_REMINDERS = "pref_fee_reminders"
        private const val KEY_MENU_UPDATES = "pref_menu_updates"
        private const val KEY_EMERGENCY_ALERTS = "pref_emergency_alerts"
        private const val KEY_BIOMETRIC_LOCK = "pref_biometric_lock"
    }
}
