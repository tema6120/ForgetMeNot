package com.odnovolov.forgetmenot.presentation.di

import com.odnovolov.forgetmenot.presentation.App
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.appscope.DaggerAppComponent
import com.odnovolov.forgetmenot.presentation.di.viewmodelscope.HomeViewModelComponent
import com.odnovolov.forgetmenot.presentation.screen.HomeFragment
import java.lang.ref.WeakReference

object Injector {

    lateinit var appComponent: AppComponent
    private var homeViewModelComponent: WeakReference<HomeViewModelComponent>? = null

    fun createAppComponent(app: App) {
        appComponent = DaggerAppComponent.builder()
            .app(app)
            .build()
    }

    fun getHomeViewModelComponent(): HomeViewModelComponent {
        val component = appComponent.homeViewModelComponentBuilder()
            .build()
        homeViewModelComponent = WeakReference(component)
        return component
    }

    fun inject(homeFragment: HomeFragment) {
        homeViewModelComponent!!.get()!!.homeFragmentComponentBuilder()
            .build()
            .inject(homeFragment)
    }
}