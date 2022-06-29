package club.electro.repository.notifications

import club.electro.repository.subscriptions.SubscriptionsRepository
import club.electro.repository.subscriptions.SubscriptionsRepositoryServerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class NotificationsRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindNotificationsRepository(impl: NotificationsRepositoryImpl): NotificationsRepository
}
