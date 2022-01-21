package club.electro.ui.thread

import androidx.lifecycle.*
import club.electro.dto.Post
import club.electro.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreadViewModel @Inject constructor(
    val repository: ThreadRepository
) : ViewModel() {
    val thread = repository.thread.asLiveData()
    val posts = repository.posts

    val lastUpdateTime = repository.lastUpdateTime

    val editorPost = MutableLiveData(emptyPost) // Пост, который в данный момент в текстовом редакторе
    val editedPost = MutableLiveData(emptyPost) // Опубликованный пост, который в данный момент редактируется в текстовом редакторе
    val answerToPost = MutableLiveData(emptyPost) // Пост, на который в данный момент пишется ответ

    fun getThread() = viewModelScope.launch {
        repository.getThread()
    }

    fun changeSubscription(newStatus: Byte) = viewModelScope.launch {
        repository.changeSubscription(newStatus)
    }

    fun reloadPosts(target: ThreadLoadTarget) {
        repository.reloadPosts(target)
    }

    fun changeEditorPostContent(content: String) {
        editorPost.value?.let {
            val text = content.trim()
            if (it.content != text) {
                editorPost.value = editorPost.value?.copy(content = text)
            }
        }
    }

    fun saveEditorPost() {
        editorPost.value?.let {
            viewModelScope.launch {
                try {
                    repository.savePostToServer(it)
                } catch (e: Exception) {

                }
            }
        }
        editedPost.value = emptyPost
        answerToPost.value = emptyPost
        editorPost.value = emptyPost
    }

    fun removePost(post: Post) {
        viewModelScope.launch {
            repository.removePost(post)
        }
    }

    fun startEditPost(post: Post) {
        editedPost.value = post
        editorPost.value = post
    }

    fun cancelEditPost() {
        editedPost.value = emptyPost
        editorPost.value = emptyPost
    }

    fun startAnswerPost(post: Post) {
        answerToPost.value = post
        editorPost.value = editorPost.value?.copy(answerTo = post.id)
    }

    fun cancelAnswerPost() {
        answerToPost.value = emptyPost
        editorPost.value = editorPost.value?.copy(answerTo = null)
    }

    fun startCheckUpdates() {
        repository.startCheckUpdates()
    }

    fun stopCheckUpdates() {
        repository.stopCheckUpdates()
    }
}

private val emptyPost = Post(
    localId = 0L,
    id = 0L,
    status = 0,
    threadType = 0,
    threadId = 0,
    authorId = 0,
    authorName = "",
    authorAvatar = "",
    content = "",
    published = 0,
)