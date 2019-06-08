package com.odnovolov.forgetmenot.presentation.screen.exercise.di

import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment
import dagger.Subcomponent

@FragmentScope
@Subcomponent(modules = [ExerciseScreenModule::class])
interface ExerciseScreenComponent {

    @Subcomponent.Builder
    interface Builder {
        fun build(): ExerciseScreenComponent
    }

    fun inject(exerciseFragment: ExerciseFragment)
}