package com.odnovolov.forgetmenot.presentation.screen.decksettings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.Action.ShowRenameDeckDialog
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.Event.RandomOrderSwitcherClicked
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsViewModel.Event.RenameDeckButtonClicked
import kotlinx.android.synthetic.main.fragment_deck_settings.*

class DeckSettingsFragment : Fragment() {

    private lateinit var viewModel: DeckSettingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = DeckSettingsInjector.viewModel(this)
    }

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
        subscribeToViewModel()
    }

    private fun setupView() {
        renameDeckLinearLayout.setOnClickListener {
            viewModel.onEvent(RenameDeckButtonClicked)
        }
        randomOrderLinearLayout.setOnClickListener {
            viewModel.onEvent(RandomOrderSwitcherClicked)
        }
    }

    private fun subscribeToViewModel() {
        with(viewModel.state) {
            deckName.observe(this@DeckSettingsFragment, Observer { deckName ->
                deckNameTextView.text = deckName
            })
            randomOrder.observe(this@DeckSettingsFragment, Observer { randomOrder ->
                randomOrderSwitcher.isChecked = randomOrder
            })
        }

        viewModel.action!!.observe(this, Observer { action ->
            when (action) {
                is ShowRenameDeckDialog -> Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

}