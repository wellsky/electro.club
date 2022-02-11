package club.electro.repository.attachments

import android.net.Uri
import club.electro.dao.PostDraftAttachmentDao
import javax.inject.Inject

class AttachmentsRepositoryServerImpl @Inject constructor(
    private val postDraftDao: PostDraftAttachmentDao
): AttachmentsRepository {
    override suspend fun addPostAttachment(uri: Uri) {
        println("repository addAttachment")
        println("URI: " + uri.toString())
        //postDraftDao.insert()
    }
}