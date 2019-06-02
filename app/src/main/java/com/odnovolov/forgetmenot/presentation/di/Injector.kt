package com.odnovolov.forgetmenot.presentation.di

import com.odnovolov.forgetmenot.presentation.App
import com.odnovolov.forgetmenot.presentation.di.appscope.AppComponent
import com.odnovolov.forgetmenot.presentation.di.appscope.DaggerAppComponent
import com.odnovolov.forgetmenot.presentation.di.viewmodelscope.HomeViewModelComponent
import com.odnovolov.forgetmenot.presentation.navigation.Navigator
import com.odnovolov.forgetmenot.presentation.navigation.NavigatorActivity
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import java.lang.ref.WeakReference

object Injector {

    lateinit var appComponent: AppComponent
    private var homeViewModelComponent: WeakReference<HomeViewModelComponent>? = null
    private var navigator: WeakReference<Navigator>? = null

    fun createAppComponent(app: App) {
        appComponent = DaggerAppComponent.builder()
            .app(app)
            .build()
    }

    fun createHomeViewModelComponent(): HomeViewModelComponent {
        val component = appComponent.homeViewModelComponentBuilder()
            .build()
        homeViewModelComponent = WeakReference(component)
        return component
    }

    fun depend(navigatorActivity: NavigatorActivity) {
        this.navigator = WeakReference(navigatorActivity)
    }

    fun inject(homeFragment: HomeFragment) {
        homeViewModelComponent!!.get()!!.homeFragmentComponentBuilder()
            .navigator(navigator!!.get()!!)
            .build()
            .inject(homeFragment)
    }
}