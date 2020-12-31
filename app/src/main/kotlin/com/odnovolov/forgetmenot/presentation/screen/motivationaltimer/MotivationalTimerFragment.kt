package com.odnovolov.forgetmenot.presentation.screen.motivationaltimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.motivationaltimer.MotivationalTimerEvent.*
import kotlinx.android.synthetic.main.fragment_motivational_timer.*
import kotlinx.coroutines.launch

class MotivationalTimerFragment : BaseFragment() {
    init {
        MotivationalTimerDiScope.reopenIfClosed()
    }

    private var controller: MotivationalTimerController? = null
    private lateinit var viewModel: MotivationalTimerViewModel

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
    }

    override fun onPause() {
        super.onPause()
        timeForAnswerEditText.hideSoftInput()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
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
}