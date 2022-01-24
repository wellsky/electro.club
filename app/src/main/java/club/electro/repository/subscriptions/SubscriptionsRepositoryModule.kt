package club.electro.repository.subscriptions

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class SubscriptionsRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindSubscriptionsRepository(impl: SubscriptionsRepositoryServerImpl): SubscriptionsRepository
}
