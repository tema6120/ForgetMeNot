package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.repetition.Repetition
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.repetition.REPETITION_SCOPE_ID
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionScopeCloser
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionService
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command.SetViewPagerPosition
import kotlinx.android.synthetic.main.fragment_repetition.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel

class RepetitionFragment : BaseFragment() {
    private val koinScope = getKoin().getOrCreateScope<Repetition>(REPETITION_SCOPE_ID)
    private val viewModel: RepetitionViewModel by koinScope.viewModel(this)
    private val controller: RepetitionViewController by koinScope.inject()
    private val repetitionCardAdapter by lazy { RepetitionCardAdapter(controller) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        koinScope.get<RepetitionScopeCloser>().isFragmentAlive = true
        (activity as AppCompatActivity).supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            hide()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_repetition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        repetitionViewPager.adapter = repetitionCardAdapter
        repetitionViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        pauseButton.setOnClickListener { controller.onPauseButtonClicked() }
        resumeButton.setOnClickListener { controller.onResumeButtonClicked() }
    }

    private fun observeViewModel() {
        with(viewModel) {
            isPlaying.observe { isPlaying ->
                if (isPlaying) {
                    resumeButton.visibility = GONE
                    pauseButton.visibility = VISIBLE
                    startService()
                } else {
                    resumeButton.visibility = VISIBLE
                    pauseButton.visibility = GONE
                }
            }
            repetitionCards.observe { repetitionCards: List<RepetitionCard> ->
                val isFirst = repetitionCardAdapter.items.isEmpty()
                repetitionCardAdapter.items = repetitionCards
                if (isFirst) {
                    repetitionViewPager.setCurrentItem(repetitionCardPosition, false)
                }
            }
        }
    }

    private fun startService() {
        val intent = Intent(context, RepetitionService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun executeCommand(command: Command) {
        when (command) {
            is SetViewPagerPosition -> {
                repetitionViewPager.currentItem = command.position
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        repetitionViewPager.adapter = null
        repetitionViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
        if (isRemoving) {
            val intent = Intent(context, RepetitionService::class.java)
            requireContext().stopService(intent)
            controller.onFragmentRemoving()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller.onNewPageBecameSelected(position)
        }
    }
}