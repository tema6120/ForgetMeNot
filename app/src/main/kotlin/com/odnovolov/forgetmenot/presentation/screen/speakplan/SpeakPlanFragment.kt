package com.odnovolov.forgetmenot.presentation.screen.speakplan

import SPEAK_PLAN_SCOPE_ID
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_speak_plan.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class SpeakPlanFragment : BaseFragment() {
    private val koinScope = getKoin().getOrCreateScope<SpeakPlanViewModel>(SPEAK_PLAN_SCOPE_ID)
    private val viewModel: SpeakPlanViewModel by koinScope.viewModel(this)
    private val controller: SpeakPlanController by koinScope.inject()
    private val adapter = SpeakEventAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_speak_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        speakPlanRecycler.adapter = adapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            speakEvents.observe {
                adapter.submitList(it)
            }
        }
    }

    private fun executeCommand(command: SpeakPlanController.Command) {
        when (command) {
        }
    }

    override fun onPause() {
        super.onPause()
        controller.onFragmentPause()
    }
}