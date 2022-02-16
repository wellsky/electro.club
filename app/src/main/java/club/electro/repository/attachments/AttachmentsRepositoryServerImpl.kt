package club.electro.repository.attachments

import android.content.Context
import android.graphics.Bitmap
import club.electro.api.ApiService
import club.electro.dao.PostAttachmentDao
import club.electro.dto.PostAttachment
import club.electro.entity.PostAttachmentEntity
import club.electro.entity.toDto
import club.electro.error.ApiError
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import okhttp3.RequestBody.Companion.toRequestBody

class AttachmentsRepositoryServerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postAttachmentDao: PostAttachmentDao,
    private val apiService: ApiService
): AttachmentsRepository {

    override fun getThreadDraftAttachments(threadType: Byte, threadId: Long) = postAttachmentDao.getForThread(threadType, threadId).map{it.toDto()}

    override suspend fun queuePostDraftAttachment(threadType: Byte, threadId: Long, name: String, path: String) {
        val newUploadId = postAttachmentDao.insert(PostAttachmentEntity(
            type = 1,
            name = name,
            localPath = path,
            threadType = threadType,
            threadId = threadId,
        ))
        postAttachmentDao.setStatus(newUploadId,PostAttachment.STATUS_READY_TO_UPLOAD)
    }

    override suspend fun uploaderJob() = postAttachmentDao.getFirstReady().distinctUntilChanged().collect { entity ->
         entity?.toDto()?.let { attachment->
             attachment.localPath?.let { localPath ->
                 val sourceFile = File(localPath)
                 if (sourceFile.exists()) {
                     postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_COMPRESSING)

                     val compressedImageFile: File? = try {
                         val outputDir = context.cacheDir // context being the Activity pointer
                         val destinationFile =
                             File.createTempFile(sourceFile.name, ".jpg", outputDir)
                         destinationFile.deleteOnExit()

                         Compressor.compress(context, sourceFile) {
                             destination(destinationFile)
                             default(
                                 width = 1920,
                                 format = Bitmap.CompressFormat.JPEG,
                                 quality = 80
                             )
                         }
                     } catch (e: Exception) {
                         postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_ERROR_COMPRESSING)
                         null
                     }

                     compressedImageFile?.let {
                         postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_UPLOADING)
                         try {
                             val response = apiService.uploadPostDraftAttachment(
                                 threadType = attachment.threadType.toString().toRequestBody(), // TODO надо узнать, скорее всего не так надо форматировать
                                 threadId = attachment.threadId.toString().toRequestBody(),
                                 attachmentName = attachment.name?.toRequestBody() ?: "".toRequestBody(),
                                 file = MultipartBody.Part.createFormData(
                                     "file",
                                     compressedImageFile.name,
                                     compressedImageFile.asRequestBody("image/*".toMediaType())
                                 )
                             )

                             if (!response.isSuccessful) {
                                 throw ApiError(response.code(), response.message())
                             }

                             val body = response.body() ?: throw ApiError(
                                 response.code(),
                                 response.message()
                             )

                             postAttachmentDao.updateLocal(
                                 localId = attachment.localId,
                                 id = body.data.id,
                                 previewUrl = body.data.previewUrl,
                                 fullUrl = body.data.fullUrl,
                                 status = PostAttachment.STATUS_UPLOADED
                             )
                         } catch (e: Exception) {
                             postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_ERROR_UPLOADING)
                         }
                     }
                 } else {
                     postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_ERROR_NOT_FOUND)
                 }
             }
        } ?: run {
            println("All files uploaded")
        }
    }

    override suspend fun removePostAttachment(attachment: PostAttachment) {
        attachment.id?.let {
            val response = apiService.removePostAttachment(
                threadType = attachment.threadType,
                threadId = attachment.threadId,
                attachmentId = attachment.id,
            )

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())

            if (body.status.equals("ok")) { // TODO
                postAttachmentDao.removeByLocalId(
                    localId = attachment.localId
                )
            }
        } ?: run {
            postAttachmentDao.removeByLocalId(
                localId = attachment.localId
            )
        }
    }
}