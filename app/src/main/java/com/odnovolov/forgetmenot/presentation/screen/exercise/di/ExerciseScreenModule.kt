package com.odnovolov.forgetmenot.presentation.screen.exercise.di

import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.*
import dagger.Module
import dagger.Provides

@Module
class ExerciseScreenModule {

    @FragmentScope
    @Provides
    fun provideExerciseScreen(): ExerciseScreen {
        return ExerciseScreen()
    }

    @FragmentScope
    @Provides
    fun provideExerciseFragmentBindings(
        feature: ExerciseFeature,
        screen: ExerciseScreen,
        viewPagerAdapter: ExerciseCardsAdapter
    ): ExerciseFragmentBindings {
        return ExerciseFragmentBindings(feature, screen, viewPagerAdapter)
    }

    @Provides
    fun provideExerciseCardFragmentBindings(screen: ExerciseScreen): ExerciseCardFragmentBindings {
        return ExerciseCardFragmentBindings(screen)
    }

    @FragmentScope
    @Provides
    fun provideExerciseCardsAdapter(exerciseFragment: ExerciseFragment): ExerciseCardsAdapter {
        return ExerciseCardsAdapter(exerciseFragment)
    }
}