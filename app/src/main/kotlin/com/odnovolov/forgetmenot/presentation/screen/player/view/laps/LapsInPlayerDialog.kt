package com.odnovolov.forgetmenot.presentation.screen.player.view.laps

import android.app.Dialog
import android.os.Bundle
import android.view.View
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.player.view.laps.LapsInPlayerEvent.*
import kotlinx.android.synthetic.main.dialog_laps_in_player.view.*
import kotlinx.coroutines.launch

class LapsInPlayerDialog : BaseDialogFragment() {
    init {
        LapsInPlayerDiScope.reopenIfClosed()
    }

    private var controller: LapsInPlayerController? = null
    private lateinit var viewModel: LapsInPlayerViewModel
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        rootView = View.inflate(context, R.layout.dialog_laps_in_player, null)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = LapsInPlayerDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
        return createDialog(rootView)
    }

    private fun setupView() {
        with(rootView) {
            specificNumberOfLapsButton.setOnClickListener {
                controller?.dispatch(LapsRadioButtonClicked)
            }
            infinitelyButton.setOnClickListener {
                controller?.dispatch(InfinitelyRadioButtonClicked)
            }
            lapsEditText.observeText { text: String ->
                controller?.dispatch(LapsInputChanged(text))
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

    private fun observeViewModel() {
        with(viewModel) {
            with(rootView) {
                lapsEditText.setText(numberOfLapsInput)
                isInfinitely.observe { isInfinitely: Boolean ->
                    infinitelyRadioButton.run {
                        isChecked = isInfinitely
                        uncover()
                    }
                    infinitelyButton.isClickable = !isInfinitely
                    specificNumberOfLapsRadioButton.run {
                        isChecked = !isInfinitely
                        uncover()
                    }
                    specificNumberOfLapsButton.isClickable = isInfinitely
                    lapsEditText.run {
                        isEnabled = !isInfinitely
                        if (isInfinitely) {
                            setSelection(0)
                        } else {
                            selectAll()
                            showSoftInput()
                        }
                    }
                }
                numberOfLaps.observe { numberOfLaps: Int ->
                    specificNumberOfLapsRadioButton.text = resources.getQuantityText(
                        R.plurals.radiobutton_specific_number_of_laps,
                        numberOfLaps
                    )
                }
                isOkButtonEnabled.observe(okButton::setEnabled)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            LapsInPlayerDiScope.close()
        }
    }
}