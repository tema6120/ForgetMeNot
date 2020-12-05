package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.content.Intent
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.GONE
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.findViewHolderForAdapterPosition
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.HintStatus.MaskingLettersAction.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.KeyGestureDetector.Gesture
import com.odnovolov.forgetmenot.presentation.screen.exercise.KeyGestureDetector.Gesture.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.NO_ACTION
import kotlinx.android.synthetic.main.dialog_exit_from_exercise.*
import kotlinx.android.synthetic.main.dialog_exit_from_exercise.view.*
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.android.synthetic.main.popup_hints.view.*
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
    private var speakErrorPopup: PopupWindow? = null
    private var gradeIntervalsPopup: PopupWindow? = null
    private var timerPopup: PopupWindow? = null
    private var hintsPopup: PopupWindow? = null
    private var walkingModePopup: PopupWindow? = null
    private val speakErrorToast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.error_message_failed_to_speak, Toast.LENGTH_SHORT)
    }
    private var exitDialog: AlertDialog? = null
    private lateinit var keyEventInterceptor: (KeyEvent) -> Boolean
    private lateinit var volumeUpGestureDetector: KeyGestureDetector
    private lateinit var volumeDownGestureDetector: KeyGestureDetector
    private var knowingWhenPagerStopped: KnowingWhenPagerStopped? = null

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
            exerciseViewPager.adapter =
                diScope.getExerciseCardAdapter(viewCoroutineScope!!, knowingWhenPagerStopped!!)
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
        knowingWhenPagerStopped = KnowingWhenPagerStopped()
        exerciseViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        gradeButton.run {
            setOnClickListener { controller?.dispatch(GradeButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        editCardButton.run {
            setOnClickListener { controller?.dispatch(EditCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        walkingModeButton.run {
            setOnClickListener { showWalkingModePopup() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        timerButton.run {
            setOnClickListener { showTimerPopup() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        hintButton.run {
            setOnClickListener { showHintsPopup() }
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
                if (speakingStatus != CannotSpeak) {
                    speakErrorPopup?.dismiss()
                }
            }
            isSpeakerPreparingToPronounce.observe { isPreparing: Boolean ->
                speakProgressBar.visibility = if (isPreparing) View.VISIBLE else View.INVISIBLE
            }
            speakerEvents.observe { event: SpeakerImpl.Event ->
                when (event) {
                    SpeakError -> speakErrorToast.show()
                }
            }
            isWalkingModeEnabled.observe { isEnabled: Boolean ->
                walkingModeButton.isActivated = isEnabled
                (activity as MainActivity).keyEventInterceptor =
                    if (isEnabled) keyEventInterceptor else null
            }
            hintStatus.observe { hintStatus: HintStatus ->
                hintButton.isActivated = hintStatus is HintStatus.Accessible
                hintButton.isVisible = hintStatus != HintStatus.Off
            }
            timerStatus.observe { timerStatus: TimerStatus ->
                when (timerStatus) {
                    TimerStatus.NotUsed -> {
                        timerButton.isVisible = false
                    }
                    is TimerStatus.Ticking -> {
                        timerButton.setImageResource(
                            if (timerStatus.secondsLeft % 2 == 0)
                                R.drawable.ic_round_timer_24_even else
                                R.drawable.ic_round_timer_24
                        )
                        timerButton.isVisible = true
                    }
                    else -> {
                        timerButton.setImageResource(R.drawable.ic_round_timer_24_off)
                        timerButton.isVisible = true
                    }
                }
            }
            gradeOfCurrentCard.observe { grade: Int ->
                updateGradeButtonColor(grade)
                gradeButton.text = grade.toString()
                if (gradeIntervalsPopup?.isShowing == true) {
                    intervalsAdapter!!.intervalItems =
                        intervalsAdapter!!.intervalItems.map { intervalItem: IntervalItem ->
                            IntervalItem(
                                grade = intervalItem.grade,
                                waitingPeriod = intervalItem.waitingPeriod,
                                isSelected = intervalItem.grade == grade
                            )
                        }
                }
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
            is ShowIntervalsPopup -> {
                showGradeIntervalsPopup(command.intervalItems)
            }
            ShowIntervalsAreOffMessage -> {
                showToast(R.string.toast_text_intervals_are_off)
            }
            is ShowThereAreUnansweredCardsMessage -> {
                showExitDialog(command.unansweredCardCount)
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

    private fun initSecondaryThings() {
        if (viewCoroutineScope == null) return
        createGradeIntervalsPopup()
        createSpeakErrorPopup()
        createTimerPopup()
        createHintsPopup()
        createWalkingModePopup()
        createExitDialog()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    private fun createGradeIntervalsPopup() {
        val recycler: RecyclerView =
            View.inflate(context, R.layout.popup_grade_intervals, null) as RecyclerView
        val onItemClick: (Int) -> Unit = { grade: Int ->
            controller?.dispatch(GradeWasChanged(grade))
            gradeIntervalsPopup?.dismiss()
        }
        intervalsAdapter = IntervalsAdapter(onItemClick)
        recycler.adapter = intervalsAdapter
        gradeIntervalsPopup = PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = recycler
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromBottomLeftAnimation
        }
    }

    private fun showGradeIntervalsPopup(intervalItems: List<IntervalItem>) {
        if (gradeIntervalsPopup == null) return
        intervalsAdapter!!.intervalItems = intervalItems
        val content = gradeIntervalsPopup!!.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        gradeButton.getLocationOnScreen(location)
        val x = location[0] + 8.dp
        val y = location[1] + gradeButton.height - 8.dp - content.measuredHeight
        gradeIntervalsPopup!!.showAtLocation(
            gradeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    private fun createSpeakErrorPopup() {
        val content = View.inflate(requireContext(), R.layout.popup_speak_error, null).apply {
            goToTtsSettingsButton.setOnClickListener {
                navigateToTtsSettings()
                speakErrorPopup?.dismiss()
            }
        }
        speakErrorPopup = PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
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

    private fun navigateToTtsSettings() {
        startActivity(
            Intent().apply {
                action = "com.android.settings.TTS_SETTINGS"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    private fun showSpeakErrorPopup() {
        if (speakErrorPopup == null) return
        speakErrorPopup!!.contentView.speakErrorDescriptionTextView.text =
            getSpeakErrorDescription()
        val content: View = speakErrorPopup!!.contentView
        content.measure(
            MeasureSpec.makeMeasureSpec(speakButton.rootView.width, MeasureSpec.AT_MOST),
            MeasureSpec.makeMeasureSpec(speakButton.rootView.height, MeasureSpec.AT_MOST)
        )
        val speakButtonLocation = IntArray(2).also(speakButton::getLocationOnScreen)
        val x: Int = 8.dp
        val y: Int = speakButtonLocation[1] + speakButton.height - 8.dp - content.measuredHeight
        speakErrorPopup!!.showAtLocation(
            speakButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
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

    private fun createTimerPopup() {
        val content = View.inflate(requireContext(), R.layout.popup_timer, null).apply {
            stopTimerButton.setOnClickListener {
                timerPopup?.dismiss()
                controller?.dispatch(StopTimerButtonClicked)
            }
        }
        timerPopup = PopupWindow(context).apply {
            contentView = content
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromBottomAnimation
        }
        subscribeTimerPopupToViewModel()
    }

    private fun subscribeTimerPopupToViewModel() {
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            diScope.viewModel.timerStatus.observe { timerStatus: TimerStatus ->
                if (timerStatus == TimerStatus.NotUsed) {
                    timerPopup?.dismiss()
                    return@observe
                }
                with(timerPopup!!.contentView) {
                    val drawableStartId =
                        if (timerStatus is TimerStatus.Ticking && timerStatus.secondsLeft % 2 == 0)
                            R.drawable.ic_round_timer_24_even_for_popup else
                            R.drawable.ic_round_timer_24_for_popup
                    timerPopupTitleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        drawableStartId, 0, 0, 0
                    )

                    val tintColorId: Int = when (timerStatus) {
                        is TimerStatus.Ticking -> R.color.ticking_timer_icon_on_popup
                        TimerStatus.TimeIsOver -> R.color.issue
                        else -> R.color.description_text_on_popup
                    }
                    val tintColor: Int = ContextCompat.getColor(context, tintColorId)
                    timerPopupTitleTextView.compoundDrawablesRelative[0].colorFilter =
                        PorterDuffColorFilter(tintColor, PorterDuff.Mode.SRC_IN)

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
                updateTimerPopupPosition()
            }
        }
    }

    private fun updateTimerPopupPosition() {
        with(timerPopup!!) {
            if (!isShowing) return
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            if (width == contentView.measuredWidth && height == contentView.measuredHeight) return
            val timerButtonLocation = IntArray(2).also(timerButton::getLocationOnScreen)
            val x =
                timerButtonLocation[0] + (timerButton.width / 2) - (contentView.measuredWidth / 2)
            val y = timerButtonLocation[1] + timerButton.height - 8.dp - contentView.measuredHeight
            update(x, y, contentView.measuredWidth, contentView.measuredHeight)
        }
    }

    private fun showTimerPopup() {
        if (timerPopup == null) return
        measureTimerPopup()
        val timerButtonLocation = IntArray(2).also(timerButton::getLocationOnScreen)
        val x = timerButtonLocation[0] + (timerButton.width / 2) - (timerPopup!!.width / 2)
        val y = timerButtonLocation[1] + timerButton.height - 8.dp - timerPopup!!.height
        timerPopup!!.showAtLocation(timerButton.rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun measureTimerPopup() {
        timerPopup!!.run {
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            width = contentView.measuredWidth
            height = contentView.measuredHeight
        }
    }

    private fun createHintsPopup() {
        val content = View.inflate(requireContext(), R.layout.popup_hints, null).apply {
            getVariantsButton.setOnClickListener {
                controller?.dispatch(GetVariantsButtonClicked)
                hintsPopup?.dismiss()
            }
            maskLettersButton.setOnClickListener {
                controller?.dispatch(MaskLettersButtonClicked)
                hintsPopup?.dismiss()
            }
        }
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        hintsPopup = PopupWindow(context).apply {
            width = content.measuredWidth
            height = content.measuredHeight
            contentView = content
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromBottomAnimation
        }
        subscribeHintsPopupToViewModel()
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
                with(hintsPopup!!.contentView) {
                    hintsPopupTitleTextView.isActivated = isHintAccessible

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
                                MaskLetters -> R.string.title_mask_letters_button
                                UnmaskTheFirstLetter -> R.string.title_unmask_the_first_letter_button
                                UnmaskSelectedRegion -> R.string.title_unmask_selected_region_button
                            }
                        )
                    }
                }
                updateHintsPopupPosition()
            }
        }
    }

    private fun updateHintsPopupPosition() {
        with(hintsPopup!!) {
            if (!isShowing) return
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            if (width == contentView.measuredWidth && height == contentView.measuredHeight) return
            val hintButtonLocation = IntArray(2).also(hintButton::getLocationOnScreen)
            val x = hintButtonLocation[0] + (hintButton.width / 2) - (contentView.measuredWidth / 2)
            val y = hintButtonLocation[1] + hintButton.height - 8.dp - contentView.measuredHeight
            update(x, y, contentView.measuredWidth, contentView.measuredHeight)
        }
    }

    private fun showHintsPopup() {
        if (hintsPopup == null) return
        measureHintsPopup()
        val hintButtonLocation = IntArray(2).also(hintButton::getLocationOnScreen)
        val x = hintButtonLocation[0] + (hintButton.width / 2) - (hintsPopup!!.width / 2)
        val y = hintButtonLocation[1] + hintButton.height - 8.dp - hintsPopup!!.height
        hintsPopup!!.showAtLocation(hintButton.rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun measureHintsPopup() {
        hintsPopup!!.run {
            contentView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
            width = contentView.measuredWidth
            height = contentView.measuredHeight
        }
    }

    private fun createWalkingModePopup() {
        val content = View.inflate(requireContext(), R.layout.popup_walking_mode, null)
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            diScope.viewModel.isWalkingModeEnabled.observe { isWalkingModeEnabled: Boolean ->
                content.walkingModeSwitch.isChecked = isWalkingModeEnabled
                content.walkingModePopupTitleTextView.isActivated = isWalkingModeEnabled
            }
        }
        content.walkingModeSettingsButton.run {
            setOnClickListener {
                walkingModePopup?.dismiss()
                controller?.dispatch(WalkingModeSettingsButtonClicked)
            }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        content.walkingModeHelpButton.run {
            setOnClickListener {
                walkingModePopup?.dismiss()
                controller?.dispatch(WalkingModeHelpButtonClicked)
            }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        content.walkingModeSwitchButton.setOnClickListener {
            controller?.dispatch(WalkingModeSwitchToggled)
        }
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        walkingModePopup = PopupWindow(context).apply {
            width = content.measuredWidth
            height = content.measuredHeight
            contentView = content
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
            animationStyle = R.style.PopupFromBottomRightAnimation
        }
    }

    private fun showWalkingModePopup() {
        if (walkingModePopup == null) return
        val walkingModeButtonLocation = IntArray(2).also(walkingModeButton::getLocationOnScreen)
        val x: Int =
            walkingModeButtonLocation[0] + walkingModeButton.width - 8.dp - walkingModePopup!!.width
        val y: Int =
            walkingModeButtonLocation[1] + walkingModeButton.height - 8.dp - walkingModePopup!!.height
        walkingModePopup!!.showAtLocation(
            walkingModeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    private fun createExitDialog() {
        val content: View = View.inflate(context, R.layout.dialog_exit_from_exercise, null)
        content.showButton.setOnClickListener {
            controller?.dispatch(ShowUnansweredCardButtonClicked)
            exitDialog?.dismiss()
        }
        exitDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_exit_dialog)
            .setView(content)
            .setPositiveButton(R.string.yes) { _, _ -> controller?.dispatch(UserConfirmedExit) }
            .setNegativeButton(R.string.no, null)
            .create()
        dialogTimeCapsule.register("exerciseExitDialog", exitDialog!!)
    }

    private fun showExitDialog(unansweredCardCount: Int) {
        exitDialog?.run {
            show()
            messageTextView.text = resources.getQuantityString(
                R.plurals.exit_message_unanswered_cards,
                unansweredCardCount,
                unansweredCardCount
            )
            showButton.text = resources.getQuantityString(
                R.plurals.text_show_unanswered_card_button,
                unansweredCardCount
            )
        }
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
        gradeIntervalsPopup = null
        speakErrorPopup = null
        timerPopup = null
        hintsPopup = null
        walkingModePopup = null
        exitDialog = null
        knowingWhenPagerStopped = null
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
            val currentViewHolder = exerciseViewPager.findViewHolderForAdapterPosition(position)
            if (currentViewHolder is EntryTestExerciseCardViewHolder) {
                currentViewHolder.onPageSelected()
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            knowingWhenPagerStopped?.updateState(state)
        }
    }

    private val backPressInterceptor by lazy {
        object : MainActivity.BackPressInterceptor {
            override fun onBackPressed(): Boolean {
                controller?.dispatch(BackButtonClicked)
                return true
            }
        }
    }
}