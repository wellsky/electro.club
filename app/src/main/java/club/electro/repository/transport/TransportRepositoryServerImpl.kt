package club.electro.repository.transport

import club.electro.api.ApiService
import club.electro.dao.TransportDao
import club.electro.dto.TransportPreview
import club.electro.entity.*
import club.electro.error.ApiError
import club.electro.error.UnknownError
import club.electro.model.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class TransportRepositoryServerImpl @Inject constructor(
    private val dao: TransportDao,
    private val apiService: ApiService,
    private val networkStatus: NetworkStatus,
): TransportRepository {

    override val  list: Flow<List<TransportPreview>> = dao.getAll().map(List<TransportEntity>::toPreviewDto).flowOn(Dispatchers.Default)

    override suspend fun getPreviewList(filter: String) {
        try {
            val response = apiService.getTransportList(filter = "")

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            dao.removeAll()
            dao.insert(body.data.list.toEntity())

            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}