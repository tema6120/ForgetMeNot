package com.odnovolov.forgetmenot.presentation.common.di

import com.odnovolov.forgetmenot.Database
import com.odnovolov.forgetmenot.domain.entity.GlobalState
import com.odnovolov.forgetmenot.persistence.DatabaseInitializer
import com.odnovolov.forgetmenot.persistence.longterm.LongTermStateSaverImpl
import com.odnovolov.forgetmenot.persistence.longterm.globalstate.provision.GlobalStateProvider
import com.odnovolov.forgetmenot.presentation.common.App
import com.odnovolov.forgetmenot.presentation.common.LongTermStateSaver
import com.odnovolov.forgetmenot.presentation.common.Navigator
import com.odnovolov.forgetmenot.presentation.common.businessLogicThread
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration

class AppDiScope(
    val app: App,
    val navigator: Navigator
) {
    val sqlDriver = DatabaseInitializer.initSqlDriver(app)

    val database: Database = DatabaseInitializer.initDatabase(sqlDriver)

    val globalState: GlobalState = GlobalStateProvider(database).load()

    val longTermStateSaver: LongTermStateSaver = LongTermStateSaverImpl(database)

    val json = Json(JsonConfiguration.Stable)

    companion object {
        private lateinit var instance: AppDiScope

        fun init(app: App) {
            val navigator = Navigator()
            app.registerActivityLifecycleCallbacks(navigator)
            GlobalScope.launch(businessLogicThread) {
                instance = AppDiScope(app, navigator)
            }
        }

        fun get() = instance
    }
}