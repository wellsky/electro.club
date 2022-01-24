package club.electro.repository.thread

import androidx.lifecycle.SavedStateHandle
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Named

@InstallIn(ViewModelComponent::class)
@Module
interface ThreadRepositoryModule {
    companion object {
        @Provides
        @ViewModelScoped
        @Named("threadType")
        fun provideThreadType(savedStateHandle: SavedStateHandle): Byte = requireNotNull(savedStateHandle["threadType"])

        @Provides
        @ViewModelScoped
        @Named("threadId")
        fun provideThreadId(savedStateHandle: SavedStateHandle): Long = requireNotNull(savedStateHandle["threadId"])
    }

    @Binds
    @ViewModelScoped
    fun bindThreadRepository(impl: ThreadRepositoryServerImpl): ThreadRepository
}