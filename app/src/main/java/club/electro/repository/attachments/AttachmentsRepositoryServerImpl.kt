package club.electro.repository.attachments

import android.net.Uri
import club.electro.dao.PostDraftAttachmentDao
import club.electro.entity.PostDraftAttachmentEntity
import javax.inject.Inject

class AttachmentsRepositoryServerImpl @Inject constructor(
    private val postDraftDao: PostDraftAttachmentDao
): AttachmentsRepository {
    override suspend fun queuePostDraftAttachment(uri: Uri, threadType: Byte, threadId: Long) {
        println("repository addAttachment")
        println("Thread: " + threadType + ":" + threadId + " URI: " + uri.toString())

        postDraftDao.insert(PostDraftAttachmentEntity(
            type = 1,
            localFile = uri.toString(),
            threadType = threadType,
            threadId = threadId,
        ))
        //postDraftDao.insert()
    }
}