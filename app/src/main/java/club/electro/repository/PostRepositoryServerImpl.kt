package club.electro.repository

import club.electro.di.DependencyContainer
import club.electro.dto.Post
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class PostRepositoryServerImpl(diContainer: DependencyContainer): PostRepository {
    val dao = diContainer.appDb.postDao()

    override suspend fun getLocalPostById(id: Long): Post? {
        GlobalScope.async {
            // TODO загрузить пост с сервера и вынести функцию в репозиторий PostRepository
        }
        return dao.getPostById(id)?.toDto()
    }
}