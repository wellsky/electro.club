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

    @Query("SELECT * FROM PostAttachmentEntity WHERE threadType = :threadType AND threadId = :threadId")
    fun getForThread(threadType: Byte, threadId: Long): Flow<List<PostAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: PostAttachmentEntity): Long

    @Query("UPDATE PostAttachmentEntity SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: Byte)

    @Query("DELETE FROM PostAttachmentEntity WHERE status = ${PostAttachment.STATUS_UPLOADED}")
    suspend fun removeUploaded()

    @Query("DELETE FROM PostAttachmentEntity")
    suspend fun removeAll()
}