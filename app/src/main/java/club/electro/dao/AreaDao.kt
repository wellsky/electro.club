package club.electro.dao

import androidx.room.*
import club.electro.entity.AreaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {
    @Query("SELECT * FROM AreaEntity WHERE `group` = :group ORDER BY lastTime DESC")
    fun getAll(group: Byte): Flow<List<AreaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(area: AreaEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(areas: List<AreaEntity>)

    @Query("DELETE FROM AreaEntity")
    suspend fun removeAll()

    @Query("DELETE FROM AreaEntity WHERE `group` = :group")
    suspend fun removeAll(group: Byte)
}