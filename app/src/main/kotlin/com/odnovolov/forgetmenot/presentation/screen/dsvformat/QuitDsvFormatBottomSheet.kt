package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.SaveButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.dsvformat.DsvFormatEvent.UserConfirmedExit
import kotlinx.android.synthetic.main.bottom_sheet_quit_cards_editor.*
import kotlinx.coroutines.launch

class QuitDsvFormatBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        DsvFormatDiScope.reopenIfClosed()
    }

    private var controller: DsvFormatController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_quit_dsv_format, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DsvFormatDiScope.getAsync() ?: return@launch
            controller = diScope.controller
        }
    }

    private fun setupView() {
        setBottomSheetAlwaysExpanded()
        saveButton.setOnClickListener {
            controller?.dispatch(SaveButtonClicked)
            dismiss()
        }
        quitWithoutSavingButton.setOnClickListener {
            controller?.dispatch(UserConfirmedExit)
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