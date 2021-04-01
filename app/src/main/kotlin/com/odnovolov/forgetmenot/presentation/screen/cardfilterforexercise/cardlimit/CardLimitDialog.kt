package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise.cardlimit.CardLimitEvent.*
import kotlinx.android.synthetic.main.dialog_card_limit.view.*
import kotlinx.coroutines.launch

class CardLimitDialog : BaseDialogFragment() {
    init {
        CardLimitDiScope.reopenIfClosed()
    }

    private var controller: CardLimitController? = null
    private lateinit var contentView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        contentView = View.inflate(requireContext(), R.layout.dialog_card_limit, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardLimitDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
        return createDialog(contentView)
    }

    private fun setupView() {
        with(contentView) {
            limitButton.setOnClickListener {
                controller?.dispatch(LimitRadioButtonClicked)
            }
            noLimitButton.setOnClickListener {
                controller?.dispatch(NoLimitRadioButtonClicked)
            }
            limitEditText.observeText { text: String ->
                controller?.dispatch(DialogTextChanged(text))
            }
            cancelButton.setOnClickListener {
                dismiss()
            }
            okButton.setOnClickListener {
                controller?.dispatch(OkButtonClicked)
                dismiss()
            }
        }
    }

    private fun observeViewModel(viewModel: CardLimitViewModel) {
        with(contentView) {
            with(viewModel) {
                limitEditText.setText(dialogText)
                isNoLimit.observe { isNoLimit: Boolean ->
                    noLimitRadioButton.run {
                        isChecked = isNoLimit
                        uncover()
                    }
                    noLimitButton.isClickable = !isNoLimit
                    limitRadioButton.run {
                        isChecked = !isNoLimit
                        uncover()
                    }
                    limitButton.isClickable = isNoLimit
                    limitEditText.run {
                        isEnabled = !isNoLimit
                        if (isNoLimit) {
                            setSelection(0)
                        } else {
                            selectAll()
                            showSoftInput()
                        }
                    }
                }
                maxNumberOfCards.observe { maxNumberOfCards: Int ->
                    limitRadioButton.text = resources.getQuantityText(
                        R.plurals.cards,
                        maxNumberOfCards
                    )
                }
                isOkButtonEnabled.observe(okButton::setEnabled)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            CardLimitDiScope.close()
        }
    }
}