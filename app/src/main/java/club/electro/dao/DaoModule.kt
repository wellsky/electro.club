package club.electro.dao

import club.electro.db.AppDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object DaoModule {
    @Provides
    fun provideAreaDao(db: AppDb): AreaDao = db.areaDao()
    @Provides
    fun provideFeedPostDao(db: AppDb): FeedPostDao = db.feedPostDao()
    @Provides
    fun provideMapMarkerDao(db: AppDb): MapMarkerDao = db.mapMarkerDao()
    @Provides
    fun providePostDao(db: AppDb): PostDao = db.postDao()
    @Provides
    fun providePostRemoteKeyDao(db: AppDb): PostRemoteKeyDao = db.postRemoteKeyDao()
    @Provides
    fun provideSocketDao(db: AppDb): SocketDao = db.socketDao()
    @Provides
    fun provideThreadDao(db: AppDb): ThreadDao = db.threadDao()
    @Provides
    fun provideUserDao(db: AppDb): UserDao = db.userDao()
    @Provides
    fun provideTransportDao(db: AppDb): TransportDao = db.transportDao()
    @Provides
    fun provideDiscussionDao(db: AppDb): DiscussionDao = db.discussionDao()
    @Provides
    fun providePostDraftAttachmentDao(db: AppDb): PostDraftAttachmentDao = db.postDraftAttachmentDao()

}