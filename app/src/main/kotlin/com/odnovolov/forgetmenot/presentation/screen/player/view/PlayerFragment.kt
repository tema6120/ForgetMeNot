package com.odnovolov.forgetmenot.presentation.screen.player.view

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
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
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerService
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.popup_infinite_playback.view.*
import kotlinx.android.synthetic.main.popup_intervals.view.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.coroutines.launch

class PlayerFragment : BaseFragment() {
    init {
        PlayerDiScope.isFragmentAlive = true
    }

    private var controller: PlayerViewController? = null
    private lateinit var viewModel: PlayerViewModel
    private var intervalsPopup: PopupWindow? = null
    private var intervalsAdapter: IntervalsAdapter? = null
    private var speakErrorPopup: PopupWindow? = null
    private val speakErrorToast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.error_message_failed_to_speak, Toast.LENGTH_SHORT)
    }
    private var infinitePlaybackPopup: PopupWindow? = null

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
                diScope.playingCardController
            )
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        playerViewPager.offscreenPageLimit = 1
        playerViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        gradeButton.run {
            setOnClickListener {
                controller?.dispatch(GradeButtonClicked)
                showIntervalsPopup()
            }
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
        infinitePlaybackButton.run {
            setOnClickListener { showInfinitePlaybackPopup() }
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
            isInfinitePlaybackEnabled.observe { isInfinitePlaybackEnabled: Boolean ->
                infinitePlaybackButton.isActivated = isInfinitePlaybackEnabled
            }
            isCompleted.observe { isCompleted: Boolean ->
                if (isCompleted) {
                    val isBottomSheetOpened = childFragmentManager
                        .findFragmentByTag(TAG_PLAYING_FINISHED_BOTTOM_SHEET) != null
                    if (!isBottomSheetOpened) {
                        PlayingFinishedBottomSheet().show(
                            childFragmentManager,
                            TAG_PLAYING_FINISHED_BOTTOM_SHEET
                        )
                    }
                }
            }
        }
    }

    private fun updateGradeButtonColor(grade: Int) {
        val gradeColor: Int = ContextCompat.getColor(requireContext(), getGradeColorRes(grade))
        gradeButton.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                gradeColor,
                BlendModeCompat.SRC_ATOP
            )
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

    private fun requireIntervalsPopup(): PopupWindow {
        if (intervalsPopup == null) {
            val content: View = View.inflate(context, R.layout.popup_intervals, null)
            val onItemClick: (Int) -> Unit = { grade: Int ->
                intervalsPopup?.dismiss()
                controller?.dispatch(GradeWasChanged(grade))
            }
            intervalsAdapter = IntervalsAdapter(onItemClick)
            content.intervalsRecycler.adapter = intervalsAdapter
            intervalsPopup = DarkPopupWindow(content)
            subscribeIntervalsPopupToViewModel()
        }
        return intervalsPopup!!
    }

    private fun subscribeIntervalsPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            diScope.viewModel.intervalItems.observe { intervalItems: List<IntervalItem>? ->
                intervalsPopup?.contentView?.run {
                    intervalItems?.let { intervalsAdapter!!.intervalItems = it }
                    intervalsIcon.isActivated = intervalItems != null
                    intervalsRecycler.isVisible = intervalItems != null
                    intervalsAreOffTextView.isVisible = intervalItems == null
                }
            }
        }
    }

    private fun showIntervalsPopup() {
        requireIntervalsPopup().show(anchor = gradeButton, gravity = Gravity.BOTTOM)
    }

    private fun requireSpeakErrorPopup(): PopupWindow {
        if (speakErrorPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_speak_error, null).apply {
                goToTtsSettingsButton.setOnClickListener {
                    navigateToTtsSettings()
                    speakErrorPopup?.dismiss()
                }
            }
            speakErrorPopup = DarkPopupWindow(content)
            subscribeSpeakErrorPopup()
        }
        return speakErrorPopup!!
    }

    private fun navigateToTtsSettings() {
        startActivity(
            Intent().apply {
                action = "com.android.settings.TTS_SETTINGS"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    private fun subscribeSpeakErrorPopup() {
        viewCoroutineScope!!.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            diScope.viewModel.reasonForInabilityToSpeak.observe { reason: ReasonForInabilityToSpeak? ->
                if (reason == null) {
                    speakErrorPopup?.dismiss()
                } else {
                    speakErrorPopup?.contentView?.run {
                        speakErrorDescriptionTextView.text = getSpeakErrorDescription(reason)
                    }
                }
            }
        }
    }

    private fun getSpeakErrorDescription(
        reasonForInabilityToSpeak: ReasonForInabilityToSpeak
    ): String {
        return when (reasonForInabilityToSpeak) {
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
        requireSpeakErrorPopup().show(anchor = speakButton, gravity = Gravity.BOTTOM)
    }

    private fun requireInfinitePlaybackPopup(): PopupWindow {
        if (infinitePlaybackPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_infinite_playback, null)
            content.infinitePlaybackSwitchButton.setOnClickListener {
                controller?.dispatch(InfinitePlaybackSwitchToggled)
            }
            infinitePlaybackPopup = DarkPopupWindow(content)
            subscribeInfinitePopupToViewModel()
        }
        return infinitePlaybackPopup!!
    }

    private fun subscribeInfinitePopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            diScope.viewModel.isInfinitePlaybackEnabled.observe { isInfinitePlaybackEnabled ->
                infinitePlaybackPopup?.contentView?.run {
                    infinitePlaybackSwitch.run {
                        isChecked = isInfinitePlaybackEnabled
                        setText(if (isInfinitePlaybackEnabled) R.string.on else R.string.off)
                    }
                    infinitePlaybackIcon.isActivated = isInfinitePlaybackEnabled
                }
            }
        }
    }

    private fun showInfinitePlaybackPopup() {
        requireInfinitePlaybackPopup()
            .show(anchor = infinitePlaybackButton, gravity = Gravity.BOTTOM)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            when {
                getBoolean(STATE_INTERVALS_POPUP, false) -> showIntervalsPopup()
                getBoolean(STATE_SPEAK_ERROR_POPUP, false) -> showSpeakErrorPopup()
                getBoolean(STATE_INFINITE_PLAYBACK_POPUP, false) -> showInfinitePlaybackPopup()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savePopupState(outState, intervalsPopup, STATE_INTERVALS_POPUP)
        savePopupState(outState, speakErrorPopup, STATE_SPEAK_ERROR_POPUP)
        savePopupState(outState, infinitePlaybackPopup, STATE_INFINITE_PLAYBACK_POPUP)
    }

    private fun savePopupState(outState: Bundle, popupWindow: PopupWindow?, key: String) {
        val isPopupShowing = popupWindow?.isShowing ?: false
        outState.putBoolean(key, isPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerViewPager.adapter = null
        playerViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        intervalsPopup?.dismiss()
        intervalsPopup = null
        speakErrorPopup?.dismiss()
        speakErrorPopup = null
        infinitePlaybackPopup?.dismiss()
        infinitePlaybackPopup = null
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
    }

    companion object {
        const val TAG_PLAYING_FINISHED_BOTTOM_SHEET = "TAG_PLAYING_FINISHED_BOTTOM_SHEET"
        private const val STATE_INTERVALS_POPUP = "STATE_INTERVALS_POPUP"
        private const val STATE_SPEAK_ERROR_POPUP = "STATE_SPEAK_ERROR_POPUP"
        private const val STATE_INFINITE_PLAYBACK_POPUP = "STATE_INFINITE_PLAYBACK_POPUP"
    }
}