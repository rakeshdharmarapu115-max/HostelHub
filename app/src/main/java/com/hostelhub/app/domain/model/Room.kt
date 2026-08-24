package com.hostelhub.app.domain.model

data class Room(
    val roomId: String = "",
    val hostelId: String = "",
    val roomNumber: String = "",
    val floor: Int = 1,
    val block: String = "A",
    val roomType: RoomType = RoomType.DOUBLE,
    val totalCapacity: Int = 2,
    val occupiedCount: Int = 0,
    val monthlyRent: Double = 0.0,
    val amenities: List<String> = emptyList(),
    val beds: List<Bed> = emptyList(),
    val status: RoomStatus = RoomStatus.AVAILABLE,
    val createdAt: Long = System.currentTimeMillis()
)

data class Bed(
    val bedId: String = "",
    val bedNumber: String = "A",
    val studentId: String? = null,
    val studentName: String? = null,
    val isOccupied: Boolean = false
)

enum class RoomType {
    SINGLE,
    DOUBLE,
    TRIPLE,
    DORMITORY
}

enum class RoomStatus {
    AVAILABLE,
    FULL,
    MAINTENANCE
}
