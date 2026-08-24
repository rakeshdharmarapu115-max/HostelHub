package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.AnnouncementApi
import com.hostelhub.app.data.remote.dto.CreateAnnouncementRequestDto
import com.hostelhub.app.domain.model.Announcement
import com.hostelhub.app.domain.repository.AnnouncementRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteAnnouncementRepositoryImpl @Inject constructor(
    private val announcementApi: AnnouncementApi
) : AnnouncementRepository {

    override fun getAnnouncements(hostelId: String): Flow<Resource<List<Announcement>>> = flow {
        emit(Resource.Loading)
        try {
            val response = announcementApi.getAnnouncements(hostelId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch announcements"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching announcements"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun createAnnouncement(announcement: Announcement): Resource<Announcement> = withContext(Dispatchers.IO) {
        try {
            val request = CreateAnnouncementRequestDto(
                hostelId = announcement.hostelId,
                senderId = announcement.senderId,
                title = announcement.title,
                message = announcement.message,
                priority = announcement.priority.name,
                targetAudience = announcement.targetAudience,
                attachmentUrls = announcement.attachmentUrls
            )
            val response = announcementApi.createAnnouncement(request)
            if (response.isSuccessful && response.body()?.data != null) {
                Resource.Success(response.body()!!.data!!.toDomain())
            } else {
                Resource.Error(response.body()?.message ?: "Failed to create announcement")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error creating announcement")
        }
    }

    override suspend fun deleteAnnouncement(announcementId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = announcementApi.deleteAnnouncement(announcementId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to delete announcement")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error deleting announcement")
        }
    }
}
