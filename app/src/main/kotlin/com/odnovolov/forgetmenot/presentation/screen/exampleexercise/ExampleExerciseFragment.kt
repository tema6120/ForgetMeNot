package com.odnovolov.forgetmenot.presentation.screen.exampleexercise

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Typeface
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
import com.odnovolov.forgetmenot.presentation.screen.exercise.SpeakingStatus.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.manual.ManualTestExerciseCardViewHolder
import kotlinx.android.synthetic.main.fragment_example_exercise.*
import kotlinx.android.synthetic.main.item_exercise_card_manual_test.view.*
import kotlinx.android.synthetic.main.popup_intervals.view.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.android.synthetic.main.popup_timer.view.*
import kotlinx.coroutines.launch

class ExampleExerciseFragment : BaseFragment() {
    init {
        ExampleExerciseDiScope.reopenIfClosed()
    }

    private var controller: ExampleExerciseController? = null
    private lateinit var viewModel: ExampleExerciseViewModel
    private val toast: Toast by lazy { Toast.makeText(requireContext(), "", Toast.LENGTH_SHORT) }
    private val vibrator: Vibrator? by lazy {
        ContextCompat.getSystemService(requireContext(), Vibrator::class.java)
    }
    private var intervalsAdapter: IntervalsAdapter? = null
    private var intervalsPopup: PopupWindow? = null
    private var speakErrorPopup: PopupWindow? = null
    private var timerPopup: PopupWindow? = null
    private var timerButtonPaintingAnimation: ValueAnimator? = null
    private var isExpanded = false

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
        gradeButton.run {
            setOnClickListener { showIntervalsPopup() }
            setTooltipTextFromContentDescription()
        }
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
            hasExerciseCards.observe { hasExerciseCards: Boolean ->
                if (!hasExerciseCards) {
                    speakFrame.isVisible = false
                    timerButton.isVisible = false
                    emptyCardView.isVisible = true
                }
            }
            if (isGradeButtonVisible) {
                gradeOfCurrentCard.observe { grade: Int ->
                    updateGradeButtonColor(grade)
                    gradeButton.text = grade.toString()
                    gradeButton.uncover()
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
            }
            speakingStatus.observe { speakingStatus: SpeakingStatus ->
                with(speakButton) {
                    setImageResource(
                        when (speakingStatus) {
                            Speaking -> R.drawable.ic_round_volume_off_24
                            NotSpeaking -> R.drawable.ic_round_volume_up_24
                            CannotSpeak -> R.drawable.ic_volume_error_24
                        }
                    )
                    val iconTintRes: Int =
                        when (speakingStatus) {
                            CannotSpeak -> R.color.issue
                            else -> R.color.icon_on_control_panel
                        }
                    setTintFromRes(iconTintRes)
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
                    uncover()
                }
                if (speakingStatus != CannotSpeak) {
                    speakErrorPopup?.dismiss()
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
            timerStatus.observe(::onTimerStatusChanged)
            vibrateCommand.observe { vibrate() }
            if (showProgressBar) progressBarForViewPager2.attach(exampleExerciseViewPager)
            if (showTextOfCardPosition) cardPosition.observe(positionTextView::setText)
            progressBarForViewPager2.isVisible = showProgressBar
            positionTextView.isVisible = showTextOfCardPosition
        }
    }

    private fun updateGradeButtonColor(grade: Int) {
        val gradeColorRes: Int = getGradeColorRes(grade)
        gradeButton.setBackgroundTintFromRes(gradeColorRes)
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
            if (timerButtonPaintingAnimation == null && isResumed && isExpanded) {
                val colorFrom =
                    ContextCompat.getColor(requireContext(), R.color.icon_on_control_panel)
                val colorTo = ContextCompat.getColor(requireContext(), R.color.issue)
                timerButtonPaintingAnimation =
                    ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo).apply {
                        duration = timerStatus.secondsLeft * 1000L
                        addUpdateListener { animator: ValueAnimator ->
                            timerButton.setTint(animator.animatedValue as Int)
                        }
                        start()
                    }
            }
        } else {
            val iconColorRes: Int = when (timerStatus) {
                is TimerStatus.Ticking -> R.color.icon_on_control_panel
                TimerStatus.TimeIsOver -> R.color.issue
                else -> R.color.icon_on_control_panel_deactivated
            }
            timerButton.setTintFromRes(iconColorRes)
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

    private fun showIntervalsPopup() {
        requireIntervalsPopup().show(anchor = gradeButton, gravity = Gravity.BOTTOM)
    }

    private fun requireIntervalsPopup(): PopupWindow {
        if (intervalsPopup == null) {
            val content: View = View.inflate(context, R.layout.popup_intervals, null)
            val onItemClick: (Int) -> Unit = { grade: Int ->
                intervalsPopup?.dismiss()
                controller?.dispatch(GradeWasSelected(grade))
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
            val diScope = ExampleExerciseDiScope.getAsync() ?: return@launch
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

    @SuppressLint("ClickableViewAccessibility")
    fun notifyBottomSheetStateChanged(newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                isExpanded = true
                blocker.setOnTouchListener(null)
                controller?.dispatch(BottomSheetExpanded)
                val currentViewHolder = exampleExerciseViewPager
                    .findViewHolderForAdapterPosition(exampleExerciseViewPager.currentItem)
                when (currentViewHolder) {
                    is ManualTestExerciseCardViewHolder -> {
                        // This is not the best solution
                        currentViewHolder.itemView.bottomButtonsLayout.translationX = 0f
                    }
                    is EntryTestExerciseCardViewHolder -> {
                        currentViewHolder.onPageSelected()
                    }
                }
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                isExpanded = false
                blocker.setOnTouchListener { _, _ -> true }
                controller?.dispatch(BottomSheetCollapsed)
                val currentViewHolder = exampleExerciseViewPager
                    .findViewHolderForAdapterPosition(exampleExerciseViewPager.currentItem)
                if (currentViewHolder is EntryTestExerciseCardViewHolder) {
                    hideKeyboardForcibly(requireActivity())
                }
                timerButtonPaintingAnimation?.cancel()
                timerButtonPaintingAnimation = null
            }
        }
    }

    fun notifyBottomSheetSlideOffsetChanged(slideOffset: Float) {
        exampleTextView.alpha = 1f - slideOffset
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
                        speakErrorDescriptionTextView.text =
                            composeSpeakErrorDescription(reason, requireContext())
                    }
                }
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
                            R.drawable.ic_round_timer_24_even else
                            R.drawable.ic_round_timer_24
                    )

                    val tintColorId: Int = when (timerStatus) {
                        is TimerStatus.Ticking -> R.color.ticking_timer_icon_on_popup
                        TimerStatus.TimeIsOver -> R.color.issue
                        else -> R.color.description_on_dark_popup
                    }
                    timerIcon.setTintFromRes(tintColorId)

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
                            R.color.description_on_dark_popup
                    timerDescriptionTextView.setTextColorFromRes(descriptionTextColorId)

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
        if (isFinishing()) {
            ExampleExerciseDiScope.close()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(PageWasChanged(position))
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