package club.electro.repository.transport

import club.electro.dto.Transport
import club.electro.dto.TransportPreview
import kotlinx.coroutines.flow.Flow

interface TransportRepository {
    val list: Flow<List<TransportPreview>>

    suspend fun getPreviewList(filter: String)
    fun setPreviewListFilter(filter: String)
    fun getTransportById(id: Long): Flow<Transport>
}