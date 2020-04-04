package com.odnovolov.forgetmenot.presentation.screen.intervals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.preset.PresetFragment
import com.odnovolov.forgetmenot.presentation.screen.intervals.IntervalsCommand.ShowModifyIntervalDialog
import com.odnovolov.forgetmenot.presentation.screen.intervals.modifyinterval.ModifyIntervalDialog
import kotlinx.android.synthetic.main.fragment_intervals.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class IntervalsFragment : BaseFragment() {
    private val koinScope = getKoin().getOrCreateScope<IntervalsViewModel>(INTERVALS_SCOPE_ID)
    private val viewModel: IntervalsViewModel by koinScope.viewModel(this)
    private val controller: IntervalsController by koinScope.inject()
    private val adapter: IntervalAdapter by lazy { IntervalAdapter(controller) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intervals, container, false)
    }

    override fun onAttachFragment(childFragment: Fragment) {
        if (childFragment is PresetFragment) {
            childFragment.controller = koinScope.get()
            childFragment.viewModel = koinScope.get()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        intervalsRecyclerView.adapter = adapter
        addIntervalButton.setOnClickListener { controller.onAddIntervalButtonClicked() }
        removeIntervalButton.setOnClickListener { controller.onRemoveIntervalButtonClicked() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            intervals.observe(adapter::submitList)
            isRemoveIntervalButtonEnabled.observe { isEnabled: Boolean ->
                removeIntervalButton.isEnabled = isEnabled
            }
            canBeEdited.observe { canBeEdited: Boolean ->
                intervalsEditionGroup.visibility = if (canBeEdited) VISIBLE else INVISIBLE
            }
        }
    }

    private fun executeCommand(command: IntervalsCommand) {
        when (command) {
            ShowModifyIntervalDialog -> {
                ModifyIntervalDialog().show(childFragmentManager, MODIFY_INTERVAL_FRAGMENT_TAG)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        intervalsRecyclerView.adapter = null
    }

    companion object {
        const val MODIFY_INTERVAL_FRAGMENT_TAG = "ModifyIntervalFragment"
    }
}