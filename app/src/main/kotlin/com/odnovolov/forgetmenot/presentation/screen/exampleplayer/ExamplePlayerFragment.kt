package com.odnovolov.forgetmenot.presentation.screen.exampleplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.autoplay.PlayingCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.CannotGainAudioFocus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerController.Command.SetCurrentPosition
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerController.Command.ShowCannotGetAudioFocusMessage
import com.odnovolov.forgetmenot.presentation.screen.exampleplayer.ExamplePlayerEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus.*
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerViewModel
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayingCardAdapter
import kotlinx.android.synthetic.main.fragment_example_player.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.coroutines.launch

class ExamplePlayerFragment : BaseFragment() {
    init {
        ExamplePlayerDiScope.reopenIfClosed()
    }

    private var controller: ExamplePlayerController? = null
    private lateinit var viewModel: PlayerViewModel
    private val toast: Toast by lazy { Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT) }
    private var speakErrorPopup: PopupWindow? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_example_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ExamplePlayerDiScope.getAsync() ?: return@launch
            controller = diScope.controller
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
        playerViewPager.children.find { it is RecyclerView }?.let {
            (it as RecyclerView).isNestedScrollingEnabled = false
        }
        playerViewPager.registerOnPageChangeCallback(onPageChangeCallback)
    }

    private fun observeViewModel() {
        with (viewModel) {
            playingCards.observe { playingCards: List<PlayingCard> ->
                val adapter = playerViewPager.adapter as PlayingCardAdapter
                adapter.items = playingCards
                progressBar.visibility = View.GONE
            }
            if (playerViewPager.currentItem != currentPosition) {
                playerViewPager.setCurrentItem(currentPosition, false)
            }
            hasPlayingCards.observe { hasPlayingCards: Boolean ->
                if (!hasPlayingCards) {
                    speakFrame.isVisible = false
                    emptyCardView.isVisible = true
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
                    SpeakError -> toast.run {
                        setText(R.string.error_message_failed_to_speak)
                        show()
                    }
                    CannotGainAudioFocus -> toast.run {
                        setText(R.string.error_message_cannot_get_audio_focus)
                        show()
                    }
                }
            }
            isPlaying.observe { isPlaying: Boolean ->
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
        }
    }

    private fun executeCommand(command: ExamplePlayerController.Command) {
        when (command) {
            is SetCurrentPosition -> {
                playerViewPager.currentItem = command.position
            }
            ShowCannotGetAudioFocusMessage -> {
                toast.run {
                    setText(R.string.error_message_cannot_get_audio_focus)
                    show()
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun notifyBottomSheetStateChanged(newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                blocker.setOnTouchListener(null)
                exampleTextView.isVisible = false
                controller?.dispatch(BottomSheetExpanded)
                backgroundView.isActivated = true
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                blocker.setOnTouchListener { _, _ -> true }
                exampleTextView.isVisible = true
                controller?.dispatch(BottomSheetCollapsed)
                if (backgroundView.isActivated) {
                    backgroundView.isActivated = false
                }
            }
            else -> {
                if (backgroundView.isActivated) {
                    backgroundView.isActivated = false
                }
            }
        }
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
            val diScope = ExamplePlayerDiScope.getAsync() ?: return@launch
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

    override fun onPause() {
        super.onPause()
        viewCoroutineScope!!.launch {
            val diScope = ExamplePlayerDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(FragmentPaused)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        playerViewPager.adapter = null
        playerViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        speakErrorPopup?.dismiss()
        speakErrorPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            ExamplePlayerDiScope.close()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(NewPageBecameSelected(position))
        }
    }
}