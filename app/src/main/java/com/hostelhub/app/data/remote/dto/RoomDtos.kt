package com.hostelhub.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.hostelhub.app.domain.model.Bed
import com.hostelhub.app.domain.model.Room
import com.hostelhub.app.domain.model.RoomStatus
import com.hostelhub.app.domain.model.RoomType

data class RoomDto(
    @SerializedName("roomId") val roomId: String = "",
    @SerializedName("hostelId") val hostelId: String = "",
    @SerializedName("roomNumber") val roomNumber: String = "",
    @SerializedName("floor") val floor: Int = 1,
    @SerializedName("block") val block: String = "A",
    @SerializedName("roomType") val roomType: String = "DOUBLE",
    @SerializedName("totalCapacity") val totalCapacity: Int = 2,
    @SerializedName("occupiedCount") val occupiedCount: Int = 0,
    @SerializedName("monthlyRent") val monthlyRent: Double = 0.0,
    @SerializedName("amenities") val amenities: List<String> = emptyList(),
    @SerializedName("beds") val beds: List<BedDto> = emptyList(),
    @SerializedName("status") val status: String = "AVAILABLE",
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Room {
        val rType = try {
            RoomType.valueOf(roomType.uppercase())
        } catch (e: Exception) {
            RoomType.DOUBLE
        }
        val rStatus = try {
            RoomStatus.valueOf(status.uppercase())
        } catch (e: Exception) {
            RoomStatus.AVAILABLE
        }
        return Room(
            roomId = roomId,
            hostelId = hostelId,
            roomNumber = roomNumber,
            floor = floor,
            block = block,
            roomType = rType,
            totalCapacity = totalCapacity,
            occupiedCount = occupiedCount,
            monthlyRent = monthlyRent,
            amenities = amenities,
            beds = beds.map { it.toDomain() },
            status = rStatus,
            createdAt = createdAt
        )
    }
}

data class BedDto(
    @SerializedName("bedId") val bedId: String = "",
    @SerializedName("bedNumber") val bedNumber: String = "A",
    @SerializedName("studentId") val studentId: String? = null,
    @SerializedName("studentName") val studentName: String? = null,
    @SerializedName("isOccupied") val isOccupied: Boolean = false
) {
    fun toDomain(): Bed {
        return Bed(
            bedId = bedId,
            bedNumber = bedNumber,
            studentId = studentId,
            studentName = studentName,
            isOccupied = isOccupied
        )
    }
}

data class AllocateBedRequestDto(
    @SerializedName("bedId") val bedId: String,
    @SerializedName("roomId") val roomId: String,
    @SerializedName("studentId") val studentId: String,
    @SerializedName("remarks") val remarks: String? = null
)

data class VacateBedRequestDto(
    @SerializedName("bedId") val bedId: String? = null,
    @SerializedName("roomId") val roomId: String? = null,
    @SerializedName("allocationId") val allocationId: String? = null
)
