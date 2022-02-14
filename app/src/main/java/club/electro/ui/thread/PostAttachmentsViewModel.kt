package club.electro.ui.thread

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.electro.repository.attachments.AttachmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostAttachmentsViewModel @Inject constructor(
    val state : SavedStateHandle,
    private val repository:AttachmentsRepository
) : ViewModel() {
    val threadType: Byte = state.get("threadType") ?: 0
    val threadId: Long = state.get("threadId") ?: 0

    init {
        viewModelScope.launch {
            repository.uploadJob()
        }
    }

    fun queueAttachment(path: String) = viewModelScope.launch {
        repository.queuePostDraftAttachment(path, threadType, threadId)
    }
}