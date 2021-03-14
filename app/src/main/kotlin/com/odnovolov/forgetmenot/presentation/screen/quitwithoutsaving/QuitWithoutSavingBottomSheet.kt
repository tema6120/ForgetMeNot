package com.odnovolov.forgetmenot.presentation.screen.quitwithoutsaving

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.odnovolov.forgetmenot.R
import kotlinx.android.synthetic.main.bottom_sheet_quit_cards_editor.*

class QuitWithoutSavingBottomSheet : BottomSheetDialogFragment() {
    var onSaveButtonClicked: (() -> Unit)? = null
    var onQuitWithoutSavingButtonClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_quit_without_saving, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
    }

    private fun setupView() {
        setBottomSheetAlwaysExpanded()
        saveButton.setOnClickListener {
            onSaveButtonClicked?.invoke()
            dismiss()
        }
        quitWithoutSavingButton.setOnClickListener {
            onQuitWithoutSavingButtonClicked?.invoke()
            dismiss()
        }
    }

    private fun setBottomSheetAlwaysExpanded() {
        dialog?.setOnShowListener { dialog1 ->
            val bottomSheetDialog = dialog1 as BottomSheetDialog
            val bottomSheet: FrameLayout? =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
            if (bottomSheet != null) {
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.skipCollapsed = true
                BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTransparentBackground
    }
}