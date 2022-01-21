package club.electro.repository.subscriptions

import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.AreaDao
import club.electro.dto.SubscriptionArea
import club.electro.entity.AreaEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.*
import club.electro.model.NetworkStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class SubscriptionsRepositoryServerImpl @Inject constructor(
    private val apiService: ApiService,
    private val appAuth : AppAuth,
    private val dao: AreaDao,
    private val networkStatus: NetworkStatus

) : SubscriptionsRepository {

    override val data: Flow<List<SubscriptionArea>> = dao.getAll().map(List<AreaEntity>::toDto).flowOn(Dispatchers.Default)

    private var lastEventTime = 0L
    private lateinit var updaterJob: Job

    override suspend fun getAll() {
        appAuth.myToken()?.let { myToken ->
            try {
                val response = apiService.getSubscriptions()

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())

                dao.removeAll()
                dao.insert(body.data.items.toEntity())
                lastEventTime = body.data.lastEventTime

                networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            } catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }

    override suspend fun checkForUpdates()  {
        while (true) {
            delay(2_000L)
            try {
                val response = apiService.getSubscriptions(
                    lastEventTime = lastEventTime
                )

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())

                body.let {
                    if (it.data.items.isNotEmpty()) {
                        dao.removeAll()
                        dao.insert(body.data.items.toEntity())
                        lastEventTime = body.data.lastEventTime
                    }
                    networkStatus.setStatus(NetworkStatus.Status.ONLINE)
                }
            }  catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
            } catch (e: Exception) {
                // throw UnknownError
            }
        }
    }

    override fun startCheckUpdates() {
        updaterJob = CoroutineScope(Dispatchers.Default).launch {
            checkForUpdates()
        }
    }

    override fun stopCheckUpdates() {
        updaterJob.cancel()
    }
}