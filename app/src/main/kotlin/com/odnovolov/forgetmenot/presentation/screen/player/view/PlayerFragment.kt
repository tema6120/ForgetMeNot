package com.odnovolov.forgetmenot.presentation.screen.player.view

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter
import com.odnovolov.forgetmenot.presentation.screen.exercise.KnowingWhenPagerStopped
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerService
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.ShowIntervalsPopup
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.popup_intervals.view.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.coroutines.launch

class PlayerFragment : BaseFragment() {
    init {
        PlayerDiScope.isFragmentAlive = true
    }

    private var controller: PlayerViewController? = null
    private lateinit var viewModel: PlayerViewModel
    private val intervalsPopup: PopupWindow by lazy(::createIntervalsPopup)
    private val intervalsAdapter: IntervalsAdapter by lazy(::createIntervalsAdapter)
    private val speakErrorPopup: PopupWindow by lazy(::createSpeakErrorPopup)
    private val speakErrorToast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.error_message_failed_to_speak, Toast.LENGTH_SHORT)
    }
    private var knowingWhenPagerStopped: KnowingWhenPagerStopped? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            controller = diScope.viewController
            viewModel = diScope.viewModel
            playerViewPager.adapter = PlayingCardAdapter(
                viewCoroutineScope!!,
                knowingWhenPagerStopped!!,
                diScope.playingCardController
            )
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        playerViewPager.offscreenPageLimit = 1
        knowingWhenPagerStopped = KnowingWhenPagerStopped()
        playerViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        gradeButton.run {
            setOnClickListener { controller?.dispatch(GradeButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        editCardButton.run {
            setOnClickListener { controller?.dispatch(EditCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        searchButton.run {
            setOnClickListener { controller?.dispatch(SearchButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        helpButton.run {
            setOnClickListener { controller?.dispatch(HelpButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            playingCards.observe { playingCards: List<PlayingCard> ->
                val adapter = playerViewPager.adapter as PlayingCardAdapter
                adapter.items = playingCards
                progressBar.visibility = GONE
            }
            gradeOfCurrentCard.observe { grade: Int ->
                updateGradeButtonColor(grade)
                gradeButton.text = grade.toString()
            }
            isCurrentCardLearned.observe { isLearned: Boolean ->
                with(markAsLearnedButton) {
                    setImageResource(
                        if (isLearned)
                            R.drawable.ic_mark_as_unlearned else
                            R.drawable.ic_mark_as_learned
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
                            R.string.description_mark_as_unlearned_button else
                            R.string.description_mark_as_learned_button
                    )
                    TooltipCompat.setTooltipText(this, contentDescription)
                }
            }
            speakingStatus.observe { speakingStatus: SpeakingStatus ->
                with(speakButton) {
                    setImageResource(
                        when (speakingStatus) {
                            Speaking -> R.drawable.ic_volume_off_white_24dp
                            NotSpeaking -> R.drawable.ic_volume_up_white_24dp
                            CannotSpeak -> R.drawable.ic_volume_error_24
                        }
                    )
                    setOnClickListener {
                        when (speakingStatus) {
                            Speaking -> controller?.dispatch(StopSpeakButtonClicked)
                            NotSpeaking -> controller?.dispatch(SpeakButtonClicked)
                            CannotSpeak -> showSpeakErrorPopup()
                        }
                    }
                    contentDescription = getString(
                        when (speakingStatus) {
                            Speaking -> R.string.description_stop_speak_button
                            NotSpeaking -> R.string.description_speak_button
                            CannotSpeak -> R.string.description_cannot_speak_button
                        }
                    )
                    TooltipCompat.setTooltipText(this, contentDescription)
                }
            }
            isSpeakerPreparingToPronounce.observe { isPreparing: Boolean ->
                speakProgressBar.isInvisible = !isPreparing
            }
            speakerEvents.observe { event: SpeakerImpl.Event ->
                when (event) {
                    SpeakError -> speakErrorToast.show()
                }
            }
            isPlaying.observe { isPlaying: Boolean ->
                if (isPlaying) startService()
                with(playButton) {
                    keepScreenOn = isPlaying
                    setImageResource(
                        if (isPlaying)
                            R.drawable.ic_pause_28 else
                            R.drawable.ic_play_28
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

    private fun updateGradeButtonColor(grade: Int) {
        val gradeColor: Int = ContextCompat.getColor(requireContext(), getGradeColorRes(grade))
        gradeButton.background.setTint(gradeColor)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val brightGradeColor: Int =
                ContextCompat.getColor(requireContext(), getBrightGradeColorRes(grade))
            gradeButton.outlineAmbientShadowColor = brightGradeColor
            gradeButton.outlineSpotShadowColor = brightGradeColor
        }
    }

    private fun startService() {
        val intent = Intent(context, PlayerService::class.java)
        ContextCompat.startForegroundService(requireContext(), intent)
    }

    private fun executeCommand(command: Command) {
        when (command) {
            is SetCurrentPosition -> {
                playerViewPager.currentItem = command.position
            }
            is ShowIntervalsPopup -> {
                showIntervalsPopup(command.intervalItems)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            val currentPosition = diScope.viewModel.currentPosition
            if (playerViewPager.currentItem != currentPosition) {
                playerViewPager.setCurrentItem(currentPosition, false)
            }
        }
    }

    private fun createIntervalsPopup(): PopupWindow {
        val content: View = View.inflate(context, R.layout.popup_intervals, null).apply {
            intervalsRecycler.adapter = intervalsAdapter
        }
        return PopupWindow(context).apply {
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            contentView = content
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromBottomLeftAnimation
        }
    }

    private fun createIntervalsAdapter(): IntervalsAdapter {
        val onItemClick: (Int) -> Unit = { grade: Int ->
            controller?.dispatch(GradeWasChanged(grade))
            intervalsPopup.dismiss()
        }
        return IntervalsAdapter(onItemClick)
    }

    private fun showIntervalsPopup(intervalItems: List<IntervalItem>?) {
        with(intervalsPopup) {
            contentView.intervalsPopupTitleTextView.isActivated = intervalItems != null
            contentView.intervalsRecycler.isVisible = intervalItems != null
            contentView.intervalsAreOffTextView.isVisible = intervalItems == null
            intervalItems?.let { intervalsAdapter.intervalItems = it }
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            val gradeButtonLocation = IntArray(2).also(gradeButton::getLocationOnScreen)
            val x = gradeButtonLocation[0] + 8.dp
            val y = gradeButtonLocation[1] + gradeButton.height - 8.dp - contentView.measuredHeight
            showAtLocation(gradeButton.rootView, Gravity.NO_GRAVITY, x, y)
        }
    }

    private fun createSpeakErrorPopup(): PopupWindow {
        val content = View.inflate(requireContext(), R.layout.popup_speak_error, null).apply {
            goToTtsSettingsButton.setOnClickListener {
                navigateToTtsSettings()
                speakErrorPopup.dismiss()
            }
        }
        return PopupWindow(context).apply {
            width = WRAP_CONTENT
            height = WRAP_CONTENT
            contentView = content
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromBottomAnimation
        }
    }

    private fun getSpeakErrorDescription(): String? {
        val reasonForInabilityToSpeak: ReasonForInabilityToSpeak? =
            viewModel.reasonForInabilityToSpeak.firstBlocking()
        return when (reasonForInabilityToSpeak) {
            null -> null
            is FailedToInitializeSpeaker -> {
                if (reasonForInabilityToSpeak.ttsEngine == null) {
                    getString(R.string.speak_error_description_failed_to_initialized)
                } else {
                    getString(
                        R.string.speak_error_description_failed_to_initialized_with_specifying_tts_engine,
                        reasonForInabilityToSpeak.ttsEngine
                    )
                }
            }
            is LanguageIsNotSupported -> {
                if (reasonForInabilityToSpeak.ttsEngine == null) {
                    getString(
                        R.string.speak_error_description_language_is_not_supported,
                        reasonForInabilityToSpeak.language.displayLanguage
                    )
                } else {
                    getString(
                        R.string.speak_error_description_language_is_not_supported_with_specifying_tts_engine,
                        reasonForInabilityToSpeak.ttsEngine,
                        reasonForInabilityToSpeak.language.displayLanguage
                    )
                }
            }
            is MissingDataForLanguage -> {
                getString(
                    R.string.speak_error_description_missing_data_for_language,
                    reasonForInabilityToSpeak.language.displayLanguage
                )
            }
        }
    }

    private fun showSpeakErrorPopup() {
        with(speakErrorPopup) {
            contentView.speakErrorDescriptionTextView.text = getSpeakErrorDescription()
            contentView.measure(
                MeasureSpec.makeMeasureSpec(speakButton.rootView.width, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(speakButton.rootView.height, MeasureSpec.AT_MOST)
            )
            val speakButtonLocation = IntArray(2).also(speakButton::getLocationOnScreen)
            val x: Int = 8.dp
            val y: Int =
                speakButtonLocation[1] + speakButton.height - 8.dp - contentView.measuredHeight
            showAtLocation(speakButton.rootView, Gravity.NO_GRAVITY, x, y)
        }
    }

    private fun navigateToTtsSettings() {
        startActivity(
            Intent().apply {
                action = "com.android.settings.TTS_SETTINGS"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerViewPager.adapter = null
        knowingWhenPagerStopped = null
        playerViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRemoving) {
            val intent = Intent(context, PlayerService::class.java)
            requireContext().stopService(intent)
        }
        if (needToCloseDiScope()) {
            PlayerDiScope.isFragmentAlive = false
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(NewPageBecameSelected(position))
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            knowingWhenPagerStopped?.updateState(state)
        }
    }
}