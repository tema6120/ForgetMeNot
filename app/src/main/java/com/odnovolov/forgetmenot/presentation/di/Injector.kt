package com.odnovolov.forgetmenot.presentation.di

import com.odnovolov.forgetmenot.presentation.App
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.appscope.DaggerAppComponent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment

object Injector {

    lateinit var appComponent: AppComponent

    fun createAppComponent(app: App) {
        appComponent = DaggerAppComponent.builder()
            .app(app)
            .build()
    }

    fun inject(homeFragment: HomeFragment) {
        appComponent.homeScreenComponentBuilder()
            .build()
            .inject(homeFragment)
    }

    fun inject(exerciseFragment: ExerciseFragment) {
        appComponent.exerciseScreenComponentBuilder()
            .build()
            .inject(exerciseFragment)
    }
}