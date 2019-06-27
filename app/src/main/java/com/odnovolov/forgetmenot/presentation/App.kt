package com.odnovolov.forgetmenot.presentation

import android.app.Application
import com.odnovolov.forgetmenot.presentation.di.ComponentStore
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.appscope.DaggerAppComponent

class App : Application() {

    private lateinit var component: AppComponent

    override fun onCreate() {
        super.onCreate()
        setupDI()
    }

    private fun setupDI() {
        component = DaggerAppComponent.builder()
            .app(this)
            .build()
        ComponentStore.keep(component)
    }
}