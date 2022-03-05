package club.electro.repository.subscriptions

import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface SubscriptionsRepository {
    fun items(group: Byte = 0): Flow<List<SubscriptionArea>>

    suspend fun getAll(group: Byte = 0)

    fun startCheckUpdates(group: Byte = 0)
    fun stopCheckUpdates()
}