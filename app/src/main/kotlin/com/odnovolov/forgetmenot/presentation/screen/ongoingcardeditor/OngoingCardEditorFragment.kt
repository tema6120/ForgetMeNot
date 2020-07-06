package com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardeditor.qaeditor.QAEditorFragment
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorEvent.AcceptButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.ongoingcardeditor.OngoingCardEditorEvent.CancelButtonClicked
import kotlinx.android.synthetic.main.fragment_ongoing_card_editor.*
import kotlinx.coroutines.*

class OngoingCardEditorFragment : BaseFragment() {
    init {
        OngoingCardEditorDiScope.reopenIfClosed()
    }

    private val fragmentCoroutineScope: CoroutineScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
    private lateinit var viewModel: OngoingCardEditorViewModel
    private var controller: OngoingCardEditorController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ongoing_card_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = OngoingCardEditorDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            viewModel.isAcceptButtonEnabled.observe(acceptButton::setEnabled)
        }
    }

    private fun setupView() {
        cancelButton.run {
            setOnClickListener { controller?.dispatch(CancelButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        acceptButton.run {
            setOnClickListener { controller?.dispatch(AcceptButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
            isEnabled = false
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is QAEditorFragment) {
            fragmentCoroutineScope.launch {
                val diScope = OngoingCardEditorDiScope.get()
                childFragment.inject(diScope.qaEditorController, diScope.qaEditorViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentCoroutineScope.cancel()
        if (needToCloseDiScope()) {
            OngoingCardEditorDiScope.close()
        }
    }
}