package com.odnovolov.forgetmenot.presentation.di.viewmodelscope

import com.odnovolov.forgetmenot.data.repository.DeckRepositoryImpl
import com.odnovolov.forgetmenot.data.repository.ExerciseRepositoryImpl
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import com.odnovolov.forgetmenot.domain.feature.deckspreview.DecksPreviewFeature
import dagger.Module
import dagger.Provides
import io.reactivex.android.schedulers.AndroidSchedulers

@Module
class FeatureModule {

    @ViewModelScope
    @Provides
    fun provideAddNewDeckFeature(repository: DeckRepositoryImpl): AddNewDeckFeature {
        return AddNewDeckFeature(repository, AndroidSchedulers.mainThread())
    }

    @ViewModelScope
    @Provides
    fun provideDecksPreviewFeature(
            deckRepositoryImpl: DeckRepositoryImpl,
            exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): DecksPreviewFeature {
        return DecksPreviewFeature(
                deckRepositoryImpl,
                exerciseRepositoryImpl,
                AndroidSchedulers.mainThread()
        )
    }
}