package club.electro.repository.user

import androidx.lifecycle.SavedStateHandle
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@InstallIn(ViewModelComponent::class)
@Module
interface UserRepositoryModule {
    companion object {
        @Provides
        @ViewModelScoped
        fun provideArg(savedStateHandle: SavedStateHandle): String = requireNotNull(savedStateHandle["userId"])
    }

    @Binds
    @ViewModelScoped
    fun bindUserRepository(impl: UserRepositoryServerImpl): UserRepository
}