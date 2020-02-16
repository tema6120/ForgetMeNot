package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz.AnswerQuizTestCommand.Vibrate
import com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard.answer.quiz.VariantStatus.*
import kotlinx.android.synthetic.main.fragment_answer_quiz_test.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.getViewModel
import org.koin.core.parameter.parametersOf

class AnswerQuizTestFragment : BaseFragment() {
    companion object {
        private const val ARG_ID = "ARG_ID"
        private const val VIBRATION_DURATION = 50L

        fun create(id: Long) = AnswerQuizTestFragment().apply {
            arguments = Bundle(1).apply {
                putLong(ARG_ID, id)
            }
        }
    }

    private lateinit var viewModel: AnswerQuizTestViewModel
    private lateinit var controller: AnswerQuizTestController
    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id: Long = arguments!!.getLong(ARG_ID)
        val koinScope = getKoin()
            .getOrCreateScope<AnswerQuizTestViewModel>(ANSWER_QUIZ_TEST_SCOPE_ID_PREFIX + id)
        viewModel = koinScope.getViewModel(owner = this, parameters = { parametersOf(id) })
        controller = koinScope.get(parameters = { parametersOf(id) })
        vibrator = getSystemService(requireContext(), Vibrator::class.java)
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
        controller.commands.observe(::executeCommand)
    }

    private fun setupView() {
        variant1Button.setOnClickListener { controller.onVariantSelected(0) }
        variant2Button.setOnClickListener { controller.onVariantSelected(1) }
        variant3Button.setOnClickListener { controller.onVariantSelected(2) }
        variant4Button.setOnClickListener { controller.onVariantSelected(3) }

        variant1TextView.observeSelectedText { controller.onAnswerTextSelectionChanged(it) }
        variant2TextView.observeSelectedText { controller.onAnswerTextSelectionChanged(it) }
        variant3TextView.observeSelectedText { controller.onAnswerTextSelectionChanged(it) }
        variant4TextView.observeSelectedText { controller.onAnswerTextSelectionChanged(it) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            variant1.observe(variant1TextView::setText)
            variant2.observe(variant2TextView::setText)
            variant3.observe(variant3TextView::setText)
            variant4.observe(variant4TextView::setText)

            observeVariantStatus(variant1Status, variant1Button)
            observeVariantStatus(variant2Status, variant2Button)
            observeVariantStatus(variant3Status, variant3Button)
            observeVariantStatus(variant4Status, variant4Button)

            isAnswered.combine(isLearned) { isAnswered, isLearned -> isAnswered to isLearned }
                .observe {
                    val (isAnswered: Boolean, isLearned: Boolean) = it

                    val isClickable = !isAnswered && !isLearned
                    variant1Button.isClickable = isClickable
                    variant2Button.isClickable = isClickable
                    variant3Button.isClickable = isClickable
                    variant4Button.isClickable = isClickable

                    val isTextSelectable = isAnswered && !isLearned
                    variant1TextView.setTextIsSelectable(isTextSelectable)
                    variant2TextView.setTextIsSelectable(isTextSelectable)
                    variant3TextView.setTextIsSelectable(isTextSelectable)
                    variant4TextView.setTextIsSelectable(isTextSelectable)
                }

            isLearned.observe { isLearned: Boolean ->
                variant1Button.isEnabled = !isLearned
                variant2Button.isEnabled = !isLearned
                variant3Button.isEnabled = !isLearned
                variant4Button.isEnabled = !isLearned

                variant1TextView.isEnabled = !isLearned
                variant2TextView.isEnabled = !isLearned
                variant3TextView.isEnabled = !isLearned
                variant4TextView.isEnabled = !isLearned
            }
        }
    }

    private fun observeVariantStatus(source: Flow<VariantStatus>, variantView: View) {
        source.observe { variantStatus ->
            with(variantView) {
                when (variantStatus) {
                    Unselected -> {
                        isSelected = false
                        background = null
                    }
                    Correct -> {
                        isSelected = true
                        background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.correct_answer_selector
                        )
                    }
                    Wrong -> {
                        isSelected = true
                        background = ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.wrong_answer_selector
                        )
                    }
                }
            }
        }
    }

    private fun executeCommand(command: AnswerQuizTestCommand) {
        when (command) {
            Vibrate -> {
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= 26) {
                        it.vibrate(
                            VibrationEffect.createOneShot(
                                VIBRATION_DURATION,
                                VibrationEffect.DEFAULT_AMPLITUDE
                            )
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(VIBRATION_DURATION)
                    }
                }
            }
        }
    }
}