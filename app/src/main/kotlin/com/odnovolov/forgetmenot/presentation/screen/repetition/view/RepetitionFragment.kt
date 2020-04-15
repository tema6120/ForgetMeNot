package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import android.annotation.SuppressLint
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
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionService
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command.SetViewPagerPosition
import kotlinx.android.synthetic.main.fragment_repetition.*
import kotlinx.coroutines.launch

class RepetitionFragment : BaseFragment() {
    init {
        RepetitionDiScope.isFragmentAlive = true
    }

    private var controller: RepetitionViewController? = null

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        viewCoroutineScope!!.launch {
            val diScope = RepetitionDiScope.get()
            controller = diScope.viewController
            repetitionViewPager.adapter = diScope.adapter
            observeViewModel(
                diScope.viewModel,
                diScope.adapter
            )
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        repetitionViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        pauseButton.setOnClickListener { controller?.dispatch(PauseButtonClicked) }
        resumeButton.setOnClickListener { controller?.dispatch(ResumeButtonClicked) }
    }

    private fun observeViewModel(viewModel: RepetitionViewModel, adapter: RepetitionCardAdapter) {
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
                val isFirst = adapter.items.isEmpty()
                adapter.items = repetitionCards
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
        }
        if (needToCloseDiScope()) {
            RepetitionDiScope.isFragmentAlive = false
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(NewPageBecameSelected(position))
        }
    }
}