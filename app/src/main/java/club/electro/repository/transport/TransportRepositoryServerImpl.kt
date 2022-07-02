package club.electro.repository.transport

import androidx.lifecycle.LiveData
import club.electro.api.ApiService
import club.electro.api.NetworkService
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
    private val networkService: NetworkService,
): TransportRepository {

    private val targetList = MutableStateFlow(value = "")

    override val list = targetList.flatMapLatest { filter->
        transportDao.getFilteredList(filter).map(List<TransportEntity>::toPreviewDto).flowOn(Dispatchers.Default)
    }

    override suspend fun getPreviewList(filter: String) {
        networkService.safeApiCall(
            apiCall = {
                apiService.getTransportList(filter = filter)
            },
            onSuccess = {
                transportDao.insert(it.data.list.toEntity())
            }
        )
//        try {
//            val response = apiService.getTransportList(filter = filter)
//
//            if (!response.isSuccessful) {
//                throw ApiError(response.code(), response.message())
//            }
//            val body = response.body() ?: throw ApiError(response.code(), response.message())
//
//            transportDao.insert(body.data.list.toEntity())
//
//            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
//        } catch (e: IOException) {
//            networkStatus.setStatus(NetworkStatus.Status.ERROR)
//        } catch (e: Exception) {
//            throw UnknownError
//        }
    }

    override fun setPreviewListFilter(filter: String) {
        targetList.value = filter
    }

    override fun getTransportById(id: Long): Flow<Transport> = flow {
        transportDao.getTransportById(id)?.let {
            emit(it.toDto())
        }

        networkService.safeApiCall(
            apiCall = {
                apiService.getTransport(
                    transportId = id
                )
            },
            onSuccess = { response ->
                transportDao.insert(response.data.transport.toEntity())
                response.data.discussions?.let {
                    discussionDao.insert(it.toEntity())
                }
                emit(response.data.transport)
            }
        )
    }.flowOn(Dispatchers.Default)

    override fun getDiscussionsByTransportId(id: Long): Flow<List<Discussion>> = discussionDao.getByTransportId(id).map{ it.toDto() }
}