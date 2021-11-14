package club.electro.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PostRemoteKeyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val type: KeyType,
    val threadType: Byte,
    val threadId: Long,
    val postId: Long,
) {
    enum class KeyType {
        AFTER, BEFORE
    }
}

