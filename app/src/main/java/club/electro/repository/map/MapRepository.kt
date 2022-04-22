package club.electro.repository.map

import club.electro.dto.MapMarkerData
import club.electro.dto.Socket
import club.electro.dto.SocketStatus
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    val markers: Flow<List<MapMarkerData>>

    suspend fun getAll()

    fun observeSocket(id: Long): Flow<Socket?>
    suspend fun updateSocket(id: Long)

    fun setMarkersFilter(list: List<Byte>)
    suspend fun setSocketStatus(socketId: Long, status: SocketStatus)
}