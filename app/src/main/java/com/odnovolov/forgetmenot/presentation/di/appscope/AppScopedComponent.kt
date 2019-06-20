package com.odnovolov.forgetmenot.presentation.di.appscope

import com.odnovolov.forgetmenot.presentation.App
import com.odnovolov.forgetmenot.presentation.di.ScopedComponent

class AppScopedComponent(private val app: App) : ScopedComponent<AppComponent>() {

    override fun create(): AppComponent {
        return DaggerAppComponent.builder()
            .app(app)
            .build()
    }
}