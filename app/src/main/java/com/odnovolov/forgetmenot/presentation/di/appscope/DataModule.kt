package com.odnovolov.forgetmenot.presentation.di.appscope

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.odnovolov.forgetmenot.data.db.AppDatabase
import com.odnovolov.forgetmenot.data.db.dao.DeckDao
import com.odnovolov.forgetmenot.data.db.dao.ExerciseDao
import com.odnovolov.forgetmenot.data.keyvaluestore.KeyValueStore
import com.odnovolov.forgetmenot.data.repository.DeckRepositoryImpl
import com.odnovolov.forgetmenot.data.repository.ExerciseRepositoryImpl
import com.odnovolov.forgetmenot.presentation.App
import dagger.Module
import dagger.Provides

@Module
class DataModule {

    @AppScope
    @Provides
    fun appDatabase(app: App): AppDatabase {
        //app.deleteDatabase(AppDatabase.NAME)
        return Room.databaseBuilder(app, AppDatabase::class.java, AppDatabase.NAME)
            .build()
    }

    @AppScope
    @Provides
    fun deckDao(db: AppDatabase): DeckDao {
        return db.deckDao()
    }

    @AppScope
    @Provides
    fun exerciseDao(db: AppDatabase): ExerciseDao {
        return db.exerciseDao()
    }

    @AppScope
    @Provides
    fun sharedPreferences(app: App): SharedPreferences {
        return app.getSharedPreferences("App preferences", Context.MODE_PRIVATE)
    }

    @AppScope
    @Provides
    fun keyValueStore(sharedPreferences: SharedPreferences): KeyValueStore {
        return KeyValueStore(sharedPreferences)
    }

    @AppScope
    @Provides
    fun deckRepository(db: AppDatabase, keyValueStore: KeyValueStore): DeckRepositoryImpl {
        return DeckRepositoryImpl(db, keyValueStore)
    }

    @AppScope
    @Provides
    fun exerciseRepository(exerciseDao: ExerciseDao): ExerciseRepositoryImpl {
        return ExerciseRepositoryImpl(exerciseDao)
    }
}