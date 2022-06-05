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
        networkService.safeApiCall(
            apiCall = {
                apiService.getMapObjects(
                    types = "$MARKER_TYPE_SOCKET+$MARKER_TYPE_GROUP"
                )
            },
            onSuccess = {
                markerDao.reset(it.data.map_objects.toEntity())
            }
        )
    }

    override fun observeSocket(id: Long): Flow<Socket?> = socketDao.observe(id)

    override suspend fun getSocketDetails(id: Long) {
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
                getAll()
            }
        )
    }
}