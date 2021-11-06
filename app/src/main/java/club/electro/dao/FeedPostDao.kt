package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.entity.FeedPostEntity
import club.electro.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedPostDao {
    @Query("SELECT * FROM FeedPostEntity ORDER BY sorting DESC")
    fun flowFeedByPublshedDESC(): Flow<List<FeedPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: FeedPostEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<FeedPostEntity>)
}