package club.electro.repository.map

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class MapRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMapRepository(impl: MapRepositoryServerImpl): MapRepository
}