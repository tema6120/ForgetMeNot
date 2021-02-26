package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*
import android.view.*
import android.view.View.GONE
import android.widget.PopupWindow
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.findViewHolderForAdapterPosition
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.CannotGainAudioFocus
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.HintStatus.MaskingLettersAction.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.KeyGestureDetector.Gesture
import com.odnovolov.forgetmenot.presentation.screen.exercise.KeyGestureDetector.Gesture.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.NO_ACTION
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.android.synthetic.main.popup_hints.view.*
import kotlinx.android.synthetic.main.popup_intervals.view.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.android.synthetic.main.popup_timer.view.*
import kotlinx.android.synthetic.main.popup_walking_mode.view.*
import kotlinx.coroutines.launch

class ExerciseFragment : BaseFragment() {
    init {
        ExerciseDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: ExerciseViewModel
    private var controller: ExerciseController? = null
    private var intervalsAdapter: IntervalsAdapter? = null
    private var intervalsPopup: PopupWindow? = null
    private var speakErrorPopup: PopupWindow? = null
    private var timerPopup: PopupWindow? = null
    private var hintsPopup: PopupWindow? = null
    private var walkingModePopup: PopupWindow? = null
    private val toast: Toast by lazy { Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT) }
    private val vibrator: Vibrator? by lazy {
        ContextCompat.getSystemService(requireContext(), Vibrator::class.java)
    }
    private val toneGenerator: ToneGenerator
            by lazy { ToneGenerator(AudioManager.STREAM_MUSIC, 100) }
    private lateinit var keyEventInterceptor: (KeyEvent) -> Boolean
    private lateinit var volumeUpGestureDetector: KeyGestureDetector
    private lateinit var volumeDownGestureDetector: KeyGestureDetector
    private var timerButtonPaintingAnimation: ValueAnimator? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        initKeyEventInterceptor()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            exerciseViewPager.adapter = diScope.getExerciseCardAdapter(viewCoroutineScope!!)
            progressBarForViewPager2.attach(exerciseViewPager)
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
        // We try to show ui as fast as possible.
        // Therefore, we init secondary things (dialogs, popups) late
        Looper.myQueue().addIdleHandler {
            // give window of 500 ms so as not to delay adding ViewPager's views (they are inflated asynchronously)
            Handler(Looper.getMainLooper()).postDelayed(::initSecondaryThings, 500)
            false
        }
    }

    private fun setupView() {
        exerciseViewPager.offscreenPageLimit = 1
        exerciseViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        gradeButton.run {
            setOnClickListener { showIntervalsPopup() }
            setTooltipTextFromContentDescription()
        }
        timerButton.run {
            setOnClickListener { showTimerPopup() }
            setTooltipTextFromContentDescription()
        }
        hintButton.run {
            setOnClickListener { showHintsPopup() }
            setTooltipTextFromContentDescription()
        }
        editCardButton.run {
            setOnClickListener { controller?.dispatch(EditCardButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        editDeckSettingsButton.run {
            setOnClickListener { controller?.dispatch(EditDeckSettingsButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        searchButton.run {
            setOnClickListener { controller?.dispatch(SearchButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        walkingModeButton.run {
            setOnClickListener { showWalkingModePopup() }
            setTooltipTextFromContentDescription()
        }
        helpButton.run {
            setOnClickListener { controller?.dispatch(HelpButtonClicked) }
            setTooltipTextFromContentDescription()
        }
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    private fun initKeyEventInterceptor() {
        volumeUpGestureDetector = KeyGestureDetector(
            coroutineScope = viewCoroutineScope!!,
            onGestureDetect = { gesture: Gesture ->
                val keyGesture: KeyGesture = when (gesture) {
                    SINGLE_PRESS -> VOLUME_UP_SINGLE_PRESS
                    DOUBLE_PRESS -> VOLUME_UP_DOUBLE_PRESS
                    LONG_PRESS -> VOLUME_UP_LONG_PRESS
                }
                controller?.dispatch(KeyGestureDetected(keyGesture))
            })
        volumeDownGestureDetector = KeyGestureDetector(
            coroutineScope = viewCoroutineScope!!,
            onGestureDetect = { gesture: Gesture ->
                val keyGesture: KeyGesture = when (gesture) {
                    SINGLE_PRESS -> VOLUME_DOWN_SINGLE_PRESS
                    DOUBLE_PRESS -> VOLUME_DOWN_DOUBLE_PRESS
                    LONG_PRESS -> VOLUME_DOWN_LONG_PRESS
                }
                controller?.dispatch(KeyGestureDetected(keyGesture))
            })
        keyEventInterceptor = { event: KeyEvent ->
            val isPressed = event.action == KeyEvent.ACTION_DOWN
            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    volumeUpGestureDetector.dispatchKeyEvent(isPressed)
                    true
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    volumeDownGestureDetector.dispatchKeyEvent(isPressed)
                    true
                }
                else -> false
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            val exerciseCardAdapter = exerciseViewPager.adapter as ExerciseCardAdapter
            exerciseCards.observe { exerciseCards: List<ExerciseCard> ->
                exerciseCardAdapter.submitList(exerciseCards)
                progressBar.visibility = GONE
            }
            if (exerciseViewPager.currentItem != currentPosition) {
                exerciseViewPager.setCurrentItem(currentPosition, false)
            }
            cardPosition.observe(positionTextView::setText)
            gradeOfCurrentCard.observe { grade: Int ->
                updateGradeButtonColor(grade)
                gradeButton.text = grade.toString()
            }
            isGradeEditedManually.observe { isEdited: Boolean ->
                with(gradeButton) {
                    if (isEdited) {
                        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
                        setTypeface(null, Typeface.BOLD)
                    } else {
                        paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
                        setTypeface(null, Typeface.NORMAL)
                    }
                }
            }
            isCurrentExerciseCardLearned.observe { isLearned: Boolean ->
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
                if (speakingStatus != CannotSpeak) {
                    speakErrorPopup?.dismiss()
                }
            }
            isSpeakerPreparingToPronounce.observe { isPreparing: Boolean ->
                speakProgressBar.visibility = if (isPreparing) View.VISIBLE else View.INVISIBLE
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
            timerStatus.observe(::onTimerStatusChanged)
            hintStatus.observe { hintStatus: HintStatus ->
                hintButton.isActivated = hintStatus is HintStatus.Accessible
                hintButton.isVisible = hintStatus != HintStatus.Off
                if (hintStatus != HintStatus.Off && hintsPopup == null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (viewCoroutineScope != null) {
                            requireHintsPopup()
                        }
                    }, 500)
                }
            }
            vibrateCommand.observe { vibrate() }
            learnedCardSoundNotification.observe {
                toneGenerator.startTone(
                    ToneGenerator.TONE_CDMA_PIP,
                    LEARNED_CARD_SOUND_DURATION
                )
            }
            isWalkingModeEnabled.observe { isEnabled: Boolean ->
                walkingModeButton.isActivated = isEnabled
                (activity as MainActivity).keyEventInterceptor =
                    if (isEnabled) keyEventInterceptor else null
                walkingModeButton.keepScreenOn = isEnabled
            }
            keyGestureMap.observe { keyGestureMap: Map<KeyGesture, KeyGestureAction> ->
                volumeUpGestureDetector.run {
                    detectSinglePress = keyGestureMap[VOLUME_UP_SINGLE_PRESS] != NO_ACTION
                    detectDoublePress = keyGestureMap[VOLUME_UP_DOUBLE_PRESS] != NO_ACTION
                    detectLongPress = keyGestureMap[VOLUME_UP_LONG_PRESS] != NO_ACTION
                }
                volumeDownGestureDetector.run {
                    detectSinglePress = keyGestureMap[VOLUME_DOWN_SINGLE_PRESS] != NO_ACTION
                    detectDoublePress = keyGestureMap[VOLUME_DOWN_DOUBLE_PRESS] != NO_ACTION
                    detectLongPress = keyGestureMap[VOLUME_DOWN_LONG_PRESS] != NO_ACTION
                }
            }
        }
    }

    private fun updateGradeButtonColor(grade: Int) {
        val gradeColorRes = getGradeColorRes(grade)
        val gradeColor: Int = ContextCompat.getColor(requireContext(), gradeColorRes)
        gradeButton.background.colorFilter =
            BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                gradeColor,
                BlendModeCompat.SRC_ATOP
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val brightGradeColor: Int =
                ContextCompat.getColor(requireContext(), getBrightGradeColorRes(grade))
            gradeButton.outlineAmbientShadowColor = brightGradeColor
            gradeButton.outlineSpotShadowColor = brightGradeColor
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
            timerStatus.secondsLeft * 1000L > TIME_TO_PAINT_TIMER_BUTTON
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
            && timerStatus.secondsLeft * 1000L <= TIME_TO_PAINT_TIMER_BUTTON
        ) {
            if (timerButtonPaintingAnimation == null && isResumed) {
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
                        VIBRATION_DURATION,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_DURATION)
            }
        }
    }

    private fun executeCommand(command: ExerciseController.Command) {
        when (command) {
            MoveToNextPosition -> {
                val nextPosition = exerciseViewPager.currentItem + 1
                exerciseViewPager.setCurrentItem(nextPosition, true)
            }
            MoveToPreviousPosition -> {
                val previousPosition = exerciseViewPager.currentItem - 1
                exerciseViewPager.setCurrentItem(previousPosition, true)
            }
            is MoveToPosition -> {
                exerciseViewPager.setCurrentItem(command.position, true)
            }
            is ShowQuitExerciseBottomSheet -> {
                QuitExerciseBottomSheet().show(childFragmentManager, "QuitExerciseBottomSheet")
            }
        }
    }

    private fun initSecondaryThings() {
        if (viewCoroutineScope == null) return
        requireIntervalsPopup()
        requireWalkingModePopup()
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
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
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
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
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
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
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

    private fun requireHintsPopup(): PopupWindow {
        if (hintsPopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_hints, null).apply {
                getVariantsButton.setOnClickListener {
                    hintsPopup?.dismiss()
                    controller?.dispatch(GetVariantsButtonClicked)
                }
                maskLettersButton.setOnClickListener {
                    hintsPopup?.dismiss()
                    controller?.dispatch(MaskLettersButtonClicked)
                }
            }
            hintsPopup = DarkPopupWindow(content)
            subscribeHintsPopupToViewModel()
        }
        return hintsPopup!!
    }

    private fun subscribeHintsPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            diScope.viewModel.hintStatus.observe { hintStatus: HintStatus ->
                if (hintStatus == HintStatus.Off) {
                    hintsPopup?.dismiss()
                    return@observe
                }
                val isHintAccessible = hintStatus is HintStatus.Accessible
                hintsPopup?.contentView?.run {
                    hintIcon.isActivated = isHintAccessible

                    when (hintStatus) {
                        HintStatus.NotAccessibleBecauseCardIsAnswered -> hintsDescriptionTextView
                            .setText(R.string.hints_are_not_accessible_because_the_card_is_answered)
                        HintStatus.NotAccessibleBecauseCardIsLearned -> hintsDescriptionTextView
                            .setText(R.string.hints_are_not_accessible_because_the_card_is_learned)
                    }
                    hintsDescriptionTextView.isVisible = !isHintAccessible

                    getVariantsButton.isVisible = hintStatus is HintStatus.Accessible
                            && hintStatus.isGettingVariantsAccessible

                    maskLettersButton.isVisible = isHintAccessible
                    if (hintStatus is HintStatus.Accessible) {
                        maskLettersButton.setText(
                            when (hintStatus.currentMaskingLettersAction) {
                                MaskLetters -> R.string.text_hint_mask_letters_button
                                UnmaskTheFirstLetter -> R.string.text_unmask_the_first_letter_button
                                UnmaskSelectedRegion -> R.string.text_unmask_selected_region_button
                            }
                        )
                    }
                }
            }
        }
    }

    private fun showHintsPopup() {
        requireHintsPopup().show(anchor = hintButton, gravity = Gravity.BOTTOM)
    }

    private fun requireWalkingModePopup(): PopupWindow {
        if (walkingModePopup == null) {
            val content = View.inflate(requireContext(), R.layout.popup_walking_mode, null)
            viewCoroutineScope!!.launch {
                val diScope = ExerciseDiScope.getAsync() ?: return@launch
                diScope.viewModel.isWalkingModeEnabled.observe { isWalkingModeEnabled: Boolean ->
                    content.walkingModeSwitch.isChecked = isWalkingModeEnabled
                    content.walkingModeIcon.isActivated = isWalkingModeEnabled
                }
            }
            content.walkingModeSettingsButton.run {
                setOnClickListener {
                    walkingModePopup?.dismiss()
                    controller?.dispatch(WalkingModeSettingsButtonClicked)
                }
                setTooltipTextFromContentDescription()
            }
            content.walkingModeHelpButton.run {
                setOnClickListener {
                    walkingModePopup?.dismiss()
                    controller?.dispatch(WalkingModeHelpButtonClicked)
                }
                setTooltipTextFromContentDescription()
            }
            content.walkingModeSwitchButton.setOnClickListener {
                controller?.dispatch(WalkingModeSwitchToggled)
            }
            walkingModePopup = DarkPopupWindow(content)
        }
        return walkingModePopup!!
    }

    private fun showWalkingModePopup() {
        requireWalkingModePopup().show(anchor = walkingModeButton, gravity = Gravity.BOTTOM)
    }

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(FragmentResumed)
        }
    }

    override fun onPause() {
        super.onPause()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(FragmentPaused)
        }
        timerButtonPaintingAnimation?.cancel()
        timerButtonPaintingAnimation = null
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            when {
                getBoolean(STATE_INTERVALS_POPUP, false) -> showIntervalsPopup()
                getBoolean(STATE_SPEAK_ERROR_POPUP, false) -> showSpeakErrorPopup()
                getBoolean(STATE_TIMER_POPUP, false) -> showTimerPopup()
                getBoolean(STATE_HINTS_POPUP, false) -> showHintsPopup()
                getBoolean(STATE_WALKING_MODE_POPUP, false) -> showWalkingModePopup()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savePopupState(outState, intervalsPopup, STATE_INTERVALS_POPUP)
        savePopupState(outState, speakErrorPopup, STATE_SPEAK_ERROR_POPUP)
        savePopupState(outState, timerPopup, STATE_TIMER_POPUP)
        savePopupState(outState, hintsPopup, STATE_HINTS_POPUP)
        savePopupState(outState, walkingModePopup, STATE_WALKING_MODE_POPUP)
    }

    private fun savePopupState(outState: Bundle, popupWindow: PopupWindow?, key: String) {
        val isPopupShowing = popupWindow?.isShowing ?: false
        outState.putBoolean(key, isPopupShowing)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewPager.adapter = null
        exerciseViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        (activity as MainActivity).run {
            keyEventInterceptor = null
            unregisterBackPressInterceptor(backPressInterceptor)
        }
        intervalsAdapter = null
        intervalsPopup?.dismiss()
        intervalsPopup = null
        speakErrorPopup?.dismiss()
        speakErrorPopup = null
        timerPopup?.dismiss()
        timerPopup = null
        hintsPopup?.dismiss()
        hintsPopup = null
        walkingModePopup?.dismiss()
        walkingModePopup = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            ExerciseDiScope.close()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(PageSelected(position))
            timerButtonPaintingAnimation?.cancel()
            timerButtonPaintingAnimation = null
            val currentViewHolder = exerciseViewPager.findViewHolderForAdapterPosition(position)
            if (currentViewHolder is EntryTestExerciseCardViewHolder) {
                currentViewHolder.onPageSelected()
            }
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        controller?.dispatch(BackButtonClicked)
        true
    }

    companion object {
        const val TIME_TO_PAINT_TIMER_BUTTON = 10_000L
        const val VIBRATION_DURATION = 50L
        const val LEARNED_CARD_SOUND_DURATION = 400
        const val STATE_INTERVALS_POPUP = "STATE_INTERVALS_POPUP"
        const val STATE_SPEAK_ERROR_POPUP = "STATE_SPEAK_ERROR_POPUP"
        const val STATE_TIMER_POPUP = "STATE_TIMER_POPUP"
        const val STATE_HINTS_POPUP = "STATE_HINTS_POPUP"
        const val STATE_WALKING_MODE_POPUP = "STATE_WALKING_MODE_POPUP"
    }
}