package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.NotificationApi
import com.hostelhub.app.domain.model.AppNotification
import com.hostelhub.app.domain.repository.NotificationRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteNotificationRepositoryImpl @Inject constructor(
    private val notificationApi: NotificationApi
) : NotificationRepository {

    override fun getNotifications(userId: String): Flow<Resource<List<AppNotification>>> = flow {
        emit(Resource.Loading)
        try {
            val response = notificationApi.getNotifications(userId)
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { it.toDomain() }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch notifications"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching notifications"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun markAsRead(notificationId: String): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = notificationApi.markAsRead(notificationId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to mark notification as read")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error marking notification as read")
        }
    }
}
