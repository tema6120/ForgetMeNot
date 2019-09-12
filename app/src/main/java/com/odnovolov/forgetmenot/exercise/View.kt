package com.odnovolov.forgetmenot.exercise

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.Speaker
import com.odnovolov.forgetmenot.common.entity.TestMethod
import com.odnovolov.forgetmenot.common.entity.TestMethod.Manual
import com.odnovolov.forgetmenot.common.entity.TestMethod.Off
import com.odnovolov.forgetmenot.exercise.ExerciseEvent.*
import com.odnovolov.forgetmenot.exercise.ExerciseOrder.MoveToNextPosition
import com.odnovolov.forgetmenot.exercise.ExerciseOrder.Speak
import com.odnovolov.forgetmenot.exercise.exercisecard.manualtestmethod.ExerciseCardManualTestMethodFragment
import com.odnovolov.forgetmenot.exercise.exercisecard.withouttest.ExerciseCardWithoutTestFragment
import kotlinx.android.synthetic.main.fragment_exercise.*
import leakcanary.LeakSentry

class ExerciseFragment : BaseFragment() {

    private val controller = ExerciseController()
    private val viewModel = ExerciseViewModel()
    private lateinit var speaker: Speaker
    private lateinit var adapter: ExerciseCardsAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)
        speaker = Speaker(requireContext())
    }

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
        observeViewModel()
        controller.orders.forEach(viewScope!!, ::executeOrder)
    }

    private fun setupView() {
        setupViewPagerAdapter()
        setupControlPanel()
    }

    private fun setupViewPagerAdapter() {
        adapter = ExerciseCardsAdapter(
            testMethod = viewModel.testMethod,
            fragment = this
        )
        exerciseViewPager.adapter = adapter
        exerciseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                controller.dispatch(NewPageBecameSelected(position))
            }
        })
    }

    private fun setupControlPanel() {
        notAskButton.setOnClickListener { controller.dispatch(NotAskButtonClicked) }
        undoButton.setOnClickListener { controller.dispatch(UndoButtonClicked) }
        speakButton.setOnClickListener { controller.dispatch(SpeakButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            // we help ViewPager to restore its state
            adapter.exerciseCardIds = exerciseCardsIdsAtStart
            exerciseCardIds.observe { adapter.exerciseCardIds = it }
            isCurrentExerciseCardLearned.observe { isCurrentCardLearned ->
                isCurrentCardLearned ?: return@observe
                notAskButton.visibility = if (isCurrentCardLearned) GONE else VISIBLE
                undoButton.visibility = if (isCurrentCardLearned) VISIBLE else GONE
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
                speaker.speak(order.text, order.language)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        speaker.shutdown()
        LeakSentry.refWatcher.watch(this)
    }
}


class ExerciseCardsAdapter(
    private val testMethod: TestMethod,
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    var exerciseCardIds: List<Long> = emptyList()
        set(value) {
            if (value != field) {
                field = value
                notifyDataSetChanged()
            }
        }

    override fun createFragment(position: Int): Fragment {
        val exerciseCardId = exerciseCardIds[position]
        return when (testMethod) {
            Off -> ExerciseCardWithoutTestFragment.create(exerciseCardId)
            Manual -> ExerciseCardManualTestMethodFragment.create(exerciseCardId)
        }
    }

    override fun getItemId(position: Int): Long = exerciseCardIds[position]

    override fun containsItem(itemId: Long): Boolean = exerciseCardIds.contains(itemId)

    override fun getItemCount(): Int = exerciseCardIds.size
}