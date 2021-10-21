package club.electro.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

interface FeedRepositoryModule {
    @Singleton
    @Binds
    fun bindFeedRepository(impl: FeedRepositoryInMemoryImpl): FeedRepository
}