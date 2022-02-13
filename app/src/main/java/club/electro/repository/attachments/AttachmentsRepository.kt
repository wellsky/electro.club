package club.electro.repository.attachments

import android.net.Uri

interface AttachmentsRepository {
    suspend fun queuePostDraftAttachment(uri: Uri, threadType: Byte, threadId: Long)
}