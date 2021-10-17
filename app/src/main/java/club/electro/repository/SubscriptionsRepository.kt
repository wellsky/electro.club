package club.electro.repository

import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.flow.Flow

interface SubscriptionsRepository {
    val data: Flow<List<SubscriptionArea>>
    suspend fun getAll()
}