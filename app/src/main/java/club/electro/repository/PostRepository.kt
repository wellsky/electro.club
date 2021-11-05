package club.electro.repository

import club.electro.dto.Post

interface PostRepository {
    suspend fun getLocalPostById(id: Long): Post?
}