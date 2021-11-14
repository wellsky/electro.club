package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Query("SELECT COUNT(*) == 0 FROM PostRemoteKeyEntity WHERE threadType = :threadType AND threadId = :threadId")
    suspend fun isEmpty(threadType: Byte, threadId: Long): Boolean

    @Query("SELECT MAX(postId) FROM PostRemoteKeyEntity WHERE threadType = :threadType AND threadId = :threadId")
    suspend fun max(threadType: Byte, threadId: Long): Long?

    @Query("SELECT MIN(postId) FROM PostRemoteKeyEntity WHERE threadType = :threadType AND threadId = :threadId")
    suspend fun min(threadType: Byte, threadId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<PostRemoteKeyEntity>)

    @Query("UPDATE PostRemoteKeyEntity SET postId = :postId WHERE type = :type AND threadType = :threadType AND threadId = :threadId")
    suspend fun update(type: PostRemoteKeyEntity.KeyType, threadType: Byte, threadId: Long, postId: Long)

    @Query("DELETE FROM PostRemoteKeyEntity WHERE threadType = :threadType AND threadId = :threadId")
    suspend fun removeThread(threadType: Byte, threadId: Long)
}