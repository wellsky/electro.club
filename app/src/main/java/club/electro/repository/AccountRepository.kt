package club.electro.repository

interface AccountRepository {
    suspend fun signIn(login: String, password: String): Boolean
    suspend fun signOut()
}