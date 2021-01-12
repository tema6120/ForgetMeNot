package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import android.animation.LayoutTransition
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.*
import kotlinx.android.synthetic.main.fragment_question_display.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.android.synthetic.main.tip.view.*
import kotlinx.coroutines.launch

class QuestionDisplayFragment : BaseFragment() {
    init {
        QuestionDisplayDiScope.reopenIfClosed()
    }

    private var controller: QuestionDisplayController? = null
    private lateinit var viewModel: QuestionDisplayViewModel
    private lateinit var exampleFragment: ExampleExerciseFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_question_display, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = QuestionDisplayDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        contentConstraintLayout.layoutTransition.enableTransitionType(LayoutTransition.CHANGING)
        exampleFragment = childFragmentManager.findFragmentByTag("ExampleExerciseFragment")
                as ExampleExerciseFragment
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        helpButton.setOnClickListener {
            controller?.dispatch(HelpButtonClicked)
        }
        questionDisplayFrame.setOnClickListener {
            controller?.dispatch(QuestionDisplaySwitchToggled)
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
            isQuestionDisplayed.observe { isQuestionDisplayed: Boolean ->
                questionDisplaySwitch.text = getString(
                    if (isQuestionDisplayed)
                        R.string.on else
                        R.string.off
                )
                questionDisplaySwitch.isChecked = isQuestionDisplayed
                questionDisplaySwitch.uncover()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        behavior.addBottomSheetCallback(bottomSheetCallback)
        exampleFragment.notifyBottomSheetStateChanged(behavior.state)
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
        if (needToCloseDiScope()) {
            QuestionDisplayDiScope.close()
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