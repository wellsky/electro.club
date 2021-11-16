package club.electro.repository

import androidx.work.*
import club.electro.R
import club.electro.di.DependencyContainer
import club.electro.dto.Post
import club.electro.dto.User
import club.electro.entity.PostEntity
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.NetworkError
import club.electro.error.UnknownError
import club.electro.workers.SavePostWorker
import kotlinx.coroutines.*
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

    override suspend fun getLocalById(threadType: Byte, threadId:Long, id: Long, onLoadedCallback:  (suspend () -> Unit)?): Post? {
        return dao.getById(threadType, threadId, id)?.let {
            it.toDto()
        } ?: onLoadedCallback?.run {
            dao.insert(Post(
                localId = 0,
                id = id,
                status = 0,
                threadType = threadType,
                threadId = threadId,
                authorId = 0,
                authorName = "Loading author...",
                authorAvatar = "",
                content = "",
                published = 0L,
            ).toEntity())

            CoroutineScope(Dispatchers.Default).launch {
                if (id == 262638L) {
                    println("coroutine")
                }
                delay(3000)
                dao.getById(threadType, threadId, id)?.let {
                    if (it.published == 0L) {
                        if (it.id == 262638L) {
                            println("load")
                        }
                        val post = getRemoteById(threadType, threadId, id)
                        dao.insert(post.toEntity())
                        delay(1000)
                    }
                    if (it.id == 262638L) {
                        println("callback " + it.authorName)
                    }
                    onLoadedCallback()
                }
            }
            null
        }
    }

    override suspend fun getRemoteById(threadType: Byte, threadId: Long, id: Long): Post {
        println("Load remote post: " + id)
        try {
            val response = apiService.getThreadPosts(
                access_token = resources.getString(R.string.electro_club_access_token),
                user_token = appAuth.myToken(),
                threadType = threadType,
                threadId = threadId,
                from = id.toString(),
                included = 1,
                count = 1
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            //dao.insert(body.data.user.toDto().toEntity())
            return body.data.messages[0]
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun updateLocalPostPreparedContent(threadType: Byte, threadId: Long, id: Long, preparedContent: String) {
        //println("Update content for: " + post.id + " localId: " + post.localId)
        dao.updatePreparedContent(threadType, threadId, id, preparedContent)
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
                authorAvatar = appAuth.myAvatar() ?: "",
                fresh = true
            )

        } else {
            val exist = dao.getById(post.threadType, post.threadId, post.id)
            PostEntity.fromDto(post).copy(
                localId = exist!!.localId, // TODO
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
        val entity = dao.getByLocalId(localId)

        entity?.let { entity ->
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
    }

    override suspend fun removePostFromServer(post: Post) {
        val exist = dao.getById(post.threadType, post.threadId, post.id)

        val removingPost = PostEntity.fromDto(post).copy(
            localId = exist!!.localId, //TODO
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