package com.odnovolov.forgetmenot.presentation.screen.decksetup

import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showActionBar
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupController.Command.ShowRenameDialogWithText
import com.odnovolov.forgetmenot.presentation.screen.decksetup.DeckSetupEvent.*
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.fragment_deck_setup.*
import kotlinx.coroutines.launch

class DeckSetupFragment : BaseFragment() {
    init {
        DeckSetupDiScope.reopenIfClosed()
    }

    private var tabLayoutMediator: TabLayoutMediator? = null
    private var controller: DeckSetupController? = null
    private lateinit var viewModel: DeckSetupViewModel
    private lateinit var renameDeckDialog: AlertDialog
    private lateinit var renameDeckEditText: EditText
    private lateinit var diScope: DeckSetupDiScope

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initRenameDeckDialog()
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_deck_setup, container, false)
    }

    private fun initRenameDeckDialog() {
        val contentView = View.inflate(context, R.layout.dialog_input, null)
        renameDeckEditText = contentView.dialogInput
        renameDeckEditText.observeText { text: String ->
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
        renameDeckDialog.setOnShowListener { renameDeckEditText.showSoftInput() }
        dialogTimeCapsule.register("renameDeckDialog", renameDeckDialog)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.deck_setup_actions, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_rename_deck -> {
                controller?.dispatch(RenameDeckButtonClicked)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewPager()
        viewCoroutineScope!!.launch {
            diScope = DeckSetupDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.deckSetupViewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupViewPager() {
        deckOverviewViewPager.adapter = DeckSetupPagerAdapter(this)
        deckOverviewViewPager.offscreenPageLimit = 1
        tabLayoutMediator = TabLayoutMediator(
            deckOverviewTabLayout,
            deckOverviewViewPager
        ) { tab, position ->
            tab.text = getString(
                when (position) {
                    0 -> R.string.tab_name_settings
                    1 -> R.string.tab_name_content
                    else -> throw IllegalArgumentException("position must be in 0..1")
                }
            )
        }.apply { attach() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckName.observe { deckName: String ->
                (activity as AppCompatActivity).supportActionBar?.title = deckName
            }
            deckNameCheckResult.observe { nameCheckResult: NameCheckResult ->
                renameDeckEditText.error = when (nameCheckResult) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
                if (renameDeckDialog.isShowing) {
                    renameDeckDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .isEnabled = nameCheckResult == Ok
                }
            }
        }
    }

    private fun executeCommand(command: DeckSetupController.Command) {
        when (command) {
            is ShowRenameDialogWithText -> {
                renameDeckEditText.setText(command.text)
                renameDeckEditText.selectAll()
                renameDeckDialog.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tabLayoutMediator?.detach()
        tabLayoutMediator = null
        deckOverviewViewPager.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckSetupDiScope.close()
        }
    }
}