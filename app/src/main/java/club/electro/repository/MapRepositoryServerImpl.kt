package club.electro.repository

import club.electro.di.DependencyContainer
import club.electro.dto.MapMarker
import club.electro.dto.Socket
import club.electro.entity.MapMarkerEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class MapRepositoryServerImpl(diContainer: DependencyContainer): MapRepository {
    val apiService = diContainer.apiService
    val appAuth = diContainer.appAuth
    val resources = diContainer.context

    val markerDao =diContainer.appDb.mapMarkerDao()
    val socketDao =diContainer.appDb.socketDao()

    val MARKER_TYPE_SOCKET: Int = 6

    override val data: Flow<List<MapMarker>> = markerDao.getAll().map(List<MapMarkerEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val response = apiService.getMapObjects(
                types = MARKER_TYPE_SOCKET.toString()
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            markerDao.insert(body.data.mapObjects.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun getSocket(id: Long): Flow<Socket> = flow {
        emit(socketDao.get(id))
        try {
            val response = apiService.getSocketDetails(
                socketId = id
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            socketDao.insert(body.data.socket.toEntity())
            emit(body.data.socket)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }.flowOn(Dispatchers.Default)
}