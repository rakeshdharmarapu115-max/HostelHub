package com.hostelhub.app.data.remote.interceptor

import android.util.Log
import com.hostelhub.app.BuildConfig
import com.hostelhub.app.data.remote.NetworkConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DynamicHostInterceptor @Inject constructor(
    private val networkConfig: NetworkConfig
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val currentBaseUrl = networkConfig.getBaseUrl()
        val parsedUrl = currentBaseUrl.toHttpUrlOrNull()

        if (parsedUrl != null) {
            val newUrlBuilder = request.url.newBuilder()
                .scheme(parsedUrl.scheme)
                .host(parsedUrl.host)
                .port(parsedUrl.port)

            var originalPath = request.url.encodedPath

            // Strip redundant leading /api segments to prevent /api/api/ duplication
            while (originalPath.startsWith("/api/api/")) {
                originalPath = originalPath.removePrefix("/api")
            }

            if (!originalPath.startsWith("/api")) {
                newUrlBuilder.encodedPath("/api" + if (originalPath.startsWith("/")) originalPath else "/$originalPath")
            } else {
                newUrlBuilder.encodedPath(originalPath)
            }

            request = request.newBuilder().url(newUrlBuilder.build()).build()
        }

        val method = request.method
        val url = request.url.toString()
        val startTime = System.currentTimeMillis()

        if (BuildConfig.DEBUG) {
            Log.d("HostelHub_Network", "🚀 [HTTP Request] $method -> $url (Base: $currentBaseUrl)")
        }

        return try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime
            if (BuildConfig.DEBUG) {
                Log.d("HostelHub_Network", "✅ [HTTP Response] ${response.code} $method -> $url (${duration}ms)")
            }
            response
        } catch (e: IOException) {
            val duration = System.currentTimeMillis() - startTime
            if (BuildConfig.DEBUG) {
                Log.e("HostelHub_Network", "❌ [HTTP Error] $method -> $url failed after ${duration}ms: ${e.message}")
            }
            throw e
        }
    }
}
