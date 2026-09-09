package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StorageUploadResponseDto(
    @SerializedName("url") val url: String = "",
    @SerializedName("publicId") val publicId: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("bytes") val bytes: Long? = null,
    @SerializedName("provider") val provider: String? = null
)
