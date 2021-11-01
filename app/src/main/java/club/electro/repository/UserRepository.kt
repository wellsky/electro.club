package club.electro.repository

import club.electro.dto.UserProfile

interface UserRepository {
    suspend fun getUserProfile(id: Long): UserProfile
}