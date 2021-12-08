package club.electro.repository

import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.MapMarker
import club.electro.entity.MapMarkerEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class MapRepositoryServerImpl(diContainer: DependencyContainer): MapRepository {
    val apiService = diContainer.apiService
    val appAuth = diContainer.appAuth
    val resources = diContainer.context
    val dao =diContainer.appDb.mapMarkerDao()

    override val data: Flow<List<MapMarker>> = dao.getAll().map(List<MapMarkerEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = apiService.getMapObjects(
                types = "6"
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.data.mapObjects.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}