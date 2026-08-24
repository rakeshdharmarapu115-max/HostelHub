package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.ComplaintApi
import com.hostelhub.app.data.remote.dto.CreateComplaintRequestDto
import com.hostelhub.app.data.remote.dto.UpdateComplaintStatusRequestDto
import com.hostelhub.app.domain.model.Complaint
import com.hostelhub.app.domain.model.ComplaintStatus
import com.hostelhub.app.domain.repository.ComplaintRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteComplaintRepositoryImpl @Inject constructor(
    private val complaintApi: ComplaintApi
) : ComplaintRepository {

    override fun getComplaintsForStudent(studentId: String): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val response = complaintApi.getComplaintsForStudent(studentId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch complaints"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching complaints"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getComplaintsForHostel(hostelId: String): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val response = complaintApi.getComplaintsForHostel(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch hostel complaints"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching complaints"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAllComplaints(): Flow<Resource<List<Complaint>>> = flow {
        emit(Resource.Loading)
        try {
            val response = complaintApi.getAllComplaints()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch all complaints"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching complaints"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getComplaintById(complaintId: String): Flow<Resource<Complaint>> = flow {
        emit(Resource.Loading)
        try {
            val response = complaintApi.getComplaintById(complaintId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch complaint details"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching complaint"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun submitComplaint(complaint: Complaint): Resource<Complaint> = withContext(Dispatchers.IO) {
        try {
            val request = CreateComplaintRequestDto(
                hostelId = complaint.hostelId,
                studentId = complaint.studentId,
                studentName = complaint.studentName,
                roomNumber = complaint.roomNumber,
                category = complaint.category.name,
                title = complaint.title,
                description = complaint.description,
                attachments = complaint.attachments,
                urgency = complaint.urgency.name
            )
            val response = complaintApi.submitComplaint(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to submit complaint")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error submitting complaint")
        }
    }

    override suspend fun updateComplaintStatus(
        complaintId: String,
        status: ComplaintStatus,
        notes: String?,
        assignedStaff: String?,
        resolutionSummary: String?
    ): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateComplaintStatusRequestDto(
                status = status.name,
                notes = notes,
                assignedStaffName = assignedStaff,
                resolutionSummary = resolutionSummary ?: notes
            )
            val response = complaintApi.updateComplaintStatus(complaintId, request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to update complaint status")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error updating complaint status")
        }
    }

    override suspend fun deleteComplaint(complaintId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = complaintApi.deleteComplaint(complaintId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to delete complaint")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error deleting complaint")
        }
    }
}
