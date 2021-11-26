package club.electro.dto

//abstract class PreparablePost {
//    abstract val content: String
//    abstract val preparedContent: String?
//    abstract val answerTo: Long?
//    abstract val threadType: Byte
//    abstract val threadId: Long
//
//    fun copy() {}
//}

interface PreparablePost<T> {
    val content: String
    val preparedContent: String?
    val answerTo: Long?
    val threadType: Byte
    val threadId: Long

    suspend fun withPreparedContent(): T
}