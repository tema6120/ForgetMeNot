package com.odnovolov.forgetmenot.presentation.screen.intervals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsEvent.HelpButtonClicked
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
            intervalsRecyclerView.adapter = diScope.adapter
            observeViewModel(diScope.viewModel, diScope.adapter)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        helpButton.setOnClickListener {
            controller?.dispatch(HelpButtonClicked)
        }
    }

    private fun observeViewModel(viewModel: IntervalsViewModel, adapter: IntervalAdapter) {
        with(viewModel) {
            intervals.observe { intervals: List<IntervalListItem> ->
                adapter.submitList(intervals)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        intervalsRecyclerView.addOnScrollListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        intervalsRecyclerView.removeOnScrollListener(scrollListener)
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

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val canScrollUp = recyclerView.canScrollVertically(-1)
            if (appBar.isActivated != canScrollUp) {
                appBar.isActivated = canScrollUp
            }
        }
    }
}