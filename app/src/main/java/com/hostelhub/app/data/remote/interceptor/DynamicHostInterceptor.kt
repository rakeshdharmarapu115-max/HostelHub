package com.hostelhub.app.data.remote.interceptor

import com.hostelhub.app.data.remote.NetworkConfig
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
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
            if (originalPath.startsWith("/api/api/")) {
                originalPath = originalPath.removePrefix("/api")
            }

            if (!originalPath.startsWith("/api") && parsedUrl.encodedPath.startsWith("/api")) {
                newUrlBuilder.encodedPath("/api" + if (originalPath.startsWith("/")) originalPath else "/$originalPath")
            } else {
                newUrlBuilder.encodedPath(originalPath)
            }

            request = request.newBuilder().url(newUrlBuilder.build()).build()
        }

        return chain.proceed(request)
    }
}
