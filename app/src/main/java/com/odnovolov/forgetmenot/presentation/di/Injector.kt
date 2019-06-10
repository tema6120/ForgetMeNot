package com.odnovolov.forgetmenot.presentation.di

import com.odnovolov.forgetmenot.presentation.App
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.appscope.DaggerAppComponent
import com.odnovolov.forgetmenot.presentation.navigation.Navigator
import com.odnovolov.forgetmenot.presentation.navigation.NavigatorActivity
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import java.lang.ref.WeakReference

object Injector {

    lateinit var appComponent: AppComponent
    private var navigator: WeakReference<Navigator>? = null

    fun createAppComponent(app: App) {
        appComponent = DaggerAppComponent.builder()
            .app(app)
            .build()
    }

    fun depend(navigatorActivity: NavigatorActivity) {
        this.navigator = WeakReference(navigatorActivity)
    }

    fun inject(homeFragment: HomeFragment) {
        appComponent.homeScreenComponentBuilder()
            .navigator(navigator!!.get()!!)
            .build()
            .inject(homeFragment)
    }

    fun inject(exerciseFragment: ExerciseFragment) {
        appComponent.exerciseScreenComponentBuilder()
            .build()
            .inject(exerciseFragment)
    }
}