package club.electro.repository

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

interface ThreadRepositoryModule {
    @Singleton
    @Binds
    fun bindThreadRepository(impl: ThreadRepositoryServerImpl): ThreadRepository
}

//@InstallIn(SingletonComponent::class)
//@Module
//class AccountModule {
//    @Provides
//    @Singleton
//    fun provideComparableVersion(threadId: Long): ThreadRepositoryServerImpl {
//        return ThreadRepositoryServerImpl((threadId as Long))
//    }
//}