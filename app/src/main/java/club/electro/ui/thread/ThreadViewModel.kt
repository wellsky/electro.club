package club.electro.ui.thread

import android.app.Application
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.dto.Post
import club.electro.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ThreadViewModel(application: Application, val threadType: Byte, val threadId: Long) : AndroidViewModel(application) {

    private val repository: ThreadRepository = ThreadRepositoryServerImpl((application as ElectroClubApp).diContainer, threadType, threadId)

    val data = repository.data.asLiveData(Dispatchers.Default)


    val editorPost = MutableLiveData(emptyPost) // Пост, который в данный момент в текстовом редакторе
    val editedPost = MutableLiveData(emptyPost) // Исходный пост, который в данный момент редактируется
    val answerToPost = MutableLiveData(emptyPost) // Пост, на который в данный момент пишется ответ

    fun loadPosts() = viewModelScope.launch {
        try {
            repository.getThreadPosts()
        } catch (e: Exception) {
            //_dataState.value = FeedModelState(error = true)
        }
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
                    repository.savePost(it)
                } catch (e: Exception) {

                }
            }
        }
        editedPost.value = emptyPost
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

    fun stop() {
        repository.stopCheckUpdates()
    }
}

private val emptyPost = Post(
    id = 0L,
    threadType = 0,
    threadId = 0,
    authorId = 0,
    authorName = "",
    authorAvatar = "",
    content = "",
    published = 0,
)