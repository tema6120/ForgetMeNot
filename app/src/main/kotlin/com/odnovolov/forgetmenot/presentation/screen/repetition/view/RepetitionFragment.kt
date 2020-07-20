package com.odnovolov.forgetmenot.presentation.screen.repetition.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.color
import com.odnovolov.forgetmenot.domain.interactor.repetition.RepetitionCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter
import com.odnovolov.forgetmenot.presentation.screen.repetition.RepetitionDiScope
import com.odnovolov.forgetmenot.presentation.screen.repetition.service.RepetitionService
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.repetition.view.RepetitionViewController.Command.*
import kotlinx.android.synthetic.main.fragment_repetition.*
import kotlinx.coroutines.launch

class RepetitionFragment : BaseFragment() {
    init {
        RepetitionDiScope.isFragmentAlive = true
    }

    private var controller: RepetitionViewController? = null
    private val levelOfKnowledgePopup: PopupWindow by lazy { createLevelOfKnowledgePopup() }
    private val intervalsAdapter: IntervalsAdapter by lazy { createIntervalsAdapter() }

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
        editCardButton.run {
            setOnClickListener { controller?.dispatch(EditCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        levelOfKnowledgeButton.run {
            setOnClickListener { controller?.dispatch(LevelOfKnowledgeButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
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
                            R.drawable.ic_baseline_replay_white_24 else
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
                    keepScreenOn = isPlaying
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
            levelOfKnowledgeForCurrentCard.observe { levelOfKnowledge: Int ->
                val backgroundRes = getBackgroundResForLevelOfKnowledge(levelOfKnowledge)
                levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
                levelOfKnowledgeTextView.text = levelOfKnowledge.toString()
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
            is ShowLevelOfKnowledgePopup -> {
                showLevelOfKnowledgePopup(command.intervalItems)
            }
            ShowIntervalsAreOffMessage -> {
                showToast(R.string.toast_text_intervals_are_off)
            }
        }
    }

    private fun showLevelOfKnowledgePopup(intervalItems: List<IntervalItem>) {
        intervalsAdapter.intervalItems = intervalItems
        val content = levelOfKnowledgePopup.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        levelOfKnowledgeButton.getLocationOnScreen(location)
        val x = location[0] + levelOfKnowledgeButton.width - 8.dp - content.measuredWidth
        val y = location[1] + levelOfKnowledgeButton.height - 8.dp - content.measuredHeight
        levelOfKnowledgePopup.showAtLocation(
            levelOfKnowledgeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val repetitionCardPosition = RepetitionDiScope.get().viewModel.repetitionCardPosition
            if (repetitionViewPager.currentItem != repetitionCardPosition) {
                repetitionViewPager.setCurrentItem(repetitionCardPosition, false)
            }
        }
        hideActionBar()
    }

    private fun createIntervalsAdapter(): IntervalsAdapter {
        val onItemClick: (Int) -> Unit = { levelOfKnowledge: Int ->
            controller?.dispatch(LevelOfKnowledgeSelected(levelOfKnowledge))
            levelOfKnowledgePopup.dismiss()
        }
        return IntervalsAdapter(onItemClick)
    }

    private fun createLevelOfKnowledgePopup(): PopupWindow {
        val recycler: RecyclerView =
            View.inflate(context, R.layout.popup_set_level_of_knowledge, null) as RecyclerView
        recycler.adapter = intervalsAdapter
        return PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = recycler
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        color.exercise_control_panel_popup_background
                    )
                )
            )
            elevation = 20f
            isOutsideTouchable = true
            isFocusable = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        repetitionViewPager.adapter = null
        repetitionViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
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