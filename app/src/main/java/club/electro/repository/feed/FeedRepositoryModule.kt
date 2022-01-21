package club.electro.repository.feed

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class FeedRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFeedRepository(impl: FeedRepositoryServerImpl): FeedRepository
}