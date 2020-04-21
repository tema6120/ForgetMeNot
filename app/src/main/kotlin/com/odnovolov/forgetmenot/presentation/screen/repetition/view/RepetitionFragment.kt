package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
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
            val adapter = diScope.getRepetitionCardAdapter(viewCoroutineScope!!)
            repetitionViewPager.adapter = adapter
            observeViewModel(diScope.viewModel, adapter)
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        repetitionViewPager.registerOnPageChangeCallback(onPageChangeCallback)
    }

    private fun observeViewModel(viewModel: RepetitionViewModel, adapter: RepetitionCardAdapter) {
        with(viewModel) {
            repetitionCards.observe { repetitionCards: List<RepetitionCard> ->
                adapter.items = repetitionCards
            }
            isCurrentRepetitionCardLearned.observe { isLearned: Boolean ->
                with(notAskButton) {
                    setImageResource(
                        if (isLearned)
                            R.drawable.ic_undo_white_24dp else
                            R.drawable.ic_block_white_24dp
                    )
                    setOnClickListener {
                        controller?.dispatch(
                            if (isLearned)
                                AskAgainButtonClicked else
                                NotAskButtonClicked
                        )
                    }
                    contentDescription = getString(
                        if (isLearned)
                            R.string.description_ask_again_button else
                            R.string.description_not_ask_button
                    )
                    TooltipCompat.setTooltipText(this, contentDescription)
                }
            }
            isSpeaking.observe { isSpeaking: Boolean ->
                with(speakButton) {
                    setImageResource(
                        if (isSpeaking)
                            R.drawable.ic_volume_off_white_24dp else
                            R.drawable.ic_volume_up_white_24dp
                    )
                    setOnClickListener {
                        controller?.dispatch(
                            if (isSpeaking)
                                StopSpeakButtonClicked else
                                SpeakButtonClicked
                        )
                    }
                    contentDescription = getString(
                        if (isSpeaking)
                            R.string.description_stop_speak_button else
                            R.string.description_speak_button
                    )
                    TooltipCompat.setTooltipText(this, contentDescription)
                }
            }
            isPlaying.observe { isPlaying: Boolean ->
                if (isPlaying) startService()
                with(pauseResumeButton) {
                    setImageResource(
                        if (isPlaying)
                            R.drawable.ic_pause_white_24dp else
                            R.drawable.ic_play_arrow_white_24dp
                    )
                    setOnClickListener {
                        controller?.dispatch(
                            if (isPlaying)
                                PauseButtonClicked else
                                ResumeButtonClicked
                        )
                    }
                    contentDescription = getString(
                        if (isPlaying)
                            R.string.description_pause_button else
                            R.string.description_resume_button
                    )
                    TooltipCompat.setTooltipText(this, contentDescription)
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

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val repetitionCardPosition = RepetitionDiScope.get().viewModel.repetitionCardPosition
            if (repetitionViewPager.currentItem != repetitionCardPosition) {
                repetitionViewPager.setCurrentItem(repetitionCardPosition, false)
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