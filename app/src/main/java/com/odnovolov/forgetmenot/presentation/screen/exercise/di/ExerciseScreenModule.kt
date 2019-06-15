package com.odnovolov.forgetmenot.presentation.screen.exercise.di

import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseCardsAdapter
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseFragmentBindings
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseScreen
import dagger.Module
import dagger.Provides

@Module
class ExerciseScreenModule {

    @FragmentScope
    @Provides
    fun provideBindings(
        feature: ExerciseFeature,
        screen: ExerciseScreen,
        viewPagerAdapter: ExerciseCardsAdapter
    ): ExerciseFragmentBindings {
        return ExerciseFragmentBindings(feature, screen, viewPagerAdapter)
    }

    @FragmentScope
    @Provides
    fun provideExerciseCardsAdapter(): ExerciseCardsAdapter {
        return ExerciseCardsAdapter()
    }

    @FragmentScope
    @Provides
    fun provideExerciseScreen(): ExerciseScreen {
        return ExerciseScreen()
    }
}