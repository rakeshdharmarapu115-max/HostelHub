package com.hostelhub.app.data.remote.repository

import com.hostelhub.app.data.remote.api.FoodMenuApi
import com.hostelhub.app.data.remote.dto.UpdateFoodMenuRequestDto
import com.hostelhub.app.domain.model.FoodMenu
import com.hostelhub.app.domain.repository.FoodMenuRepository
import com.hostelhub.app.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteFoodMenuRepositoryImpl @Inject constructor(
    private val foodMenuApi: FoodMenuApi
) : FoodMenuRepository {

    override fun getWeeklyMenu(hostelId: String, weekStartDate: String): Flow<Resource<FoodMenu>> = flow {
        emit(Resource.Loading)
        try {
            val response = foodMenuApi.getWeeklyMenu(hostelId, weekStartDate)
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!.toDomain()))
            } else {
                emit(Resource.Error(response.body()?.message ?: "Failed to fetch food menu"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Network error fetching food menu"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun updateWeeklyMenu(menu: FoodMenu): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = UpdateFoodMenuRequestDto(
                hostelId = menu.hostelId,
                weekStartDate = menu.weekStartDate,
                schedule = menu.schedule,
                specialNotice = menu.specialNotice,
                isPublished = menu.isPublished
            )
            val response = foodMenuApi.updateWeeklyMenu(request)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error(response.body()?.message ?: "Failed to update food menu")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error updating food menu")
        }
    }
}
