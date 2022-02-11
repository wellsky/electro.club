package club.electro.repository.attachments

import android.net.Uri

interface AttachmentsRepository {
    suspend fun addPostAttachment(uri: Uri)
}