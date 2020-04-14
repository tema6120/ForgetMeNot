package com.odnovolov.forgetmenot.presentation.common

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.odnovolov.forgetmenot.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Navigator : ActivityLifecycleCallbacks {
    private var navController: NavController? = null

    fun navigateToExercise() {
        navigate(R.id.action_home_screen_to_exercise_screen)
    }

    fun navigateToEditCard() {
        navigate(R.id.action_exercise_screen_to_edit_card_screen)
    }

    fun navigateToDeckSettings() {
        navigate(R.id.action_home_screen_to_deck_settings_screen)
    }

    fun navigateToIntervals() {
        navigate(R.id.action_deck_settings_screen_to_intervals_screen)
    }

    fun navigateToPronunciation() {
        navigate(R.id.action_deck_settings_screen_to_pronunciation_screen)
    }

    fun navigateToSpeakPlan() {
        navigate(R.id.action_deck_settings_screen_to_speak_plan_screen)
    }

    fun showSpeakEventDialog() {
        navigate(R.id.action_show_speak_event_dialog)
    }

    fun navigateToRepetitionSettings() {
        navigate(R.id.action_home_screen_to_repetition_settings_screen)
    }

    fun showLastAnswerFilterDialog() {
        navigate(R.id.action_show_last_answer_filter_dialog)
    }

    fun showRepetitionLapsDialog() {
        navigate(R.id.action_show_repetition_last_dialog)
    }

    fun navigateToRepetition() {
        navigate(R.id.action_repetition_settings_screen_to_repetition_screen)
    }

    fun navigateToSettings() {
        navigate(R.id.action_home_screen_to_settings_screen)
    }

    fun navigateToWalkingModeSettings() {
        navigate(R.id.action_settings_screen_to_walking_mode_settings_screen)
    }

    fun navigateUp() {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            navController?.navigateUp()
        }
    }

    private fun navigate(actionId: Int) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            navController!!.navigate(actionId)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            if (activity is MainActivity && navController == null) {
                navController = activity.findNavController(R.id.navHostFragment)
            }
        }
    }

    override fun onActivityDestroyed(activity: Activity) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            if (activity is MainActivity) {
                navController = null
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityStopped(activity: Activity) {}
}