package club.electro.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import club.electro.api.ApiService
import club.electro.dto.PushToken
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext val context: Context,
) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"
    private val nameKey = "name"
    private val avatarKey = "avatar"
    private val transportNameKey = "transportName"
    private val transportImageKey = "transportImage"

    private val _authStateFlow: MutableStateFlow<AuthState>

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)
        val name = prefs.getString(nameKey, null)
        val avatar = prefs.getString(avatarKey, null)
        val transportName = prefs.getString(transportNameKey, null)
        val transportImage = prefs.getString(transportImageKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token, name, avatar, transportName, transportImage))
        }
        sendPushToken()
    }

    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()
    val authState: LiveData<AuthState> = authStateFlow.asLiveData()

    fun myId(): Long {
        return authStateFlow.value.id
    }

    fun myToken(): String? {
        return authStateFlow.value.token
    }

    fun myName(): String? {
        return authStateFlow.value.name
    }

    fun myAvatar(): String? {
        return authStateFlow.value.avatar
    }

    fun authorized():Boolean {
        return (myId() != 0L)
    }

    @Synchronized
    fun setAuth(id: Long, token: String, name: String, avatar: String, transportName: String?, transportImage: String?) {
        _authStateFlow.value = AuthState(id, token, name, avatar, transportName, transportImage)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            putString(nameKey, name)
            putString(avatarKey, avatar)
            putString(transportNameKey, transportName)
            putString(transportImageKey, transportImage)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val apiService =  getApiService()
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val params = HashMap<String?, String?>()

                params["push_token"] = pushToken.token

                if (myId() != 0L) {
                    params["user_token"] = myToken()
                    params["method"] = "setPushToken"
                } else {
                    params["method"] = "destroyPushToken"
                }

                //apiService.setPushToken(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun apiService(): ApiService
    }

    private fun getApiService(): ApiService {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context,
            AppAuthEntryPoint::class.java
        )
        return hiltEntryPoint.apiService()
    }
}

data class AuthState(
    val id: Long = 0L,
    val token: String? = null,
    val name: String? = null,
    val avatar: String? = null,
    val transportName: String? = null,
    val transportImage: String? = null,

    val authorized: Boolean = (id!= 0L)
)