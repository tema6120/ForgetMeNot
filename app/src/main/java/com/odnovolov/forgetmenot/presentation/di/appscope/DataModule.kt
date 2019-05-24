package com.odnovolov.forgetmenot.presentation.di.appscope

import androidx.room.Room
import com.odnovolov.forgetmenot.data.db.AppDatabase
import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.data.repository.DeckRepositoryImpl
import com.odnovolov.forgetmenot.presentation.App
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @AppScope
    @Provides
    fun provideDb(app: App): AppDatabase {
        //app.deleteDatabase(AppDatabase.NAME)
        return Room.databaseBuilder(app, AppDatabase::class.java, AppDatabase.NAME)
            .build()
    }

    @AppScope
    @Provides
    fun provideDeckDao(db: AppDatabase): DeckDao {
        return db.deckDao()
    }

    @AppScope
    @Provides
    fun provideRepository(deckDao: DeckDao): DeckRepositoryImpl {
        return DeckRepositoryImpl(deckDao)
    }
}