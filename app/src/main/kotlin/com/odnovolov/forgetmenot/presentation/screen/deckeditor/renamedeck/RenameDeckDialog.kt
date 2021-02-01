package com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckEvent.OkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.renamedeck.RenameDeckEvent.TextChanged
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.coroutines.launch

class RenameDeckDialog : BaseDialogFragment() {
    init {
        RenameDeckDiScope.reopenIfClosed()
    }

    private var controller: RenameDeckController? = null
    private lateinit var viewModel: RenameDeckViewModel
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        rootView = View.inflate(requireContext(), R.layout.dialog_input, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = RenameDeckDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel(isRecreated = savedInstanceState != null)
        }
        return AlertDialog.Builder(requireContext())
            .setView(rootView)
            .create()
            .apply {
                window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_dialog)
                )
                setOnShowListener { rootView.dialogInput.showSoftInput() }
            }
    }

    private fun setupView() {
        with(rootView) {
            dialogTitle.setText(R.string.title_rename_deck_dialog)
            dialogInput.observeText { text: String ->
                controller?.dispatch(TextChanged(text))
            }
            okButton.setOnClickListener {
                controller?.dispatch(OkButtonClicked)
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun observeViewModel(isRecreated: Boolean) {
        with(viewModel) {
            if (!isRecreated) {
                rootView.dialogInput.setText(deckName)
                rootView.dialogInput.selectAll()
            }
            deckNameCheckResult.observe { nameCheckResult: NameCheckResult ->
                rootView.dialogInput.error = when (nameCheckResult) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
                rootView.okButton.isEnabled = nameCheckResult == Ok
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            RenameDeckDiScope.close()
        }
    }
}