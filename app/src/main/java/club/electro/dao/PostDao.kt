package club.electro.dao

import androidx.room.*
import club.electro.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId ORDER BY published DESC")
    fun flowThreadByPublshedDESC(threadType: Byte, threadId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId ORDER BY published")
    suspend fun getAllList(threadType: Byte, threadId: Long): List<PostEntity>

    @Query("SELECT * FROM PostEntity WHERE id = :id")
    fun getPostById(id: Long): PostEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM PostEntity")
    suspend fun clearAll()

    @Query("DELETE FROM PostEntity WHERE threadType = :threadType AND threadId = :threadId AND published >= :from AND published <= :to")
    suspend fun clearThreadPeriod(threadType: Byte, threadId: Long, from: Long, to: Long)

    @Transaction
    suspend fun clearAndInsert(posts: List<PostEntity>, threadType: Byte, threadId: Long, from: Long, to: Long) {
        clearThreadPeriod(threadType, threadId, from, to)
        insert(posts)
    }
}