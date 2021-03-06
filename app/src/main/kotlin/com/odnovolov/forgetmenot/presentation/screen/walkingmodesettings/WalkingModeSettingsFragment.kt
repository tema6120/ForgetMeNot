package com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModeSettingsEvent.HelpButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.WalkingModeSettingsEvent.KeyGestureActionSelected
import kotlinx.android.synthetic.main.fragment_walking_mode_settings.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class WalkingModeSettingsFragment : BaseFragment() {
    init {
        WalkingModeSettingsDiScope.reopenIfClosed()
    }

    private lateinit var viewModel: WalkingModeSettingsViewModel
    private var controller: WalkingModeSettingsController? = null
    private lateinit var chooseKeyGestureActionDialog: Dialog
    private lateinit var chooseKeyGestureActionDialogAdapter: ItemAdapter
    private var activeRemappingKeyGesture: KeyGesture? = null
    private var dialogTitleTextView: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initDialog()
        return inflater.inflate(R.layout.fragment_walking_mode_settings, container, false)
    }

    private fun initDialog() {
        chooseKeyGestureActionDialog = ChoiceDialogCreator.create(
            context = requireContext(),
            itemForm = AsRadioButton,
            takeTitle = { titleTextView: TextView ->
                dialogTitleTextView = titleTextView
            },
            onItemClick = { item: Item ->
                item as KeyGestureActionItem
                chooseKeyGestureActionDialog.dismiss()
                controller?.dispatch(
                    KeyGestureActionSelected(
                        activeRemappingKeyGesture!!,
                        item.keyGestureAction
                    )
                )
                activeRemappingKeyGesture = null
            },
            takeAdapter = { chooseKeyGestureActionDialogAdapter = it }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            val diScope = WalkingModeSettingsDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            restoreViewState(savedInstanceState)
            setupView()
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
        volumeUpSinglePressButton.setOnClickListener {
            activeRemappingKeyGesture = VOLUME_UP_SINGLE_PRESS
            showDialog()
        }
        volumeUpDoublePressButton.setOnClickListener {
            activeRemappingKeyGesture = VOLUME_UP_DOUBLE_PRESS
            showDialog()
        }
        volumeUpLongPressButton.setOnClickListener {
            activeRemappingKeyGesture = VOLUME_UP_LONG_PRESS
            showDialog()
        }
        volumeDownSinglePressButton.setOnClickListener {
            activeRemappingKeyGesture = VOLUME_DOWN_SINGLE_PRESS
            showDialog()
        }
        volumeDownDoublePressButton.setOnClickListener {
            activeRemappingKeyGesture = VOLUME_DOWN_DOUBLE_PRESS
            showDialog()
        }
        volumeDownLongPressButton.setOnClickListener {
            activeRemappingKeyGesture = VOLUME_DOWN_LONG_PRESS
            showDialog()
        }
    }

    private fun showDialog() {
        updateAdapterItems()
        updateDialogTitle()
        chooseKeyGestureActionDialog.show()
    }

    private fun updateAdapterItems() {
        val selectedKeyGestureAction: KeyGestureAction = with(viewModel) {
            when (activeRemappingKeyGesture!!) {
                VOLUME_UP_SINGLE_PRESS -> selectedVolumeUpSinglePressAction
                VOLUME_UP_DOUBLE_PRESS -> selectedVolumeUpDoublePressAction
                VOLUME_UP_LONG_PRESS -> selectedVolumeUpLongPressAction
                VOLUME_DOWN_SINGLE_PRESS -> selectedVolumeDownSinglePressAction
                VOLUME_DOWN_DOUBLE_PRESS -> selectedVolumeDownDoublePressAction
                VOLUME_DOWN_LONG_PRESS -> selectedVolumeDownLongPressAction
            }.firstBlocking()
        }
        val items: List<KeyGestureActionItem> = KeyGestureAction.values().map {
            KeyGestureActionItem(
                keyGestureAction = it,
                text = getString(getStringResIdOf(it)),
                isSelected = it === selectedKeyGestureAction
            )
        }
        chooseKeyGestureActionDialogAdapter.submitList(items)
    }

    private fun updateDialogTitle() {
        val resId: Int = when (activeRemappingKeyGesture!!) {
            VOLUME_UP_SINGLE_PRESS -> R.string.text_single_press_title
            VOLUME_UP_DOUBLE_PRESS -> R.string.text_double_press_title
            VOLUME_UP_LONG_PRESS -> R.string.text_long_press_title
            VOLUME_DOWN_SINGLE_PRESS -> R.string.text_single_press_title
            VOLUME_DOWN_DOUBLE_PRESS -> R.string.text_double_press_title
            VOLUME_DOWN_LONG_PRESS -> R.string.text_long_press_title
        }
        dialogTitleTextView?.setText(resId)
        val drawableRes: Int = when (activeRemappingKeyGesture!!) {
            VOLUME_UP_SINGLE_PRESS -> R.drawable.ic_wm_volume_up_single_press
            VOLUME_UP_DOUBLE_PRESS -> R.drawable.ic_wm_volume_up_double_press
            VOLUME_UP_LONG_PRESS -> R.drawable.ic_wm_volume_up_long_press
            VOLUME_DOWN_SINGLE_PRESS -> R.drawable.ic_wm_volume_down_single_press
            VOLUME_DOWN_DOUBLE_PRESS -> R.drawable.ic_wm_volume_down_double_press
            VOLUME_DOWN_LONG_PRESS -> R.drawable.ic_wm_volume_down_long_press
        }
        dialogTitleTextView?.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableRes, 0, 0, 0)
    }

    private fun observeViewModel() {
        with(viewModel) {
            selectedVolumeUpSinglePressAction.observeBy(
                selectedVolumeUpSinglePressActionTextView
            )
            selectedVolumeUpDoublePressAction.observeBy(
                selectedVolumeUpDoublePressActionTextView
            )
            selectedVolumeUpLongPressAction.observeBy(
                selectedVolumeUpLongPressActionTextView
            )
            selectedVolumeDownSinglePressAction.observeBy(
                selectedVolumeDownSinglePressActionTextView
            )
            selectedVolumeDownDoublePressAction.observeBy(
                selectedVolumeDownDoublePressActionTextView
            )
            selectedVolumeDownLongPressAction.observeBy(
                selectedVolumeDownLongPressActionTextView
            )
        }
    }

    private fun Flow<KeyGestureAction>.observeBy(textView: TextView) {
        observe { keyGestureAction: KeyGestureAction ->
            val resId = getStringResIdOf(keyGestureAction)
            textView.setText(resId)
        }
    }

    private fun getStringResIdOf(keyGestureAction: KeyGestureAction) = when (keyGestureAction) {
        NO_ACTION -> R.string.key_gesture_action_no_action
        MOVE_TO_NEXT_CARD -> R.string.key_gesture_action_move_to_next_card
        MOVE_TO_PREVIOUS_CARD -> R.string.key_gesture_action_move_to_previous_card
        MARK_AS_REMEMBER -> R.string.key_gesture_action_mark_as_remember
        MARK_AS_NOT_REMEMBER -> R.string.key_gesture_action_mark_as_not_remember
        MARK_CARD_AS_LEARNED -> R.string.key_gesture_action_mark_card_as_learned
        SPEAK_QUESTION -> R.string.key_gesture_action_speak_question
        SPEAK_ANSWER -> R.string.key_gesture_action_speak_answer
    }

    private fun restoreViewState(savedInstanceState: Bundle?) {
        savedInstanceState?.run {
            activeRemappingKeyGesture = getSerializable(STATE_ACTIVE_REMAPPING_KEY_GESTURE)
                    as? KeyGesture
            if (activeRemappingKeyGesture != null)
                updateAdapterItems()
            val dialogState: Bundle? = getBundle(STATE_CHOOSE_KEY_GESTURE_ACTION_DIALOG)
            if (dialogState != null) {
                updateDialogTitle()
                chooseKeyGestureActionDialog.onRestoreInstanceState(dialogState)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(STATE_ACTIVE_REMAPPING_KEY_GESTURE, activeRemappingKeyGesture)
        if (chooseKeyGestureActionDialog.isShowing) {
            outState.putBundle(
                STATE_CHOOSE_KEY_GESTURE_ACTION_DIALOG,
                chooseKeyGestureActionDialog.onSaveInstanceState()
            )
        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
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
        if (isFinishing()) {
            WalkingModeSettingsDiScope.close()
        }
    }

    data class KeyGestureActionItem(
        val keyGestureAction: KeyGestureAction,
        override val text: String,
        override val isSelected: Boolean
    ) : Item

    companion object {
        private const val STATE_ACTIVE_REMAPPING_KEY_GESTURE = "STATE_ACTIVE_REMAPPING_KEY_GESTURE"
        private const val STATE_CHOOSE_KEY_GESTURE_ACTION_DIALOG =
            "STATE_CHOOSE_KEY_GESTURE_ACTION_DIALOG"
    }
}