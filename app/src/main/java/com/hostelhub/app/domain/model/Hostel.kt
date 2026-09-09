package com.hostelhub.app.domain.model

data class Hostel(
    val hostelId: String = "",
    val hostId: String = "",
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val postalCode: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val genderType: HostelGenderType = HostelGenderType.COED,
    val amenities: List<String> = emptyList(),
    val rules: List<String> = emptyList(),
    val images: List<String> = emptyList(),
    val totalRooms: Int = 0,
    val totalBeds: Int = 0,
    val occupiedBeds: Int = 0,
    val baseMonthlyRent: Double = 0.0,
    val cautionDeposit: Double = 0.0,
    val rating: Double = 0.0,
    val ratingCount: Int = 0,
    val contactEmail: String = "",
    val contactPhone: String = "",
    val paymentAccountId: String? = null,
    val paymentAccountStatus: String = "ACTIVE",
    val paymentQrUrl: String? = null,
    val qrPaymentEnabled: Boolean = true,
    val upiId: String? = null,
    val merchantName: String? = null,
    val distanceKm: Double? = null,
    val availableBeds: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val reviews: List<HostelReview> = emptyList()
)

data class HostelPaymentConfig(
    val hostelId: String = "",
    val hostelName: String = "",
    val hostId: String = "",
    val hostName: String = "",
    val hostContactPhone: String = "",
    val hostContactEmail: String = "",
    val paymentAccountId: String? = null,
    val paymentAccountStatus: String = "ACTIVE",
    val paymentQrUrl: String? = null,
    val qrPaymentEnabled: Boolean = true,
    val upiId: String? = null,
    val merchantName: String? = null,
    val studentId: String? = null,
    val studentName: String? = null,
    val studentRollNumber: String? = null,
    val roomNumber: String? = null,
    val totalPendingDues: Double = 0.0
)

data class HostelReview(
    val reviewId: String = "",
    val hostelId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val rating: Double = 5.0,
    val comment: String? = null,
    val cleanliness: Double? = 5.0,
    val foodQuality: Double? = 5.0,
    val amenitiesRating: Double? = 5.0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class HostelGenderType {
    BOYS,
    GIRLS,
    COED
}
