package com.hostelhub.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.hostelhub.app.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ConnectException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException

data class ConnectionTestResult(
    val isSuccess: Boolean,
    val message: String,
    val latencyMs: Long? = null,
    val httpCode: Int? = null,
    val dbStatus: String? = null
)

@Singleton
class NetworkConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hostelhub_network_prefs", Context.MODE_PRIVATE)

    init {
        // Auto-cleanup stale placeholder domain if saved in preferences
        val stored = prefs.getString(KEY_CUSTOM_BASE_URL, null)
        if (stored != null && stored.contains("hostelhub-backend.onrender.com")) {
            prefs.edit().remove(KEY_CUSTOM_BASE_URL).apply()
        }
    }

    /**
     * Returns the active API Base URL.
     * Order of precedence:
     * 1. User-customized URL in SharedPreferences (for instant in-app cloud / tunnel / LAN switching)
     * 2. BuildConfig.BASE_URL (configured via build.gradle.kts)
     * 3. Fallback: Production Render Cloud URL
     */
    fun getBaseUrl(): String {
        val customUrl = prefs.getString(KEY_CUSTOM_BASE_URL, null)
        if (!customUrl.isNullOrBlank()) {
            return formatUrl(customUrl)
        }

        return try {
            if (BuildConfig.BASE_URL.isNotBlank() && !BuildConfig.BASE_URL.contains("hostelhub-backend.onrender.com")) {
                formatUrl(BuildConfig.BASE_URL)
            } else {
                DEFAULT_GLOBAL_URL
            }
        } catch (e: Exception) {
            DEFAULT_GLOBAL_URL
        }
    }

    fun setCustomBaseUrl(url: String?) {
        if (url.isNullOrBlank() || url.trim() == DEFAULT_GLOBAL_URL.trim()) {
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
            url.contains("hostelhub-yp73.onrender.com") -> "Render Cloud (Live Production)"
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
     * - Handles public domains with https://
     * - Handles local IP addresses with http:// (e.g. 192.168.x.x, 10.0.2.2, localhost)
     * - Guarantees single /api/ suffix without /api/api/ duplication
     * - Ensures trailing slash
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

        // Clean any double api segments
        if (formatted.endsWith("/api/api", ignoreCase = true)) {
            formatted = formatted.dropLast(4)
        }

        if (!formatted.endsWith("/api", ignoreCase = true)) {
            formatted = "$formatted/api"
        }

        return "$formatted/"
    }

    /**
     * Tests live connectivity by querying the /health or /api/hostels endpoints.
     * Categorizes exact network errors (DNS, Timeout, SSL, Refusal, HTTP errors).
     */
    suspend fun testConnection(targetUrl: String? = null): ConnectionTestResult = withContext(Dispatchers.IO) {
        val base = if (!targetUrl.isNullOrBlank()) formatUrl(targetUrl) else getBaseUrl()
        val healthUrl = try {
            val hostRoot = base.substringBefore("/api")
            "$hostRoot/health"
        } catch (e: Exception) {
            "${base}hostels"
        }

        val startTime = System.currentTimeMillis()
        var connection: HttpURLConnection? = null

        try {
            val url = URL(healthUrl)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 12000 // 12s for Render container spin-up
                readTimeout = 12000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Bypass-Tunnel-Reminder", "true")
            }

            val responseCode = connection.responseCode
            val latency = System.currentTimeMillis() - startTime

            if (responseCode in 200..299) {
                var dbStatus = "Connected"
                try {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val body = reader.readText()
                    reader.close()
                    val json = JSONObject(body)
                    if (json.has("database")) {
                        dbStatus = json.getJSONObject("database").optString("status", "connected")
                    }
                } catch (_: Exception) {}

                ConnectionTestResult(
                    isSuccess = true,
                    message = "✓ Connected to Cloud Backend! Latency: ${latency}ms (Database: $dbStatus)",
                    latencyMs = latency,
                    httpCode = responseCode,
                    dbStatus = dbStatus
                )
            } else {
                ConnectionTestResult(
                    isSuccess = false,
                    message = "⚠️ Server reachable but returned HTTP $responseCode (${latency}ms)",
                    latencyMs = latency,
                    httpCode = responseCode
                )
            }
        } catch (e: UnknownHostException) {
            ConnectionTestResult(
                isSuccess = false,
                message = "❌ DNS Error: Unable to resolve host '${e.message}'. Please check internet connection or server URL.",
                httpCode = null
            )
        } catch (e: SocketTimeoutException) {
            ConnectionTestResult(
                isSuccess = false,
                message = "⏳ Connection Timeout: Cloud backend may be starting up (Render free tier). Please retry in 15 seconds.",
                httpCode = 408
            )
        } catch (e: ConnectException) {
            ConnectionTestResult(
                isSuccess = false,
                message = "❌ Connection Refused: Backend server is not reachable on this port/address.",
                httpCode = null
            )
        } catch (e: SSLException) {
            ConnectionTestResult(
                isSuccess = false,
                message = "🔒 SSL/TLS Error: Handshake failed (${e.localizedMessage ?: "Invalid certificate"}).",
                httpCode = null
            )
        } catch (e: Exception) {
            ConnectionTestResult(
                isSuccess = false,
                message = "❌ Connection Failed: ${e.localizedMessage ?: e.javaClass.simpleName}",
                httpCode = null
            )
        } finally {
            connection?.disconnect()
        }
    }

    companion object {
        private const val KEY_CUSTOM_BASE_URL = "custom_base_url"
        const val DEFAULT_GLOBAL_URL = "https://hostelhub-yp73.onrender.com/api/"
        const val DEFAULT_LOCAL_URL = "http://192.168.1.2:5000/api/"
        const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:5000/api/"
    }
}
