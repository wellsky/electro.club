package club.electro.repository.map

import android.content.Context
import club.electro.api.ApiService
import club.electro.api.NetworkService
import club.electro.dao.MapMarkerDao
import club.electro.dao.SocketDao
import club.electro.dto.MARKER_TYPE_GROUP
import club.electro.dto.MARKER_TYPE_SOCKET
import club.electro.dto.Socket
import club.electro.dto.SocketStatus
import club.electro.entity.MapMarkerDataEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

class MapRepositoryServerImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val apiService: ApiService,
    private val markerDao: MapMarkerDao,
    private val socketDao: SocketDao,
    private val networkService: NetworkService
): MapRepository {

    val resources = context.resources

    var targetFlow = MutableStateFlow(value = listOf<Byte>())

    override val markers = targetFlow.flatMapLatest { list ->
        markerDao.getByTypes(list).map(List<MapMarkerDataEntity>::toDto).flowOn(Dispatchers.Default)
    }

    override fun setMarkersFilter(list: List<Byte>) {
        targetFlow.value = list.toList() // Необходимо создавать копию списка, чтобы трегернуть flatMapLatest
    }

    override suspend fun getAll() {
        try {
            val response = apiService.getMapObjects(
                types = MARKER_TYPE_SOCKET.toString() + "+" + MARKER_TYPE_GROUP.toString()
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            markerDao.reset(body.data.map_objects.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun observeSocket(id: Long): Flow<Socket?> = socketDao.observe(id)

    override suspend fun updateSocket(id: Long) {
        networkService.safeApiCall(
            apiCall = {
                apiService.getSocketDetails(
                    socketId = id
                )
            },
            onSuccess = {
                socketDao.insert(it.data.socket.toEntity())
            }
        )
    }

    override suspend fun setSocketStatus(socketId: Long, status: SocketStatus) {
        networkService.safeApiCall(
            apiCall = {
                apiService.setSocketStatus(
                    socketId = socketId,
                    status = status
                )
            },
            onSuccess = {
                socketDao.insert(it.data.socket.toEntity())
            }
        )
    }
}