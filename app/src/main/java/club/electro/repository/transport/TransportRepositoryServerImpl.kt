package club.electro.repository.transport

import androidx.lifecycle.LiveData
import club.electro.api.ApiService
import club.electro.dao.DiscussionDao
import club.electro.dao.TransportDao
import club.electro.dto.Discussion
import club.electro.dto.Transport
import club.electro.entity.*
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import club.electro.model.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.IOException
import javax.inject.Inject

class TransportRepositoryServerImpl @Inject constructor(
    private val transportDao: TransportDao,
    private val discussionDao: DiscussionDao,
    private val apiService: ApiService,
    private val networkStatus: NetworkStatus,
): TransportRepository {

    val targetList = MutableStateFlow(value = "")

    override val list = targetList.flatMapLatest { filter->
        transportDao.getFilteredList(filter).map(List<TransportEntity>::toPreviewDto).flowOn(Dispatchers.Default)
    }

    override suspend fun getPreviewList(filter: String) {
        try {
            val response = apiService.getTransportList(filter = filter)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            transportDao.insert(body.data.list.toEntity())

            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override fun setPreviewListFilter(filter: String) {
        targetList.value = filter
    }

    override fun getTransportById(id: Long): Flow<Transport> = flow {
        transportDao.getTransportById(id)?.let {
            emit(it.toDto())
        }

        try {
            val response = apiService.getTransport(
                transportId = id
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            transportDao.insert(body.data.transport.toEntity())
            body.data.discussions?.let {
                discussionDao.insert(it.toEntity())
            }

            emit(body.data.transport)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }.flowOn(Dispatchers.Default)

    override fun getDiscussionsByTransportId(id: Long): Flow<List<Discussion>> = discussionDao.getByTransportId(id).map{ it.toDto() }
}