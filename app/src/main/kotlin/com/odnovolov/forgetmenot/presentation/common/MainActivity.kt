package com.odnovolov.forgetmenot.presentation.common

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.id
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.presentation.screen.home.HOME_SCREEN_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.HomeViewModel
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.ADD_DECK_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckViewModel
import org.koin.android.ext.android.getKoin

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    var keyEventInterceptor: ((KeyEvent) -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            initFirstScreenState()
        }
        setContentView(R.layout.activity_main)
        initNavController()
    }

    private fun initFirstScreenState() {
        val homeScreenScope = getKoin().createScope<HomeViewModel>(HOME_SCREEN_SCOPE_ID)
        homeScreenScope.declare(HomeScreenState(), override = true)
        val addDeckScope = getKoin().createScope<AddDeckViewModel>(ADD_DECK_SCOPE_ID)
        addDeckScope.declare(DeckAdder.State(), override = true)
        addDeckScope.declare(AddDeckScreenState(), override = true)
    }

    private fun initNavController() {
        navController = findNavController(id.navHostFragment)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        if (event != null && keyEventInterceptor != null) {
            val intercepted = keyEventInterceptor!!.invoke(event)
            if (intercepted) return true
        }
        return super.dispatchKeyEvent(event)
    }
}