package com.odnovolov.forgetmenot.presentation.screen.cardinversion

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.CardInversion
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.cardinversion.CardInversionEvent.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseFragment
import kotlinx.android.synthetic.main.fragment_card_inversion.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.android.synthetic.main.tip.view.*
import kotlinx.coroutines.launch

class CardInversionFragment : BaseFragment() {
    init {
        CardInversionDiScope.reopenIfClosed()
    }

    private var controller: CardInversionController? = null
    private lateinit var viewModel: CardInversionViewModel
    private lateinit var exampleFragment: ExampleExerciseFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_card_inversion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardInversionDiScope.getAsync() ?: return@launch
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
        offButton.setOnClickListener {
            controller?.dispatch(OffRadioButtonClicked)
        }
        onButton.setOnClickListener {
            controller?.dispatch(OnRadioButtonClicked)
        }
        everyOtherLapButton.setOnClickListener {
            controller?.dispatch(EveryOtherLapRadioButtonClicked)
        }
        randomlyButton.setOnClickListener {
            controller?.dispatch(RandomlyButtonClicked)
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
            cardInversion.observe { cardInversion: CardInversion ->
                offRadioButton.isChecked = cardInversion == CardInversion.Off
                offRadioButton.uncover()
                onRadioButton.isChecked = cardInversion == CardInversion.On
                onRadioButton.uncover()
                everyOtherLapRadioButton.isChecked = cardInversion == CardInversion.EveryOtherLap
                everyOtherLapRadioButton.uncover()
                randomlyRadioButton.isChecked = cardInversion == CardInversion.Randomly
                randomlyRadioButton.uncover()
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
        if (isFinishing()) {
            CardInversionDiScope.close()
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