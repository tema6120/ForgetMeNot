package com.odnovolov.forgetmenot.presentation

import android.app.Application
import com.odnovolov.forgetmenot.presentation.di.Injector

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Injector.init(this)
    }
}