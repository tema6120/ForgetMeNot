package com.odnovolov.forgetmenot.presentation

import android.app.Application
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        AppComponent.createWith(this)
    }
}