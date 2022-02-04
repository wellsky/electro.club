package club.electro.repository.transport

import club.electro.dto.TransportPreview
import kotlinx.coroutines.flow.Flow

interface TransportRepository {
    val list: Flow<List<TransportPreview>>
    //fun getTransportPreview(id: Long): TransportPreview
    suspend fun getPreviewList(filter: String)
}