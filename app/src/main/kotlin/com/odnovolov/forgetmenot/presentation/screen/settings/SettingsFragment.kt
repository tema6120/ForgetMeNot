package com.odnovolov.forgetmenot.presentation.screen.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.entity.FullscreenPreference
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.settings.SettingsEvent.*
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.reflect.KProperty1

class SettingsFragment : BaseFragment() {
    init {
        SettingsDiScope.reopenIfClosed()
    }

    private var controller: SettingsController? = null
    private lateinit var fullscreenModeDialog: Dialog
    private lateinit var fullscreenPreferenceAdapter: ItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initFullscreenModeDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun initFullscreenModeDialog() {
        fullscreenModeDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            itemForm = AsCheckBox,
            takeTitle = { titleTextView: TextView ->
                titleTextView.setText(R.string.title_fullscreen_mode_dialog)
                val titleDrawable = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_round_fullscreen_24
                )
                titleTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    titleDrawable, null, null, null
                )
            },
            onItemClick = { item: Item ->
                item as FullscreenPreferenceItem
                controller?.dispatch(
                    when (item.property) {
                        FullscreenPreference::isEnabledInExercise ->
                            FullscreenInExerciseCheckboxClicked
                        FullscreenPreference::isEnabledInCardPlayer ->
                            FullscreenInRepetitionCheckboxClicked
                        FullscreenPreference::isEnabledInOtherPlaces ->
                            FullscreenInOtherPlacesCheckboxClicked
                        else -> throw AssertionError()
                    }
                )
            },
            takeAdapter = { fullscreenPreferenceAdapter = it }
        )
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = SettingsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        walkingModeSettingsButton.setOnClickListener {
            controller?.dispatch(WalkingModeSettingsButtonClicked)
        }
        fullscreenSettingsSettingsButton.setOnClickListener {
            fullscreenModeDialog.show()
        }
    }

    @ExperimentalStdlibApi
    private fun observeViewModel(viewModel: SettingsViewModel) {
        with(viewModel) {
            fullscreenPreference.observe { fullscreenPreference: FullscreenPreference ->
                (requireActivity() as MainActivity).fullscreenModeManager
                    .setFullscreenMode(fullscreenPreference.isEnabledInOtherPlaces)

                val items = listOf(
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInExercise,
                        text = getString(R.string.item_text_fullscreen_in_exercise),
                        isSelected = fullscreenPreference.isEnabledInExercise
                    ),
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInCardPlayer,
                        text = getString(R.string.item_text_fullscreen_in_card_player),
                        isSelected = fullscreenPreference.isEnabledInCardPlayer
                    ),
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInOtherPlaces,
                        text = getString(R.string.item_text_fullscreen_in_other_places),
                        isSelected = fullscreenPreference.isEnabledInOtherPlaces
                    )
                )
                fullscreenPreferenceAdapter.submitList(items)

                fullscreenSettingsDescription.text = with(fullscreenPreference) {
                    when {
                        isEnabledInOtherPlaces
                                && isEnabledInExercise
                                && isEnabledInCardPlayer -> getString(R.string.everywhere)
                        !isEnabledInOtherPlaces
                                && !isEnabledInExercise
                                && !isEnabledInCardPlayer -> getString(R.string.nowhere)
                        else -> {
                            items.filter { item: Item -> item.isSelected }
                                .joinToString { item: Item ->
                                    item.text.toLowerCase(Locale.ROOT)
                                }.run { capitalize(Locale.ROOT) }
                        }
                    }
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            val dialogSavedState: Bundle? = getBundle(STATE_FULLSCREEN_MODE_DIALOG)
            if (dialogSavedState != null) {
                fullscreenModeDialog.onRestoreInstanceState(dialogSavedState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (fullscreenModeDialog.isShowing) {
            outState.putBundle(
                STATE_FULLSCREEN_MODE_DIALOG,
                fullscreenModeDialog.onSaveInstanceState()
            )
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

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            SettingsDiScope.close()
        }
    }

    data class FullscreenPreferenceItem(
        val property: KProperty1<FullscreenPreference, Boolean>,
        override val text: String,
        override val isSelected: Boolean
    ) : Item

    companion object {
        private const val STATE_FULLSCREEN_MODE_DIALOG = "STATE_FULLSCREEN_MODE_DIALOG"
    }
}