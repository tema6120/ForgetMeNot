package com.odnovolov.forgetmenot.presentation.screen.home.choosepreset

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseDialogFragment
import com.odnovolov.forgetmenot.presentation.common.createDialog
import com.odnovolov.forgetmenot.presentation.common.setDrawableStart
import com.odnovolov.forgetmenot.presentation.screen.home.HomeController
import com.odnovolov.forgetmenot.presentation.screen.home.HomeDiScope
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.PresetButtonClicked
import kotlinx.android.synthetic.main.dialog_choice.view.*
import kotlinx.android.synthetic.main.dialog_title.view.*
import kotlinx.coroutines.launch

class ChoosePresetDialog : BaseDialogFragment() {
    init {
        HomeDiScope.reopenIfClosed()
    }

    private var controller: HomeController? = null
    private lateinit var contentView: View
    private lateinit var titleView: View
    private val adapter = PresetAdapter(
        onPresetButtonClicked = { exercisePreferenceId: Long ->
            controller?.dispatch(PresetButtonClicked(exercisePreferenceId))
            dismiss()
        }
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog()
        initContentView()
        initTitleView()
        viewCoroutineScope!!.launch {
            val diScope = HomeDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            val viewModel = diScope.choosePresetViewModel
            observeViewModel(viewModel)
        }
        return createDialog(contentView, titleView)
    }

    private fun initContentView() {
        contentView = View.inflate(requireContext(), R.layout.dialog_choice, null)
        contentView.choiceRecycler.adapter = adapter
    }

    private fun initTitleView() {
        titleView = View.inflate(context, R.layout.dialog_title, null).apply {
            dialogTitle.setText(R.string.dialog_title_choose_preset)
            dialogTitle.setDrawableStart(R.drawable.ic_round_tune_24, R.color.title_icon_in_dialog)
            closeButton.setOnClickListener {
                dismiss()
            }
            divider.isVisible = contentView.choiceRecycler.canScrollVertically(-1)
        }
    }

    private fun observeViewModel(viewModel: ChoosePresetViewModel) {
        viewModel.presets.observe { presets: List<SelectablePreset> ->
            adapter.items = presets
        }
    }

    override fun onResume() {
        super.onResume()
        contentView.choiceRecycler.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentView.choiceRecycler.removeOnScrollListener(scrollListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentView.choiceRecycler.adapter = null
    }

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val canScrollUp = recyclerView.canScrollVertically(-1)
            if (titleView.divider.isVisible != canScrollUp) {
                titleView.divider.isVisible = canScrollUp
            }
        }
    }
}