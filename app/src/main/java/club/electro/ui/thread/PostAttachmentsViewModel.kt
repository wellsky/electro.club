package club.electro.ui.thread

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import club.electro.repository.attachments.AttachmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostAttachmentsViewModel @Inject constructor(
    val state : SavedStateHandle,
    private val repository:AttachmentsRepository
) : ViewModel() {
    val threadType: Byte = state.get("threadType") ?: 0
    val threadId: Long = state.get("threadId") ?: 0

    val attachments = repository.getThreadAttachments(threadType, threadId).flowOn(Dispatchers.Default).asLiveData()

    init {
        viewModelScope.launch {
            repository.uploadJob()
        }
    }

    fun queueAttachment(path: String) = viewModelScope.launch {
        repository.queuePostDraftAttachment(path, threadType, threadId)
    }
}