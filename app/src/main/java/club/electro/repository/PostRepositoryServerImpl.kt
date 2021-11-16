package club.electro.repository

import androidx.work.*
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.Post
import club.electro.entity.PostEntity
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import club.electro.workers.SavePostWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.IOException

class PostRepositoryServerImpl(diContainer: DependencyContainer): PostRepository {
    private val dao = diContainer.appDb.postDao()
    private val resources = diContainer.context.resources
    private val appAuth = diContainer.appAuth
    private val apiService = diContainer.apiService

    lateinit var workManager: WorkManager

    override fun setupWorkManager(workManager: WorkManager) {
        this.workManager = workManager
    }

    override suspend fun getLocalPostById(id: Long): Post? {
        GlobalScope.async {
            // TODO загрузить пост с сервера и вынести функцию в репозиторий PostRepository
        }
        return dao.getPostById(id)?.toDto()
    }

    override suspend fun savePostToChache(post: Post) {
        dao.insert(post.toEntity())
    }

    override suspend fun savePostToServer(post: Post) {
        println("Insert new post")

        val savingEntity = if (post.id == 0L) {
            PostEntity.fromDto(post).copy(
                status = Post.STATUS_CREATED_LOCAL,
                published = System.currentTimeMillis()/1000,
                authorId = appAuth.myId(),
                authorName = appAuth.myName() ?: "",
                authorAvatar = appAuth.myAvatar() ?: ""
            )

        } else {
            val exist = dao.getPostById(post.id)
            PostEntity.fromDto(post).copy(
                localId = exist.localId,
                status = Post.STATUS_SAVING_LOCAL,
            )
        }

        val localId = dao.insert(savingEntity)

        try {
            val data = workDataOf(SavePostWorker.localPostId to localId)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val request = OneTimeWorkRequestBuilder<SavePostWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .build()
            workManager.enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun savePostWork(localId: Long) {
        val entity = dao.getPostByLocalId(localId)
        println("Saving work for post: " + entity.localId)

        try {
            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "savePost"
            params["thread_type"] = entity.threadType.toString()
            params["thread_id"] = entity.threadId.toString()
            params["post_id"] = entity.id.toString()
            params["post_content"] = entity.content

            entity.answerTo?.let {
                params["answer_to"] = it.toString()
            }

            val response = apiService.savePost(params)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            val newPost = PostEntity.fromDto(body.data.message).copy(localId = localId)
            dao.insert(newPost)
            println("Have response new post id:" + body.data.message.id)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removePostFromServer(post: Post) {
        val exist = dao.getPostById(post.id)
        val removingPost = PostEntity.fromDto(post).copy(
            localId = exist.localId,
            status = Post.STATUS_REMOVING_LOCAL,
        )

        dao.insert(removingPost)

        try {
            val params = HashMap<String?, String?>()
            params["access_token"] = resources.getString(R.string.electro_club_access_token)
            params["user_token"] = appAuth.myToken()
            params["method"] = "removePost"
            params["thread_type"] = post.threadType.toString()
            params["thread_id"] = post.threadId.toString()
            params["post_id"] = post.id.toString()

            val response = apiService.removePost(params)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            //dao.insert(PostEntity.fromDto(body))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}