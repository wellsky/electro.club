package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.entity.DiscussionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscussionDao {
    @Query("SELECT * FROM DiscussionEntity ORDER BY lastMessageTime DESC")
    fun getAll(): Flow<List<DiscussionEntity>>

    @Query("SELECT * FROM DiscussionEntity WHERE transportId = :id ORDER BY lastMessageTime DESC")
    fun getByTransportId(id: Long): Flow<List<DiscussionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: DiscussionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<DiscussionEntity>)

    @Query("DELETE FROM DiscussionEntity")
    suspend fun removeAll()
}