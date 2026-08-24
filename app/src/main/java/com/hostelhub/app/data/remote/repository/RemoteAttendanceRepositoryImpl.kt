package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.AttendanceApi
import com.hostelhub.app.data.remote.dto.BatchAttendanceRequestDto
import com.hostelhub.app.data.remote.dto.MarkAttendanceRequestDto
import com.hostelhub.app.domain.model.AttendanceRecord
import com.hostelhub.app.domain.repository.AttendanceRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAttendanceRepositoryImpl @Inject constructor(
    private val attendanceApi: AttendanceApi
) : AttendanceRepository {

    override fun getAttendanceForStudent(
        studentId: String,
        month: Int,
        year: Int
    ): Flow<Resource<List<AttendanceRecord>>> = flow {
        emit(Resource.Loading)
        try {
            val response = attendanceApi.getAttendanceForStudent(studentId, month, year)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch attendance records"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching attendance"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAttendanceForHostel(
        hostelId: String,
        date: String
    ): Flow<Resource<List<AttendanceRecord>>> = flow {
        emit(Resource.Loading)
        try {
            val response = attendanceApi.getAttendanceForHostel(hostelId, date)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch hostel attendance"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching attendance"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun markAttendance(record: AttendanceRecord): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = MarkAttendanceRequestDto(
                hostelId = record.hostelId,
                studentId = record.studentId,
                studentName = record.studentName,
                roomNumber = record.roomNumber,
                date = record.date,
                status = record.status.name,
                checkInTime = record.checkInTime,
                remarks = record.remarks,
                markedBy = record.markedBy
            )
            val response = attendanceApi.markAttendance(request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to record attendance")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error recording attendance")
        }
    }

    override suspend fun markBatchAttendance(records: List<AttendanceRecord>): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestList = records.map {
                MarkAttendanceRequestDto(
                    hostelId = it.hostelId,
                    studentId = it.studentId,
                    studentName = it.studentName,
                    roomNumber = it.roomNumber,
                    date = it.date,
                    status = it.status.name,
                    checkInTime = it.checkInTime,
                    remarks = it.remarks,
                    markedBy = it.markedBy
                )
            }
            val response = attendanceApi.markBatchAttendance(BatchAttendanceRequestDto(requestList))
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to record batch attendance")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error recording batch attendance")
        }
    }
}
