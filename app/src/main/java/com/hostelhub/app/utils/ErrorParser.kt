package com.hostelhub.app.utils

import org.json.JSONObject
import retrofit2.Response

object ErrorParser {

    /**
     * Parses error messages from a Retrofit HTTP response.
     * Accurately extracts backend JSON messages and classifies status codes (400, 401, 403, 404, 409, 500, 502, 503).
     */
    fun parseErrorMessage(response: Response<*>?, fallback: String = "An unexpected error occurred"): String {
        if (response == null) return fallback

        val statusCode = response.code()
        val raw = try {
            response.errorBody()?.string()
        } catch (e: Exception) {
            null
        }

        var parsedMessage: String? = null
        if (!raw.isNullOrBlank()) {
            try {
                val json = JSONObject(raw)
                val msg = json.optString("message")
                if (!msg.isNullOrBlank()) {
                    parsedMessage = msg
                }
                val errorsArray = json.optJSONArray("errors")
                if (errorsArray != null && errorsArray.length() > 0) {
                    val firstErr = errorsArray.optJSONObject(0)
                    val detailMsg = firstErr?.optString("message")
                    if (!detailMsg.isNullOrBlank()) {
                        parsedMessage = if (parsedMessage != null) "$parsedMessage: $detailMsg" else detailMsg
                    }
                }
            } catch (e: Exception) {
                if (raw.length < 200 && !raw.startsWith("<")) {
                    parsedMessage = raw.trim()
                }
            }
        }

        if (!parsedMessage.isNullOrBlank()) {
            return parsedMessage
        }

        return when (statusCode) {
            400 -> "Invalid request. Please verify all submitted fields."
            401 -> "Invalid email or password. Please check your credentials."
            403 -> "Access denied. Your account may be inactive or lack required permissions."
            404 -> "API endpoint not found (HTTP 404). Please verify your server API Base URL."
            409 -> "Conflict: An account or record with these details already exists."
            500 -> "Backend server error (HTTP 500). Please check server logs or database status."
            502 -> "Bad Gateway (HTTP 502): The cloud backend is starting up or proxy is unavailable. Please retry in a few moments."
            503 -> "Service Unavailable (HTTP 503): Database connection failure or server undergoing maintenance."
            504 -> "Gateway Timeout (HTTP 504): Cloud service took too long to respond. Please try again."
            else -> fallback
        }
    }

    /**
     * Parses network exceptions into actionable, informative messages.
     */
    fun parseExceptionMessage(e: Throwable, fallback: String = "Network connection error"): String {
        val msg = e.localizedMessage ?: e.message ?: ""
        val lower = msg.lowercase()

        if (e is java.net.UnknownHostException || lower.contains("unable to resolve host")) {
            return "Unable to resolve server address. Please check your internet connection or verify the Cloud API URL."
        }

        if (e is java.net.ConnectException || lower.contains("connection refused") || lower.contains("failed to connect")) {
            return "Cannot connect to backend server. Make sure the cloud server is running and the API URL is correct."
        }

        if (e is java.net.SocketTimeoutException || lower.contains("timeout")) {
            return "Server request timed out. If using a free cloud tier (Render/Railway), the instance may be cold-starting. Please retry in 15 seconds."
        }

        if (e is java.net.NoRouteToHostException || lower.contains("network is unreachable") || lower.contains("no route to host")) {
            return "No route to host. Check your Wi-Fi/Mobile network connectivity."
        }

        if (lower.contains("ssl") || lower.contains("cert") || lower.contains("handshake")) {
            return "SSL/TLS Connection error. Please ensure your cloud endpoint supports HTTPS and valid certificates."
        }

        if (lower.contains("serialization") || lower.contains("json") || lower.contains("malformed")) {
            return "Data parsing error. Server returned an incompatible response format."
        }

        return if (msg.isNotBlank()) msg else fallback
    }
}

