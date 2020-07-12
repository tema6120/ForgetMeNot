package com.odnovolov.forgetmenot.presentation.common.mainactivity

import android.os.Bundle
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator
import com.odnovolov.forgetmenot.persistence.DbCleaner
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingDiScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    init {
        MainActivityDiScope.reopenIfClosed()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    var keyEventInterceptor: ((KeyEvent) -> Boolean)? = null
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var fullscreenModeManager: FullscreenModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            DbCleaner.cleanupDatabase()
            openFirstScreenDiScopes()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initNavController()
        coroutineScope.launch {
            val diScope = MainActivityDiScope.get()
            fullscreenModeManager = FullscreenModeManager(
                diScope.fullScreenPreference,
                window.decorView,
                findViewById(android.R.id.content),
                window,
                navController
            )
        }
    }

    private fun openFirstScreenDiScopes() {
        HomeDiScope.open { HomeDiScope.create(HomeScreenState()) }
        DeckSortingDiScope.open { DeckSortingDiScope() }
        AddDeckDiScope.open { AddDeckDiScope.create(DeckFromFileCreator.State(), AddDeckScreenState()) }
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

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing || !isChangingConfigurations) {
            MainActivityDiScope.close()
        }
    }
}