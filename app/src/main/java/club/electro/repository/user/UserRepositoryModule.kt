package club.electro.repository.user

import androidx.lifecycle.SavedStateHandle
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
interface UserRepositoryModule {
    companion object {
        @Provides
        @ViewModelScoped
        fun provideArg(savedStateHandle: SavedStateHandle): String = requireNotNull(savedStateHandle["userId"])
    }

    @Reusable
    @Binds
    fun bindUserRepository(impl: UserRepositoryServerImpl): UserRepository
}