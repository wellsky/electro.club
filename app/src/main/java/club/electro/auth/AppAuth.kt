package club.electro.auth

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.PushToken
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class AppAuth private constructor(context: Context, diContainer: DependencyContainer) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"
    private val nameKey = "name"
    private val avatarKey = "avatar"

    private val _authStateFlow: MutableStateFlow<AuthState>

    private val resources = diContainer.context.resources
    private val apiService = diContainer.apiService

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)
        val name = prefs.getString(nameKey, null)
        val avatar = prefs.getString(avatarKey, null)

        if (id == 0L || token == null) {
            _authStateFlow = MutableStateFlow(AuthState())
            with(prefs.edit()) {
                clear()
                apply()
            }
        } else {
            _authStateFlow = MutableStateFlow(AuthState(id, token, name, avatar))
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

    @Synchronized
    fun setAuth(id: Long, token: String, name: String, avatar: String) {
        _authStateFlow.value = AuthState(id, token, name, avatar)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            putString(nameKey, name)
            putString(avatarKey, avatar)
            apply()

            //authState.value = _authStateFlow.value
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
                println("setPushToken")
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val params = HashMap<String?, String?>()
                params["access_token"] = resources.getString(R.string.electro_club_access_token)
                params["push_token"] = pushToken.token

                if (myId() != 0L) {
                    println("Token 1")
                    params["user_token"] = myToken()
                    params["method"] = "setPushToken"
                } else {
                    println("Token 0")
                    params["method"] = "destroyPushToken"
                }

                apiService.setPushToken(params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        @Volatile
        private var instance: AppAuth? = null

        fun getInstance(): AppAuth = synchronized(this) {
            instance ?: throw IllegalStateException(
                "AppAuth is not initialized, you must call AppAuth.initializeApp(Context context) first."
            )
        }

        fun initApp(context: Context, diContainer: DependencyContainer): AppAuth = instance ?: synchronized(this) {
            instance ?: buildAuth(context, diContainer).also { instance = it }
        }

        private fun buildAuth(context: Context, diContainer: DependencyContainer): AppAuth = AppAuth(context, diContainer)
    }
}

data class AuthState(val id: Long = 0, val token: String? = null, val name: String? = null, val avatar: String? = null)