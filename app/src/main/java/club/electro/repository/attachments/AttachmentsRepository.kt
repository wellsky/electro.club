package club.electro.repository.attachments

import android.net.Uri
import club.electro.dto.PostAttachment
import club.electro.entity.PostAttachmentEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface AttachmentsRepository {
    fun getThreadAttachments(threadType: Byte, threadId: Long): Flow<List<PostAttachment>>
    suspend fun queuePostDraftAttachment(path: String, threadType: Byte, threadId: Long)

    suspend fun uploadJob()
}