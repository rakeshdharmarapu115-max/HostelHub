package com.hostelhub.app.utils

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

object ImageUtils {

    /**
     * Resolves an image URL (relative / absolute / data URI) to a fully qualified URL
     * using the app's configured backend base URL.
     */
    fun resolveFullImageUrl(rawUrl: String?, baseUrl: String): String {
        if (rawUrl.isNullOrBlank()) return ""
        val trimmed = rawUrl.trim()

        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("data:", ignoreCase = true)
        ) {
            return trimmed
        }

        // Handle relative URLs like "/uploads/payment_qrs/qr_123.png"
        val hostRoot = baseUrl.substringBefore("/api").removeSuffix("/")
        val cleanPath = if (trimmed.startsWith("/")) trimmed else "/$trimmed"
        return "$hostRoot$cleanPath"
    }

    /**
     * Converts a content:// or file:// Uri into a MultipartBody.Part for upload.
     */
    fun createMultipartFromUri(
        context: Context,
        uri: Uri,
        partName: String = "file"
    ): MultipartBody.Part? {
        return try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri) ?: "image/png"
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.use { it.readBytes() }

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull(), 0, bytes.size)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "png"
            val filename = "qr_${System.currentTimeMillis()}.$extension"

            MultipartBody.Part.createFormData(partName, filename, requestBody)
        } catch (e: Exception) {
            null
        }
    }
}
