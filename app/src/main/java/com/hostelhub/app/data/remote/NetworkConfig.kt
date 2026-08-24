package com.hostelhub.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.hostelhub.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hostelhub_network_prefs", Context.MODE_PRIVATE)

    /**
     * Returns the active API Base URL.
     * Order of precedence:
     * 1. User-customized URL in SharedPreferences (for instant in-app cloud / tunnel / LAN switching)
     * 2. BuildConfig.BASE_URL (configured via build.gradle.kts)
     * 3. Fallback: Cloud URL / Local LAN URL
     */
    fun getBaseUrl(): String {
        val customUrl = prefs.getString(KEY_CUSTOM_BASE_URL, null)
        if (!customUrl.isNullOrBlank()) {
            return formatUrl(customUrl)
        }

        return try {
            if (BuildConfig.BASE_URL.isNotBlank()) {
                formatUrl(BuildConfig.BASE_URL)
            } else {
                DEFAULT_GLOBAL_URL
            }
        } catch (e: Exception) {
            DEFAULT_GLOBAL_URL
        }
    }

    fun setCustomBaseUrl(url: String?) {
        if (url.isNullOrBlank()) {
            prefs.edit().remove(KEY_CUSTOM_BASE_URL).apply()
        } else {
            prefs.edit().putString(KEY_CUSTOM_BASE_URL, formatUrl(url)).apply()
        }
    }

    fun resetToDefault() {
        prefs.edit().remove(KEY_CUSTOM_BASE_URL).apply()
    }

    fun isCustomUrlSet(): Boolean {
        return !prefs.getString(KEY_CUSTOM_BASE_URL, null).isNullOrBlank()
    }

    fun isCloudOrTunnel(): Boolean {
        val url = getBaseUrl().lowercase()
        return url.startsWith("https://") ||
               url.contains("onrender.com") ||
               url.contains("railway.app") ||
               url.contains("fly.dev") ||
               url.contains("trycloudflare.com") ||
               url.contains("loca.lt") ||
               url.contains("ngrok") ||
               url.contains("appspot.com") ||
               url.contains("run.app")
    }

    fun getCloudProviderName(): String {
        val url = getBaseUrl().lowercase()
        return when {
            url.contains("onrender.com") -> "Render Cloud"
            url.contains("railway.app") -> "Railway Cloud"
            url.contains("fly.dev") -> "Fly.io Cloud"
            url.contains("run.app") -> "Google Cloud Run"
            url.contains("loca.lt") -> "Localtunnel Gateway"
            url.contains("trycloudflare.com") -> "Cloudflare Tunnel"
            url.contains("ngrok") -> "Ngrok Gateway"
            url.startsWith("https://") -> "Custom Cloud HTTPS"
            else -> "Local Wi-Fi Network"
        }
    }

    fun getDisplayHost(): String {
        val url = getBaseUrl()
        return try {
            val clean = url.removePrefix("https://").removePrefix("http://").removeSuffix("/")
            clean.substringBefore("/")
        } catch (e: Exception) {
            url
        }
    }

    /**
     * Formats and sanitizes any user-entered or configured URL:
     * - Handles public domains with https:// (e.g. *.onrender.com, *.trycloudflare.com, *.ngrok-free.app, *.loca.lt)
     * - Handles local IP addresses with http:// (e.g. 192.168.x.x, 10.0.2.2, localhost)
     * - Ensures trailing slash and /api/ endpoint path suffix
     */
    fun formatUrl(rawUrl: String): String {
        var formatted = rawUrl.trim()

        while (formatted.endsWith("/")) {
            formatted = formatted.dropLast(1)
        }

        if (!formatted.startsWith("http://", ignoreCase = true) && !formatted.startsWith("https://", ignoreCase = true)) {
            val isLocal = formatted.startsWith("192.168.") ||
                          formatted.startsWith("10.") ||
                          formatted.startsWith("172.") ||
                          formatted.startsWith("localhost") ||
                          formatted.startsWith("127.0.0.1") ||
                          formatted.startsWith("0.0.0.0")

            formatted = if (isLocal) {
                "http://$formatted"
            } else {
                "https://$formatted"
            }
        }

        if (!formatted.endsWith("/api", ignoreCase = true)) {
            formatted = "$formatted/api"
        }

        return "$formatted/"
    }

    companion object {
        private const val KEY_CUSTOM_BASE_URL = "custom_base_url"
        const val DEFAULT_GLOBAL_URL = "https://hostelhub-backend.onrender.com/api/"
        const val DEFAULT_LOCAL_URL = "http://192.168.1.2:5000/api/"
        const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:5000/api/"
    }
}
