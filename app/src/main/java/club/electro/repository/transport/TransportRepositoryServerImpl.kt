package club.electro.repository.transport

import club.electro.dto.TransportPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TransportRepositoryServerImpl @Inject constructor(

): TransportRepository {
    override fun getTransportPreview(id: Long): TransportPreview {
        TODO("Not yet implemented")
    }

    override fun getTransportPreview(filter: String): Flow<List<TransportPreview>> = flow {
        emit(listOf(
            TransportPreview(
                1,
                "Transport1",
                "https://electro.club/data/transport/400/80.jpg?upd=1534494032",
                25,
                4.5F
            ),
            TransportPreview(
                2,
                "Transport2",
                "https://electro.club/data/transport/928/80.jpg?upd=1606214880",
                25,
                4.5F
            ),
            TransportPreview(
                3,
                "Transport3",
                "https://electro.club/data/transport/864/80.jpg?upd=1597670783",
                25,
                4.5F
            )
        ))
    }
}