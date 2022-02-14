package club.electro.repository.attachments

import android.net.Uri
import kotlinx.coroutines.Job

interface AttachmentsRepository {
    suspend fun queuePostDraftAttachment(path: String, threadType: Byte, threadId: Long)

    suspend fun uploadJob()
}