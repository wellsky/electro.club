package club.electro.repository

import club.electro.R
import club.electro.dao.AreaDao
import club.electro.di.DependencyContainer
import club.electro.dto.SubscriptionArea
import club.electro.entity.AreaEntity
import club.electro.entity.toDto
import club.electro.entity.toEntity
import club.electro.error.*
import club.electro.model.NetworkStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.io.IOException

class SubscriptionsRepositoryServerImpl(diContainer: DependencyContainer) : SubscriptionsRepository {
    val appDb = diContainer.appDb
    val apiService = diContainer.apiService
    val appAuth = diContainer.appAuth
    val networkStatus = diContainer.networkStatus

    private val dao: AreaDao = appDb.areaDao()
    override val data: Flow<List<SubscriptionArea>> = dao.getAll().map(List<AreaEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        appAuth.myToken()?.let { myToken ->
            try {
                val response = apiService.getSubscriptions()

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())
                dao.insert(body.data.items.toEntity())
                networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            } catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
            } catch (e: Exception) {
                throw UnknownError
            }
        }
    }
}