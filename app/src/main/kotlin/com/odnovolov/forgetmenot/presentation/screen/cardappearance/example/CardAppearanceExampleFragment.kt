package com.odnovolov.forgetmenot.presentation.screen.cardappearance.example

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceController
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearanceDiScope
import kotlinx.android.synthetic.main.fragment_card_appearance_example.*
import kotlinx.coroutines.launch

class CardAppearanceExampleFragment : BaseFragment() {
    init {
        CardAppearanceDiScope.reopenIfClosed()
    }

    private var controller: CardAppearanceController? = null
    private val cardAdapter = CardAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_card_appearance_example, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardAppearanceDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.exampleViewModel)
        }
    }

    private fun setupView() {
        cardAppearanceExampleViewPager.adapter = cardAdapter
        cardAppearanceExampleViewPager.children.find { it is RecyclerView }?.let {
            (it as RecyclerView).isNestedScrollingEnabled = false
        }
    }

    private fun observeViewModel(viewModel: CardAppearanceExampleViewModel) {
        with(viewModel) {
            cardAdapter.items = exampleCards
            questionTextAlignment.observe(cardAdapter::questionTextAlignment::set)
            questionTextSize.observe(cardAdapter::questionTextSize::set)
            answerTextAlignment.observe(cardAdapter::answerTextAlignment::set)
            answerTextSize.observe(cardAdapter::answerTextSize::set)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun notifyBottomSheetStateChanged(newState: Int) {
        when (newState) {
            BottomSheetBehavior.STATE_EXPANDED -> {
                blocker.setOnTouchListener(null)
                exampleTextView.isVisible = false
            }
            BottomSheetBehavior.STATE_COLLAPSED -> {
                blocker.setOnTouchListener { _, _ -> true }
                exampleTextView.isVisible = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardAppearanceExampleViewPager.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            CardAppearanceDiScope.close()
        }
    }
}