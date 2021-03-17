package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerEvent.*
import kotlinx.android.synthetic.main.fragment_motivational_timer.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.android.synthetic.main.tip.view.*
import kotlinx.coroutines.launch

class MotivationalTimerFragment : BaseFragment() {
    init {
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
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        exampleFragment = (childFragmentManager.findFragmentByTag("ExampleExerciseFragment")
                as ExampleExerciseFragment)
        backButton.setOnClickListener {
            controller?.dispatch(BackButtonClicked)
        }
        helpButton.setOnClickListener {
            controller?.dispatch(HelpButtonClicked)
        }
        timerSwitchFrame.setOnClickListener {
            savedTextView.isVisible = false
            controller?.dispatch(TimeForAnswerSwitchToggled)
        }
        timeForAnswerEditText.observeText { text: String ->
            savedTextView.isVisible = false
            controller?.dispatch(TimeInputChanged(text))
        }
        timeForAnswerEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                controller?.dispatch(OkButtonClicked)
                true
            } else {
                false
            }
        }
        okButton.setOnClickListener {
            controller?.dispatch(OkButtonClicked)
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
                    if (isTimerEnabled) {
                        isEnabled = true
                        selectAll()
                        showSoftInput()
                    } else {
                        setSelection(0)
                        hideSoftInput()
                        isEnabled = false
                    }
                }
            }
            isOkButtonVisible.observe { isVisible: Boolean ->
                okButton.isVisible = isVisible
            }
        }
    }

    private fun executeCommand(command: MotivationalTimerController.Command) {
        when (command) {
            ShowInvalidEntryError -> {
                timeForAnswerEditText.error = getString(R.string.error_motivational_timer_input)
            }
            ShowSavedMessage -> {
                savedTextView.alpha = 1f
                savedTextView.isVisible = true
                savedTextView.animate()
                    .setStartDelay(1000)
                    .setDuration(1500)
                    .alpha(0f)
                    .start()
                timeForAnswerEditText.hideSoftInput()
                timeForAnswerEditText.clearFocus()
            }
            AskUserToSaveChanges -> {
                QuitMotivationalTimerBottomSheet().show(
                    childFragmentManager,
                    "QuitMotivationalTimerBottomSheet"
                )
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
        timeForAnswerEditText.hideSoftInput()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        behavior.removeBottomSheetCallback(bottomSheetCallback)
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
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
            if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                appBar.requestFocus()
            }
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
        } else {
            controller?.dispatch(BackButtonClicked)
        }
        true
    }
}