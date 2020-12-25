package com.odnovolov.forgetmenot.presentation.screen.pronunciationplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanController.Command.ShowCannotChangeLastSpeakAnswerMessage
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanController.Command.ShowCannotChangeLastSpeakQuestionMessage
import com.odnovolov.forgetmenot.presentation.screen.pronunciationplan.PronunciationPlanUiEvent.AddPronunciationEventButtonClicked
import kotlinx.android.synthetic.main.fragment_pronunciation_plan.*
import kotlinx.coroutines.launch

class PronunciationPlanFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        PronunciationPlanDiScope.reopenIfClosed()
    }

    private var controller: PronunciationPlanController? = null
    private lateinit var viewModel: PronunciationPlanViewModel
    private lateinit var pronunciationEventAdapter: PronunciationEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pronunciation_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAddSpeakButton()
        viewCoroutineScope!!.launch {
            val diScope = PronunciationPlanDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            pronunciationEventAdapter = PronunciationEventAdapter(controller!!)
            presetView.inject(diScope.presetController, diScope.presetViewModel)
            setupPronunciationPlanRecyclerView()
            observePronunciationEvents()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupAddSpeakButton() {
        addPronunciationEventButton.setOnClickListener {
            controller?.dispatch(AddPronunciationEventButtonClicked)
        }
    }

    private fun setupPronunciationPlanRecyclerView() {
        pronunciationPlanRecyclerView.adapter = pronunciationEventAdapter
        val itemTouchHelperCallback = PronunciationEventItemTouchHelperCallback(
            controller!!,
            pronunciationEventAdapter
        )
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        pronunciationEventAdapter.itemTouchHelper = itemTouchHelper
        itemTouchHelper.attachToRecyclerView(pronunciationPlanRecyclerView)
    }

    private fun observePronunciationEvents() {
        viewModel.pronunciationEventItems.observe(pronunciationEventAdapter::setItems)
    }

    private fun executeCommand(command: PronunciationPlanController.Command) {
        when (command) {
            ShowCannotChangeLastSpeakQuestionMessage ->
                showToast(R.string.error_message_cannot_change_last_speak_question)
            ShowCannotChangeLastSpeakAnswerMessage ->
                showToast(R.string.error_message_cannot_change_last_speak_answer)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        pronunciationPlanRecyclerView.adapter = null
        pronunciationEventAdapter.itemTouchHelper = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            PronunciationPlanDiScope.close()
        }
    }
}