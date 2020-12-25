package com.odnovolov.forgetmenot.presentation.screen.intervals

import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Interval
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.getBackgroundResForGrade
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsEvent.*
import kotlinx.android.synthetic.main.fragment_intervals.*
import kotlinx.coroutines.launch

class IntervalsFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        IntervalsDiScope.reopenIfClosed()
    }

    private var controller: IntervalsController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intervals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = IntervalsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            presetView.inject(diScope.presetController, diScope.presetViewModel)
            intervalsRecyclerView.adapter = diScope.adapter
            observeViewModel(diScope.viewModel, diScope.adapter)
        }
    }

    private fun setupView() {
        addIntervalButton.setOnClickListener { controller?.dispatch(AddIntervalButtonClicked) }
        removeIntervalButton.setOnClickListener {
            controller?.dispatch(RemoveIntervalButtonClicked)
        }
    }

    private fun observeViewModel(viewModel: IntervalsViewModel, adapter: IntervalAdapter) {
        with(viewModel) {
            intervals.observe { intervals: List<Interval> ->
                adapter.submitList(intervals)
                updateExcellentGradeTextView(intervals)
            }
            isRemoveIntervalButtonVisible.observe { isVisible: Boolean ->
                removeIntervalButton.isVisible = isVisible
            }
            canBeEdited.observe { canBeEdited: Boolean ->
                intervalsEditionGroup.isVisible = canBeEdited
            }
        }
    }

    private fun updateExcellentGradeTextView(intervals: List<Interval>) {
        val maxGrade: Int = intervals.map { it.grade }.maxOrNull() ?: -1
        val excellentGrade: Int = maxGrade + 1
        excellentGradeTextView.text = excellentGrade.toString()
        excellentGradeTextView.setBackgroundResource(
            getBackgroundResForGrade(excellentGrade)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        intervalsRecyclerView.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            IntervalsDiScope.close()
        }
    }
}