package com.odnovolov.forgetmenot.presentation.common.customview.preset

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.TextUtils
import android.util.AttributeSet
import android.util.SparseArray
import android.util.TypedValue
import android.view.Gravity
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.END
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetEvent.*
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController.Command.ShowPresetNameDialog
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController.Command.ShowRemovePresetDialog
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import kotlinx.android.synthetic.main.dialog_input.view.*
import kotlinx.android.synthetic.main.dialog_remove_preset.view.*
import kotlinx.android.synthetic.main.popup_preset.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class PresetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : LinearLayout(
    context,
    attrs,
    defStyleAttr,
    defStyleRes
) {
    private val savePresetButton = ImageButton(context, attrs, defStyleAttr, defStyleRes).apply {
        layoutParams = LayoutParams(48.dp, 48.dp).apply {
            gravity = CENTER_VERTICAL
        }
        setPadding(12.dp)
        setBackgroundResource(getBorderlessRippleId())
        contentDescription = context.getString(R.string.description_save_preset)
        visibility = GONE
        setImageResource(R.drawable.ic_save_dark_24dp)
        TooltipCompat.setTooltipText(this, contentDescription)
    }

    private val selectPresetButton = TextView(context, attrs, defStyleAttr, defStyleRes).apply {
        layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            gravity = CENTER_VERTICAL
        }
        setBackgroundResource(getRippleId())
        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_more_expand_more_with_inset, 0)
        ellipsize = TextUtils.TruncateAt.END
        maxLines = 1
        setPadding(16.dp)
        setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
        textSize = 22f
        setTypeface(null, Typeface.BOLD)
    }

    private fun getRippleId(): Int {
        return getAttrResourceId(android.R.attr.selectableItemBackground)
    }

    private fun getBorderlessRippleId(): Int {
        return getAttrResourceId(android.R.attr.selectableItemBackgroundBorderless)
    }

    private fun getAttrResourceId(resId: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(resId, outValue, true)
        return outValue.resourceId
    }

    init {
        orientation = HORIZONTAL
        gravity = END
        addView(savePresetButton)
        addView(selectPresetButton)
    }

    private lateinit var popup: PopupWindow
    private lateinit var presetAdapter: PresetAdapter
    private lateinit var nameInputDialog: AlertDialog
    private lateinit var nameEditText: EditText
    private lateinit var removePresetDialog: AlertDialog
    private val affectedDeckNameAdapter = AffectedDeckNameAdapter()
    private var coroutineScope: CoroutineScope? = null
    private var controller: SkeletalPresetController? = null
    private lateinit var viewModel: SkeletalPresetViewModel
    private var pendingNameInputDialogState: Bundle? = null
    private var pendingRemovePresetDialogState: Bundle? = null
    private var pendingPopupState: Boolean? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (controller == null) return
        coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        observePrimaries()
        Looper.myQueue().addIdleHandler {
            if (isAttachedToWindow) {
                observeSecondaries()
            }
            false
        }
    }

    fun inject(controller: SkeletalPresetController, viewModel: SkeletalPresetViewModel) {
        this.controller = controller
        this.viewModel = viewModel

        if (isAttachedToWindow) {
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
            observePrimaries()
            Looper.myQueue().addIdleHandler {
                initSecondaries()
                if (isAttachedToWindow) {
                    observeSecondaries()
                }
                false
            }
        } else {
            Looper.myQueue().addIdleHandler {
                initSecondaries()
                false
            }
        }
    }

    private fun observePrimaries() {
        viewModel.currentPreset.observe(coroutineScope!!) { preset: Preset ->
            selectPresetButton.text = preset.toString(context)
            savePresetButton.isVisible = preset.isIndividual()
        }
    }

    private fun observeSecondaries() {
        with(viewModel) {
            availablePresets.observe(coroutineScope!!, presetAdapter::submitList)
            presetInputCheckResult.observe(coroutineScope!!) { nameCheckResult: NameCheckResult ->
                nameEditText.error = when (nameCheckResult) {
                    NameCheckResult.Ok -> null
                    NameCheckResult.Empty -> context.getString(R.string.error_message_empty_name)
                    NameCheckResult.Occupied ->
                        context.getString(R.string.error_message_occupied_name)
                }
                if (nameInputDialog.isShowing) {
                    nameInputDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .isEnabled = nameCheckResult == NameCheckResult.Ok
                }
            }
            deckNamesThatUsePreset.observe(coroutineScope!!) { deckNames: List<String> ->
                affectedDeckNameAdapter.items = deckNames
            }
        }
        controller!!.commands.observe(coroutineScope!!, ::executeCommand)
    }

    private fun executeCommand(command: SkeletalPresetController.Command) {
        when (command) {
            is ShowPresetNameDialog -> {
                nameInputDialog.show()
                nameEditText.setText(command.presetName)
                nameEditText.selectAll()
            }
            is ShowRemovePresetDialog -> {
                removePresetDialog.show()
            }
        }
    }

    private fun initSecondaries() {
        initPopup()
        initNameInputDialog()
        initRemovePresetDialog()
        savePresetButton.setOnClickListener { controller?.dispatch(SavePresetButtonClicked) }
        selectPresetButton.setOnClickListener { showPopup() }
        restoreByPendingState()
    }

    private fun initPopup() {
        popup = PopupWindow(context).apply {
            width = 256.dp
            height = WindowManager.LayoutParams.WRAP_CONTENT
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
            elevation = 20f
            isOutsideTouchable = true
            isFocusable = true
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            contentView = View.inflate(context, R.layout.popup_preset, null)
            presetAdapter = PresetAdapter(
                onSetPresetButtonClick = { id: Long? ->
                    controller?.dispatch(SetPresetButtonClicked(id))
                    popup.dismiss()
                },
                onRenamePresetButtonClick = { id: Long ->
                    controller?.dispatch(RenamePresetButtonClicked(id))
                },
                onDeletePresetButtonClick = { id: Long ->
                    controller?.dispatch(DeletePresetButtonClicked(id))
                }
            )
            contentView.presetRecyclerView.adapter = presetAdapter
            contentView.addPresetButton.setOnClickListener {
                controller?.dispatch(AddNewPresetButtonClicked)
            }
        }
    }

    private fun initNameInputDialog() {
        val contentView = View.inflate(context, R.layout.dialog_input, null)
        nameEditText = contentView.dialogInput
        nameEditText.observeText { text: String ->
            controller?.dispatch(PresetNameInputChanged(text))
        }
        nameInputDialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_preset_name_input_dialog)
            .setView(contentView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(PresetNamePositiveDialogButtonClicked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        nameInputDialog.setOnShowListener { nameEditText.showSoftInput() }
    }

    private fun initRemovePresetDialog() {
        val contentView = View.inflate(context, R.layout.dialog_remove_preset, null)
        contentView.removePresetRecycler.adapter = affectedDeckNameAdapter
        removePresetDialog = AlertDialog.Builder(context)
            .setTitle(R.string.title_remove_preset_dialog)
            .setView(contentView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                controller?.dispatch(RemovePresetPositiveDialogButtonClicked)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    private fun showPopup() {
        val location = IntArray(2)
        selectPresetButton.getLocationOnScreen(location)
        val x = location[0] + selectPresetButton.width - popup.width
        val y = location[1]
        popup.showAtLocation(parent as View, Gravity.NO_GRAVITY, x, y)
    }

    private fun restoreByPendingState() {
        pendingNameInputDialogState?.let(nameInputDialog::onRestoreInstanceState)
        pendingRemovePresetDialogState?.let(removePresetDialog::onRestoreInstanceState)
        pendingPopupState?.let { isVisible: Boolean -> if (isVisible) showPopup() }
        pendingNameInputDialogState = null
        pendingRemovePresetDialogState = null
        pendingPopupState = null
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                pendingNameInputDialogState = state.nameInputDialogState
                pendingRemovePresetDialogState = state.removePresetDialogState
                pendingPopupState = state.isPopupVisible
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState()).apply {
            nameInputDialogState = nameInputDialog.onSaveInstanceState()
            removePresetDialogState = removePresetDialog.onSaveInstanceState()
            isPopupVisible = popup.isShowing
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope!!.cancel()
        coroutineScope = null
    }

    class SavedState : BaseSavedState {
        var nameInputDialogState: Bundle? = null
        var removePresetDialogState: Bundle? = null
        var isPopupVisible: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            val bundle = source.readBundle(javaClass.classLoader)
            nameInputDialogState = bundle?.getBundle(NAME_INPUT_DIALOG_STATE_KEY)
            removePresetDialogState = bundle?.getBundle(REMOVE_PRESET_DIALOG_STATE_KEY)
            isPopupVisible = bundle?.getBoolean(IS_POPUP_VISIBLE_STATE_KEY) ?: false
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            val bundle = Bundle()
            bundle.putBundle(NAME_INPUT_DIALOG_STATE_KEY, nameInputDialogState)
            bundle.putBundle(REMOVE_PRESET_DIALOG_STATE_KEY, removePresetDialogState)
            bundle.putBoolean(IS_POPUP_VISIBLE_STATE_KEY, isPopupVisible)
            out.writeBundle(bundle)
        }

        companion object {
            @Suppress("UNUSED")
            @JvmField
            val CREATOR = object : Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }

            private const val NAME_INPUT_DIALOG_STATE_KEY = "NAME_INPUT_DIALOG_STATE_KEY"
            private const val REMOVE_PRESET_DIALOG_STATE_KEY = "NAME_INPUT_DIALOG_STATE_KEY"
            private const val IS_POPUP_VISIBLE_STATE_KEY = "NAME_INPUT_DIALOG_STATE_KEY"
        }
    }
}