package com.odnovolov.forgetmenot.presentation.screen.grading

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnCorrectAnswer
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnWrongAnswer
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.getGradeChangeDisplayText
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingEvent.SelectedGradeChange
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingScreenState.DialogPurpose
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingScreenState.DialogPurpose.*
import kotlinx.coroutines.launch

class ChangeGradingDialog : BaseDialogFragment() {
    init {
        GradingDiScope.reopenIfClosed()
    }

    private var controller: GradingController? = null
    private lateinit var titleView: TextView
    private lateinit var gradeChangeItemAdapter: ItemAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        return ChoiceDialogCreator.create(
            context = requireContext(),
            itemForm = AsRadioButton,
            takeTitle = ::titleView::set,
            onItemClick = { item: Item ->
                item as GradeChangeItem
                controller?.dispatch(SelectedGradeChange(item.gradeChange))
                dismiss()
            },
            takeAdapter = ::gradeChangeItemAdapter::set
        ).also {
            viewCoroutineScope!!.launch {
                val diScope = GradingDiScope.getAsync() ?: return@launch
                controller = diScope.controller
                observeViewModel(diScope.viewModel)
            }
        }
    }

    private fun observeViewModel(viewModel: GradingViewModel) {
        with(viewModel) {
            dialogPurpose.observe(::setTitle)
            getGradeChangeItems(
                ::gradeChangeOnCorrectAnswerToDisplayText,
                ::gradeChangeOnWrongAnswerToDisplayText
            ).observe(gradeChangeItemAdapter::submitList)
        }
    }

    private fun setTitle(dialogPurpose: DialogPurpose?) {
        if (dialogPurpose == null) {
            dismiss()
            return
        }
        titleView.setText(
            when (dialogPurpose) {
                ToChangeGradingOnFirstCorrectAnswer ->
                    R.string.dialog_title_change_grading_on_first_correct_answer
                ToChangeGradingOnFirstWrongAnswer ->
                    R.string.dialog_title_change_grading_on_first_wrong_answer
                ToChangeGradingOnRepeatedCorrectAnswer ->
                    R.string.dialog_title_change_grading_on_repeated_correct_answer
                ToChangeGradingOnRepeatedWrongAnswer ->
                    R.string.dialog_title_change_grading_on_repeated_wrong_answer
            }
        )
    }

    private fun gradeChangeOnCorrectAnswerToDisplayText(
        gradeChange: GradeChangeOnCorrectAnswer
    ): String = getGradeChangeDisplayText(gradeChange, requireContext())

    private fun gradeChangeOnWrongAnswerToDisplayText(
        gradeChange: GradeChangeOnWrongAnswer
    ): String = getGradeChangeDisplayText(gradeChange, requireContext())
}