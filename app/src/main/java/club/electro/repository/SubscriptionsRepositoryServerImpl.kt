package club.electro.repository

import android.app.Application
import club.electro.R
import club.electro.api.Api
import club.electro.dao.AreaDao
import club.electro.dto.SubscriptionArea
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.entity.AreaEntity
import ru.netology.nmedia.entity.toDto
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.*
import java.io.IOException

// TODO - убрать val перед aaplication, когда getString() уже не понадобится
class SubscriptionsRepositoryServerImpl(val application: Application) : SubscriptionsRepository {
    private val dao: AreaDao = AppDb.getInstance(context = application).areaDao()
    override val data: Flow<List<SubscriptionArea>> = dao.getAll().map(List<AreaEntity>::toDto).flowOn(Dispatchers.Default)

    override suspend fun getAll() {
        try {
            val params = HashMap<String?, String?>()
            params["access_token"] = application.getString(R.string.electro_club_access_token)
            params["user_token"] = application.getString(R.string.temp_user_token)
            params["method"] = "whatsUp"
            params["lastEventTime"] = "0"
            val response = Api.service.getSubscriptions(params)

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.data.items.toEntity())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}