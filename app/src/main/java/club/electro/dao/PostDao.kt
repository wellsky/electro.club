package club.electro.dao

import androidx.paging.PagingSource
import androidx.room.*
import club.electro.dto.Post
import club.electro.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId ORDER BY published DESC")
    fun flowThreadByPublshedDESC(threadType: Byte, threadId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId ORDER BY published DESC")
    fun pagingSource(threadType: Byte, threadId: Long): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId ORDER BY published")
    suspend fun getAllList(threadType: Byte, threadId: Long): List<PostEntity>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    suspend fun getPostById(id: Long): PostEntity

    @Query("SELECT * FROM PostEntity WHERE localId = :id")
    suspend fun getPostByLocalId(id: Long): PostEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId")
    suspend fun removeThread(threadType: Byte, threadId: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()
}

//    @Query("DELETE FROM PostEntity WHERE (status = ${club.electro.dto.Post.STATUS_PUBLISHED} OR status = ${club.electro.dto.Post.STATUS_REMOVING_LOCAL}) AND threadType = :threadType AND threadId = :threadId AND published >= :from AND published <= :to")
//    suspend fun clearThreadPeriod(threadType: Byte, threadId: Long, from: Long, to: Long)
//
//    @Transaction
//    suspend fun clearAndInsert(posts: List<PostEntity>, threadType: Byte, threadId: Long, from: Long, to: Long) {
//        clearThreadPeriod(threadType, threadId, from, to)
//        insert(posts)
//    }