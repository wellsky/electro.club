package club.electro.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatDelegate
import club.electro.ui.settings.SETTINGS_THEME_KEY
import club.electro.ui.settings.SETTINGS_THEME_VALUE_DARK
import club.electro.ui.settings.SETTINGS_THEME_VALUE_LIGHT

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setTheme(settingsThemeKey: String?) {
        when (settingsThemeKey) {
            SETTINGS_THEME_VALUE_DARK -> {
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
            }

            SETTINGS_THEME_VALUE_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )}

            else -> AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            )
        }
    }
}