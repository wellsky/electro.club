package club.electro.repository

import club.electro.dto.MapMarker
import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.flow.Flow

interface MapRepository {
    val data: Flow<List<MapMarker>>
    suspend fun getAll()
}