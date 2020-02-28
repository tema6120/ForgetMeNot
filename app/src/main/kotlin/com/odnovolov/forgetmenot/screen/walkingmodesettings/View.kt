package com.odnovolov.forgetmenot.screen.walkingmodesettings

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.Item
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.ItemAdapter
import com.odnovolov.forgetmenot.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGesture.*
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction
import com.odnovolov.forgetmenot.presentation.screen.walkingmodesettings.KeyGestureAction.*
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.screen.walkingmodesettings.WalkingModeSettingsEvent.KeyGestureActionSelected
import kotlinx.android.synthetic.main.fragment_walking_mode_settings.*
import kotlinx.coroutines.flow.Flow

class WalkingModeSettingsFragment : BaseFragment() {
    private val controller = WalkingModeSettingsController()
    private val viewModel = WalkingModeSettingsViewModel()
    private lateinit var chooseKeyGestureActionDialog: Dialog
    private lateinit var chooseKeyGestureActionDialogAdapter: ItemAdapter<KeyGestureActionItem>
    private var activeRemappingKeyGesture: KeyGesture? = null

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
            onItemClick = { item: KeyGestureActionItem ->
                chooseKeyGestureActionDialog.dismiss()
                val event =
                    KeyGestureActionSelected(activeRemappingKeyGesture!!, item.keyGestureAction)
                controller.dispatch(event)
                activeRemappingKeyGesture = null
            },
            takeAdapter = { chooseKeyGestureActionDialogAdapter = it }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
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
        chooseKeyGestureActionDialogAdapter.items = KeyGestureAction.values().map {
            KeyGestureActionItem(
                keyGestureAction = it,
                text = getString(getStringResIdOf(it)),
                isSelected = it === selectedKeyGestureAction
            )
        }
    }

    private fun updateDialogTitle() {
        val resId = when (activeRemappingKeyGesture!!) {
            VOLUME_UP_SINGLE_PRESS -> R.string.text_single_press_title
            VOLUME_UP_DOUBLE_PRESS -> R.string.text_double_press_title
            VOLUME_UP_LONG_PRESS -> R.string.text_long_press_title
            VOLUME_DOWN_SINGLE_PRESS -> R.string.text_single_press_title
            VOLUME_DOWN_DOUBLE_PRESS -> R.string.text_double_press_title
            VOLUME_DOWN_LONG_PRESS -> R.string.text_long_press_title
        }
        chooseKeyGestureActionDialog.setTitle(resId)
    }

    private fun observeViewModel() {
        with(viewModel) {
            selectedVolumeUpSinglePressAction.observeBy(selectedVolumeUpSinglePressActionTextView)
            selectedVolumeUpDoublePressAction.observeBy(selectedVolumeUpDoublePressActionTextView)
            selectedVolumeUpLongPressAction.observeBy(selectedVolumeUpLongPressActionTextView)
            selectedVolumeDownSinglePressAction.observeBy(
                selectedVolumeDownSinglePressActionTextView
            )
            selectedVolumeDownDoublePressAction.observeBy(
                selectedVolumeDownDoublePressActionTextView
            )
            selectedVolumeDownLongPressAction.observeBy(selectedVolumeDownLongPressActionTextView)
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
        SET_CARD_AS_REMEMBER -> R.string.key_gesture_action_set_card_as_remember
        SET_CARD_AS_NOT_REMEMBER -> R.string.key_gesture_action_set_card_as_not_remember
        SET_CARD_AS_LEARNED -> R.string.key_gesture_action_set_card_as_learned
        SPEAK_QUESTION -> R.string.key_gesture_action_speak_question
        SPEAK_ANSWER -> R.string.key_gesture_action_speak_answer
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            getBundle(STATE_KEY_CHOOSE_KEY_GESTURE_ACTION_DIALOG)
                ?.let(chooseKeyGestureActionDialog::onRestoreInstanceState)
            activeRemappingKeyGesture = getSerializable(STATE_KEY_ACTIVE_REMAPPING_KEY_GESTURE)
                    as? KeyGesture
            if (activeRemappingKeyGesture != null) updateAdapterItems()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::chooseKeyGestureActionDialog.isInitialized) {
            outState.putBundle(
                STATE_KEY_CHOOSE_KEY_GESTURE_ACTION_DIALOG,
                chooseKeyGestureActionDialog.onSaveInstanceState()
            )
        }
        outState.putSerializable(STATE_KEY_ACTIVE_REMAPPING_KEY_GESTURE, activeRemappingKeyGesture)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
    }

    companion object {
        const val STATE_KEY_CHOOSE_KEY_GESTURE_ACTION_DIALOG = "chooseKeyGestureActionDialog"
        const val STATE_KEY_ACTIVE_REMAPPING_KEY_GESTURE = "activeRemappingKeyGesture"
    }
}

data class KeyGestureActionItem(
    val keyGestureAction: KeyGestureAction,
    override val text: String,
    override val isSelected: Boolean
) : Item