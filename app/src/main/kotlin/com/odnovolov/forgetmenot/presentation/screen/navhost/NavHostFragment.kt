package com.odnovolov.forgetmenot.presentation.screen.navhost

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.GravityCompat
import androidx.core.view.updatePadding
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.about.AboutFragment
import com.odnovolov.forgetmenot.presentation.screen.help.HelpFragment
import com.odnovolov.forgetmenot.presentation.screen.home.HomeFragment
import com.odnovolov.forgetmenot.presentation.screen.settings.SettingsFragment
import kotlinx.android.synthetic.main.fragment_nav_host.*
import kotlinx.android.synthetic.main.main_drawer.*
import kotlin.reflect.KClass

class NavHostFragment : BaseFragment() {

    private var actionOnDrawerClosed: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nav_host, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        drawerColumn.updatePadding(top = 24.dp + getStatusBarHeight())
        if (childFragmentManager.fragments.isEmpty()) {
            childFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeFragment())
                .commitNow()
        }
        updateDrawerItems()
        childFragmentManager.addOnBackStackChangedListener {
            updateDrawerItems()
        }
        drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerClosed(drawerView: View) {
                actionOnDrawerClosed?.invoke()
                actionOnDrawerClosed = null
            }

            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
        })
        setClickListeners()
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun setClickListeners() {
        NavigationDestination.values().forEach { navigationDestination: NavigationDestination ->
            val button: View = requireView().findViewById(navigationDestination.itemButtonId)
            button.setOnClickListener {
                scheduleFragmentReplacement(navigationDestination)
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun scheduleFragmentReplacement(
        navigationDestination: NavigationDestination
    ) {
        val currentChildFragmentClass = getCurrentChildFragmentClass()
        actionOnDrawerClosed = when {
            currentChildFragmentClass == navigationDestination.fragmentClass -> {
                null
            }
            currentChildFragmentClass == HomeFragment::class -> {
                {
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, navigationDestination.createFragment())
                        .addToBackStack(null)
                        .commit()
                    updateDrawerItems()
                }
            }
            navigationDestination.fragmentClass == HomeFragment::class -> {
                {
                    childFragmentManager.popBackStack()
                    updateDrawerItems()
                }
            }
            else -> {
                {
                    childFragmentManager.popBackStack()
                    childFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, navigationDestination.createFragment())
                        .addToBackStack(null)
                        .commit()
                    updateDrawerItems()
                }
            }
        }
    }

    private fun getCurrentChildFragmentClass(): KClass<out Fragment>? {
        return childFragmentManager.fragments.run {
            if (isEmpty())
                null
            else
                last()::class
        }
    }

    private fun updateDrawerItems() {
        val currentChildFragmentClass = getCurrentChildFragmentClass() ?: return
        NavigationDestination.values().forEach { navigationDestination: NavigationDestination ->
            val isSelected = currentChildFragmentClass == navigationDestination.fragmentClass
            val itemButton: LinearLayout =
                requireView().findViewById(navigationDestination.itemButtonId)
            itemButton.isSelected = isSelected
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity)
            .registerBackPressInterceptor(backPressInterceptorForClosingDrawer)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity)
            .unregisterBackPressInterceptor(backPressInterceptorForClosingDrawer)
    }

    private val backPressInterceptorForClosingDrawer = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            return if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            } else {
                false
            }
        }
    }

    private enum class NavigationDestination(
        val fragmentClass: KClass<out Fragment>,
        val itemButtonId: Int,
        val createFragment: () -> Fragment
    ) {
        Decks(
            HomeFragment::class,
            R.id.decksDrawerItem,
            ::HomeFragment
        ),
        Settings(
            SettingsFragment::class,
            R.id.settingsDrawerItem,
            ::SettingsFragment
        ),
        Help(
            HelpFragment::class,
            R.id.helpDrawerItem,
            ::HelpFragment
        ),
        About(
            AboutFragment::class,
            R.id.aboutDrawerItem,
            ::AboutFragment
        )
    }
}