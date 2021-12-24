package club.electro.dao

import androidx.paging.PagingSource
import androidx.room.*
import club.electro.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId AND fresh = 1 ORDER BY published DESC")
    fun freshPosts(threadType: Byte, threadId: Long): PagingSource<Int, PostEntity>

    @Query("UPDATE PostEntity SET fresh = 0 WHERE threadType = :threadType AND threadId = :threadId")
    suspend fun unfreshThread(threadType: Byte, threadId: Long)

    @Query("UPDATE PostEntity SET preparedContent = :content WHERE threadType = :threadType AND threadId = :threadId AND id = :id")
    suspend fun updatePreparedContent(threadType: Byte, threadId: Long, id: Long, content: String)

    @Query("SELECT * FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId AND id = :id")
    suspend fun getById(threadType: Byte, threadId:Long, id: Long): PostEntity?

    @Query("SELECT * FROM PostEntity WHERE localId = :id")
    suspend fun getByLocalId(id: Long): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()
}