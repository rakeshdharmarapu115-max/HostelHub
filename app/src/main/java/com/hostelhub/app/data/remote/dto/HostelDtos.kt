package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.Hostel
import com.hostelhub.app.domain.model.HostelGenderType
import com.hostelhub.app.domain.model.HostelReview

data class HostelDto(
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("hostId") val hostId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("address") val address: String = "",
    @SerializedName("city") val city: String = "",
    @SerializedName("state") val state: String = "",
    @SerializedName("postalCode") val postalCode: String = "",
    @SerializedName("latitude") val latitude: Double = 0.0,
    @SerializedName("longitude") val longitude: Double = 0.0,
    @SerializedName("description") val description: String = "",
    @SerializedName("genderType") val genderType: String = "COED",
    @SerializedName("amenities") val amenities: List<String> = emptyList(),
    @SerializedName("rules") val rules: List<String> = emptyList(),
    @SerializedName("images") val images: List<String> = emptyList(),
    @SerializedName("totalRooms") val totalRooms: Int = 0,
    @SerializedName("totalBeds") val totalBeds: Int = 0,
    @SerializedName("occupiedBeds") val occupiedBeds: Int = 0,
    @SerializedName("baseMonthlyRent") val baseMonthlyRent: Double = 0.0,
    @SerializedName("cautionDeposit") val cautionDeposit: Double = 0.0,
    @SerializedName("rating") val rating: Double = 0.0,
    @SerializedName("ratingCount") val ratingCount: Int = 0,
    @SerializedName("contactEmail") val contactEmail: String = "",
    @SerializedName("contactPhone") val contactPhone: String = "",
    @SerializedName("paymentAccountId") val paymentAccountId: String? = null,
    @SerializedName("paymentAccountStatus") val paymentAccountStatus: String = "ACTIVE",
    @SerializedName("paymentQrUrl") val paymentQrUrl: String? = null,
    @SerializedName("qrPaymentEnabled") val qrPaymentEnabled: Boolean = true,
    @SerializedName("upiId") val upiId: String? = null,
    @SerializedName("merchantName") val merchantName: String? = null,
    @SerializedName("distanceKm") val distanceKm: Double? = null,
    @SerializedName("availableBeds") val availableBeds: Int? = null,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis(),
    @SerializedName("reviews") val reviews: List<HostelReviewDto> = emptyList()
) {
    fun toDomain(): Hostel {
        val gender = try {
            HostelGenderType.valueOf(genderType.uppercase())
        } catch (e: Exception) {
            HostelGenderType.COED
        }
        return Hostel(
            hostelId = hostelId,
            hostId = hostId,
            name = name,
            address = address,
            city = city,
            state = state,
            postalCode = postalCode,
            latitude = latitude,
            longitude = longitude,
            description = description,
            genderType = gender,
            amenities = amenities,
            rules = rules,
            images = images,
            totalRooms = totalRooms,
            totalBeds = totalBeds,
            occupiedBeds = occupiedBeds,
            baseMonthlyRent = baseMonthlyRent,
            cautionDeposit = cautionDeposit,
            rating = rating,
            ratingCount = ratingCount,
            contactEmail = contactEmail,
            contactPhone = contactPhone,
            paymentAccountId = paymentAccountId,
            paymentAccountStatus = paymentAccountStatus,
            paymentQrUrl = paymentQrUrl,
            qrPaymentEnabled = qrPaymentEnabled,
            upiId = upiId,
            merchantName = merchantName,
            distanceKm = distanceKm,
            availableBeds = availableBeds,
            createdAt = createdAt,
            reviews = reviews.map { it.toDomain() }
        )
    }
}

data class HostelPaymentConfigDto(
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("hostelName") val hostelName: String = "",
    @SerializedName("hostId") val hostId: String = "",
    @SerializedName("hostName") val hostName: String = "",
    @SerializedName("hostContactPhone") val hostContactPhone: String = "",
    @SerializedName("hostContactEmail") val hostContactEmail: String = "",
    @SerializedName("paymentAccountId") val paymentAccountId: String? = null,
    @SerializedName("paymentAccountStatus") val paymentAccountStatus: String = "ACTIVE",
    @SerializedName("paymentQrUrl") val paymentQrUrl: String? = null,
    @SerializedName("qrPaymentEnabled") val qrPaymentEnabled: Boolean = true,
    @SerializedName("upiId") val upiId: String? = null,
    @SerializedName("merchantName") val merchantName: String? = null,
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("studentRollNumber") val studentRollNumber: String? = null,
    @SerializedName("roomNumber") val roomNumber: String? = null,
    @SerializedName("totalPendingDues") val totalPendingDues: Double = 0.0
) {
    fun toDomain(): com.hostelhub.app.domain.model.HostelPaymentConfig {
        return com.hostelhub.app.domain.model.HostelPaymentConfig(
            hostelId = hostelId,
            hostelName = hostelName,
            hostId = hostId,
            hostName = hostName,
            hostContactPhone = hostContactPhone,
            hostContactEmail = hostContactEmail,
            paymentAccountId = paymentAccountId,
            paymentAccountStatus = paymentAccountStatus,
            paymentQrUrl = paymentQrUrl,
            qrPaymentEnabled = qrPaymentEnabled,
            upiId = upiId,
            merchantName = merchantName,
            studentId = studentId,
            studentName = studentName,
            studentRollNumber = studentRollNumber,
            roomNumber = roomNumber,
            totalPendingDues = totalPendingDues
        )
    }
}

data class UpdatePaymentConfigRequestDto(
    @SerializedName("paymentAccountId") val paymentAccountId: String? = null,
    @SerializedName("paymentAccountStatus") val paymentAccountStatus: String? = null,
    @SerializedName("paymentQrUrl") val paymentQrUrl: String? = null,
    @SerializedName("qrPaymentEnabled") val qrPaymentEnabled: Boolean? = null,
    @SerializedName("upiId") val upiId: String? = null,
    @SerializedName("merchantName") val merchantName: String? = null
)

data class HostelReviewDto(
    @SerializedName("reviewId") val reviewId: String = "",
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("studentId") val studentId: String = "",
    @SerializedName("studentName") val studentName: String = "",
    @SerializedName("rating") val rating: Double = 5.0,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("cleanliness") val cleanliness: Double? = 5.0,
    @SerializedName("foodQuality") val foodQuality: Double? = 5.0,
    @SerializedName("amenitiesRating") val amenitiesRating: Double? = 5.0,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): HostelReview {
        return HostelReview(
            reviewId = reviewId,
            hostelId = hostelId,
            studentId = studentId,
            studentName = studentName,
            rating = rating,
            comment = comment,
            cleanliness = cleanliness,
            foodQuality = foodQuality,
            amenitiesRating = amenitiesRating,
            createdAt = createdAt
        )
    }
}

data class CreateReviewRequestDto(
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("rating") val rating: Double,
    @SerializedName("comment") val comment: String? = null,
    @SerializedName("cleanliness") val cleanliness: Double? = 5.0,
    @SerializedName("foodQuality") val foodQuality: Double? = 5.0,
    @SerializedName("amenitiesRating") val amenitiesRating: Double? = 5.0
)

data class AddHostelImagesRequestDto(
    @SerializedName("images") val images: List<String>
)
