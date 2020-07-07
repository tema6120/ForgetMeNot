package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command.MoveToPosition
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.qaeditor.QAEditorFragment
import kotlinx.android.synthetic.main.fragment_cards_editor.*
import kotlinx.coroutines.*

class CardsEditorFragment : BaseFragment() {
    init {
        CardsEditorDiScope.reopenIfClosed()
    }

    private var controller: CardsEditorController? = null
    private lateinit var viewModel: CardsEditorViewModel
    private val fragmentCoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            hide()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cards_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardsEditorDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        cardsViewPager.adapter = EditableCardAdapter(this)
        cardsViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        cancelButton.setOnClickListener { controller?.dispatch(CancelButtonClicked) }
        acceptButton.setOnClickListener { controller?.dispatch(AcceptButtonClicked) }
    }

    private fun observeViewModel() {
        with(viewModel) {
            val editableCardAdapter = cardsViewPager.adapter as EditableCardAdapter
            cardIds.observe { cardIds: List<Long> ->
                editableCardAdapter.cardIds = cardIds
                if (cardsViewPager.currentItem != currentPosition) {
                    cardsViewPager.setCurrentItem(currentPosition, false)
                }
            }
        }
    }

    private fun executeCommand(command: CardsEditorController.Command) {
        when (command) {
            is MoveToPosition -> {
                cardsViewPager.setCurrentItem(command.position, true)
            }
        }
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (childFragment is QAEditorFragment) {
            fragmentCoroutineScope.launch {
                val diScope = CardsEditorDiScope.get()
                val cardId: Long = childFragment.requireArguments().getLong(QAEditorFragment.ARG_ID)
                val qaEditorController = diScope.qaEditorController(cardId)
                val qaEditorViewModel = diScope.qaEditorViewModel(cardId)
                childFragment.inject(qaEditorController, qaEditorViewModel)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cardsViewPager.unregisterOnPageChangeCallback(onPageChangeCallback)
        cardsViewPager.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        fragmentCoroutineScope.cancel()
        (activity as AppCompatActivity).supportActionBar?.show()
        if (needToCloseDiScope()) {
            CardsEditorDiScope.close()
        }
    }

    private val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            controller?.dispatch(PageSelected(position))
        }
    }
}