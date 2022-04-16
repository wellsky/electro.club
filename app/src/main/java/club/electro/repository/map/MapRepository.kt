package club.electro.repository.map

import club.electro.dto.MapMarker
import club.electro.dto.Socket
import club.electro.dto.SocketStatus
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    val markers: Flow<List<MapMarker>>

    suspend fun getAll()

    fun observeSocket(id: Long): Flow<Socket?>
    suspend fun updateSocket(id: Long)

    fun setMarkersFilter(list: List<Byte>)
    suspend fun setSocketStatus(socketId: Long, status: SocketStatus)
}