package com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForAnswer
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceScreenState.TextSizeDialogDestination.ForQuestion
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog.CardTextSizeDialogEvent.TextSizeDialogOkButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog.CardTextSizeDialogEvent.TextSizeDialogTextChanged
import kotlinx.android.synthetic.main.dialog_card_text_size.view.*
import kotlinx.coroutines.launch

class CardTextSizeDialog : BaseDialogFragment() {
    init {
        CardAppearanceDiScope.reopenIfClosed()
    }

    private var controller: CardTextSizeController? = null
    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        contentView = View.inflate(requireContext(), R.layout.dialog_card_text_size, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardAppearanceDiScope.getAsync() ?: return@launch
            controller = diScope.dialogController
            observeViewModel(diScope.dialogViewModel, isRecreated = savedInstanceState != null)
        }
        return createDialog(contentView).apply {
            setOnShowListener { contentView.dialogInput.showSoftInput() }
        }
    }

    private fun setupView() {
        with(contentView) {
            dialogInput.observeText { text: String ->
                controller?.dispatch(TextSizeDialogTextChanged(text))
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            okButton.setOnClickListener {
                controller?.dispatch(TextSizeDialogOkButtonClicked)
                dismiss()
            }
        }
    }

    private fun observeViewModel(viewModel: CardTextSizeViewModel, isRecreated: Boolean) {
        with(contentView) {
            dialogTitle.text = when (viewModel.destination) {
                ForQuestion -> getString(R.string.dialog_title_question_text_size)
                ForAnswer -> getString(R.string.dialog_title_answer_text_size)
                null -> {
                    dismiss()
                    ""
                }
            }
            if (!isRecreated) {
                dialogInput.setText(viewModel.dialogText)
                dialogInput.selectAll()
            }
            viewModel.isOkButtonEnabled.observe(okButton::setEnabled)
        }
    }
}