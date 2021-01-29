package com.odnovolov.forgetmenot.presentation.screen.player.view.laps

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.player.view.laps.LapsInPlayerEvent.*
import kotlinx.android.synthetic.main.dialog_laps_in_player.view.*
import kotlinx.coroutines.launch

class LapsInPlayerDialog : BaseDialogFragment() {
    init {
        LapsInPlayerDiScope.reopenIfClosed()
    }

    private var controller: LapsInPlayerController? = null
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        rootView = View.inflate(context, R.layout.dialog_laps_in_player, null)
        setupView()
        return AlertDialog.Builder(requireContext())
            .setView(rootView)
            .create()
            .apply {
                window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(requireContext(), R.drawable.background_dialog)
                )
            }
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

    override fun onStart() {
        super.onStart()
        viewCoroutineScope!!.launch {
            val diScope = LapsInPlayerDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun observeViewModel(viewModel: LapsInPlayerViewModel) {
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