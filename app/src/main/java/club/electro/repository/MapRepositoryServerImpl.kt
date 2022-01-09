package club.electro.repository

import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.MapMarkerDao
import club.electro.dao.SocketDao
import club.electro.di.DependencyContainer
import club.electro.dto.MARKER_TYPE_GROUP
import club.electro.dto.MARKER_TYPE_SOCKET
import club.electro.dto.MapMarker
import club.electro.dto.Socket
import club.electro.entity.MapMarkerEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

class MapRepositoryServerImpl(diContainer: DependencyContainer): MapRepository {
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var markerDao: MapMarkerDao
    @Inject
    lateinit var socketDao: SocketDao
    @Inject
    lateinit var appAuth: AppAuth

    val resources = diContainer.context

    var targetFlow = MutableStateFlow(value = listOf<Byte>())

    override val markers = targetFlow.flatMapLatest { list ->
        markerDao.getByTypes(list).map(List<MapMarkerEntity>::toDto).flowOn(Dispatchers.Default)
    }

    override fun setMerkersFilter(list: List<Byte>) {
        targetFlow.value = list.toList() // Необходимо создавать копию списка, чтобы трегернуть flatMapLatest
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getMapObjects(
                types = MARKER_TYPE_SOCKET.toString() + "+" + MARKER_TYPE_GROUP.toString()
                //types = MARKER_TYPE_GROUP.toString()
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
        socketDao.get(id)?.let {
            emit(it)
        }

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