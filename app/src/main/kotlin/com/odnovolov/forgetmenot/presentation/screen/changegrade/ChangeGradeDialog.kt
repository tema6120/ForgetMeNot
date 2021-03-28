package com.odnovolov.forgetmenot.presentation.screen.changegrade

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.changegrade.ChangeGradeEvent.GradeSelected
import kotlinx.android.synthetic.main.dialog_change_grade.view.*
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.coroutines.launch

class ChangeGradeDialog : BaseDialogFragment() {
    init {
        ChangeGradeDiScope.reopenIfClosed()
    }

    private lateinit var dialogContentView: View
    private var controller: ChangeGradeController? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        dialogContentView = View.inflate(requireContext(), R.layout.dialog_change_grade, null)
        val titleView = createDialogTitle()
        viewCoroutineScope!!.launch {
            val diScope = ChangeGradeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
        return createDialog(dialogContentView, titleView)
    }

    private fun createDialogTitle(): View {
        return View.inflate(context, R.layout.dialog_title, null).apply {
            dialogTitle.setText(R.string.dialog_title_change_grade)
            dialogTitle.setDrawableStart(R.drawable.ic_medal_24, R.color.title_icon_in_dialog)
            divider.isVisible = dialogContentView.gradeRecycler.canScrollVertically(-1)
            val scrollListener = object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val canScrollUp = recyclerView.canScrollVertically(-1)
                    if (divider.isVisible != canScrollUp) {
                        divider.isVisible = canScrollUp
                    }
                }
            }
            dialogContentView.gradeRecycler.addOnScrollListener(scrollListener)
            closeButton.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun observeViewModel(viewModel: ChangeGradeViewModel) {
        dialogContentView.gradeRecycler.adapter = GradeItemAdapter(
            viewModel.gradeItems,
            onGradeSelected = { grade: Int ->
                controller?.dispatch(GradeSelected(grade))
                dismiss()
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        dialogContentView.gradeRecycler.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            ChangeGradeDiScope.close()
        }
    }
}