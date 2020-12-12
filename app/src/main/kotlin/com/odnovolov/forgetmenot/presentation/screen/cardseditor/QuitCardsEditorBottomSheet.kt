package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.SaveButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.UserConfirmedExit
import kotlinx.android.synthetic.main.bottom_sheet_quit_cards_editor.*
import kotlinx.coroutines.launch

class QuitCardsEditorBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        CardsEditorDiScope.reopenIfClosed()
    }

    private var controller: CardsEditorController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_quit_cards_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardsEditorDiScope.getAsync() ?: return@launch
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