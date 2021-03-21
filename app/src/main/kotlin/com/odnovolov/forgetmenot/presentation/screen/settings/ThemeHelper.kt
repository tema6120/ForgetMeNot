package com.odnovolov.forgetmenot.presentation.screen.settings

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.architecturecomponents.FlowMaker
import com.odnovolov.forgetmenot.presentation.common.App
import com.odnovolov.forgetmenot.presentation.screen.settings.ThemeHelper.Theme.*

object ThemeHelper {
    enum class Theme(val stringRes: Int) {
        Light(R.string.theme_light),
        Dark(R.string.theme_dark),
        Default(R.string.theme_default)
    }

    class State : FlowMaker<State>() {
        var currentTheme: Theme by flowMaker(Default)
    }

    val state = State()

    fun init(app: App) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(app)
        val themeName: String = sharedPreferences.getString(PREFS_THEME, Default.name)!!
        val theme = Theme.valueOf(themeName)
        applyTheme(theme, app)
    }

    fun applyTheme(theme: Theme, context: Context) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                Light -> AppCompatDelegate.MODE_NIGHT_NO
                Dark -> AppCompatDelegate.MODE_NIGHT_YES
                Default -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                }
            }
        )
        state.currentTheme = theme
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPreferences.edit()
            .putString(PREFS_THEME, theme.name)
            .apply()
    }

    private const val PREFS_THEME = "PREFS_THEME"
}