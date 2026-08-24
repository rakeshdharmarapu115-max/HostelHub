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
            createdAt = createdAt,
            reviews = reviews.map { it.toDomain() }
        )
    }
}

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
