package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettings.Companion.DEFAULT_CARD_NUMBER_LIMITATION
import com.odnovolov.forgetmenot.presentation.screen.exercisesettings.ExerciseSettingsEvent.*
import kotlinx.android.synthetic.main.fragment_exercise_settings.*
import kotlinx.coroutines.launch

class ExerciseSettingsFragment : BaseFragment() {
    init {
        ExerciseSettingsDiScope.reopenIfClosed()
    }

    private var controller: ExerciseSettingsController? = null
    private lateinit var viewModel: ExerciseSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exercise_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = ExerciseSettingsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        doNotFilterButton.setOnClickListener {
            controller?.dispatch(DoNotFilterButtonClicked)
        }
        limitCardsButton.setOnClickListener {
            controller?.dispatch(LimitCardsButtonClicked)
        }
        conditionallyShowCardFilterButton.setOnClickListener {
            controller?.dispatch(ConditionallyShowCardFilterButtonClicked)
        }
        alwaysShowCardFilterButton.setOnClickListener {
            controller?.dispatch(AlwaysShowCardFilterButtonClicked)
        }
        showProgressBarButton.setOnClickListener {
            controller?.dispatch(ShowProgressBarButtonClicked)
        }
        showTextOfCardPositionButton.setOnClickListener {
            controller?.dispatch(ShowTextOfCardPositionButtonClicked)
        }
        yesVibrateOnWrongAnswerButton.setOnClickListener {
            controller?.dispatch(YesVibrateOnWrongAnswerButtonClicked)
        }
        noVibrateOnWrongAnswerButton.setOnClickListener {
            controller?.dispatch(NoVibrateOnWrongAnswerButtonClicked)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            cardPrefilterMode.observe { cardPrefilterMode: CardPrefilterMode ->
                doNotFilterButton.isSelected = cardPrefilterMode is CardPrefilterMode.DoNotFilter
                limitCardsButton.isSelected = cardPrefilterMode is CardPrefilterMode.LimitCardsTo
                conditionallyShowCardFilterButton.isSelected =
                    cardPrefilterMode is CardPrefilterMode.ShowFilterWhenCardsMoreThan
                alwaysShowCardFilterButton.isSelected =
                    cardPrefilterMode is CardPrefilterMode.AlwaysShowFilter

                val cardNumberLimitation: Int =
                    if (cardPrefilterMode is CardPrefilterMode.LimitCardsTo) {
                        cardPrefilterMode.numberOfCards
                    } else {
                        DEFAULT_CARD_NUMBER_LIMITATION
                    }
                limitCardsButton.text = resources.getQuantityString(
                    R.plurals.card_prefilter_mode_limit_cards_to,
                    cardNumberLimitation,
                    cardNumberLimitation
                )

                val cardNumberLimitationToShowFilter: Int =
                    if (cardPrefilterMode is CardPrefilterMode.ShowFilterWhenCardsMoreThan) {
                        cardPrefilterMode.numberOfCards
                    } else {
                        DEFAULT_CARD_NUMBER_LIMITATION
                    }
                conditionallyShowCardFilterButton.text = getString(
                    R.string.card_prefilter_mode_show_filter_when_cards_more_than,
                    cardNumberLimitationToShowFilter
                )
            }
            showProgressBar.observe(showProgressBarButton::setSelected)
            showTextOfCardPosition.observe(showTextOfCardPositionButton::setSelected)
            vibrateOnWrongAnswer.observe { vibrateOnWrongAnswer: Boolean ->
                yesVibrateOnWrongAnswerButton.isSelected = vibrateOnWrongAnswer
                noVibrateOnWrongAnswerButton.isSelected = !vibrateOnWrongAnswer
            }
        }
    }

    override fun onResume() {
        super.onResume()
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            ExerciseSettingsDiScope.close()
        }
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }
}