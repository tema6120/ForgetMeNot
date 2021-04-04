package com.odnovolov.forgetmenot.presentation.screen.grading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnCorrectAnswer
import com.odnovolov.forgetmenot.domain.entity.GradeChangeOnWrongAnswer
import com.odnovolov.forgetmenot.presentation.common.addBottomSheetCallbackWithInitialNotification
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.getGradeChangeDisplayText
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.grading.GradingEvent.*
import kotlinx.android.synthetic.main.fragment_grading.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.android.synthetic.main.tip.view.*
import kotlinx.coroutines.launch

class GradingFragment : BaseFragment() {
    init {
        GradingDiScope.reopenIfClosed()
    }

    private var controller: GradingController? = null
    private lateinit var viewModel: GradingViewModel
    private lateinit var exampleFragment: ExampleExerciseFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_grading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = GradingDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        exampleFragment = (childFragmentManager.findFragmentByTag("ExampleExerciseFragment")
                as ExampleExerciseFragment)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        helpButton.setOnClickListener {
            controller?.dispatch(HelpButtonClicked)
        }
        firstCorrectAnswerButton.setOnClickListener {
            controller?.dispatch(FirstCorrectAnswerButtonClicked)
        }
        firstWrongAnswerButton.setOnClickListener {
            controller?.dispatch(FirstWrongAnswerButtonClicked)
        }
        yesAskAgainButton.setOnClickListener {
            controller?.dispatch(YesAskAgainButtonClicked)
        }
        noAskAgainButton.setOnClickListener {
            controller?.dispatch(NoAskAgainButtonClicked)
        }
        repeatedCorrectAnswerButton.setOnClickListener {
            controller?.dispatch(RepeatedCorrectAnswerButtonClicked)
        }
        repeatedWrongAnswerButton.setOnClickListener {
            controller?.dispatch(RepeatedWrongAnswerButtonClicked)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            tip.observe { tip: Tip? ->
                if (tip != null) {
                    if (tipStub != null) {
                        tipStub.inflate()
                        closeTipButton.setOnClickListener {
                            controller?.dispatch(CloseTipButtonClicked)
                        }
                    }
                    val tipLayout = rootView.findViewById<View>(R.id.tipLayout)
                    tipLayout.tipTextView.setText(tip.stringId)
                    tipLayout.isVisible = true
                } else {
                    if (tipStub == null) {
                        val tipLayout = rootView.findViewById<View>(R.id.tipLayout)
                        tipLayout.isVisible = false
                    }
                }
            }
            onFirstCorrectAnswer.observe { gradeChange: GradeChangeOnCorrectAnswer ->
                onFirstCorrectAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
            onFirstWrongAnswer.observe { gradeChange: GradeChangeOnWrongAnswer ->
                onFirstWrongAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
            askAgain.observe { askAgain: Boolean ->
                yesAskAgainButton.isSelected = askAgain
                noAskAgainButton.isSelected = !askAgain
                onRepeatedAnswerGroup.isVisible = askAgain
            }
            onRepeatedCorrectAnswer.observe { gradeChange: GradeChangeOnCorrectAnswer ->
                onRepeatedCorrectAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
            onRepeatedWrongAnswer.observe { gradeChange: GradeChangeOnWrongAnswer ->
                onRepeatedWrongAnswerValueTextView.text =
                    getGradeChangeDisplayText(gradeChange, requireContext())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        exampleFragmentContainerView
            .addBottomSheetCallbackWithInitialNotification(bottomSheetCallback)
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        behavior.removeBottomSheetCallback(bottomSheetCallback)
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            GradingDiScope.close()
        }
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            exampleFragment.notifyBottomSheetStateChanged(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            exampleFragment.notifyBottomSheetSlideOffsetChanged(slideOffset)
            screenFrame.alpha = 1f - slideOffset
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        if (behavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            true
        } else {
            false
        }
    }
}