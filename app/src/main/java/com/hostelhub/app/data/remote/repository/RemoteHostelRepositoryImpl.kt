package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.HostelApi
import com.hostelhub.app.data.remote.dto.AddHostelImagesRequestDto
import com.hostelhub.app.data.remote.dto.CreateReviewRequestDto
import com.hostelhub.app.data.remote.dto.HostelDto
import com.hostelhub.app.domain.model.AdminDashboardStats
import com.hostelhub.app.domain.model.HostDashboardStats
import com.hostelhub.app.domain.model.Hostel
import com.hostelhub.app.domain.model.HostelReview
import com.hostelhub.app.domain.repository.HostelRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteHostelRepositoryImpl @Inject constructor(
    private val hostelApi: HostelApi
) : HostelRepository {

    override fun getHostels(): Flow<Resource<List<Hostel>>> = flow {
        emit(Resource.Loading)
        try {
            val response = hostelApi.getHostels()
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch hostels"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching hostels"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getHostelById(hostelId: String): Flow<Resource<Hostel>> = flow {
        emit(Resource.Loading)
        try {
            val response = hostelApi.getHostelById(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch hostel details"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching hostel details"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateHostel(hostel: Hostel): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val dto = HostelDto(
                hostelId = hostel.hostelId,
                hostId = hostel.hostId,
                name = hostel.name,
                address = hostel.address,
                city = hostel.city,
                state = hostel.state,
                postalCode = hostel.postalCode,
                latitude = hostel.latitude,
                longitude = hostel.longitude,
                description = hostel.description,
                genderType = hostel.genderType.name,
                amenities = hostel.amenities,
                rules = hostel.rules,
                images = hostel.images,
                totalRooms = hostel.totalRooms,
                totalBeds = hostel.totalBeds,
                occupiedBeds = hostel.occupiedBeds,
                baseMonthlyRent = hostel.baseMonthlyRent,
                cautionDeposit = hostel.cautionDeposit,
                rating = hostel.rating,
                ratingCount = hostel.ratingCount,
                contactEmail = hostel.contactEmail,
                contactPhone = hostel.contactPhone
            )
            val response = hostelApi.updateHostel(hostel.hostelId, dto)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to update hostel")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error updating hostel")
        }
    }

    override fun getHostDashboardStats(hostelId: String): Flow<Resource<HostDashboardStats>> = flow {
        emit(Resource.Loading)
        try {
            val response = hostelApi.getHostDashboardStats(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch host dashboard stats"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching host dashboard stats"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getAdminDashboardStats(): Flow<Resource<AdminDashboardStats>> = flow {
        emit(Resource.Loading)
        try {
            val response = hostelApi.getAdminDashboardStats()
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch admin metrics"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching admin metrics"))
        }
    }.flowOn(Dispatchers.IO)

    override fun getHostelReviews(hostelId: String): Flow<Resource<List<HostelReview>>> = flow {
        emit(Resource.Loading)
        try {
            val response = hostelApi.getReviews(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch hostel reviews"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching reviews"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun submitReview(
        hostelId: String,
        studentId: String?,
        studentName: String?,
        rating: Double,
        comment: String,
        cleanliness: Double,
        foodQuality: Double,
        amenitiesRating: Double
    ): Resource<HostelReview> = withContext(Dispatchers.IO) {
        try {
            val request = CreateReviewRequestDto(
                studentId = studentId,
                studentName = studentName,
                rating = rating,
                comment = comment,
                cleanliness = cleanliness,
                foodQuality = foodQuality,
                amenitiesRating = amenitiesRating
            )
            val response = hostelApi.addReview(hostelId, request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to submit hostel review")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error submitting review")
        }
    }

    override suspend fun addHostelImages(hostelId: String, images: List<String>): Resource<Hostel> = withContext(Dispatchers.IO) {
        try {
            val request = AddHostelImagesRequestDto(images = images)
            val response = hostelApi.addHostelImages(hostelId, request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to upload hostel images")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error uploading images")
        }
    }
}
