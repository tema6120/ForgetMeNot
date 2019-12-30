package com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestEvent.AnswerTextSelectionChanged
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestEvent.VariantSelected
import com.odnovolov.forgetmenot.screen.exercise.exercisecard.answer.quiz.VariantStatus.*
import kotlinx.android.synthetic.main.fragment_answer_quiz_test.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class AnswerQuizTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"

        fun create(id: Long) = AnswerQuizTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var controller: AnswerQuizTestController
    private lateinit var viewModel: AnswerQuizTestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = arguments!!.getLong(ARG_ID)
        controller = AnswerQuizTestController(id)
        viewModel = AnswerQuizTestViewModel(id)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_answer_quiz_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        variant1Button.setOnClickListener { controller.dispatch(VariantSelected(1)) }
        variant2Button.setOnClickListener { controller.dispatch(VariantSelected(2)) }
        variant3Button.setOnClickListener { controller.dispatch(VariantSelected(3)) }
        variant4Button.setOnClickListener { controller.dispatch(VariantSelected(4)) }

        variant1TextView.observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
        variant2TextView.observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
        variant3TextView.observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
        variant4TextView.observeSelectedText { controller.dispatch(AnswerTextSelectionChanged(it)) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            variant1.observe(onChange = variant1TextView::setText)
            variant2.observe(onChange = variant2TextView::setText)
            variant3.observe(onChange = variant3TextView::setText)
            variant4.observe(onChange = variant4TextView::setText)

            observeVariantStatus(variant1Status, variant1Button)
            observeVariantStatus(variant2Status, variant2Button)
            observeVariantStatus(variant3Status, variant3Button)
            observeVariantStatus(variant4Status, variant4Button)

            isAnswered.combine(isLearned) { isAnswered, isLearned -> isAnswered to isLearned }
                .observe {
                    val (isAnswered: Boolean?, isLearned: Boolean?) = it

                    val isClickable = isAnswered == false && isLearned == false
                    variant1Button.isClickable = isClickable
                    variant2Button.isClickable = isClickable
                    variant3Button.isClickable = isClickable
                    variant4Button.isClickable = isClickable

                    val isTextSelectable = isAnswered == true && isLearned == false
                    variant1TextView.setTextIsSelectable(isTextSelectable)
                    variant2TextView.setTextIsSelectable(isTextSelectable)
                    variant3TextView.setTextIsSelectable(isTextSelectable)
                    variant4TextView.setTextIsSelectable(isTextSelectable)
                }

            isLearned.observe { isLearned: Boolean? ->
                val alpha = if (isLearned == true) 0.26f else 1f

                variant1Button.alpha = alpha
                variant2Button.alpha = alpha
                variant3Button.alpha = alpha
                variant4Button.alpha = alpha

                variant1TextView.alpha = alpha
                variant2TextView.alpha = alpha
                variant3TextView.alpha = alpha
                variant4TextView.alpha = alpha
            }
        }
    }

    private fun observeVariantStatus(source: Flow<VariantStatus>, variantView: View) {
        source.observe { variantStatus ->
            when (variantStatus) {
                Unselected -> variantView.background = null
                Correct -> variantView.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.correct_answer)
                )
                Wrong -> variantView.setBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.wrong_answer)
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }
}