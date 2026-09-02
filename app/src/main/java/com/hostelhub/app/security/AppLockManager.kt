package com.hostelhub.app.security

import android.content.Context
import android.content.SharedPreferences
import android.os.SystemClock
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hostelhub_app_lock_secure", Context.MODE_PRIVATE)

    private val _isAppLockEnabled = MutableStateFlow(prefs.getBoolean(KEY_LOCK_ENABLED, false))
    val isAppLockEnabled: StateFlow<Boolean> = _isAppLockEnabled.asStateFlow()

    private val _hasPinSet = MutableStateFlow(prefs.getString(KEY_PIN_HASH, null) != null)
    val hasPinSet: StateFlow<Boolean> = _hasPinSet.asStateFlow()

    // App is locked immediately on launch if lock is enabled
    private val _isLocked = MutableStateFlow(prefs.getBoolean(KEY_LOCK_ENABLED, false))
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastBackgroundTimeMs: Long = 0L
    private val gracePeriodMs: Long = 15_000L // 15 seconds grace period

    companion object {
        private const val KEY_LOCK_ENABLED = "pref_app_lock_enabled"
        private const val KEY_PIN_HASH = "pref_pin_hash"
        private const val KEY_PIN_SALT = "pref_pin_salt"
        private const val KEY_BIOMETRIC_PREF = "pref_biometric_enabled"
    }

    /**
     * Checks if biometric hardware is present and has enrolled credentials.
     */
    fun checkBiometricStatus(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        return when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HW_UNAVAILABLE
            else -> BiometricStatus.UNSUPPORTED
        }
    }

    /**
     * Stores a 4-digit PIN securely using salted SHA-256.
     */
    fun setPin(pin: String): Boolean {
        if (pin.length != 4 || !pin.all { it.isDigit() }) return false

        val salt = generateSalt()
        val hash = hashPin(pin, salt)

        prefs.edit()
            .putString(KEY_PIN_SALT, salt)
            .putString(KEY_PIN_HASH, hash)
            .putBoolean(KEY_LOCK_ENABLED, true)
            .apply()

        _hasPinSet.value = true
        _isAppLockEnabled.value = true
        return true
    }

    /**
     * Verifies user PIN against the salted hash.
     */
    fun verifyPin(pin: String): Boolean {
        val savedSalt = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false

        val computedHash = hashPin(pin, savedSalt)
        val matches = (computedHash == savedHash)
        if (matches) {
            unlock()
        }
        return matches
    }

    fun unlock() {
        _isLocked.value = false
        lastBackgroundTimeMs = SystemClock.elapsedRealtime()
    }

    fun lock() {
        if (_isAppLockEnabled.value) {
            _isLocked.value = true
        }
    }

    fun disableLock(pin: String): Boolean {
        if (!verifyPin(pin)) return false

        prefs.edit()
            .remove(KEY_PIN_HASH)
            .remove(KEY_PIN_SALT)
            .putBoolean(KEY_LOCK_ENABLED, false)
            .apply()

        _hasPinSet.value = false
        _isAppLockEnabled.value = false
        _isLocked.value = false
        return true
    }

    /**
     * Dispatches native Android BiometricPrompt authentication.
     */
    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        title: String = "Unlock HostelHub",
        subtitle: String = "Verify your fingerprint or face to proceed",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN Instead")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlock()
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Biometric authentication failed. Please try again or enter PIN.")
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }

    // App Lifecycle tracking
    fun onAppBackgrounded() {
        lastBackgroundTimeMs = SystemClock.elapsedRealtime()
    }

    fun onAppForegrounded() {
        if (_isAppLockEnabled.value && lastBackgroundTimeMs > 0L) {
            val elapsed = SystemClock.elapsedRealtime() - lastBackgroundTimeMs
            if (elapsed > gracePeriodMs) {
                _isLocked.value = true
            }
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    private fun hashPin(pin: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = (salt + pin).toByteArray(Charsets.UTF_8)
        val digest = md.digest(input)
        return digest.joinToString("") { "%02x".format(it) }
    }
}

enum class BiometricStatus {
    AVAILABLE,
    NOT_ENROLLED,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    UNSUPPORTED
}
