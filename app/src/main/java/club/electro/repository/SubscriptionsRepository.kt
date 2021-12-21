package club.electro.repository

import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface SubscriptionsRepository {
    val data: Flow<List<SubscriptionArea>>
    suspend fun getAll()
    suspend fun checkForUpdates()

    fun startCheckUpdates()
    fun stopCheckUpdates()
}