package club.electro.ui.thread;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import club.electro.repository.ThreadLoadTarget;

// https://stackoverflow.com/questions/46283981/android-viewmodel-additional-arguments
public class ThreadViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Byte mThreadType;
    private Long mThreadId;
    private ThreadLoadTarget mTargetPost;

    public ThreadViewModelFactory(Application application, Byte threadType, Long threadId, ThreadLoadTarget targetPost) {
        mApplication = application;
        mThreadType = threadType;
        mThreadId = threadId;
        mTargetPost = targetPost;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ThreadViewModel(mApplication, mThreadType, mThreadId, mTargetPost);
    }
}