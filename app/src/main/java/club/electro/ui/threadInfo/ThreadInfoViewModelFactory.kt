package club.electro.ui.threadInfo

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import club.electro.ui.user.ThreadInfoViewModel

// https://stackoverflow.com/questions/46283981/android-viewmodel-additional-arguments
class ThreadInfoViewModelFactory(
    private val mThreadType: Byte,
    private val mThreadId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ThreadInfoViewModel(mThreadType, mThreadId) as T
    }
}