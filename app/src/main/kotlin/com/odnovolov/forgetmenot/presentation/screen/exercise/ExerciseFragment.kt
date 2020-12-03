package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.content.Intent
import android.graphics.Paint
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
import com.odnovolov.forgetmenot.R.plurals
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseViewModel.HintStatus
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseViewModel.HintStatus.*
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
import kotlinx.android.synthetic.main.popup_choose_hint.view.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.android.synthetic.main.popup_walking_mode.view.*
import kotlinx.coroutines.launch

class ExerciseFragment : BaseFragment() {
    init {
        ExerciseDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: ExerciseViewModel
    private var controller: ExerciseController? = null
    private var walkingModePopup: PopupWindow? = null
    private var chooseHintPopup: PopupWindow? = null
    private var intervalsAdapter: IntervalsAdapter? = null
    private var intervalsPopup: PopupWindow? = null
    private var speakErrorPopup: PopupWindow? = null
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
            setOnClickListener { controller?.dispatch(TimerButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        hintButton.run {
            setOnClickListener { controller?.dispatch(HintButtonClicked) }
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
                when (hintStatus) {
                    Accessible -> {
                        hintButton.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp)
                        hintButton.isVisible = true
                    }
                    NotAccessible -> {
                        hintButton.setImageResource(R.drawable.ic_lightbulb_outline_white_24dp_disabled)
                        hintButton.isVisible = true
                    }
                    Off -> {
                        hintButton.isVisible = false
                    }
                }
            }
            timeLeft.observe { timeLeft: Int? ->
                if (timeLeft == null) {
                    timerButton.isVisible = false
                } else {
                    timerButton.setImageResource(
                        when {
                            timeLeft == 0 -> R.drawable.ic_timer_white_24dp_off
                            timeLeft % 2 == 1 -> R.drawable.ic_timer_white_24dp_odd
                            else -> R.drawable.ic_timer_white_24dp
                        }
                    )
                    timerButton.isVisible = true
                }
            }
            gradeOfCurrentCard.observe { grade: Int ->
                updateGradeButtonColor(grade)
                gradeButton.text = grade.toString()
                if (intervalsPopup?.isShowing == true) {
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
            ShowTimerIsAlreadyOffMessage -> {
                showToast(R.string.toast_timer_is_already_off)
            }
            ShowChooseHintPopup -> {
                showChooseHintPopup()
            }
            ShowHintIsNotAccessibleMessage -> {
                showToast(R.string.toast_hint_is_not_accessible)
            }
            is ShowIntervalsPopup -> {
                showIntervalsPopup(command.intervalItems)
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

    private fun showChooseHintPopup() {
        if (chooseHintPopup == null) return
        val hintButtonLocation = IntArray(2).also { hintButton.getLocationOnScreen(it) }
        val x = hintButtonLocation[0] + (hintButton.width / 2) - (chooseHintPopup!!.width / 2)
        val y = hintButtonLocation[1] + hintButton.height - 8.dp - chooseHintPopup!!.height
        chooseHintPopup!!.showAtLocation(
            hintButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    private fun showIntervalsPopup(intervalItems: List<IntervalItem>) {
        if (intervalsPopup == null) return
        intervalsAdapter!!.intervalItems = intervalItems
        val content = intervalsPopup!!.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        gradeButton.getLocationOnScreen(location)
        val x = location[0] + 8.dp
        val y = location[1] + gradeButton.height - 8.dp - content.measuredHeight
        intervalsPopup!!.showAtLocation(
            gradeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    private fun showSpeakErrorPopup() {
        if (speakErrorPopup == null) return
        speakErrorPopup!!.contentView.speakErrorDescriptionTextView.text =
            getSpeakErrorDescription()
        val content: View = speakErrorPopup!!.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val speakButtonLocation = IntArray(2).also { speakButton.getLocationOnScreen(it) }
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

    private fun showExitDialog(unansweredCardCount: Int) {
        exitDialog?.run {
            show()
            messageTextView.text = resources.getQuantityString(
                plurals.exit_message_unanswered_cards,
                unansweredCardCount,
                unansweredCardCount
            )
            showButton.text = resources.getQuantityString(
                plurals.text_show_unanswered_card_button,
                unansweredCardCount
            )
        }
    }

    private fun initSecondaryThings() {
        if (viewCoroutineScope == null) return
        createWalkingModePopup()
        createChooseHintPopup()
        createIntervalsPopup()
        createSpeakErrorPopup()
        createExitDialog()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    private fun createWalkingModePopup() {
        val content = View.inflate(requireContext(), R.layout.popup_walking_mode, null)
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            diScope.viewModel.isWalkingModeEnabled.observe(content.walkingModeSwitch::setChecked)
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

    private fun createChooseHintPopup() {
        val content = View.inflate(requireContext(), R.layout.popup_choose_hint, null).apply {
            getVariantsButton.setOnClickListener {
                controller?.dispatch(HintAsQuizButtonClicked)
                chooseHintPopup?.dismiss()
            }
            maskLettersButton.setOnClickListener {
                controller?.dispatch(MaskLettersButtonClicked)
                chooseHintPopup?.dismiss()
            }
        }
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        chooseHintPopup = PopupWindow(context).apply {
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
    }

    private fun createIntervalsPopup() {
        val recycler: RecyclerView =
            View.inflate(context, R.layout.popup_set_grade, null) as RecyclerView
        val onItemClick: (Int) -> Unit = { grade: Int ->
            controller?.dispatch(GradeWasChanged(grade))
            intervalsPopup?.dismiss()
        }
        intervalsAdapter = IntervalsAdapter(onItemClick)
        recycler.adapter = intervalsAdapter
        intervalsPopup = PopupWindow(context).apply {
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

    override fun onResume() {
        super.onResume()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.getAsync() ?: return@launch
            diScope.controller.dispatch(FragmentResumed)
        }
        hideActionBar()
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
        walkingModePopup = null
        chooseHintPopup = null
        intervalsAdapter = null
        intervalsPopup = null
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

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            controller?.dispatch(BackButtonClicked)
            return true
        }
    }
}