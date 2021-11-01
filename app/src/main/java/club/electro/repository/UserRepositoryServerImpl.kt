package club.electro.repository

import club.electro.di.DependencyContainer
import club.electro.dto.UserProfile

class UserRepositoryServerImpl(
    diContainer: DependencyContainer,
) : UserRepository {

    override suspend fun getUserProfile(id: Long): UserProfile {
        return UserProfile(
            id = 123,
            name = "Vasya Pupkin",
            avatar = "https://electro.club/data/users/2193/avatar_large-sq200.jpg",
            messages = 256,
            rating = 123
        )
    }
}