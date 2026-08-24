package com.hostelhub.app.data.local

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppSettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hostelhub_app_settings", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(KEY_DARK_MODE, false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _pushNotifications = MutableStateFlow(prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true))
    val pushNotifications: StateFlow<Boolean> = _pushNotifications.asStateFlow()

    private val _feeReminders = MutableStateFlow(prefs.getBoolean(KEY_FEE_REMINDERS, true))
    val feeReminders: StateFlow<Boolean> = _feeReminders.asStateFlow()

    private val _menuUpdates = MutableStateFlow(prefs.getBoolean(KEY_MENU_UPDATES, true))
    val menuUpdates: StateFlow<Boolean> = _menuUpdates.asStateFlow()

    private val _emergencyAlerts = MutableStateFlow(prefs.getBoolean(KEY_EMERGENCY_ALERTS, true))
    val emergencyAlerts: StateFlow<Boolean> = _emergencyAlerts.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setPushNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply()
        _pushNotifications.value = enabled
    }

    fun setFeeReminders(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FEE_REMINDERS, enabled).apply()
        _feeReminders.value = enabled
    }

    fun setMenuUpdates(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MENU_UPDATES, enabled).apply()
        _menuUpdates.value = enabled
    }

    fun setEmergencyAlerts(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_EMERGENCY_ALERTS, enabled).apply()
        _emergencyAlerts.value = enabled
    }

    companion object {
        private const val KEY_DARK_MODE = "pref_dark_mode"
        private const val KEY_PUSH_NOTIFICATIONS = "pref_push_notifications"
        private const val KEY_FEE_REMINDERS = "pref_fee_reminders"
        private const val KEY_MENU_UPDATES = "pref_menu_updates"
        private const val KEY_EMERGENCY_ALERTS = "pref_emergency_alerts"
    }
}
