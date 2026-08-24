package com.hostelhub.app.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RetryInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0
        val maxLimit = 3

        while (tryCount < maxLimit) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful || response.code in 400..499) {
                    return response
                }
                // Server 502/503/504 Bad Gateway from cloud container spin-up or proxy: retry
                if (response.code in 502..504 && tryCount < maxLimit - 1) {
                    response.close()
                    tryCount++
                    Thread.sleep((600L * tryCount))
                    continue
                }
                return response
            } catch (e: UnknownHostException) {
                // Invalid domain or no internet: throw immediately
                throw e
            } catch (e: ConnectException) {
                // Connection refused: server process is not running or port is closed
                exception = e
                tryCount++
                if (tryCount >= 2) throw e
                try { Thread.sleep(400L) } catch (_: Exception) {}
            } catch (e: SocketTimeoutException) {
                // Container cold-start on cloud: retry
                exception = e
                tryCount++
                if (tryCount >= maxLimit) throw e
                try { Thread.sleep(600L * tryCount) } catch (_: Exception) {}
            } catch (e: IOException) {
                exception = e
                tryCount++
                if (tryCount >= maxLimit) throw e
                try { Thread.sleep(500L * tryCount) } catch (_: Exception) {}
            }
        }

        throw exception ?: IOException("Network request failed after $maxLimit attempts")
    }
}

