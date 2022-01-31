package club.electro.dao

import androidx.room.*
import club.electro.entity.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM AreaEntity ORDER BY lastTime DESC")
    fun getAll(): Flow<List<AreaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(area: AreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(areas: List<AreaEntity>)

    @Query("DELETE FROM AreaEntity")
    suspend fun removeAll()
}