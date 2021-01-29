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
import com.odnovolov.forgetmenot.presentation.screen.exercise.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus.*
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.service.PlayerService
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.*
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewModel.Laps
import kotlinx.android.synthetic.main.dialog_laps_in_player.*
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.fragment_player.editCardButton
import kotlinx.android.synthetic.main.fragment_player.gradeButton
import kotlinx.android.synthetic.main.fragment_player.helpButton
import kotlinx.android.synthetic.main.fragment_player.lapsTextView
import kotlinx.android.synthetic.main.fragment_player.markAsLearnedButton
import kotlinx.android.synthetic.main.fragment_player.progressBar
import kotlinx.android.synthetic.main.fragment_player.searchButton
import kotlinx.android.synthetic.main.fragment_player.speakButton
import kotlinx.android.synthetic.main.fragment_player.speakProgressBar
import kotlinx.android.synthetic.main.popup_editing_card.view.*
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
    private var editingPopup: PopupWindow? = null

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
            setTooltipTextFromContentDescription()
        }
        editCardButton.run {
            setOnClickListener { showEditingPopup() }
            setTooltipTextFromContentDescription()
        }
        searchButton.run {
            setOnClickListener { controller?.dispatch(SearchButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        lapsButton.run {
            setOnClickListener { controller?.dispatch(LapsButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        helpButton.run {
            setOnClickListener { controller?.dispatch(HelpButtonClicked) }
            setTooltipTextFromContentDescription()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            playingCards.observe { playingCards: List<PlayingCard> ->
                val adapter = playerViewPager.adapter as PlayingCardAdapter
                adapter.items = playingCards
                if (playerViewPager.currentItem != currentPosition) {
                    playerViewPager.setCurrentItem(currentPosition, false)
                }
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
                                MarkAsUnlearnedButtonClicked else
                                MarkAsLearnedButtonClicked
                        )
                    }
                    contentDescription = getString(
                        if (isLearned)
                            R.string.description_mark_as_unlearned_button else
                            R.string.description_mark_as_learned_button
                    )
                    setTooltipTextFromContentDescription()
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
                            Speaking -> R.string.description_stop_speaking_button
                            NotSpeaking -> R.string.description_speak_button
                            CannotSpeak -> R.string.description_cannot_speak_button
                        }
                    )
                    setTooltipTextFromContentDescription()
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
                    setTooltipTextFromContentDescription()
                }
            }
            laps.observe { laps: Laps ->
                lapsIcon.setImageResource(
                    if (laps == Laps.Infinitely)
                        R.drawable.ic_round_all_inclusive_24 else
                        R.drawable.ic_laps
                )
                lapsTextView.isVisible = laps is Laps.SpecificNumberOfText
                if (laps is Laps.SpecificNumberOfText) {
                    lapsTextView.text = laps.text
                }
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
            diScope.viewController.dispatch(FragmentResumed)
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
                    openTtsSettings()
                    speakErrorPopup?.dismiss()
                }
            }
            speakErrorPopup = DarkPopupWindow(content)
            subscribeSpeakErrorPopup()
        }
        return speakErrorPopup!!
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

    private fun requireEditingPopup(): PopupWindow {
        if (editingPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_editing_card, null).apply {
                editDeckSettingsButton.setOnClickListener {
                    editingPopup?.dismiss()
                    controller?.dispatch(EditDeckSettingsButtonClicked)
                }
                editCardContentButton.setOnClickListener {
                    editingPopup?.dismiss()
                    controller?.dispatch(EditCardContentButtonClicked)
                }
            }
            editingPopup = DarkPopupWindow(content)
        }
        return editingPopup!!
    }

    private fun showEditingPopup() {
        requireEditingPopup().show(anchor = editCardButton, gravity = Gravity.BOTTOM)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            when {
                getBoolean(STATE_INTERVALS_POPUP, false) -> showIntervalsPopup()
                getBoolean(STATE_SPEAK_ERROR_POPUP, false) -> showSpeakErrorPopup()
                getBoolean(STATE_EDITING_POPUP, false) -> showEditingPopup()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savePopupState(outState, intervalsPopup, STATE_INTERVALS_POPUP)
        savePopupState(outState, speakErrorPopup, STATE_SPEAK_ERROR_POPUP)
        savePopupState(outState, editingPopup, STATE_EDITING_POPUP)
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
        editingPopup?.dismiss()
        editingPopup = null
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
        private const val STATE_EDITING_POPUP = "STATE_EDITING_POPUP"
    }
}