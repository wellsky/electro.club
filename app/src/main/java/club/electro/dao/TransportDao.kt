package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.dto.Transport
import club.electro.entity.TransportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportDao {
    @Query("SELECT * FROM TransportEntity WHERE id = :id")
    fun getTransportById(id: Long): TransportEntity?

    @Query("SELECT * FROM TransportEntity WHERE name LIKE '%' || :filter || '%' ORDER BY rating DESC LIMIT 100")
    fun getFilteredList(filter: String): Flow<List<TransportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transport: TransportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transports: List<TransportEntity>)

    @Query("DELETE FROM TransportEntity")
    suspend fun removeAll()
}