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
import com.odnovolov.forgetmenot.common.Speaker
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.dp
import com.odnovolov.forgetmenot.common.getBackgroundResForLevelOfKnowledge
import com.odnovolov.forgetmenot.screen.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.screen.exercise.ExerciseOrder.*
import com.odnovolov.forgetmenot.screen.exercise.IntervalsAdapter.ViewHolder
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.ExerciseCardFragment
import kotlinx.android.synthetic.main.fragment_exercise.*
import kotlinx.android.synthetic.main.item_level_of_knowledge.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseFragment : BaseFragment() {

    private val controller = ExerciseController()
    private val viewModel = ExerciseViewModel()
    private lateinit var speaker: Speaker
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
        setLevelOfKnowledgePopup = createLevelOfKnowledgePopup()
        return inflater.inflate(R.layout.fragment_exercise, container, false)
    }

    private fun createLevelOfKnowledgePopup(): PopupWindow {
        val onItemClick: (Int) -> Unit = { levelOfKnowledge: Int ->
            controller.dispatch(LevelOfKnowledgeSelected(levelOfKnowledge))
            setLevelOfKnowledgePopup.dismiss()
        }
        intervalsAdapter = IntervalsAdapter(onItemClick)
        val recycler: RecyclerView =
            inflate(context, R.layout.popup_set_level_of_knowledge, null) as RecyclerView
        recycler.adapter = intervalsAdapter
        return PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = recycler
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.level_of_knowledge_popup_background
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
        levelOfKnowledgeButton.setOnClickListener {
            controller.dispatch(LevelOfKnowledgeButtonClicked)
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
            is Speak -> {
                fragmentScope.launch(Dispatchers.Default) {
                    speaker.speak(order.text, order.language)
                }
            }
            NavigateToEditCard -> {
                findNavController().navigate(R.id.action_exercise_screen_to_edit_card_screen)
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