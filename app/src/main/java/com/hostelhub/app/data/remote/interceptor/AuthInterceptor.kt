package com.hostelhub.app.data.remote.interceptor

import com.hostelhub.app.data.remote.datasource.TokenManager
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        // Exclude authentication endpoints from attaching bearer token
        val isAuthEndpoint = path.contains("auth/login") ||
                             path.contains("auth/register") ||
                             path.contains("auth/refresh")

        val token = if (!isAuthEndpoint) tokenManager.getAccessToken() else null

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Bypass-Tunnel-Reminder", "true")

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 403 || response.code == 401) {
            try {
                val peek = response.peekBody(2048).string()
                if (peek.contains("HOSTEL_ALLOCATION_INACTIVE") ||
                    peek.contains("ACCOUNT_DEALLOCATED") ||
                    peek.contains("allocation has ended") ||
                    peek.contains("allocation has been removed") ||
                    peek.contains("ACCOUNT_INACTIVE")) {
                    tokenManager.notifyDeallocated("Your hostel allocation has ended. You have been logged out.")
                }
            } catch (_: Exception) {}
        }

        return response
    }
}
