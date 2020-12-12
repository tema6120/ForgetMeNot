package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.ShowUnansweredCardButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.UserConfirmedExit
import kotlinx.android.synthetic.main.bottom_sheet_quit_exercise.*
import kotlinx.coroutines.launch

class QuitExerciseBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        ExerciseDiScope.reopenIfClosed()
    }

    private var controller: ExerciseController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_quit_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        setBottomSheetAlwaysExpanded()
        showUnansweredCardButton.setOnClickListener {
            controller?.dispatch(ShowUnansweredCardButtonClicked)
            dismiss()
        }
        quitExerciseButton.setOnClickListener {
            controller?.dispatch(UserConfirmedExit)
        }
    }

    private fun setBottomSheetAlwaysExpanded() {
        dialog?.setOnShowListener { dialog1 ->
            val bottomSheetDialog = dialog1 as BottomSheetDialog
            val bottomSheet: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    private fun observeViewModel(viewModel: ExerciseViewModel) {
        unansweredCardNumberTextView.text = viewModel.unansweredCardCount.toString()
        quitExerciseMessageTextView.text = resources.getQuantityString(
            R.plurals.message_quit_exercise,
            viewModel.unansweredCardCount,
            viewModel.unansweredCardCount
        )
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTransparentBackground
    }
}