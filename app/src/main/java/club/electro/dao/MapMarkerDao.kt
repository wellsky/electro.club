package club.electro.dao

import androidx.room.*
import club.electro.entity.MapMarkerEntity
import club.electro.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapMarkerDao {
    @Query("SELECT * FROM MapMarkerEntity")
    fun getAll(): Flow<List<MapMarkerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(marker: MapMarkerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(markers: List<MapMarkerEntity>)

    @Query("DELETE FROM MapMarkerEntity")
    suspend fun clear()
}