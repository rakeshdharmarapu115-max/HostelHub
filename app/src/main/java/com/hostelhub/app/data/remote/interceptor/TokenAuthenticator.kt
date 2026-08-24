package com.hostelhub.app.data.remote.interceptor

import com.google.gson.Gson
import com.hostelhub.app.data.remote.NetworkConfig
import com.hostelhub.app.data.remote.datasource.TokenManager
import com.hostelhub.app.data.remote.dto.ApiResponse
import com.hostelhub.app.data.remote.dto.RefreshTokenRequestDto
import com.hostelhub.app.data.remote.dto.RefreshTokenResponseDto
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val networkConfig: NetworkConfig,
    private val gson: Gson
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if refresh endpoint itself returns 401
        if (response.request.url.encodedPath.contains("auth/refresh") ||
            response.request.url.encodedPath.contains("auth/login") ||
            response.request.url.encodedPath.contains("auth/register")
        ) {
            return null
        }

        // Only retry once per request
        if (responseCount(response) >= 3) {
            return null
        }

        val refreshToken = tokenManager.getRefreshToken() ?: return null

        synchronized(this) {
            val currentToken = tokenManager.getAccessToken()
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")?.trim()

            // If token has already been refreshed by another concurrent thread, retry with new token
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val newAccessToken = refreshAccessTokenSync(refreshToken)
            return if (!newAccessToken.isNullOrBlank()) {
                tokenManager.saveAccessToken(newAccessToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
            } else {
                null
            }
        }
    }

    private fun refreshAccessTokenSync(refreshToken: String): String? {
        return try {
            val refreshUrl = networkConfig.getBaseUrl() + "auth/refresh"
            val jsonBody = gson.toJson(RefreshTokenRequestDto(refreshToken))
            val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url(refreshUrl)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Bypass-Tunnel-Reminder", "true")
                .build()

            val client = OkHttpClient.Builder().build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBodyStr = response.body?.string()
                if (!responseBodyStr.isNullOrBlank()) {
                    val apiResponse = gson.fromJson(responseBodyStr, RefreshTokenApiResponse::class.java)
                    apiResponse?.data?.accessToken
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    private data class RefreshTokenApiResponse(
        val success: Boolean,
        val message: String?,
        val data: RefreshTokenResponseDto?
    )
}
