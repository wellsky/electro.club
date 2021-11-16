package club.electro.ui.thread

import android.app.Application
import androidx.lifecycle.*
import club.electro.application.ElectroClubApp
import club.electro.dto.Post
import club.electro.repository.*
import kotlinx.coroutines.launch

class ThreadViewModel(application: Application, val threadType: Byte, val threadId: Long) : AndroidViewModel(application) {

    private val repository: ThreadRepository = ThreadRepositoryServerImpl((application as ElectroClubApp).diContainer, threadType, threadId)

    val thread = repository.thread.asLiveData()
    val posts = repository.posts

    val lastUpdateTime = repository.lastUpdateTime

    val editorPost = MutableLiveData(emptyPost) // Пост, который в данный момент в текстовом редакторе
    val editedPost = MutableLiveData(emptyPost) // Исходный пост, который в данный момент редактируется
    val answerToPost = MutableLiveData(emptyPost) // Пост, на который в данный момент пишется ответ

//    fun loadPosts() = viewModelScope.launch {
//        try {
//            repository.getThreadPosts()
//        } catch (e: Exception) {
//            //_dataState.value = FeedModelState(error = true)
//        }
//    }
    fun getThread() = viewModelScope.launch {
        repository.getThread()
    }


//    fun loadThreadBegining() {
//        repository.changeTargetPost(ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_FIRST))
//    }
//
//    fun loadThreadEnd() {
//        repository.changeTargetPost(ThreadLoadTarget(targetPostPosition = ThreadLoadTarget.TARGET_POSITION_LAST))
//    }

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
                    repository.saveToServerPost(it)
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