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
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.observe
import com.odnovolov.forgetmenot.presentation.common.observeText
import com.odnovolov.forgetmenot.presentation.common.customview.preset.PresetEvent.*
import com.odnovolov.forgetmenot.presentation.common.customview.preset.SkeletalPresetController.Command.ShowDialogWithText
import com.odnovolov.forgetmenot.presentation.common.showSoftInput
import kotlinx.android.synthetic.main.dialog_input.view.*
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
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var controller: SkeletalPresetController? = null
    private lateinit var viewModel: SkeletalPresetViewModel
    private var pendingDialogState: Bundle? = null
    private var pendingPopupState: Boolean? = null

    fun inject(controller: SkeletalPresetController, viewModel: SkeletalPresetViewModel) {
        this.controller = controller
        this.viewModel = viewModel
        setupPrimary()
        Looper.myQueue().addIdleHandler {
            setupSecondary()
            false
        }
    }

    private fun setupPrimary() {
        viewModel.currentPreset.observe(coroutineScope) { preset: Preset ->
            selectPresetButton.text = preset.toString(context)
            savePresetButton.isVisible = preset.isIndividual()
        }
    }

    private fun setupSecondary() {
        initPopup()
        initDialog()
        savePresetButton.setOnClickListener { controller?.dispatch(SavePresetButtonClicked) }
        selectPresetButton.setOnClickListener { showPopup() }
        with(viewModel) {
            availablePresets.observe(coroutineScope, presetAdapter::submitList)
            presetInputCheckResult.observe(coroutineScope) { nameCheckResult: NameCheckResult ->
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
        }
        restoreByPendingState()
        controller!!.commands.observe(coroutineScope, ::executeCommand)
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

    private fun initDialog() {
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

    private fun showPopup() {
        val location = IntArray(2)
        selectPresetButton.getLocationOnScreen(location)
        val x = location[0] + selectPresetButton.width - popup.width
        val y = location[1]
        popup.showAtLocation(parent as View, Gravity.NO_GRAVITY, x, y)
    }

    private fun executeCommand(command: SkeletalPresetController.Command) {
        when (command) {
            is ShowDialogWithText -> {
                nameInputDialog.show()
                nameEditText.setText(command.text)
                nameEditText.selectAll()
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(super.onSaveInstanceState()).apply {
            dialogState = nameInputDialog.onSaveInstanceState()
            isPopupVisible = popup.isShowing
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        when (state) {
            is SavedState -> {
                super.onRestoreInstanceState(state.superState)
                pendingDialogState = state.dialogState
                pendingPopupState = state.isPopupVisible
            }
            else -> super.onRestoreInstanceState(state)
        }
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        dispatchFreezeSelfOnly(container)
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        dispatchThawSelfOnly(container)
    }

    private fun restoreByPendingState() {
        pendingDialogState?.let(nameInputDialog::onRestoreInstanceState)
        pendingPopupState?.let { isVisible: Boolean -> if (isVisible) showPopup() }
        pendingDialogState = null
        pendingPopupState = null
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        coroutineScope.cancel()
    }

    class SavedState : BaseSavedState {
        var dialogState: Bundle? = null
        var isPopupVisible: Boolean = false

        constructor(superState: Parcelable?) : super(superState)

        constructor(source: Parcel) : super(source) {
            dialogState = source.readBundle(javaClass.classLoader)
            isPopupVisible = source.readInt() == 1
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeBundle(dialogState)
            out.writeInt(if (isPopupVisible) 1 else 0)
        }

        companion object {
            @Suppress("UNUSED")
            @JvmField
            val CREATOR = object : Creator<SavedState> {
                override fun createFromParcel(source: Parcel) = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }
}