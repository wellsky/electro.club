package club.electro.repository.transport

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class TransportRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTransportRepository(impl: TransportRepositoryServerImpl): TransportRepository
}