package club.electro.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import club.electro.dao.*
import club.electro.entity.*


@Database(entities = [
    AreaEntity::class,
    PostEntity::class,
    PostRemoteKeyEntity::class,
    MapMarkerEntity::class,
    UserEntity::class,
    FeedPostEntity::class,
    ThreadEntity::class
 ], version = 88, exportSchema = false)
@TypeConverters(DBTypeConverters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun feedPostDao(): FeedPostDao
    abstract fun areaDao(): AreaDao
    abstract fun mapMarkerDao(): MapMarkerDao
    abstract fun userDao(): UserDao
    abstract fun threadDao(): ThreadDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context, AppDb::class.java, "app.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}