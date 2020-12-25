package com.odnovolov.forgetmenot.presentation.screen.deckeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorController.Command.ShowRenameDialogWithText
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.DeckEditorEvent.*
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
    private var renameDeckDialog: AlertDialog? = null
    private var renameDeckEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_editor, container, false)
    }

    private fun requireRenameDeckDialog(): AlertDialog {
        if (renameDeckDialog == null) {
            val contentView = View.inflate(context, R.layout.dialog_input, null)
            renameDeckEditText = contentView.dialogInput
            renameDeckEditText!!.observeText { text: String ->
                controller?.dispatch(RenameDeckDialogTextChanged(text))
            }
            renameDeckDialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_deck_name_dialog)
                .setView(contentView)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    controller?.dispatch(RenameDeckDialogPositiveButtonClicked)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .apply { setOnShowListener { renameDeckEditText?.showSoftInput() } }
        }
        return renameDeckDialog!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DeckEditorDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.deckSetupViewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        deckNameTextView.setOnClickListener {
            controller?.dispatch(RenameDeckButtonClicked)
        }
        setupViewPager()
    }

    private fun setupViewPager() {
        deckEditorViewPager.offscreenPageLimit = 1
        deckEditorViewPager.adapter = DeckEditorPagerAdapter(this)
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
        deckEditorViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    appBarElevationManager.viewPagerPosition = position
                }
            }
        )
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckName.observe { deckName: String ->
                deckNameTextView.text = deckName
            }
            deckNameCheckResult.observe { nameCheckResult: NameCheckResult ->
                renameDeckEditText?.error = when (nameCheckResult) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
                if (renameDeckDialog?.isShowing == true) {
                    renameDeckDialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
                        .isEnabled = nameCheckResult == Ok
                }
            }
        }
    }

    private fun executeCommand(command: DeckEditorController.Command) {
        when (command) {
            is ShowRenameDialogWithText -> {
                requireRenameDeckDialog()
                renameDeckEditText!!.setText(command.text)
                renameDeckEditText!!.selectAll()
                renameDeckDialog!!.show()
            }
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

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            val dialogSavedState: Bundle? = getBundle(STATE_RENAME_DECK_DIALOG)
            if (dialogSavedState != null) {
                requireRenameDeckDialog().onRestoreInstanceState(dialogSavedState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (renameDeckDialog?.isShowing == true) {
            outState.putBundle(STATE_RENAME_DECK_DIALOG, renameDeckDialog!!.onSaveInstanceState())
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

    companion object {
        private const val STATE_RENAME_DECK_DIALOG = "STATE_RENAME_DECK_DIALOG"
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