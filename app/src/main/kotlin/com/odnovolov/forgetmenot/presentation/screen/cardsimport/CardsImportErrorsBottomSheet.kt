package com.odnovolov.forgetmenot.presentation.screen.cardsimport

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportEvent.FixErrorsButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.cardsimport.CardsImportEvent.ImportIgnoringErrorsButtonClicked
import kotlinx.android.synthetic.main.bottom_sheet_import_errors.*
import kotlinx.coroutines.launch

class CardsImportErrorsBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        CardsImportDiScope.reopenIfClosed()
    }

    private var controller: CardsImportController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_import_errors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardsImportDiScope.getAsync() ?: return@launch
            controller = diScope.cardsImportController
            observeViewModel(diScope.cardsImportViewModel)
        }
    }

    private fun setupView() {
        setBottomSheetAlwaysExpanded()
        fixErrorsButton.setOnClickListener {
            controller?.dispatch(FixErrorsButtonClicked)
            dismiss()
        }
        importIgnoringErrorsButton.setOnClickListener {
            controller?.dispatch(ImportIgnoringErrorsButtonClicked)
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

    @SuppressLint("SetTextI18n")
    private fun observeViewModel(viewModel: CardsImportViewModel) {
        val errorsInfo = viewModel.errorsInfo
        val part1 = resources.getQuantityString(
            R.plurals.import_error_message_1_part,
            errorsInfo.numberOfDecksContainingErrors,
            errorsInfo.numberOfDecksContainingErrors
        )
        val part2 = resources.getQuantityString(
            R.plurals.import_error_message_2_part,
            errorsInfo.totalNumberOfErrors,
            errorsInfo.totalNumberOfErrors
        )
        messageTextView.text = "$part1 $part2"
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTransparentBackground
    }
}