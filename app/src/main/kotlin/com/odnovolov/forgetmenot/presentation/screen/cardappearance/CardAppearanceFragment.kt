package com.odnovolov.forgetmenot.presentation.screen.cardappearance

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceController.Command.ShowTextSizeDialog
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceEvent.*
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.example.CardAppearanceExampleFragment
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.textsizedialog.CardTextSizeDialog
import kotlinx.android.synthetic.main.fragment_card_appearance.*
import kotlinx.coroutines.launch

class CardAppearanceFragment : BaseFragment() {
    init {
        CardAppearanceDiScope.reopenIfClosed()
    }

    private var controller: CardAppearanceController? = null
    private lateinit var exampleFragment: CardAppearanceExampleFragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_card_appearance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardAppearanceDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            val viewModel = diScope.viewModel
            observeViewModel(viewModel)
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        exampleFragment = childFragmentManager.findFragmentByTag("CardAppearanceExampleFragment")
                as CardAppearanceExampleFragment
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        alignQuestionToEdgeButton.setOnClickListener {
            controller?.dispatch(AlignQuestionToEdgeButtonClicked)
        }
        alignQuestionToCenterButton.setOnClickListener {
            controller?.dispatch(AlignQuestionToCenterButtonClicked)
        }
        questionTextSizeButton.setOnClickListener {
            controller?.dispatch(QuestionTextSizeButtonClicked)
        }
        alignAnswerToEdgeButton.setOnClickListener {
            controller?.dispatch(AlignAnswerToEdgeButtonClicked)
        }
        alignAnswerToCenterButton.setOnClickListener {
            controller?.dispatch(AlignAnswerToCenterButtonClicked)
        }
        answerTextSizeButton.setOnClickListener {
            controller?.dispatch(AnswerTextSizeButtonClicked)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel(viewModel: CardAppearanceViewModel) {
        with(viewModel) {
            questionTextAlignment.observe { questionTextAlignment: CardTextAlignment ->
                alignQuestionToEdgeButton.isSelected =
                    questionTextAlignment == CardTextAlignment.Edge
                alignQuestionToCenterButton.isSelected =
                    questionTextAlignment == CardTextAlignment.Center
            }
            questionTextSize.observe { questionTextSize: Int ->
                questionTextSizeButton.text = "$questionTextSize sp"
            }
            answerTextAlignment.observe { answerTextAlignment: CardTextAlignment ->
                alignAnswerToEdgeButton.isSelected =
                    answerTextAlignment == CardTextAlignment.Edge
                alignAnswerToCenterButton.isSelected =
                    answerTextAlignment == CardTextAlignment.Center
            }
            answerTextSize.observe { answerTextSize: Int ->
                answerTextSizeButton.text = "$answerTextSize sp"
            }
        }
    }

    private fun executeCommand(command: CardAppearanceController.Command) {
        when (command) {
            ShowTextSizeDialog ->
                CardTextSizeDialog().show(childFragmentManager, "CardTextSizeDialog")
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
            CardAppearanceDiScope.close()
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
            screenFrame.alpha = 1f - slideOffset
            exampleFragment.notifyBottomSheetSlideOffsetChanged(slideOffset)
        }
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        when {
            behavior.state != BottomSheetBehavior.STATE_COLLAPSED -> {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                true
            }
            else -> {
                false
            }
        }
    }
}