package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.dto.PostsThread
import club.electro.entity.ThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ThreadDao {
    @Query("SELECT * FROM ThreadEntity WHERE type = :type AND id = :id")
    fun get(type: Byte, id: Long): Flow<PostsThread>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(thread: ThreadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(threads: List<ThreadEntity>)

    @Query("DELETE FROM ThreadEntity")
    suspend fun removeAll()
}