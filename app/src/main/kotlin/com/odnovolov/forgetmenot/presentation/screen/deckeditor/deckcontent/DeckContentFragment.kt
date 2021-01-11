package com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.inflateAsync
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.openFileCreator
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.deckcontent.DeckContentEvent.OutputStreamOpened
import kotlinx.android.synthetic.main.fragment_deck_content.*
import kotlinx.coroutines.launch

class DeckContentFragment : BaseFragment() {
    init {
        DeckContentDiScope.reopenIfClosed()
    }

    private var controller: DeckContentController? = null
    private lateinit var viewModel: DeckContentViewModel
    private var pendingEvent: OutputStreamOpened? = null
    private var isInflated = false
    lateinit var scrollListener: OnScrollListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (savedInstanceState == null) {
            inflater.inflateAsync(R.layout.fragment_deck_content, ::onViewInflated)
        } else {
            inflater.inflate(R.layout.fragment_deck_content, container, false)
        }
    }

    private fun onViewInflated() {
        if (viewCoroutineScope != null) {
            isInflated = true
            setupIfReady()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            isInflated = true
        }
        viewCoroutineScope!!.launch {
            val diScope = DeckContentDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            setupIfReady()
        }
    }

    private fun setupIfReady() {
        if (viewCoroutineScope == null || controller == null || !isInflated) return
        val adapter = CardOverviewAdapter(controller!!)
        cardsRecycler.adapter = adapter
        viewModel.cards.observe(adapter::submitItems)
        controller!!.commands.observe(::executeCommand)
        pendingEvent?.let(controller!!::dispatch)
        pendingEvent = null
        cardsRecycler.addOnScrollListener(scrollListener)
    }

    private fun executeCommand(command: DeckContentController.Command) {
        when (command) {
            is ShowCreateFileDialog -> {
                openFileCreator(CREATE_FILE_REQUEST_CODE, command.fileName)
            }
            ShowDeckIsExportedMessage -> {
                showToast(R.string.toast_deck_is_exported)
            }
            is ShowExportErrorMessage -> {
                val errorMessage = getString(
                    R.string.toast_error_while_exporting_deck,
                    command.e.message
                )
                showToast(errorMessage)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (requestCode != CREATE_FILE_REQUEST_CODE
            || resultCode != Activity.RESULT_OK
            || intent == null
        ) {
            return
        }
        val uri = intent.data ?: return
        val outputStream = requireContext().contentResolver?.openOutputStream(uri)
        if (outputStream != null) {
            val event = OutputStreamOpened(outputStream)
            if (controller == null) {
                pendingEvent = event
            } else {
                controller!!.dispatch(event)
            }
        }
    }

    override fun onDestroyView() {
        cardsRecycler.removeOnScrollListener(scrollListener)
        super.onDestroyView()
        isInflated = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckContentDiScope.close()
        }
    }

    companion object {
        const val CREATE_FILE_REQUEST_CODE = 40
    }
}