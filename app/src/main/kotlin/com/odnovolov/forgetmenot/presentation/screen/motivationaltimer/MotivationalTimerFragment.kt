package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.example.ExampleExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.example.ExampleExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerEvent.*
import kotlinx.android.synthetic.main.fragment_motivational_timer.*
import kotlinx.coroutines.launch

class MotivationalTimerFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        ExampleExerciseDiScope.reopenIfClosed()
        MotivationalTimerDiScope.reopenIfClosed()
    }

    private var controller: MotivationalTimerController? = null
    private lateinit var viewModel: MotivationalTimerViewModel
    private lateinit var exampleFragment: ExampleExerciseFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_motivational_timer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = MotivationalTimerDiScope.getAsync() ?: return@launch
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
        timerSwitchFrame.setOnClickListener {
            controller?.dispatch(TimeForAnswerSwitchToggled)
        }
        timeForAnswerEditText.observeText { text: String ->
            controller?.dispatch(TimeInputChanged(text))
        }
        okButton.setOnClickListener {
            controller?.dispatch(OkButtonClicked)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            timeForAnswerEditText.setText(timeInput)
            isTimerEnabled.observe { isTimerEnabled: Boolean ->
                timerSwitch.run {
                    isChecked = isTimerEnabled
                    timerSwitch.setText(
                        if (isTimerEnabled)
                            R.string.on else
                            R.string.off
                    )
                    uncover()
                }
                secTextView.isEnabled = isTimerEnabled
                timeForAnswerEditText.run {
                    isEnabled = isTimerEnabled
                    if (isTimerEnabled) {
                        selectAll()
                        showSoftInput()
                    } else {
                        setSelection(0)
                    }
                }
            }
            isOkButtonVisible.observe { isVisible: Boolean ->
                okButton.isVisible = isVisible
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
        timeForAnswerEditText.hideSoftInput()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        behavior.removeBottomSheetCallback(bottomSheetCallback)
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            MotivationalTimerDiScope.close()
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