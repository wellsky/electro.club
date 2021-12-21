package club.electro.ui.threadInfo;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import club.electro.ui.user.ThreadInfoViewModel;

// https://stackoverflow.com/questions/46283981/android-viewmodel-additional-arguments
public class ThreadInfoViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Byte mThreadType;
    private Long mThreadId;

    public ThreadInfoViewModelFactory(Application application, Byte threadType, Long threadId) {
        mApplication = application;
        mThreadType = threadType;
        mThreadId = threadId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new ThreadInfoViewModel(mApplication, mThreadType, mThreadId);
    }
}