package com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorFragment
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorController.Command.AskUserToConfirmExit
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorEvent.*
import kotlinx.android.synthetic.main.fragment_ongoing_card_editor.*
import kotlinx.coroutines.*

class OngoingCardEditorFragment : BaseFragment() {
    init {
        OngoingCardEditorDiScope.reopenIfClosed()
    }

    private val fragmentCoroutineScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
    private lateinit var viewModel: OngoingCardEditorViewModel
    private var controller: OngoingCardEditorController? = null
    private lateinit var exitDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createExitDialog()
        return inflater.inflate(R.layout.fragment_ongoing_card_editor, container, false)
    }

    private fun createExitDialog() {
        exitDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_exit_dialog)
            .setMessage(R.string.message_changes_will_be_lost)
            .setPositiveButton(R.string.yes) { _, _ -> controller?.dispatch(UserConfirmedExit) }
            .setNegativeButton(R.string.no, null)
            .create()
        dialogTimeCapsule.register("exitDialog", exitDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = OngoingCardEditorDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            viewModel.isAcceptButtonEnabled.observe(doneButton::setEnabled)
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun executeCommand(command: OngoingCardEditorController.Command) {
        when (command) {
            AskUserToConfirmExit -> {
                exitDialog.show()
            }
        }
    }

    private fun setupView() {
        cancelButton.run {
            setOnClickListener { controller?.dispatch(CancelButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        doneButton.run {
            setOnClickListener { controller?.dispatch(AcceptButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
            isEnabled = false
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is QAEditorFragment) {
            fragmentCoroutineScope.launch {
                val diScope = OngoingCardEditorDiScope.get()
                childFragment.inject(diScope.qaEditorController, diScope.qaEditorViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentCoroutineScope.cancel()
        if (needToCloseDiScope()) {
            OngoingCardEditorDiScope.close()
        }
    }

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            controller?.dispatch(BackButtonClicked)
            return true
        }
    }
}