package com.odnovolov.forgetmenot.presentation.screen.repetitionsettings.laps

import REPETITION_LAPS_SCOPE_ID
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import kotlinx.android.synthetic.main.dialog_repetition_laps.view.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class RepetitionLapsDialog : BaseDialogFragment() {
    private val koinScope = getKoin()
        .getOrCreateScope<RepetitionLapsViewModel>(REPETITION_LAPS_SCOPE_ID)
    private val viewModel: RepetitionLapsViewModel by koinScope.viewModel(this)
    private val controller: RepetitionLapsController by koinScope.inject()
    private lateinit var rootView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        onCreateDialog()
        rootView = View.inflate(context, R.layout.dialog_repetition_laps, null)
        observeViewModel()
        setupView()
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_number_of_laps)
            .setView(rootView)
            .setPositiveButton(android.R.string.ok) { _, _ -> controller.onOkButtonClicked() }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun observeViewModel() {
        with(rootView) {
            with(viewModel) {
                lapsEditText.setText(numberOfLapsInput)
                isInfinitely.observe { isInfinitely: Boolean ->
                    if (isInfinitely) {
                        infinitelyRadioButton.isChecked = true
                        lapsRadioButton.isChecked = false
                        lapsRadioButton.jumpDrawablesToCurrentState()
                        lapsEditText.isEnabled = false
                        lapsEditText.isClickable = false
                    } else {
                        infinitelyRadioButton.isChecked = false
                        infinitelyRadioButton.jumpDrawablesToCurrentState()
                        lapsRadioButton.isChecked = true
                        lapsEditText.isEnabled = true
                        lapsEditText.selectAll()
                        lapsEditText.showSoftInput(showImplicit = true)
                    }
                    if (infinitelyRadioButton.visibility == View.INVISIBLE) {
                        infinitelyRadioButton.jumpDrawablesToCurrentState()
                        infinitelyRadioButton.visibility = View.VISIBLE
                    }
                    if (lapsRadioButton.visibility == View.INVISIBLE) {
                        lapsRadioButton.jumpDrawablesToCurrentState()
                        lapsRadioButton.visibility = View.VISIBLE
                    }
                }
                numberOfLaps.observe { numberOfLaps: Int ->
                    lapsRadioButton.text = resources
                        .getQuantityText(R.plurals.number_of_laps, numberOfLaps)
                }
                isOkButtonEnabled.observe { isEnabled ->
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                        isEnabled
                }
            }
        }
    }

    private fun setupView() {
        with(rootView) {
            lapsButton.setOnClickListener { controller.onLapsRadioButtonClicked() }
            infinitelyButton.setOnClickListener { controller.onInfinitelyRadioButtonClicked() }
            lapsEditText.observeText(controller::onLapsInputChanged)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isRemoving) {
            controller.performSaving()
        }
    }
}