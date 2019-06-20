package com.odnovolov.forgetmenot.presentation.di

import com.odnovolov.forgetmenot.presentation.App
import com.odnovolov.forgetmenot.presentation.di.appscope.AppScopedComponent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.di.ExerciseScreenScopedComponent
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import com.odnovolov.forgetmenot.presentation.screen.home.di.HomeScreenScopedComponent
import io.reactivex.Completable

object Injector {

    fun init(app: App) {
        val appScopedComponent = AppScopedComponent(app)
        ComponentsStore.keep(appScopedComponent)
    }

    fun inject(homeFragment: HomeFragment) {
        HomeScreenScopedComponent().get()
            .inject(homeFragment)
    }

    fun inject(exerciseFragment: ExerciseFragment) {
        val exerciseScreenScopedComponent = ExerciseScreenScopedComponent(exerciseFragment)
        val destroySignal: Completable = DestroySignalFactory.from(exerciseFragment.lifecycle)
        ComponentsStore.keep(exerciseScreenScopedComponent, destroySignal)
        exerciseScreenScopedComponent.get()
            .inject(exerciseFragment)
    }

    fun inject(exerciseCardFragment: ExerciseCardFragment) {
        ComponentsStore.find<ExerciseScreenScopedComponent>().get()
            .inject(exerciseCardFragment)
    }
}