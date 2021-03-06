package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.dto.PostAttachment
import club.electro.entity.PostAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostAttachmentDao {
    @Query("SELECT * FROM PostAttachmentEntity WHERE status = ${PostAttachment.STATUS_READY_TO_UPLOAD}")
    fun getFirstReady(): Flow<PostAttachmentEntity?>

    @Query("SELECT * FROM PostAttachmentEntity WHERE threadType = :threadType AND threadId = :threadId AND postId = 0 ORDER BY localId, created")
    fun flowForThreadDraft(threadType: Byte, threadId: Long): Flow<List<PostAttachmentEntity>>

    @Query("SELECT * FROM PostAttachmentEntity WHERE threadType = :threadType AND threadId = :threadId AND postId = :postId ORDER BY localId, created")
    fun flowForPost(threadType: Byte, threadId: Long, postId: Long): Flow<List<PostAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: PostAttachmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachments: List<PostAttachmentEntity>)

    @Query("UPDATE PostAttachmentEntity SET id = :id, previewUrl = :previewUrl, fullUrl = :fullUrl, status = :status WHERE localId = :localId")
    suspend fun updateLocal(localId: Long, id: Long?, previewUrl: String?, fullUrl: String?, status: Byte)

    @Query("UPDATE PostAttachmentEntity SET status = :status WHERE localId = :localId")
    suspend fun setStatus(localId: Long, status: Byte)

    @Query("DELETE FROM PostAttachmentEntity WHERE localId = :localId")
    suspend fun removeByLocalId(localId: Long)

    @Query("DELETE FROM PostAttachmentEntity WHERE status = ${PostAttachment.STATUS_UPLOADED} AND threadType = :threadType AND threadId = :threadId AND postId = 0")
    suspend fun removeUploadedDrafts(threadType: Byte, threadId: Long)

    @Query("DELETE FROM PostAttachmentEntity WHERE threadType = :threadType AND threadId = :threadId")
    suspend fun removeAll(threadType: Byte, threadId: Long)
}