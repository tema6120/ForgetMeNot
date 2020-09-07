package com.odnovolov.forgetmenot.presentation.screen.speakplan

import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.ItemTouchHelper
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.showActionBar
import com.odnovolov.forgetmenot.presentation.common.showToast
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakAnswerMessage
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanController.Command.ShowCannotChangeLastSpeakQuestionMessage
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanUiEvent.AddSpeakEventButtonClicked
import com.odnovolov.forgetmenot.presentation.screen.speakplan.SpeakPlanUiEvent.HelpButtonClicked
import kotlinx.android.synthetic.main.fragment_speak_plan.*
import kotlinx.coroutines.launch

class SpeakPlanFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        SpeakPlanDiScope.reopenIfClosed()
    }

    private var controller: SpeakPlanController? = null
    private lateinit var viewModel: SpeakPlanViewModel
    private lateinit var speakEventAdapter: SpeakEventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_speak_plan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAddSpeakButton()
        viewCoroutineScope!!.launch {
            val diScope = SpeakPlanDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            speakEventAdapter = SpeakEventAdapter(controller!!)
            presetView.inject(diScope.presetController, diScope.presetViewModel)
            setupSpeakPlanRecyclerView()
            observeSpeakEvents()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupAddSpeakButton() {
        addSpeakEventButton.setOnClickListener {
            controller?.dispatch(AddSpeakEventButtonClicked)
        }
    }

    private fun setupSpeakPlanRecyclerView() {
        speakPlanRecyclerView.adapter = speakEventAdapter
        val itemTouchHelperCallback = SpeakEventItemTouchHelperCallback(
            controller!!,
            speakEventAdapter
        )
        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        speakEventAdapter.itemTouchHelper = itemTouchHelper
        itemTouchHelper.attachToRecyclerView(speakPlanRecyclerView)
    }

    private fun observeSpeakEvents() {
        viewModel.speakEventItems.observe(speakEventAdapter::setItems)
    }

    private fun executeCommand(command: SpeakPlanController.Command) {
        when (command) {
            ShowCannotChangeLastSpeakQuestionMessage ->
                showToast(R.string.error_message_cannot_change_last_speak_question)
            ShowCannotChangeLastSpeakAnswerMessage ->
                showToast(R.string.error_message_cannot_change_last_speak_answer)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.help, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_help -> {
                controller?.dispatch(HelpButtonClicked)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        showActionBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        speakPlanRecyclerView.adapter = null
        speakEventAdapter.itemTouchHelper = null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            SpeakPlanDiScope.close()
        }
    }
}