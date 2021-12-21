package club.electro.ui.map.socket;

import android.app.Application;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SocketViewModelFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Long mSocketId;

    public SocketViewModelFactory(Application application, Long socketId) {
        mApplication = application;
        mSocketId = socketId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new SocketViewModel(mApplication, mSocketId);
    }
}