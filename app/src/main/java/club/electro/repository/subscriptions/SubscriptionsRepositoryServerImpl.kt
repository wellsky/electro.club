package club.electro.repository.subscriptions

import androidx.room.withTransaction
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.AreaDao
import club.electro.db.AppDb
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
    private val db: AppDb,
    private val dao: AreaDao,
    private val networkStatus: NetworkStatus
) : SubscriptionsRepository {

    //override val data: Flow<List<SubscriptionArea>> = dao.getAll().map(List<AreaEntity>::toDto).flowOn(Dispatchers.Default)

    private var lastEventTime = 0L
    private lateinit var updaterJob: Job

    override fun items(group: Byte): Flow<List<SubscriptionArea>> =
        dao.getAll(group).map(List<AreaEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun getAll(group: Byte) {
        try {
            val response = apiService.getSubscriptions(
                group = group
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())

            insertItems(body.data.items, group)

            lastEventTime = body.data.lastEventTime
            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
        } catch (e: Exception) {
            //throw UnknownError
        }
    }

    private suspend fun checkForUpdates(group: Byte) {
        while (true) {
            delay(2_000L)
            try {
                val response = apiService.getSubscriptions(
                    group = group,
                    lastEventTime = lastEventTime
                )

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())

                body.let {
                    if (it.data.items.isNotEmpty()) {
                        insertItems(body.data.items, group)
                        lastEventTime = body.data.lastEventTime
                    }
                    networkStatus.setStatus(NetworkStatus.Status.ONLINE)
                }
            } catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
            } catch (e: Exception) {
                // throw UnknownError
            }
        }
    }

    private suspend fun insertItems(items: List<SubscriptionArea>, group: Byte) {
        db.withTransaction {
            dao.removeAll(group)
            dao.insert(items.toEntity().map {
                it.copy(
                    group = group
                )
            })
        }
    }

    override fun startCheckUpdates(group: Byte) {
        updaterJob = CoroutineScope(Dispatchers.Default).launch {
            checkForUpdates(group)
        }
    }

    override fun stopCheckUpdates() {
        updaterJob.cancel()
    }
}