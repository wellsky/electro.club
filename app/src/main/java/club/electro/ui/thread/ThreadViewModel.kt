package club.electro.ui.thread

import androidx.lifecycle.*
import androidx.paging.cachedIn
import club.electro.auth.AppAuth
import club.electro.dto.Post
import club.electro.repository.attachments.AttachmentsRepository
import club.electro.repository.thread.ThreadLoadTarget
import club.electro.repository.thread.ThreadRepository
import club.electro.repository.thread.ThreadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreadViewModel @Inject constructor(
    state : SavedStateHandle,
    val repository: ThreadRepository,
    val attachmentsRepository: AttachmentsRepository,
    val appAuth: AppAuth
) : ViewModel() {
    val threadType: Byte = state.get("threadType") ?: 0
    val threadId: Long = state.get("threadId") ?: 0

    val thread = repository.thread.asLiveData()
    val draftAttachments = attachmentsRepository.getThreadDraftAttachments(threadType, threadId).flowOn(Dispatchers.Default).asLiveData()

    val mutablePosts = MutableStateFlow(value = ThreadLoadTarget((state.get("postId") ?: 0L)))
    val posts = mutablePosts.flatMapLatest { refreshTarget ->
        repository.posts(refreshTarget)
    }.cachedIn(viewModelScope)

    val editorPost = MutableLiveData(emptyPost) // Пост, который в данный момент в текстовом редакторе
    val editedPost = MutableLiveData(emptyPost) // Опубликованный пост, который в данный момент редактируется в текстовом редакторе
    val answerToPost = MutableLiveData(emptyPost) // Пост, на который в данный момент пишется ответ

    // Текущее состояние группы. Время последнего изменения, количество сообщений.
    // Постоянно обновляется с сервера подписок. Если есть изменения, отправляется запрос на сервер сообщений.
    private val _threadStatus = MutableLiveData(ThreadStatus())
    val threadStatus
        get() = _threadStatus

    // После изменения состояния группы отправляется запрос на обновление сообщений
    // incomingChangesStatus сохраняет информацию о предстоящем ответе (появились ли новые сообщения, переходить ли к определенному посту)
    private var _incomingChangesStatus: IncomingChangesStatus? = null
    val incomingChangesStatus
        get() = _incomingChangesStatus

    //val lastUpdateTime = MutableLiveData(0L)

    init {
        viewModelScope.launch {
            repository.threadStatus.asFlow().collectLatest { newStatus->
                println("Repository thread status changed")
                if (newStatus.lastUpdateTime > 0) { // Не инициализация переменной в репозитории
                    _threadStatus.value?.let { currentStatus-> // Не первые полученные данные
                        println("Viewmodel thread status changed")
                        if (newStatus.messagesCount > currentStatus.messagesCount) {
                            // Появились новые сообщения
                            _incomingChangesStatus = IncomingChangesStatus(
                                newMessages = newStatus.messagesCount - currentStatus.messagesCount
                            )
                        }

                    }
                    _threadStatus.value = newStatus
                }
            }
        }
    }

    fun setIncomingChangesStatus(status: IncomingChangesStatus?) {
        _incomingChangesStatus = status
    }

    fun getThread() = viewModelScope.launch {
        repository.getThread()
    }

    fun changeSubscription(newStatus: Byte) = viewModelScope.launch {
        repository.changeSubscription(newStatus)
    }

    fun reloadPosts(target: ThreadLoadTarget) {
        _incomingChangesStatus = IncomingChangesStatus(
            targetPost = target
        )
        mutablePosts.value = target
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
            if (it.id == 0L) {
                println("New message saved")
                _incomingChangesStatus = IncomingChangesStatus(
                    newMessages = 1,
                    targetPost = ThreadLoadTarget(ThreadLoadTarget.TARGET_POSITION_LAST)
                )
            }

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

data class IncomingChangesStatus(
    val targetPost: ThreadLoadTarget? = null,
    val newMessages: Long? = null
)