package club.electro.repository.post

import android.content.Context
import androidx.work.*
import club.electro.adapter.PostsEntitiesPreparator
import club.electro.api.ApiService
import club.electro.auth.AppAuth
import club.electro.dao.PostDao
import club.electro.dto.Post
import club.electro.entity.PostEntity
import club.electro.entity.toEntity
import club.electro.error.ApiError
import club.electro.error.UnknownError
import club.electro.model.NetworkStatus
import club.electro.workers.SavePostWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.io.IOException
import javax.inject.Inject

class PostRepositoryServerImpl @Inject constructor(
    @ApplicationContext private val context : Context,
    private val dao : PostDao,
    private val apiService: ApiService,
    private val appAuth: AppAuth,
    private val networkStatus: NetworkStatus,
    private val postsEntitiesPreparatorFactory: PostsEntitiesPreparator.Factory
): PostRepository {


    private val workManager: WorkManager = WorkManager.getInstance(context)

    override suspend fun getLocalById(threadType: Byte, threadId:Long, id: Long, onLoadedCallback:  (suspend () -> Unit)?): Post? {
        return dao.getById(threadType, threadId, id)?.let {
            if (it.status != Post.STATUS_WAITING_FOR_LOAD) {
                it.toDto()
            } else {
                null
            }
        } ?: onLoadedCallback?.run {
            dao.insert(Post(
                localId = 0,
                id = id,
                status = Post.STATUS_WAITING_FOR_LOAD,
                threadType = threadType,
                threadId = threadId,
                authorId = 0,
                authorName = "Loading author...",
                published = 0L,
            ).toEntity())

            CoroutineScope(Dispatchers.Default).launch {
                delay(2000) // Это задержка нужна!!! Скорее всего, запрашиваемые посты уже запрошены пагинатором. Поэтому даем ему время 2 сек и если они не появятся в базе, то запрашиваем отдельно.
                dao.getById(threadType, threadId, id)?.let {
                    if (it.published == 0L) {
                        val post = getRemoteById(threadType, threadId, id)
                        post?.let {
                            dao.insert(post.toEntity())
                        } ?: run {
                            //TODO удалить созданный временно пост
                        }
                    }
                    onLoadedCallback()
                }
            }
            null
        }
    }

    override suspend fun getRemoteById(threadType: Byte, threadId: Long, id: Long): Post? {
        try {
            val response = apiService.getThreadPosts(
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

            networkStatus.setStatus(NetworkStatus.Status.ONLINE)

            return if (body.data.messages.size > 0) {
                body.data.messages[0]
            } else {
                null
            }
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
            return null
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun prepareAndSaveLocal(postsEntities: List<PostEntity>) {
        postsEntitiesPreparatorFactory.create(
            postsEntities = postsEntities,
            onFirstResult = {
                dao.insert(it)
            },
            onEveryDataUpdate = {
                dao.updatePreparedContent(
                    it.threadType,
                    it.threadId,
                    it.id,
                    it.preparedContent ?: ""
                )
            }
        ).prepareAll()
    }

    override suspend fun prepareAndSaveLocal(postEntity: PostEntity) {
        prepareAndSaveLocal(listOf(postEntity))
    }

    override suspend fun savePostToServer(post: Post) {
        val savingEntity = if (post.id == 0L) {
            PostEntity.fromDto(post).copy(
                status = Post.STATUS_CREATED_LOCAL,
                published = System.currentTimeMillis()/1000,
                authorId = appAuth.myId(),
                authorName = appAuth.myName() ?: "",
                authorAvatar = appAuth.myAvatar() ?: "",
                preparedContent = post.content,
                fresh = true
            )
        } else {
            val exist = dao.getById(post.threadType, post.threadId, post.id)
            PostEntity.fromDto(post).copy(
                localId = exist!!.localId, // TODO
                status = Post.STATUS_SAVING_LOCAL,
                fresh = true
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
        dao.getByLocalId(localId)?.let { entity ->
            try {
                val response = apiService.savePost(
                    threadType = entity.threadType,
                    threadId = entity.threadId,
                    postId = entity.id,
                    postContent = entity.content,
                    answerTo = entity.answerTo
                )

                if (!response.isSuccessful) {
                    throw ApiError(response.code(), response.message())
                }
                val body = response.body() ?: throw ApiError(response.code(), response.message())

                val currentCached = dao.getByLocalId(localId)

                val newPost = PostEntity.fromDto(body.data.message).copy(localId = localId, fresh = (currentCached?.fresh ?: false))

                prepareAndSaveLocal(newPost)

                networkStatus.setStatus(NetworkStatus.Status.ONLINE)
            } catch (e: IOException) {
                networkStatus.setStatus(NetworkStatus.Status.ERROR)
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
            val response = apiService.removePost(
                threadType = post.threadType,
                threadId = post.threadId,
                postId = post.id
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            networkStatus.setStatus(NetworkStatus.Status.ONLINE)
        } catch (e: IOException) {
            networkStatus.setStatus(NetworkStatus.Status.ERROR)
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}