package club.electro.repository.account

interface AccountRepository {
    suspend fun signIn(login: String, password: String): Boolean
    suspend fun signOut()
}