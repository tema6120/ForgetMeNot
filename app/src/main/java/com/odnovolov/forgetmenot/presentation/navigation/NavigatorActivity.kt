package com.odnovolov.forgetmenot.presentation.navigation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.di.Injector
import com.odnovolov.forgetmenot.presentation.navigation.Navigator.Event.NAVIGATE_TO_EXERCISE
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment

class NavigatorActivity : AppCompatActivity(), Navigator {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Injector.depend(this)

        if (savedInstanceState == null) {
            navigateTo(HomeFragment())
        }
    }

    override fun accept(navEvent: Navigator.Event?) {
        when (navEvent) {
            NAVIGATE_TO_EXERCISE -> navigateTo(ExerciseFragment(), true)
        }
    }

    private fun navigateTo(fragment: Fragment, needToAddToBackStack: Boolean = false) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, null)
            .apply { if (needToAddToBackStack) addToBackStack(null) }
            .commit()
    }
}