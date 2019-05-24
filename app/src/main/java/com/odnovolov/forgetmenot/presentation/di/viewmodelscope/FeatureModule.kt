package com.odnovolov.forgetmenot.presentation.di.viewmodelscope

import com.odnovolov.forgetmenot.data.repository.DeckRepositoryImpl
import com.odnovolov.forgetmenot.domain.feature.addnewdeck.AddNewDeckFeature
import dagger.Module
import dagger.Provides

@Module
class FeatureModule {

    @ViewModelScope
    @Provides
    fun provideFeature(repository: DeckRepositoryImpl): AddNewDeckFeature {
        return AddNewDeckFeature(repository)
    }
}