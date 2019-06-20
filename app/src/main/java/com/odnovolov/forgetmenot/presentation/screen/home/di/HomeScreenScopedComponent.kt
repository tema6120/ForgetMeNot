package com.odnovolov.forgetmenot.presentation.screen.home.di

import com.odnovolov.forgetmenot.presentation.di.ComponentsStore
import com.odnovolov.forgetmenot.presentation.di.ScopedComponent
import com.odnovolov.forgetmenot.presentation.di.appscope.AppScopedComponent

class HomeScreenScopedComponent : ScopedComponent<HomeScreenComponent>() {

    override fun create(): HomeScreenComponent {
        return ComponentsStore.find<AppScopedComponent>().dependAndGet(this)
            .homeScreenComponentBuilder()
            .build()
    }
}