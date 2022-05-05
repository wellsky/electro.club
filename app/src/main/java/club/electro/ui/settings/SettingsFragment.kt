package club.electro.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import club.electro.MainViewModel
import club.electro.R
import club.electro.ToolBarConfig
import club.electro.auth.AppAuth
import club.electro.databinding.FragmentLoginBinding
import club.electro.util.AndroidUtils
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.viewmodel.LoginFormState
import ru.netology.nmedia.viewmodel.LoginViewModel
import club.electro.utils.loadCircleCrop
import com.google.android.gms.maps.GoogleMap
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        private const val SETTINGS_KEY = "settings"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())


        val sharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                println(key)
                println(prefs.getString(key, ""))
                if (key == "theme") {
                    when (prefs.getString(key, "")) {
                        "dark" -> {
                            println("AZZIS!!! DARK")
                            AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_YES
                            )
                        }
                        "light" -> {
                            println("AZZIS!!! LIGHT")
                            AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_NO
                        )}
                        else -> AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        )
                    }
                }
            }

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener)
    }

}