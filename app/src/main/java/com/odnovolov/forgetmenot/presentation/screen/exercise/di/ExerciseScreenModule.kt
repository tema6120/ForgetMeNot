package com.odnovolov.forgetmenot.presentation.screen.exercise.di

import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardsAdapter
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragmentBindings
import dagger.Module
import dagger.Provides

@Module
class ExerciseScreenModule {

    @FragmentScope
    @Provides
    fun provideBindings(feature: ExerciseFeature): ExerciseFragmentBindings {
        return ExerciseFragmentBindings(feature)
    }

    @FragmentScope
    @Provides
    fun provideExerciseCardsAdapter(): ExerciseCardsAdapter {
        return ExerciseCardsAdapter()
    }
}