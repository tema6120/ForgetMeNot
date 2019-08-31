package com.odnovolov.forgetmenot.decksettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.BaseFragment
import com.odnovolov.forgetmenot.decksettings.DeckSettingsEvent.*
import com.odnovolov.forgetmenot.decksettings.DeckSettingsOrder.NavigateToPronunciation
import com.odnovolov.forgetmenot.decksettings.DeckSettingsOrder.ShowRenameDeckDialog
import kotlinx.android.synthetic.main.fragment_deck_settings.*
import kotlinx.coroutines.launch
import leakcanary.LeakSentry

class DeckSettingsFragment : BaseFragment() {

    private val viewModel = DeckSettingsViewModel()
    private val controller = DeckSettingsController()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        takeOrders()
    }

    private fun setupView() {
        renameDeckLinearLayout.setOnClickListener {
            controller.dispatch(RenameDeckButtonClicked)
        }
        randomOrderLinearLayout.setOnClickListener {
            controller.dispatch(RandomOrderSwitcherClicked)
        }
        pronunciationButton.setOnClickListener {
            controller.dispatch(PronunciationButtonClicked)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            deckName.observe(onChange = deckNameTextView::setText)
            randomOrder.observe(onChange = randomOrderSwitcher::setChecked)
            pronunciationName.observe(onChange = pronunciationTextView::setText)
        }
    }

    private fun takeOrders() {
        fragmentScope.launch {
            for (order in controller.orders) {
                when (order) {
                    ShowRenameDeckDialog -> {
                        Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT)
                            .show()
                    }
                    is NavigateToPronunciation -> {
                        findNavController()
                            .navigate(R.id.action_deck_settings_screen_to_pronunciation_screen)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        LeakSentry.refWatcher.watch(this)
    }
}