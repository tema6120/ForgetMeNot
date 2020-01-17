package com.odnovolov.forgetmenot.screen.exercise

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.MainActivity
import com.odnovolov.forgetmenot.common.Speaker
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.dp
import com.odnovolov.forgetmenot.common.entity.KeyGesture
import com.odnovolov.forgetmenot.common.entity.KeyGesture.*
import com.odnovolov.forgetmenot.common.getBackgroundResForLevelOfKnowledge
import com.odnovolov.forgetmenot.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.screen.exercise.ExerciseOrder.*
import com.odnovolov.forgetmenot.screen.exercise.IntervalsAdapter.ViewHolder
import com.odnovolov.forgetmenot.screen.exercise.KeyGestureDetector.Gesture.*
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.ExerciseCardFragment
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.android.synthetic.main.item_level_of_knowledge.view.*
import kotlinx.android.synthetic.main.popup_choose_hint.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseFragment : BaseFragment() {

    private val controller = ExerciseController()
    private val viewModel = ExerciseViewModel()
    private lateinit var speaker: Speaker
    private lateinit var chooseHintPopup: PopupWindow
    private lateinit var setLevelOfKnowledgePopup: PopupWindow
    private lateinit var intervalsAdapter: IntervalsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        speaker = Speaker(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            hide()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createLevelOfKnowledgePopup()
        createChooseHintPopup()
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    private fun createLevelOfKnowledgePopup() {
        val onItemClick: (Int) -> Unit = { levelOfKnowledge: Int ->
            controller.dispatch(LevelOfKnowledgeSelected(levelOfKnowledge))
            setLevelOfKnowledgePopup.dismiss()
        }
        intervalsAdapter = IntervalsAdapter(onItemClick)
        val recycler: RecyclerView =
            inflate(context, R.layout.popup_set_level_of_knowledge, null) as RecyclerView
        recycler.adapter = intervalsAdapter
        setLevelOfKnowledgePopup = PopupWindow(context).apply {
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

    private fun createChooseHintPopup() {
        val content = inflate(requireContext(), R.layout.popup_choose_hint, null).apply {
            hintAsQuizButton.setOnClickListener {
                controller.dispatch(HintAsQuizButtonClicked)
                chooseHintPopup.dismiss()
            }
            hintMaskLettersButton.setOnClickListener {
                controller.dispatch(HintMaskLettersButtonClicked)
                chooseHintPopup.dismiss()
            }
        }
        content.measure(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.orders.forEach(::executeOrder)
    }

    private fun setupView() {
        setupViewPagerAdapter()
        setupControlPanel()
        setupWalkingModeIfEnabled()
    }

    private fun setupViewPagerAdapter() {
        exerciseViewPager.adapter = ExerciseCardsAdapter(fragment = this)
        exerciseViewPager.registerOnPageChangeCallback(onPageChangeCallback)
    }

    private fun setupControlPanel() {
        notAskButton.setOnClickListener { controller.dispatch(NotAskButtonClicked) }
        undoButton.setOnClickListener { controller.dispatch(UndoButtonClicked) }
        speakButton.setOnClickListener { controller.dispatch(SpeakButtonClicked) }
        editCardButton.setOnClickListener { controller.dispatch(EditCardButtonClicked) }
        hintButton.setOnClickListener { controller.dispatch(HintButtonClicked) }
        levelOfKnowledgeButton.setOnClickListener {
            controller.dispatch(LevelOfKnowledgeButtonClicked)
        }
    }

    private fun setupWalkingModeIfEnabled() {
        if (viewModel.isWalkingMode) {
            val volumeUpDetectFlags = viewModel.needToDetectVolumeUpGestures
            val volumeUpGestureDetector: KeyGestureDetector? =
                if (with(volumeUpDetectFlags) {
                        !detectSinglePress && !detectDoublePress && !detectLongPress
                    }) null
                else KeyGestureDetector(
                    detectSinglePress = volumeUpDetectFlags.detectSinglePress,
                    detectDoublePress = volumeUpDetectFlags.detectDoublePress,
                    detectLongPress = volumeUpDetectFlags.detectLongPress,
                    coroutineScope = viewScope!!
                ) {
                    val keyGesture: KeyGesture = when (it) {
                        SINGLE_PRESS -> VOLUME_UP_SINGLE_PRESS
                        DOUBLE_PRESS -> VOLUME_UP_DOUBLE_PRESS
                        LONG_PRESS -> VOLUME_UP_LONG_PRESS
                    }
                    controller.dispatch(KeyGestureDetected(keyGesture))
                }
            val volumeDownDetectFlags = viewModel.needToDetectVolumeDownGestures
            val volumeDownGestureDetector: KeyGestureDetector? =
                if (with(volumeDownDetectFlags) {
                        !detectSinglePress && !detectDoublePress && !detectLongPress
                    }) null
                else KeyGestureDetector(
                    detectSinglePress = volumeDownDetectFlags.detectSinglePress,
                    detectDoublePress = volumeDownDetectFlags.detectDoublePress,
                    detectLongPress = volumeDownDetectFlags.detectLongPress,
                    coroutineScope = viewScope!!
                ) {
                    val keyGesture: KeyGesture = when (it) {
                        SINGLE_PRESS -> VOLUME_DOWN_SINGLE_PRESS
                        DOUBLE_PRESS -> VOLUME_DOWN_DOUBLE_PRESS
                        LONG_PRESS -> VOLUME_DOWN_LONG_PRESS
                    }
                    controller.dispatch(KeyGestureDetected(keyGesture))
                }
            val keyEventInterceptor: (KeyEvent) -> Boolean = { event: KeyEvent ->
                when (event.keyCode) {
                    KeyEvent.KEYCODE_VOLUME_UP -> {
                        if (volumeUpGestureDetector == null) {
                            false
                        } else {
                            val isPressed = event.action == KeyEvent.ACTION_DOWN
                            volumeUpGestureDetector.dispatchKeyEvent(isPressed)
                            true
                        }
                    }
                    KeyEvent.KEYCODE_VOLUME_DOWN -> {
                        if (volumeDownGestureDetector == null) {
                            false
                        } else {
                            val isPressed = event.action == KeyEvent.ACTION_DOWN
                            volumeDownGestureDetector.dispatchKeyEvent(isPressed)
                            true
                        }
                    }
                    else -> false
                }
            }
            (activity as MainActivity).keyEventInterceptor = keyEventInterceptor
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            val exerciseCardsAdapter = exerciseViewPager.adapter as ExerciseCardsAdapter
            // we help ViewPager restore its state
            exerciseCardsAdapter.exerciseCardIds = exerciseCardsIdsAtStart
            exerciseCardIds.observe { exerciseCardsAdapter.exerciseCardIds = it }
            isCurrentExerciseCardLearned.observe { isCurrentCardLearned ->
                isCurrentCardLearned ?: return@observe
                notAskButton.visibility = if (isCurrentCardLearned) GONE else VISIBLE
                undoButton.visibility = if (isCurrentCardLearned) VISIBLE else GONE
            }
            isHintButtonVisible.observe { isVisible: Boolean ->
                hintButton.visibility = if (isVisible) VISIBLE else GONE
            }
            levelOfKnowledgeForCurrentCard.observe { levelOfKnowledge: Int? ->
                levelOfKnowledge ?: return@observe
                if (levelOfKnowledge == -1) {
                    levelOfKnowledgeTextView.visibility = GONE
                } else {
                    val backgroundRes = getBackgroundResForLevelOfKnowledge(levelOfKnowledge)
                    levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
                    levelOfKnowledgeTextView.text = levelOfKnowledge.toString()
                    levelOfKnowledgeTextView.visibility = VISIBLE
                }
            }
        }
    }

    private fun executeOrder(order: ExerciseOrder) {
        when (order) {
            MoveToNextPosition -> {
                val nextPosition = exerciseViewPager.currentItem + 1
                exerciseViewPager.setCurrentItem(nextPosition, true)
            }
            MoveToPreviousPosition -> {
                val previousPosition = exerciseViewPager.currentItem - 1
                exerciseViewPager.setCurrentItem(previousPosition, true)
            }
            is Speak -> {
                fragmentScope.launch(Dispatchers.Default) {
                    speaker.speak(order.text, order.language)
                }
            }
            NavigateToEditCard -> {
                findNavController().navigate(R.id.action_exercise_screen_to_edit_card_screen)
            }
            ShowChooseHintPopup -> {
                showChooseHintPopup()
            }
            is ShowLevelOfKnowledgePopup -> {
                showLevelOfKnowledgePopup(order.intervalItems)
            }
            ShowIntervalsAreOffMessage -> {
                Toast.makeText(
                    requireContext(),
                    R.string.toast_text_intervals_are_off,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showChooseHintPopup() {
        val hintButtonLocation = IntArray(2).also { hintButton.getLocationOnScreen(it) }
        val x = hintButtonLocation[0] + (hintButton.width / 2) - (chooseHintPopup.width / 2)
        val y = hintButtonLocation[1] + hintButton.height - 8.dp - chooseHintPopup.height
        chooseHintPopup.showAtLocation(
            hintButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    private fun showLevelOfKnowledgePopup(intervalItems: List<IntervalItem>) {
        intervalsAdapter.intervalItems = intervalItems
        val content = setLevelOfKnowledgePopup.contentView
        content.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val location = IntArray(2)
        levelOfKnowledgeButton.getLocationOnScreen(location)
        val x = location[0] + levelOfKnowledgeButton.width - 8.dp - content.measuredWidth
        val y = location[1] + levelOfKnowledgeButton.height - 8.dp - content.measuredHeight
        setLevelOfKnowledgePopup.showAtLocation(
            levelOfKnowledgeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        exerciseViewPager.adapter = null
        exerciseViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        (setLevelOfKnowledgePopup.contentView as RecyclerView).adapter = null
        if (viewModel.isWalkingMode) {
            (activity as MainActivity).keyEventInterceptor = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
        controller.dispose()
        speaker.shutdown()
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller.dispatch(NewPageBecameSelected(position))
        }
    }
}

class ExerciseCardsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    var exerciseCardIds: List<Long> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun createFragment(position: Int): Fragment {
        val id = exerciseCardIds[position]
        return ExerciseCardFragment.create(id)
    }

    override fun getItemId(position: Int): Long = exerciseCardIds[position]

    override fun containsItem(itemId: Long): Boolean = exerciseCardIds.contains(itemId)

    override fun getItemCount(): Int = exerciseCardIds.size
}

class IntervalsAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {
    var intervalItems: List<IntervalItem> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_level_of_knowledge, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val intervalItem: IntervalItem = intervalItems[position]
        with(holder.itemView) {
            if (intervalItem.isSelected) {
                setBackgroundColor(
                    ContextCompat.getColor(context, R.color.current_level_of_knowledge_background)
                )
            } else {
                background = null
            }
            val backgroundRes =
                getBackgroundResForLevelOfKnowledge(intervalItem.levelOfKnowledge.toInt())
            levelOfKnowledgeTextView.setBackgroundResource(backgroundRes)
            levelOfKnowledgeTextView.text = intervalItem.levelOfKnowledge.toString()
            waitingPeriodTextView.text = intervalItem.waitingPeriod
            setLevelOfKnowledgeButton.setOnClickListener {
                onItemClick(intervalItem.levelOfKnowledge.toInt())
            }
        }
    }

    override fun getItemCount(): Int = intervalItems.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}