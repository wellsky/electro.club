package club.electro.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import club.electro.R
import club.electro.utils.AndroidUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())


        val sharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                if (key == SETTINGS_THEME_KEY) {
                    AndroidUtils.setTheme(prefs.getString(SETTINGS_THEME_KEY, ""))
                }
            }

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

}