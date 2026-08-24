package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean = false,
    @SerializedName("message") val message: String = "",
    @SerializedName("data") val data: T? = null,
    @SerializedName("errors") val errors: List<ApiErrorDetail>? = null
)

data class ApiErrorDetail(
    @SerializedName("path") val path: String = "",
    @SerializedName("message") val message: String = ""
)
