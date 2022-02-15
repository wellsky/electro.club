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

    override fun getThreadAttachments(threadType: Byte, threadId: Long) = postAttachmentDao.getForThread(threadType, threadId).map{it.toDto()}

    override suspend fun queuePostDraftAttachment(threadType: Byte, threadId: Long, name: String, path: String) {
        println("Queue attachment: " + threadType + ":" + threadId + " URI: " + path.toString())
        val newUploadId = postAttachmentDao.insert(PostAttachmentEntity(
            type = 1,
            name = name,
            localPath = path,
            threadType = threadType,
            threadId = threadId,
        ))
        postAttachmentDao.setStatus(newUploadId,PostAttachment.STATUS_READY_TO_UPLOAD)
    }

    override suspend fun uploadJob() = postAttachmentDao.getFirstReady().distinctUntilChanged().collect { entity ->
        //val sourceFile = File("/storage/emulated/0/DCIM/Camera/IMG_20220213_130617.jpg")
         entity?.toDto()?.let { attachment->
             attachment.localPath?.let { localPath ->
                 val sourceFile = File(localPath)
                 if (sourceFile.exists()) {
                     try {
                         println("Uploading... " + sourceFile.name)

                         val outputDir = context.cacheDir // context being the Activity pointer
                         val destinationFile =
                             File.createTempFile(sourceFile.name, ".jpg", outputDir)
                         destinationFile.deleteOnExit()

                         val compressedImageFile = Compressor.compress(context, sourceFile) {
                             destination(destinationFile)
                             default(
                                 width = 1920,
                                 format = Bitmap.CompressFormat.JPEG,
                                 quality = 80
                             )
                         }

                         val response = apiService.uploadPostDraftAttachment(
                             threadType = attachment.threadType.toString().toRequestBody(), // TODO надо узнать, скорее всего не так надо форматировать
                             threadId = attachment.threadId.toString().toRequestBody(),
                             file = MultipartBody.Part.createFormData(
                                 "file",
                                 compressedImageFile.name,
                                 compressedImageFile.asRequestBody("image/*".toMediaType())
                             )
                         )

                         if (!response.isSuccessful) {
                             throw ApiError(response.code(), response.message())
                         }
                         val body = response.body() ?: throw ApiError(response.code(), response.message())

                         postAttachmentDao.updateLocal(
                             localId = attachment.localId,
                             id = body.data.id,
                             previewUrl = body.data.previewUrl,
                             fullUrl = body.data.fullUrl,
                             status = PostAttachment.STATUS_UPLOADED
                         )

                         println("Done: " + sourceFile.name)
                         //postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_UPLOADED)
                     } catch (e: Exception) {
                         // TODO отметить как ошибку при компрессии или отправке
                         postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_ERROR)
                     }
                 } else {
                     // TODO отметить как не найденный файл
                     postAttachmentDao.setStatus(attachment.localId, PostAttachment.STATUS_ERROR)
                 }
             }
        } ?: run {
            println("All files uploaded")
        }
    }
}