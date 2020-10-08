package com.odnovolov.forgetmenot.presentation.common.mainactivity

import android.content.res.Configuration
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.deckcreator.DeckFromFileCreator
import com.odnovolov.forgetmenot.persistence.DbCleaner
import com.odnovolov.forgetmenot.presentation.common.mainactivity.InitialDecksAdder.Event.AppStarted
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.adddeck.AddDeckScreenState
import com.odnovolov.forgetmenot.presentation.screen.home.decksorting.DeckSortingDiScope
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    init {
        MainActivityDiScope.reopenIfClosed()
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    var keyEventInterceptor: ((KeyEvent) -> Boolean)? = null
    private val backPressInterceptors: MutableList<BackPressInterceptor> = ArrayList()
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    lateinit var fullscreenModeManager: FullscreenModeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            DbCleaner.cleanupDatabase()
            openFirstScreenDiScopes()
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.setBackgroundColor(ContextCompat.getColor(this, R.color.background_window))
        initNavController()
        coroutineScope.launch {
            val diScope = MainActivityDiScope.getAsync() ?: return@launch
            if (savedInstanceState == null) {
                diScope.initialDecksAdder.dispatch(AppStarted)
            }
            val isInMultiWindowMode = if (VERSION.SDK_INT >= VERSION_CODES.N) {
                isInMultiWindowMode
            } else false
            fullscreenModeManager = FullscreenModeManager(
                diScope.fullScreenPreference,
                window.decorView,
                findViewById(android.R.id.content),
                window,
                navController,
                isInMultiWindowMode
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
            Handler(Looper.getMainLooper()).post {
                fullscreenModeManager.isInMultiWindow = isInMultiWindowMode
            }
        }
    }

    private fun openFirstScreenDiScopes() {
        HomeDiScope.open { HomeDiScope.create(HomeScreenState()) }
        DeckSortingDiScope.open { DeckSortingDiScope() }
        AddDeckDiScope.open {
            AddDeckDiScope.create(
                DeckFromFileCreator.State(),
                AddDeckScreenState()
            )
        }
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

    fun registerBackPressInterceptor(backPressInterceptor: BackPressInterceptor) {
        backPressInterceptors.add(backPressInterceptor)
    }

    fun unregisterBackPressInterceptor(backPressInterceptor: BackPressInterceptor) {
        backPressInterceptors.remove(backPressInterceptor)
    }

    override fun onBackPressed() {
        backPressInterceptors.forEach { backPressInterceptor ->
            val intercepted = backPressInterceptor.onBackPressed()
            if (intercepted) return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenModeManager.setFullscreenMode(false)
        coroutineScope.cancel()
        if (isFinishing || !isChangingConfigurations) {
            MainActivityDiScope.close()
        }
    }

    interface BackPressInterceptor {
        fun onBackPressed(): Boolean
    }
}