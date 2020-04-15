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
import com.odnovolov.forgetmenot.domain.interactor.deckadder.DeckAdder
import com.odnovolov.forgetmenot.persistence.DbCleaner
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingDiScope

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    var keyEventInterceptor: ((KeyEvent) -> Boolean)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            DbCleaner.cleanupDatabase()
            openFirstScreenDiScopes()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNavController()
    }

    private fun openFirstScreenDiScopes() {
        HomeDiScope.open { HomeDiScope.create(HomeScreenState()) }
        DeckSortingDiScope.open { DeckSortingDiScope() }
        AddDeckDiScope.open { AddDeckDiScope.create(DeckAdder.State(), AddDeckScreenState()) }
    }

    private fun initNavController() {
        navController = findNavController(R.id.navHostFragment)
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