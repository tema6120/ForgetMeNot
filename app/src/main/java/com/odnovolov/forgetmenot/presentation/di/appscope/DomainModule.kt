package com.odnovolov.forgetmenot.presentation.di.appscope

import com.odnovolov.forgetmenot.data.repository.ExerciseRepositoryImpl
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers

@Module
class DomainModule {

    @AppScope
    @Provides
    fun provideExerciseFeature(exerciseRepository: ExerciseRepositoryImpl): ExerciseFeature {
        return ExerciseFeature(exerciseRepository, AndroidSchedulers.mainThread())
    }
}