package club.electro.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryServerImpl): AccountRepository
    @Binds
    @Singleton
    abstract fun bindPostRepository(impl: PostRepositoryServerImpl): PostRepository
    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryServerImpl): UserRepository
}