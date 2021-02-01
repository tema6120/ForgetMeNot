package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.*
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.findViewHolderForAdapterPosition
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.CannotGainAudioFocus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardViewHolder
import kotlinx.android.synthetic.main.fragment_example_exercise.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.android.synthetic.main.popup_timer.view.*
import kotlinx.coroutines.launch

class ExampleExerciseFragment : BaseFragment() {
    init {
        ExampleExerciseDiScope.reopenIfClosed()
    }

    private var controller: ExampleExerciseController? = null
    private lateinit var viewModel: ExampleExerciseViewModel
    private val speakErrorToast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.error_message_failed_to_speak, Toast.LENGTH_SHORT)
    }
    private val vibrator: Vibrator? by lazy {
        ContextCompat.getSystemService(requireContext(), Vibrator::class.java)
    }
    private var speakErrorPopup: PopupWindow? = null
    private var timerPopup: PopupWindow? = null
    private var timerButtonPaintingAnimation: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_example_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ExampleExerciseDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            exampleExerciseViewPager.adapter = diScope.getExerciseCardAdapter(viewCoroutineScope!!)
            observeViewModel()
        }
    }

    private fun setupView() {
        exampleExerciseViewPager.offscreenPageLimit = 1
        exampleExerciseViewPager.children.find { it is RecyclerView }?.let {
            (it as RecyclerView).isNestedScrollingEnabled = false
        }
        exampleExerciseViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        timerButton.run {
            setOnClickListener { showTimerPopup() }
            setTooltipTextFromContentDescription()
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            val exerciseCardAdapter = exampleExerciseViewPager.adapter as ExerciseCardAdapter
            exerciseCards.observe { exerciseCards: List<ExerciseCard> ->
                exerciseCardAdapter.submitList(exerciseCards)
                progressBar.visibility = View.GONE
            }
            if (exampleExerciseViewPager.currentItem != currentPosition) {
                exampleExerciseViewPager.setCurrentItem(currentPosition, false)
            }
            speakingStatus.observe { speakingStatus: SpeakingStatus ->
                with(speakButton) {
                    setImageResource(
                        when (speakingStatus) {
                            SpeakingStatus.Speaking -> R.drawable.ic_volume_off_white_24dp
                            SpeakingStatus.NotSpeaking -> R.drawable.ic_volume_up_white_24dp
                            SpeakingStatus.CannotSpeak -> R.drawable.ic_volume_error_24
                        }
                    )
                    setOnClickListener {
                        when (speakingStatus) {
                            SpeakingStatus.Speaking -> controller?.dispatch(StopSpeakButtonClicked)
                            SpeakingStatus.NotSpeaking -> controller?.dispatch(SpeakButtonClicked)
                            SpeakingStatus.CannotSpeak -> showSpeakErrorPopup()
                        }
                    }
                    contentDescription = getString(
                        when (speakingStatus) {
                            SpeakingStatus.Speaking -> R.string.description_stop_speaking_button
                            SpeakingStatus.NotSpeaking -> R.string.description_speak_button
                            SpeakingStatus.CannotSpeak -> R.string.description_cannot_speak_button
                        }
                    )
                    setTooltipTextFromContentDescription()
                }
                if (speakingStatus != SpeakingStatus.CannotSpeak) {
                    speakErrorPopup?.dismiss()
                }
            }
            isSpeakerPreparingToPronounce.observe { isPreparing: Boolean ->
                speakProgressBar.isInvisible = !isPreparing
            }
            speakerEvents.observe { event: SpeakerImpl.Event ->
                when (event) {
                    SpeakError -> speakErrorToast.run {
                        setText(R.string.error_message_failed_to_speak)
                        show()
                    }
                    CannotGainAudioFocus -> speakErrorToast.run {
                        setText(R.string.error_message_cannot_get_audio_focus)
                        show()
                    }
                }
            }
            timerStatus.observe(::onTimerStatusChanged)
            vibrateCommand.observe { vibrate() }
        }
    }

    private fun onTimerStatusChanged(timerStatus: TimerStatus) {
        if (timerStatus != TimerStatus.NotUsed && timerPopup == null) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (viewCoroutineScope != null) {
                    requireTimerPopup()
                }
            }, 500)
        }
        timerButton.isVisible = timerStatus != TimerStatus.NotUsed
        if (timerStatus !is TimerStatus.Ticking ||
            timerStatus.secondsLeft * 1000L > ExerciseFragment.TIME_TO_PAINT_TIMER_BUTTON
        ) {
            timerButtonPaintingAnimation?.cancel()
            timerButtonPaintingAnimation = null
        }
        if (timerStatus == TimerStatus.NotUsed) return
        timerButton.setImageResource(
            if (timerStatus is TimerStatus.Ticking && timerStatus.secondsLeft % 2 == 0)
                R.drawable.ic_round_timer_24_even else
                R.drawable.ic_round_timer_24
        )
        if (timerStatus is TimerStatus.Ticking
            && timerStatus.secondsLeft * 1000L <= ExerciseFragment.TIME_TO_PAINT_TIMER_BUTTON
        ) {
            if (timerButtonPaintingAnimation == null && isResumed && !exampleTextView.isVisible) {
                val colorFrom = Color.WHITE
                val colorTo = ContextCompat.getColor(requireContext(), R.color.issue)
                timerButtonPaintingAnimation =
                    ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                        duration = timerStatus.secondsLeft * 1000L
                        addUpdateListener { animator: ValueAnimator ->
                            timerButton.setColorFilter(
                                animator.animatedValue as Int,
                                PorterDuff.Mode.SRC_IN
                            )
                        }
                        start()
                    }
            }
        } else {
            val iconColor: Int = when (timerStatus) {
                is TimerStatus.Ticking -> Color.WHITE
                TimerStatus.TimeIsOver -> ContextCompat.getColor(requireContext(), R.color.issue)
                else ->
                    ContextCompat.getColor(requireContext(), R.color.icon_exercise_button_unabled)
            }
            timerButton.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun vibrate() {
        vibrator?.let { vibrator: Vibrator ->
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        ExerciseFragment.VIBRATION_DURATION,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ExerciseFragment.VIBRATION_DURATION)
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
                exampleExerciseFragmentRootView.isActivated = true
                val currentViewHolder = exampleExerciseViewPager
                    .findViewHolderForAdapterPosition(exampleExerciseViewPager.currentItem)
                if (currentViewHolder is EntryTestExerciseCardViewHolder) {
                    currentViewHolder.onPageSelected()
                }
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                blocker.setOnTouchListener { _, _ -> true }
                exampleTextView.isVisible = true
                controller?.dispatch(BottomSheetCollapsed)
                if (exampleExerciseFragmentRootView.isActivated) {
                    exampleExerciseFragmentRootView.isActivated = false
                }
                val currentViewHolder = exampleExerciseViewPager
                    .findViewHolderForAdapterPosition(exampleExerciseViewPager.currentItem)
                if (currentViewHolder is EntryTestExerciseCardViewHolder) {
                    hideKeyboardForcibly(requireActivity())
                }
                timerButtonPaintingAnimation?.cancel()
                timerButtonPaintingAnimation = null
            }
            else -> {
                if (exampleExerciseFragmentRootView.isActivated) {
                    exampleExerciseFragmentRootView.isActivated = false
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
            val diScope = ExampleExerciseDiScope.getAsync() ?: return@launch
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

    private fun requireTimerPopup(): PopupWindow {
        if (timerPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_timer, null).apply {
                stopTimerButton.setOnClickListener {
                    timerPopup?.dismiss()
                    controller?.dispatch(StopTimerButtonClicked)
                }
            }
            timerPopup = DarkPopupWindow(content)
            subscribeTimerPopupToViewModel()
        }
        return timerPopup!!
    }

    private fun subscribeTimerPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = ExampleExerciseDiScope.getAsync() ?: return@launch
            diScope.viewModel.timerStatus.observe { timerStatus: TimerStatus ->
                if (timerStatus == TimerStatus.NotUsed) {
                    timerPopup?.dismiss()
                    return@observe
                }
                timerPopup?.contentView?.run {
                    timerIcon.setImageResource(
                        if (timerStatus is TimerStatus.Ticking && timerStatus.secondsLeft % 2 == 0)
                            R.drawable.ic_round_timer_24_even_for_popup else
                            R.drawable.ic_round_timer_24_for_popup
                    )

                    val tintColorId: Int = when (timerStatus) {
                        is TimerStatus.Ticking -> R.color.ticking_timer_icon_on_popup
                        TimerStatus.TimeIsOver -> R.color.issue
                        else -> R.color.description_text_on_popup
                    }
                    val tintColor: Int = ContextCompat.getColor(context, tintColorId)
                    timerIcon.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)

                    timerDescriptionTextView.text = when (timerStatus) {
                        TimerStatus.NotUsed -> null
                        TimerStatus.OffBecauseWalkingMode ->
                            getString(R.string.timer_is_off_because_walking_mode)
                        is TimerStatus.Ticking ->
                            getString(R.string.time_for_answer, timerStatus.secondsLeft)
                        TimerStatus.Stopped -> getString(R.string.timer_stopped)
                        TimerStatus.TimeIsOver -> getString(R.string.time_is_over)
                    }

                    val descriptionTextColorId: Int =
                        if (timerStatus == TimerStatus.TimeIsOver)
                            R.color.issue else
                            R.color.description_text_on_popup
                    val descriptionTextColor: Int =
                        ContextCompat.getColor(context, descriptionTextColorId)
                    timerDescriptionTextView.setTextColor(descriptionTextColor)

                    stopTimerButton.isVisible = timerStatus is TimerStatus.Ticking
                }
            }
        }
    }

    private fun showTimerPopup() {
        requireTimerPopup().show(anchor = timerButton, gravity = Gravity.BOTTOM)
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = ExampleExerciseDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(FragmentResumed)
        }
    }

    override fun onPause() {
        super.onPause()
        viewCoroutineScope!!.launch {
            val diScope = ExampleExerciseDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(FragmentPaused)
        }
        timerButtonPaintingAnimation?.cancel()
        timerButtonPaintingAnimation = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            when {
                getBoolean(ExerciseFragment.STATE_SPEAK_ERROR_POPUP, false) -> showSpeakErrorPopup()
                getBoolean(ExerciseFragment.STATE_TIMER_POPUP, false) -> showTimerPopup()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savePopupState(outState, speakErrorPopup, ExerciseFragment.STATE_SPEAK_ERROR_POPUP)
        savePopupState(outState, timerPopup, ExerciseFragment.STATE_TIMER_POPUP)
    }

    private fun savePopupState(outState: Bundle, popupWindow: PopupWindow?, key: String) {
        val isPopupShowing = popupWindow?.isShowing ?: false
        outState.putBoolean(key, isPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exampleExerciseViewPager.adapter = null
        exampleExerciseViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        speakErrorPopup?.dismiss()
        speakErrorPopup = null
        timerPopup?.dismiss()
        timerPopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            ExampleExerciseDiScope.close()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(PageSelected(position))
            timerButtonPaintingAnimation?.cancel()
            timerButtonPaintingAnimation = null
            val currentViewHolder =
                exampleExerciseViewPager.findViewHolderForAdapterPosition(position)
            if (currentViewHolder is EntryTestExerciseCardViewHolder) {
                currentViewHolder.onPageSelected()
            }
        }
    }
}