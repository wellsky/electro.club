package club.electro.dto

interface PreparablePost<T> {
    val content: String
    val preparedContent: String?
    val answerTo: Long?
    val threadType: Byte
    val threadId: Long

    suspend fun withPreparedContent(): T
}