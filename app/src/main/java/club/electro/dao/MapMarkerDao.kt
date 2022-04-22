package club.electro.dao

import androidx.room.*
import club.electro.entity.MapMarkerDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MapMarkerDao {
    @Query("SELECT * FROM MapMarkerDataEntity")
    fun getAll(): Flow<List<MapMarkerDataEntity>>

    @Query("SELECT * FROM MapMarkerDataEntity WHERE type IN(:types)")
    fun getByTypes(types: List<Byte>): Flow<List<MapMarkerDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(markerData: MapMarkerDataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(markerData: List<MapMarkerDataEntity>)

    @Transaction
    suspend fun reset(markerData: List<MapMarkerDataEntity>) {
        clear()
        insert(markerData)
    }

    @Query("DELETE FROM MapMarkerDataEntity")
    suspend fun clear()
}