package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.DO_NOT_USE_TIMER
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerEvent.QuitButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerEvent.SaveButtonClicked
import kotlinx.android.synthetic.main.bottom_sheet_quit_motivational_timer.*
import kotlinx.coroutines.launch

class QuitMotivationalTimerBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        ExampleExerciseDiScope.reopenIfClosed()
        MotivationalTimerDiScope.reopenIfClosed()
    }

    private var controller: MotivationalTimerController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_quit_motivational_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = MotivationalTimerDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        setBottomSheetAlwaysExpanded()
        saveButton.setOnClickListener {
            controller?.dispatch(SaveButtonClicked)
            dismiss()
        }
        quitWithoutSavingButton.setOnClickListener {
            controller?.dispatch(QuitButtonClicked)
        }
    }

    private fun setBottomSheetAlwaysExpanded() {
        dialog?.setOnShowListener { dialog1 ->
            val bottomSheetDialog = dialog1 as BottomSheetDialog
            val bottomSheet: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.skipCollapsed = true
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun observeViewModel(viewModel: MotivationalTimerViewModel) {
        with (viewModel) {
            val oldValue: String =
                if (currentTimeForAnswer == DO_NOT_USE_TIMER)
                    getString(R.string.off) else
                    getString(R.string.time_for_answer, currentTimeForAnswer)
            val newValue: String = when (editedTimeForAnswer) {
                null -> ""
                0 -> getString(R.string.off)
                else -> getString(R.string.time_for_answer, editedTimeForAnswer)
            }
            quitMessageTextView.text =
                getString(R.string.message_quit_motivational_timer, oldValue, newValue)
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTransparentBackground
    }
}