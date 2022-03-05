package club.electro.ui.subscriptions

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SubscriptionsTabsViewModel @Inject constructor(
    @ApplicationContext context: Context,
) : ViewModel() {
    private val prefs = context.getSharedPreferences("activityTabs", Context.MODE_PRIVATE)

    private val tabPositionKey = "tabPosition"

    fun getCurrentTabPosition(): Int {
        return prefs.getInt(tabPositionKey, 0)
    }

    fun setCurrentTabPosition(position: Int) {
        with(prefs.edit()) {
            putInt(tabPositionKey, position)
            apply()
        }
    }
}