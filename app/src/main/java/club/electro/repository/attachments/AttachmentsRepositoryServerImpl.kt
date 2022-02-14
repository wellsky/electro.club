package club.electro.repository.attachments

import android.content.Context
import android.graphics.Bitmap
import club.electro.api.ApiService
import club.electro.dao.PostAttachmentDao
import club.electro.dto.PostAttachment
import club.electro.entity.PostAttachmentEntity
import club.electro.entity.toDto
import dagger.hilt.android.qualifiers.ApplicationContext
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import id.zelory.compressor.constraint.destination
import kotlinx.coroutines.Job
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AttachmentsRepositoryServerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val postDraftDao: PostAttachmentDao,
    private val apiService: ApiService
): AttachmentsRepository {

    override fun getThreadAttachments(threadType: Byte, threadId: Long) = postDraftDao.getForThread(threadType, threadId).map{it.toDto()}

    override suspend fun queuePostDraftAttachment(path: String, threadType: Byte, threadId: Long) {
        println("Queue attachment: " + threadType + ":" + threadId + " URI: " + path.toString())
        val newUploadId = postDraftDao.insert(PostAttachmentEntity(
            type = 1,
            localFile = path,
            threadType = threadType,
            threadId = threadId,
        ))
        postDraftDao.setStatus(newUploadId,PostAttachment.STATUS_READY_TO_UPLOAD)
    }

    override suspend fun uploadJob() = postDraftDao.getFirstReady().distinctUntilChanged().collect { entity ->
        //val sourceFile = File("/storage/emulated/0/DCIM/Camera/IMG_20220213_130617.jpg")
        entity?.let {
            val sourceFile = File(it.localFile)
            if (sourceFile.exists()) {
                try {
                    println("Uploading... " + sourceFile.name)

                    val outputDir = context.cacheDir // context being the Activity pointer
                    val destinationFile = File.createTempFile(sourceFile.name, ".jpg", outputDir)
                    destinationFile.deleteOnExit()

                    val compressedImageFile = Compressor.compress(context, sourceFile) {
                        destination(destinationFile)
                        default(width = 1920, format = Bitmap.CompressFormat.JPEG, quality = 80)
                    }

                    apiService.uploadPostDraftAttachment(
                        file = MultipartBody.Part.createFormData(
                            "file",
                            compressedImageFile.name,
                            compressedImageFile.asRequestBody("image/*".toMediaType())
                        )
                    )
                    println("Done: " + sourceFile.name)
                    postDraftDao.setStatus(it.id, PostAttachment.STATUS_UPLOADED)

                } catch (e: Exception) {
                    // TODO отметить как ошибку при компрессии или отправке
                }
            } else {
                // TODO отметить как не найденный файл
                postDraftDao.setStatus(it.id, PostAttachment.STATUS_ERROR)
            }
        } ?: run {
            println("All files uploaded")
            //postDraftDao.removeUploaded()
        }
    }
}