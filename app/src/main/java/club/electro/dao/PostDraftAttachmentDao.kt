package club.electro.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import club.electro.entity.PostDraftAttachmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDraftAttachmentDao {
    @Query("SELECT * FROM PostDraftAttachmentEntity WHERE threadType = :threadType AND threadId = :threadId")
    fun getForThread(threadType: Byte, threadId: Long): Flow<List<PostDraftAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachment: PostDraftAttachmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attachments: List<PostDraftAttachmentEntity>)

    @Query("DELETE FROM PostDraftAttachmentEntity")
    suspend fun removeAll()
}