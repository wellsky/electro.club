package club.electro.repository.attachments

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class AttachmentsRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAttachmentsRepository(impl: AttachmentsRepositoryServerImpl): AttachmentsRepository
}