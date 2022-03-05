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

    override suspend fun getAll(global: Boolean) {
        try {
            val response = apiService.getSubscriptions(
                global = if (global) 1 else 0
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            insertItems(body.data.items, global)

            lastEventTime = body.data.lastEventTime
            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun checkForUpdates(global: Boolean)  {
        while (true) {
            delay(2_000L)
            try {
                val response = apiService.getSubscriptions(
                    global = if (global) 1 else 0,
                    lastEventTime = lastEventTime
                )

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())

                body.let {
                    if (it.data.items.isNotEmpty()) {
                        insertItems(body.data.items, global)
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

    private suspend fun insertItems(items: List<SubscriptionArea>, global: Boolean) {
        if (global) {
            dao.removeAll()
            dao.insert(items.toEntity())
        } else {
            dao.removeAll()
            dao.insert(items.toEntity())
        }
    }

    override fun startCheckUpdates(global: Boolean) {
        updaterJob = CoroutineScope(Dispatchers.Default).launch {
            checkForUpdates(global)
        }
    }

    override fun stopCheckUpdates() {
        updaterJob.cancel()
    }
}