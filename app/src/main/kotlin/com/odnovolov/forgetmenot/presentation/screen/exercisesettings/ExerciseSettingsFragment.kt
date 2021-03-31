package com.odnovolov.forgetmenot.presentation.screen.exercisesettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
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
        alwaysShowCardFilterButton.setOnClickListener {
            controller?.dispatch(AlwaysShowCardFilterButtonClicked)
        }
        conditionallyShowCardFilterButton.setOnClickListener {
            controller?.dispatch(ConditionallyShowCardFilterButtonClicked)
            CardsThresholdForShowingFilterDialog()
                .show(childFragmentManager, "CardsThresholdForShowingFilterDialog")
        }
        neverShowCardFilterButton.setOnClickListener {
            controller?.dispatch(NeverShowCardFilterButtonClicked)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            cardFilterDisplay.observe { cardFilterDisplay: CardFilterDisplay ->
                alwaysShowCardFilterButton.isSelected =
                    cardFilterDisplay is CardFilterDisplay.Always
                conditionallyShowCardFilterButton.isSelected =
                    cardFilterDisplay is CardFilterDisplay.WhenCardsMoreThan
                if (cardFilterDisplay is CardFilterDisplay.WhenCardsMoreThan) {
                    conditionallyShowCardFilterButton.text = getString(
                        R.string.exercise_setting_show_card_filter_when_cards_more_than,
                        cardFilterDisplay.numberOfCards
                    )
                }
                neverShowCardFilterButton.isSelected =
                    cardFilterDisplay is CardFilterDisplay.Never
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            ExerciseSettingsDiScope.close()
        }
    }
}