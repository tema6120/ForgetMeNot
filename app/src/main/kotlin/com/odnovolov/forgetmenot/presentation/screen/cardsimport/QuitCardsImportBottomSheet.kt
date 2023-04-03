package com.odnovolov.forgetmenot.presentation.screen.cardsimport

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportEvent.*
import kotlinx.android.synthetic.main.bottom_sheet_quit_file_import.*
import kotlinx.coroutines.launch

class QuitCardsImportBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        CardsImportDiScope.reopenIfClosed()
    }

    private var controller: CardsImportController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_quit_file_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardsImportDiScope.getAsync() ?: return@launch
            controller = diScope.cardsImportController
        }
    }

    private fun setupView() {
        setBottomSheetAlwaysExpanded()
        yesButton.setOnClickListener {
            controller?.dispatch(UserConfirmedExit)
            dismiss()
        }
        noButton.setOnClickListener {
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