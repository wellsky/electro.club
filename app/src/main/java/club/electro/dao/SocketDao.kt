package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.dto.PostsThread
import club.electro.dto.Socket
import club.electro.entity.SocketEntity
import club.electro.entity.ThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SocketDao {
    @Query("SELECT * FROM SocketEntity WHERE id = :id")
    fun observe(id: Long): Flow<Socket?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(socket: SocketEntity)

    @Query("DELETE FROM SocketEntity")
    suspend fun removeAll()
}