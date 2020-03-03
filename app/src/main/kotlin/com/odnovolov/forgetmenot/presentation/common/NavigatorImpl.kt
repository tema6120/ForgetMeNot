package com.odnovolov.forgetmenot.presentation.common

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.odnovolov.forgetmenot.R

class NavigatorImpl : Navigator,
    ActivityLifecycleCallbacks {
    private var navController: NavController? = null

    override fun navigateToExercise() {
        navController?.navigate(R.id.action_home_screen_to_exercise_screen)
    }

    override fun navigateToEditCard() {
        navController?.navigate(R.id.action_exercise_screen_to_edit_card_screen)
    }

    override fun navigateToDeckSettings() {
        navController?.navigate(R.id.action_home_screen_to_deck_settings_screen)
    }

    override fun navigateToIntervals() {
        navController?.navigate(R.id.action_deck_settings_screen_to_intervals_screen)
    }

    override fun navigateToPronunciation() {
        navController?.navigate(R.id.action_deck_settings_screen_to_pronunciation_screen)
    }

    override fun navigateToRepetition() {
        navController?.navigate(R.id.action_home_screen_to_repetition_screen)
    }

    override fun navigateToSettings() {
        navController?.navigate(R.id.action_home_screen_to_settings_screen)
    }

    override fun navigateToWalkingModeSettings() {
        navController?.navigate(R.id.action_settings_screen_to_walking_mode_settings_screen)
    }

    override fun navigateUp() {
        navController?.navigateUp()
    }

    override fun onActivityStarted(activity: Activity) {
        if (activity is MainActivity) {
            navController = activity.findNavController(R.id.nav_host_fragment)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity is MainActivity) {
            navController = null
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}