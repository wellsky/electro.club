package club.electro.ui.thread

import android.app.Application
import club.electro.repository.ThreadLoadTarget
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

// https://stackoverflow.com/questions/46283981/android-viewmodel-additional-arguments
class ThreadViewModelFactory(
    private val mApplication: Application,
    private val mThreadType: Byte,
    private val mThreadId: Long,
    private val mTargetPost: ThreadLoadTarget
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ThreadViewModel(mApplication, mThreadType, mThreadId, mTargetPost) as T
    }
}