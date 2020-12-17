package com.odnovolov.forgetmenot.presentation.screen.player.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseBottomSheetDialogFragment
import com.odnovolov.forgetmenot.presentation.screen.player.PlayerDiScope
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.EndButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.player.view.PlayerFragmentEvent.PlayAgainButtonClicked
import kotlinx.android.synthetic.main.bottom_sheet_playing_completed.*
import kotlinx.coroutines.launch

class PlayingFinishedBottomSheet : BaseBottomSheetDialogFragment() {
    init {
        PlayerDiScope.reopenIfClosed()
    }

    private var controller: PlayerViewController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_playing_completed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = PlayerDiScope.getAsync() ?: return@launch
            controller = diScope.viewController
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        setBottomSheetAlwaysExpanded()
        playAgainButton.setOnClickListener {
            controller?.dispatch(PlayAgainButtonClicked)
        }
        endButton.setOnClickListener {
            controller?.dispatch(EndButtonClicked)
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

    private fun observeViewModel(viewModel: PlayerViewModel) {
        viewModel.isCompleted.observe { isCompleted: Boolean ->
            if (!isCompleted) {
                dismiss()
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTransparentBackground
    }
}