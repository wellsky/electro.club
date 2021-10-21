package club.electro.dao

import club.electro.db.AppDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
object AreaDaoModule {
    @Provides
    fun provideAreaDao(db: AppDb): AreaDao = db.areaDao()
}