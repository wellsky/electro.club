package club.electro.ui.thread

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import club.electro.repository.attachments.AttachmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class PostAttachmentsViewModel @Inject constructor(
    private val repository:AttachmentsRepository
) : ViewModel() {

    fun addAttachment(uri: Uri) = viewModelScope.launch{
        println("viewModel addAttachment")
        repository.addPostAttachment(uri)
    }
}