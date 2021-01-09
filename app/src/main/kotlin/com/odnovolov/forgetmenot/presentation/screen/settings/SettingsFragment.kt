package com.odnovolov.forgetmenot.presentation.screen.settings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.widget.TooltipCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.dp
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initFullscreenModeDialog()
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    private fun initFullscreenModeDialog() {
        fullscreenModeDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_fullscreen_mode_dialog),
            itemForm = AsCheckBox,
            onItemClick = { item: Item ->
                item as FullscreenPreferenceItem
                controller?.dispatch(
                    when (item.property) {
                        FullscreenPreference::isEnabledInHomeAndSettings ->
                            FullscreenInHomeAndSettingsCheckboxClicked
                        FullscreenPreference::isEnabledInExercise ->
                            FullscreenInExerciseCheckboxClicked
                        FullscreenPreference::isEnabledInRepetition ->
                            FullscreenInRepetitionCheckboxClicked
                        else -> throw AssertionError()
                    }
                )
            },
            takeAdapter = { fullscreenPreferenceAdapter = it }
        )
        dialogTimeCapsule.register("fullscreenModeDialog", fullscreenModeDialog)
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
        requireView().viewTreeObserver.addOnGlobalLayoutListener(
            object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    requireView().viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val rootViewWidth = requireView().width
                    walkingModeSettingsTitle.maxWidth = rootViewWidth - 96.dp
                    walkingModeSettingsDescription.maxWidth = rootViewWidth - 96.dp
                }
            })
        walkingModeSettingsButton.setOnClickListener {
            controller?.dispatch(WalkingModeSettingsButton)
        }
        walkingModeHelpButton.run {
            setOnClickListener { controller?.dispatch(WalkingModeHelpButton) }
            TooltipCompat.setTooltipText(this, contentDescription)
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
                    .setFullscreenMode(fullscreenPreference.isEnabledInHomeAndSettings)

                val items = listOf(
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInHomeAndSettings,
                        text = getString(R.string.item_text_fullscreen_in_other_places),
                        isSelected = fullscreenPreference.isEnabledInHomeAndSettings
                    ),
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInExercise,
                        text = getString(R.string.item_text_fullscreen_in_exercise),
                        isSelected = fullscreenPreference.isEnabledInExercise
                    ),
                    FullscreenPreferenceItem(
                        property = FullscreenPreference::isEnabledInRepetition,
                        text = getString(R.string.item_text_fullscreen_in_player),
                        isSelected = fullscreenPreference.isEnabledInRepetition
                    )
                )
                fullscreenPreferenceAdapter.submitList(items)

                fullscreenSettingsDescription.text = with(fullscreenPreference) {
                    when {
                        isEnabledInHomeAndSettings
                                && isEnabledInExercise
                                && isEnabledInRepetition -> getString(R.string.everywhere)
                        !isEnabledInHomeAndSettings
                                && !isEnabledInExercise
                                && !isEnabledInRepetition -> getString(R.string.nowhere)
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
}