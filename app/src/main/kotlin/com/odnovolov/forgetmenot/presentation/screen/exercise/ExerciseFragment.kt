package com.odnovolov.forgetmenot.presentation.screen.exercise

import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.View.GONE
import android.view.View.MeasureSpec
import android.widget.PopupWindow
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
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.KeyGestureDetector.Gesture
import com.odnovolov.forgetmenot.presentation.screen.exercise.KeyGestureDetector.Gesture.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.entry.EntryTestExerciseCardViewHolder
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.NO_ACTION
import kotlinx.android.synthetic.main.dialog_exit_from_exercise.*
import kotlinx.android.synthetic.main.dialog_exit_from_exercise.view.*
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.android.synthetic.main.popup_choose_hint.view.*
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
    private var levelOfKnowledgePopup: PopupWindow? = null
    private var exitDialog: AlertDialog? = null
    private lateinit var keyEventInterceptor: (KeyEvent) -> Boolean
    private lateinit var volumeUpGestureDetector: KeyGestureDetector
    private lateinit var volumeDownGestureDetector: KeyGestureDetector

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
            val diScope = ExerciseDiScope.get()
            controller = diScope.controller
            exerciseViewPager.adapter = diScope.getExerciseCardAdapter(viewCoroutineScope!!)
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
        // We try to show ui as fast as possible.
        // Therefore, we init secondary things (dialogs, popups) late
        Looper.myQueue().addIdleHandler {
            // give window of 500 ms so as not to delay adding ViewPager's views (they are inflated asynchronously)
            Handler().postDelayed(::initSecondaryThings, 500)
            false
        }
    }

    private fun setupView() {
        exerciseViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        editCardButton.run {
            setOnClickListener { controller?.dispatch(EditCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        walkingModeButton.run {
            setOnClickListener { showWalkingModePopup() }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        hintButton.run {
            setOnClickListener { controller?.dispatch(HintButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        timerButton.run {
            setOnClickListener { controller?.dispatch(TimerButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        levelOfKnowledgeButton.run {
            setOnClickListener { controller?.dispatch(LevelOfKnowledgeButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
    }

    private fun showWalkingModePopup() {
        if (walkingModePopup == null) return
        val walkingModeButtonLocation =
            IntArray(2).also { walkingModeButton.getLocationOnScreen(it) }
        val x =
            walkingModeButtonLocation[0] + (walkingModeButton.width / 2) - (walkingModePopup!!.width / 2)
        val y =
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
            isWalkingModeEnabled.observe { isEnabled: Boolean ->
                walkingModeButton.isActivated = isEnabled
                (activity as MainActivity).keyEventInterceptor =
                    if (isEnabled) keyEventInterceptor else null
            }
            isHintAccessible.observe { isHintAccessible: Boolean ->
                hintButton.isVisible = isHintAccessible
            }
            timeLeft.observe { timeLeft: Int ->
                timerButton.isVisible = timeLeft > 0
            }
            levelOfKnowledgeForCurrentCard.observe { levelOfKnowledge: Int ->
                val backgroundRes = getBackgroundResForLevelOfKnowledge(levelOfKnowledge)
                levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
                levelOfKnowledgeTextView.text = levelOfKnowledge.toString()
            }
            isLevelOfKnowledgeEditedManually.observe { isEdited: Boolean ->
                with(levelOfKnowledgeTextView) {
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
            ShowChooseHintPopup -> {
                showChooseHintPopup()
            }
            is ShowLevelOfKnowledgePopup -> {
                showLevelOfKnowledgePopup(command.intervalItems)
            }
            ShowIntervalsAreOffMessage -> {
                showToast(R.string.toast_text_intervals_are_off)
            }
            is ShowThereAreUnansweredCardsMessage -> {
                showExitDialog(command.unansweredCardCount)
            }
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

    private fun showLevelOfKnowledgePopup(intervalItems: List<IntervalItem>) {
        if (levelOfKnowledgePopup == null) return
        intervalsAdapter!!.intervalItems = intervalItems
        val content = levelOfKnowledgePopup!!.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        levelOfKnowledgeButton.getLocationOnScreen(location)
        val x = location[0] + levelOfKnowledgeButton.width - 8.dp - content.measuredWidth
        val y = location[1] + levelOfKnowledgeButton.height - 8.dp - content.measuredHeight
        levelOfKnowledgePopup!!.showAtLocation(
            levelOfKnowledgeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
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
        createLevelOfKnowledgePopup()
        createExitDialog()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    private fun createWalkingModePopup() {
        val content = View.inflate(requireContext(), R.layout.popup_walking_mode, null)
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.get()
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
                showToast("Not implemented yet")
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
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.exercise_control_panel_popup_background
                    )
                )
            )
            elevation = 20f
            isOutsideTouchable = true
            isFocusable = true
        }
    }

    private fun createChooseHintPopup() {
        val content = View.inflate(requireContext(), R.layout.popup_choose_hint, null).apply {
            hintAsQuizButton.setOnClickListener {
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
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.exercise_control_panel_popup_background
                    )
                )
            )
            elevation = 20f
            isOutsideTouchable = true
            isFocusable = true
        }
    }

    private fun createLevelOfKnowledgePopup() {
        val recycler: RecyclerView =
            View.inflate(context, R.layout.popup_set_level_of_knowledge, null) as RecyclerView
        val onItemClick: (Int) -> Unit = { levelOfKnowledge: Int ->
            controller?.dispatch(LevelOfKnowledgeSelected(levelOfKnowledge))
            levelOfKnowledgePopup?.dismiss()
        }
        intervalsAdapter = IntervalsAdapter(onItemClick)
        recycler.adapter = intervalsAdapter
        levelOfKnowledgePopup = PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = recycler
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.exercise_control_panel_popup_background
                    )
                )
            )
            elevation = 20f
            isOutsideTouchable = true
            isFocusable = true
        }
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
            val diScope = ExerciseDiScope.get()
            diScope.controller.dispatch(FragmentResumed)
        }
        hideActionBar()
    }

    override fun onPause() {
        super.onPause()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseDiScope.get()
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
        levelOfKnowledgePopup = null
        exitDialog = null
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
    }

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            controller?.dispatch(BackButtonClicked)
            return true
        }
    }
}