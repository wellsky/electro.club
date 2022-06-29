package club.electro.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import club.electro.dao.*
import club.electro.entity.*


@Database(entities = [
    AreaEntity::class,
    PostEntity::class,
    PostRemoteKeyEntity::class,
    MapMarkerDataEntity::class,
    UserEntity::class,
    FeedPostEntity::class,
    ThreadEntity::class,
    SocketEntity::class,
    TransportEntity::class,
    DiscussionEntity::class,
    PostAttachmentEntity::class,
    NotificationEntity::class,
 ], version = 150, exportSchema = false)
@TypeConverters(DBTypeConverters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao
    abstract fun feedPostDao(): FeedPostDao
    abstract fun areaDao(): AreaDao
    abstract fun mapMarkerDao(): MapMarkerDao
    abstract fun userDao(): UserDao
    abstract fun threadDao(): ThreadDao
    abstract fun socketDao(): SocketDao
    abstract fun transportDao(): TransportDao
    abstract fun discussionDao(): DiscussionDao
    abstract fun postDraftAttachmentDao(): PostAttachmentDao
    abstract fun notificationDao(): NotificationDao
}