package club.electro.repository

import club.electro.dto.MapMarker
import club.electro.dto.Socket
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    val data: Flow<List<MapMarker>>

    suspend fun getAll()
    fun getSocket(id: Long): Flow<Socket>
}