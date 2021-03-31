package com.odnovolov.forgetmenot.presentation.common.di

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.domain.interactor.decklistseditor.recheckDeckIdsInDeckLists
import com.odnovolov.forgetmenot.domain.interactor.fileimport.FileImportStorage
import com.odnovolov.forgetmenot.persistence.DatabaseInitializer
import com.odnovolov.forgetmenot.persistence.longterm.LongTermStateSaverImpl
import com.odnovolov.forgetmenot.persistence.longterm.cardappearance.CardAppearanceProvider
import com.odnovolov.forgetmenot.persistence.longterm.exercisesettings.ExerciseSettingsProvider
import com.odnovolov.forgetmenot.persistence.longterm.fileimportstorage.FileImportStorageProvider
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision.GlobalStateProvider
import com.odnovolov.forgetmenot.persistence.longterm.lastusedlanguages.LastUsedLanguagesProvider
import com.odnovolov.forgetmenot.persistence.longterm.tipstate.TipStateProvider
import com.odnovolov.forgetmenot.persistence.longterm.walkingmodepreference.WalkingModePreferenceProvider
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModePreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AppDiScope(
    val app: App,
    val navigator: Navigator,
    activityLifecycleCallbacksInterceptor: ActivityLifecycleCallbacksInterceptor
) {
    val sqlDriver = DatabaseInitializer.initSqlDriver(app)

    val database: Database = DatabaseInitializer.initDatabase(sqlDriver)

    val globalState: GlobalState = GlobalStateProvider(database).load()

    val walkingModePreference: WalkingModePreference =
        WalkingModePreferenceProvider(database).load()

    val fileImportStorage: FileImportStorage =
        FileImportStorageProvider(database).load()

    init {
        TipStateProvider(database).load()
    }

    val json = Json

    val longTermStateSaver: LongTermStateSaver = LongTermStateSaverImpl(database, json)

    val audioFocusManager = AudioFocusManager(app)

    val speakerImpl = SpeakerImpl(
        app,
        activityLifecycleCallbacksInterceptor.activityLifecycleEventFlow,
        audioFocusManager,
        lastUsedLanguages = LastUsedLanguagesProvider(database).load()
    )

    val cardAppearance: CardAppearance = CardAppearanceProvider(database).load()

    val exerciseSettings: ExerciseSettings = ExerciseSettingsProvider(database, json).load()

    init {
        recheckDeckIdsInDeckLists(globalState)
        longTermStateSaver.saveStateByRegistry()
    }

    companion object {
        @Volatile
        private lateinit var instance: AppDiScope

        fun init(app: App) {
            val activityLifecycleCallbacksInterceptor = ActivityLifecycleCallbacksInterceptor()
            app.registerActivityLifecycleCallbacks(activityLifecycleCallbacksInterceptor)
            val navigator = Navigator()
            app.registerActivityLifecycleCallbacks(navigator)
            GlobalScope.launch(businessLogicThread) {
                instance = AppDiScope(app, navigator, activityLifecycleCallbacksInterceptor)
            }
        }

        fun get() = instance
    }
}