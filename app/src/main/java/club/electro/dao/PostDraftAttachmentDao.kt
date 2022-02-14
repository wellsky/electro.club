package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.dto.PostDraftAttachment
import club.electro.entity.PostDraftAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDraftAttachmentDao {
    @Query("SELECT * FROM PostDraftAttachmentEntity WHERE status = ${PostDraftAttachment.STATUS_READY_TO_UPLOAD}")
    fun getFirstReady(): Flow<PostDraftAttachmentEntity?>

    @Query("SELECT * FROM PostDraftAttachmentEntity WHERE threadType = :threadType AND threadId = :threadId")
    fun getForThread(threadType: Byte, threadId: Long): Flow<List<PostDraftAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: PostDraftAttachmentEntity): Long

    @Query("UPDATE PostDraftAttachmentEntity SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: Byte)

    @Query("DELETE FROM PostDraftAttachmentEntity WHERE status = ${PostDraftAttachment.STATUS_UPLOADED}")
    suspend fun removeUploaded()

    @Query("DELETE FROM PostDraftAttachmentEntity")
    suspend fun removeAll()
}