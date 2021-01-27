package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.setTooltipTextFromContentDescription
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab.Content
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorScreenTab.Settings
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentFragment
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsFragment
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_deck_editor.*
import kotlinx.coroutines.launch

class DeckEditorFragment : BaseFragment() {
    init {
        DeckEditorDiScope.reopenIfClosed()
    }

    private var tabLayoutMediator: TabLayoutMediator? = null
    private var controller: DeckEditorController? = null
    private lateinit var viewModel: DeckEditorViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DeckEditorDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel(isRecreated = savedInstanceState != null)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        deckNameTextView.setOnClickListener {
            controller?.dispatch(RenameDeckButtonClicked)
        }
        addCardButton.run {
            setOnClickListener { controller?.dispatch(AddCardButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        setupViewPager()
    }

    private fun setupViewPager() {
        deckEditorViewPager.isUserInputEnabled = false
        deckEditorViewPager.adapter = DeckEditorPagerAdapter(this)
        deckEditorViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                private var isAddCardButtonVisible = false
                    set(value) {
                        if (field != value) {
                            field = value
                            with(addCardButton) { if (value) show() else hide() }
                        }
                    }

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    isAddCardButtonVisible = position == 1 && positionOffset == 0f
                }
            }
        )
        deckEditorViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    appBarElevationManager.viewPagerPosition = position
                }
            }
        )
    }

    private fun observeViewModel(isRecreated: Boolean) {
        with(viewModel) {
            setupViewPager(tabs, isRecreated)
            deckName.observe(deckNameTextView::setText)
        }
    }

    private fun setupViewPager(tabs: DeckEditorTabs, isRecreated: Boolean) {
        val needTabs: Boolean = tabs is DeckEditorTabs.All
        deckEditorTabLayout.isVisible = needTabs
        deckEditorViewPager.offscreenPageLimit =
            if (needTabs) 1
            else ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
        deckEditorViewPager.isUserInputEnabled = needTabs
        if (!isRecreated) {
            val activeTab: Int = when (tabs) {
                is DeckEditorTabs.All -> {
                    when (tabs.initialTab) {
                        Settings -> 0
                        Content -> 1
                    }
                }
                DeckEditorTabs.OnlyDeckSettings -> 0
            }
            deckEditorViewPager.setCurrentItem(activeTab, false)
        }
        if (needTabs) {
            tabLayoutMediator = TabLayoutMediator(
                deckEditorTabLayout,
                deckEditorViewPager
            ) { tab, position ->
                val customTab = View.inflate(requireContext(), R.layout.tab, null) as TextView
                customTab.text = getString(
                    when (position) {
                        0 -> R.string.tab_name_settings
                        1 -> R.string.tab_name_content
                        else -> throw IllegalArgumentException("position must be in 0..1")
                    }
                )
                tab.customView = customTab
            }.apply { attach() }
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is DeckSettingsFragment -> {
                childFragment.scrollListener =
                    NestedScrollView.OnScrollChangeListener { nestedScrollView, _, _, _, _ ->
                        appBarElevationManager.canDeckSettingsScrollUp =
                            nestedScrollView?.canScrollVertically(-1) ?: false
                    }
            }
            is DeckContentFragment -> {
                childFragment.scrollListener = object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        appBarElevationManager.canDeckContentScrollUp =
                            recyclerView.canScrollVertically(-1)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        deckEditorViewPager.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckEditorDiScope.close()
        }
    }

    private val appBarElevationManager = object {
        var viewPagerPosition = 0
            set(value) {
                if (field != value) {
                    field = value
                    updateAppBarElevation()
                }
            }

        var canDeckSettingsScrollUp = false
            set(value) {
                if (field != value) {
                    field = value
                    updateAppBarElevation()
                }
            }

        var canDeckContentScrollUp = false
            set(value) {
                if (field != value) {
                    field = value
                    updateAppBarElevation()
                }
            }

        private fun updateAppBarElevation() {
            appBarLayout.isActivated = viewPagerPosition == 0 && canDeckSettingsScrollUp ||
                    viewPagerPosition == 1 && canDeckContentScrollUp
        }
    }
}