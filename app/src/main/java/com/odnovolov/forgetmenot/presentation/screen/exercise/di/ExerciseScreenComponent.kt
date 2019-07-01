package com.odnovolov.forgetmenot.presentation.screen.exercise.di

import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment
import dagger.BindsInstance
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [ExerciseScreenModule::class])
interface ExerciseScreenComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun with(exerciseFragment: ExerciseFragment): Builder

        @BindsInstance
        fun with(timeCapsule: AndroidTimeCapsule): Builder

        fun build(): ExerciseScreenComponent
    }

    fun inject(exerciseFragment: ExerciseFragment)
    fun inject(exerciseCardFragment: ExerciseCardFragment)

    companion object {
        private var INSTANCE: ExerciseScreenComponent? = null

        fun createWith(
            exerciseFragment: ExerciseFragment,
            timeCapsule: AndroidTimeCapsule
        ): ExerciseScreenComponent {
            INSTANCE = AppComponent.get()
                .exerciseScreenComponentBuilder()
                .with(exerciseFragment)
                .with(timeCapsule)
                .build()
            return INSTANCE!!
        }

        fun get() = INSTANCE

        fun destroy() {
            INSTANCE = null
        }
    }
}