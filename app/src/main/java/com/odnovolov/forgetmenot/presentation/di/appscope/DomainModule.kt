package com.odnovolov.forgetmenot.presentation.di.appscope

import com.odnovolov.forgetmenot.data.repository.DeckRepositoryImpl
import com.odnovolov.forgetmenot.data.repository.ExerciseRepositoryImpl
import com.odnovolov.forgetmenot.domain.feature.adddeck.AddDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import com.odnovolov.forgetmenot.domain.feature.deletedeck.DeleteDeckFeature
import com.odnovolov.forgetmenot.domain.feature.exercise.ExerciseFeature
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers

@Module
class DomainModule {

    @AppScope
    @Provides
    fun addDeckFeature(
        repository: DeckRepositoryImpl
    ) = AddDeckFeature(
        repository, AndroidSchedulers.mainThread()
    )

    @AppScope
    @Provides
    fun decksPreviewFeature(
        deckRepositoryImpl: DeckRepositoryImpl,
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ) = DecksPreviewFeature(
        deckRepositoryImpl,
        exerciseRepositoryImpl,
        AndroidSchedulers.mainThread()
    )

    @AppScope
    @Provides
    fun deleteDeckFeature(
        deckRepositoryImpl: DeckRepositoryImpl
    ) = DeleteDeckFeature(
        deckRepositoryImpl, AndroidSchedulers.mainThread()
    )

    @AppScope
    @Provides
    fun exerciseFeature(
        exerciseRepository: ExerciseRepositoryImpl
    ) = ExerciseFeature(
        exerciseRepository, AndroidSchedulers.mainThread()
    )
}