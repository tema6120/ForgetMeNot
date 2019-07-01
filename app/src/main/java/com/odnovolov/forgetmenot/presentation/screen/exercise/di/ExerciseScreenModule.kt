package com.odnovolov.forgetmenot.presentation.screen.exercise.di

import com.badoo.mvicore.android.AndroidTimeCapsule
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import com.odnovolov.forgetmenot.presentation.di.fragmentscope.FragmentScope
import com.odnovolov.forgetmenot.presentation.screen.exercise.*
import dagger.Module
import dagger.Provides

@Module
class ExerciseScreenModule {

    @FragmentScope
    @Provides
    fun exerciseCardsAdapter(exerciseFragment: ExerciseFragment): ExerciseCardsAdapter {
        return ExerciseCardsAdapter(exerciseFragment)
    }

    @FragmentScope
    @Provides
    fun exerciseScreenFeature(
        timeCapsule: AndroidTimeCapsule,
        exerciseFeature: ExerciseFeature
    ): ExerciseScreenFeature {
        return ExerciseScreenFeature(timeCapsule, exerciseFeature)
    }

    @Provides
    fun exerciseFragmentBindings(
        feature: ExerciseScreenFeature,
        fragment: ExerciseFragment,
        viewPagerAdapter: ExerciseCardsAdapter
    ): ExerciseFragmentBindings {
        return ExerciseFragmentBindings(feature, fragment, viewPagerAdapter)
    }

    @Provides
    fun exerciseCardFragmentBindings(exerciseScreenFeature: ExerciseScreenFeature): ExerciseCardFragmentBindings {
        return ExerciseCardFragmentBindings(exerciseScreenFeature)
    }
}