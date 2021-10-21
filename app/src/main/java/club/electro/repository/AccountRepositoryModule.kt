package club.electro.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

interface AccountRepositoryModule {
    @Singleton
    @Binds
    fun bindAccountRepository(impl: AccountRepositoryServerImpl): AccountRepository
}