package club.electro.repository.transport

import club.electro.dto.TransportPreview
import kotlinx.coroutines.flow.Flow

interface TransportRepository {
    fun getTransportPreview(id: Long): TransportPreview
    fun getTransportPreview(filter: String): Flow<List<TransportPreview>>
}