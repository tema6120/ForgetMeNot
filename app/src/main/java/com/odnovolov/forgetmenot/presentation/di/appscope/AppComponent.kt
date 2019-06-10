package com.odnovolov.forgetmenot.presentation.di.appscope

import com.odnovolov.forgetmenot.presentation.App
import com.odnovolov.forgetmenot.presentation.screen.exercise.di.ExerciseScreenComponent
import com.odnovolov.forgetmenot.presentation.screen.home.di.HomeScreenComponent
import dagger.BindsInstance
import dagger.Component

@AppScope
@Component(modules = [DataModule::class, DomainModule::class])
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun app(app: App): Builder

        fun build(): AppComponent
    }

    fun homeScreenComponentBuilder(): HomeScreenComponent.Builder
    fun exerciseScreenComponentBuilder(): ExerciseScreenComponent.Builder
}