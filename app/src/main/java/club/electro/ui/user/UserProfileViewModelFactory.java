package club.electro.ui.user;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class UserProfileViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Long mUserId;

    public UserProfileViewModelFactory(Application application, Long userId) {
        mApplication = application;
        mUserId = userId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new UserProfileViewModel(mApplication, mUserId);
    }
}