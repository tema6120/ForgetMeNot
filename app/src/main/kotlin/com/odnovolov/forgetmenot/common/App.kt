package com.odnovolov.forgetmenot.common

import android.app.Application
import com.odnovolov.forgetmenot.common.database.initDatabase

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        initDatabase(applicationContext = this)
    }
}