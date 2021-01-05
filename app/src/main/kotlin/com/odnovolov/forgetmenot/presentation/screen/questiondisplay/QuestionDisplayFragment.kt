package com.odnovolov.forgetmenot.presentation.screen.questiondisplay

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.HelpButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.questiondisplay.QuestionDisplayEvent.QuestionDisplaySwitchToggled
import kotlinx.android.synthetic.main.fragment_question_display.*
import kotlinx.coroutines.launch

class QuestionDisplayFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        ExampleExerciseDiScope.reopenIfClosed()
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

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
            return if (behavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                true
            } else {
                false
            }
        }
    }
}