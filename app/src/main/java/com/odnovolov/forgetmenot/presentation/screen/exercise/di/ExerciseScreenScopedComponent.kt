package com.odnovolov.forgetmenot.presentation.screen.exercise.di

import com.odnovolov.forgetmenot.presentation.di.ComponentsStore
import com.odnovolov.forgetmenot.presentation.di.ScopedComponent
import com.odnovolov.forgetmenot.presentation.di.appscope.AppScopedComponent
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragment

class ExerciseScreenScopedComponent(
    private val exerciseFragment: ExerciseFragment
) : ScopedComponent<ExerciseScreenComponent>() {

    override fun create(): ExerciseScreenComponent {
        return ComponentsStore.find<AppScopedComponent>().dependAndGet(this)
            .exerciseScreenComponentBuilder()
            .exerciseFragment(exerciseFragment)
            .build()
    }
}