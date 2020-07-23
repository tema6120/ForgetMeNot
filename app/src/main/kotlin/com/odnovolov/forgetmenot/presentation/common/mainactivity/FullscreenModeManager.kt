package com.odnovolov.forgetmenot.presentation.common.mainactivity

import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.Window
import android.view.WindowManager.LayoutParams
import androidx.navigation.NavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference

class FullscreenModeManager(
    private val fullscreenPreference: FullscreenPreference,
    private val decorView: View,
    private val contentView: View,
    private val window: Window,
    navController: NavController
) {
    private val onGlobalLayoutListener = OnGlobalLayoutListener {
        val rect = Rect()
        decorView.getWindowVisibleDisplayFrame(rect)
        val height = decorView.context.resources.displayMetrics.heightPixels
        val diff = height - rect.bottom
        if (diff != 0) {
            if (contentView.paddingBottom != diff) {
                contentView.setPadding(0, 0, 0, diff)
            }
        } else {
            if (contentView.paddingBottom != 0) {
                contentView.setPadding(0, 0, 0, 0)
            }
        }
    }

    private var isFullscreenModeEnabled = false

    init {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            with(fullscreenPreference) {
                when (destination.id) {
                    R.id.exercise_screen -> {
                        setFullscreenMode(isEnabledInExercise)
                    }
                    R.id.repetition_screen -> {
                        setFullscreenMode(isEnabledInRepetition)
                    }
                    else -> {
                        setFullscreenMode(isEnabledInDashboardAndSettings)
                    }
                }
            }
        }
    }

    fun setFullscreenMode(isEnabled: Boolean) {
        if (isFullscreenModeEnabled == isEnabled) return
        if (isEnabled) {
            window.addFlags(LayoutParams.FLAG_FULLSCREEN)
            decorView.viewTreeObserver.addOnGlobalLayoutListener(onGlobalLayoutListener)
        } else {
            window.clearFlags(LayoutParams.FLAG_FULLSCREEN)
            decorView.viewTreeObserver.removeOnGlobalLayoutListener(onGlobalLayoutListener)
        }
        isFullscreenModeEnabled = isEnabled
    }
}