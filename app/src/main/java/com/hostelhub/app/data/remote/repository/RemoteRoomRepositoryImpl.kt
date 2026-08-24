package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.RoomApi
import com.hostelhub.app.data.remote.dto.AllocateBedRequestDto
import com.hostelhub.app.data.remote.dto.RoomDto
import com.hostelhub.app.data.remote.dto.VacateBedRequestDto
import com.hostelhub.app.domain.model.Room
import com.hostelhub.app.domain.repository.RoomRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteRoomRepositoryImpl @Inject constructor(
    private val roomApi: RoomApi
) : RoomRepository {

    override fun getRoomsByHostel(hostelId: String): Flow<Resource<List<Room>>> = flow {
        emit(Resource.Loading)
        try {
            val response = roomApi.getRoomsByHostel(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch rooms"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching rooms"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getRoomById(roomId: String): Flow<Resource<Room>> = flow {
        emit(Resource.Loading)
        try {
            val response = roomApi.getRoomById(roomId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch room details"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching room details"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addRoom(room: Room): Resource<Room> = withContext(Dispatchers.IO) {
        try {
            val dto = RoomDto(
                roomId = room.roomId,
                hostelId = room.hostelId,
                roomNumber = room.roomNumber,
                floor = room.floor,
                block = room.block,
                roomType = room.roomType.name,
                totalCapacity = room.totalCapacity,
                monthlyRent = room.monthlyRent,
                amenities = room.amenities
            )
            val response = roomApi.addRoom(room.hostelId, dto)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to add room")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error adding room")
        }
    }

    override suspend fun updateRoom(room: Room): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val dto = RoomDto(
                roomId = room.roomId,
                hostelId = room.hostelId,
                roomNumber = room.roomNumber,
                floor = room.floor,
                block = room.block,
                roomType = room.roomType.name,
                totalCapacity = room.totalCapacity,
                monthlyRent = room.monthlyRent,
                amenities = room.amenities,
                status = room.status.name
            )
            val response = roomApi.updateRoom(room.roomId, dto)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to update room")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error updating room")
        }
    }

    override suspend fun deleteRoom(roomId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = roomApi.deleteRoom(roomId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to delete room")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error deleting room")
        }
    }

    override suspend fun assignBed(roomId: String, bedId: String, studentId: String, studentName: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = roomApi.assignBed(
                AllocateBedRequestDto(
                    bedId = bedId,
                    roomId = roomId,
                    studentId = studentId
                )
            )
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to allocate bed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error allocating bed")
        }
    }

    override suspend fun vacateBed(bedId: String, roomId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = roomApi.vacateBed(
                VacateBedRequestDto(
                    bedId = bedId,
                    roomId = roomId
                )
            )
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to vacate bed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error vacating bed")
        }
    }
}
