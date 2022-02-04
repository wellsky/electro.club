package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.entity.TransportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportDao {
    @Query("SELECT * FROM TransportEntity ORDER BY rating DESC")
    fun getAll(): Flow<List<TransportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transport: TransportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transports: List<TransportEntity>)

    @Query("DELETE FROM TransportEntity")
    suspend fun removeAll()
}