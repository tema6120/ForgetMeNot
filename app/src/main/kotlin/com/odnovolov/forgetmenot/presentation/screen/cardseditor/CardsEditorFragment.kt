package com.odnovolov.forgetmenot.presentation.screen.cardseditor

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorController.Command.*
import com.odnovolov.forgetmenot.presentation.screen.cardseditor.CardsEditorEvent.*
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalItem
import com.odnovolov.forgetmenot.presentation.screen.exercise.IntervalsAdapter
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
    private val intervalsPopup: PopupWindow by lazy { createIntervalsPopup() }
    private val intervalsAdapter: IntervalsAdapter by lazy { createIntervalsAdapter() }
    private lateinit var exitDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        createExitDialog()
        return inflater.inflate(R.layout.fragment_cards_editor, container, false)
    }

    private fun createExitDialog() {
        exitDialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.title_exit_dialog)
            .setMessage(R.string.message_changes_will_be_lost)
            .setPositiveButton(R.string.yes) { _, _ -> controller?.dispatch(UserConfirmedExit) }
            .setNegativeButton(R.string.no, null)
            .create()
        dialogTimeCapsule.register("exitDialog", exitDialog)
    }

    private fun createIntervalsAdapter(): IntervalsAdapter {
        val onItemClick: (Int) -> Unit = { grade: Int ->
            controller?.dispatch(GradeWasChanged(grade))
            intervalsPopup.dismiss()
        }
        return IntervalsAdapter(onItemClick)
    }

    private fun createIntervalsPopup(): PopupWindow {
        val recycler: RecyclerView =
            View.inflate(context, R.layout.popup_grade_intervals, null) as RecyclerView
        recycler.adapter = intervalsAdapter
        return PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = recycler
            setBackgroundDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.background_popup_dark)
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardsEditorDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun setupView() {
        cardsViewPager.adapter = EditableCardAdapter(this)
        cardsViewPager.registerOnPageChangeCallback(onPageChangeCallback)
        gradeButton.run {
            setOnClickListener { controller?.dispatch(GradeButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        removeCardButton.run {
            setOnClickListener { controller?.dispatch(RemoveCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        helpButton.run {
            setOnClickListener { controller?.dispatch(HelpButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        cancelButton.run {
            setOnClickListener { controller?.dispatch(CancelButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
        doneButton.run {
            setOnClickListener { controller?.dispatch(DoneButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
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
            gradeOfCurrentCard.observe { grade: Int? ->
                with(gradeTextView) {
                    if (grade == null) {
                        isVisible = false
                    } else {
                        val backgroundRes = getBackgroundResForGrade(grade)
                        setBackgroundResource(backgroundRes)
                        text = grade.toString()
                        isVisible = true
                    }
                }
                gradeButton.isVisible = grade != null
            }
            isCurrentEditableCardLearned.observe { isLearned: Boolean? ->
                with(markAsLearnedButton) {
                    if (isLearned == null) {
                        isVisible = false
                    } else {
                        setImageResource(
                            if (isLearned)
                                R.drawable.ic_mark_as_unlearned else
                                R.drawable.ic_mark_as_learned
                        )
                        setOnClickListener {
                            controller?.dispatch(
                                if (isLearned)
                                    AskAgainButtonClicked else
                                    NotAskButtonClicked
                            )
                        }
                        contentDescription = getString(
                            if (isLearned)
                                R.string.description_mark_as_unlearned_button else
                                R.string.description_mark_as_learned_button
                        )
                        TooltipCompat.setTooltipText(this, contentDescription)
                        isVisible = true
                    }
                }
            }
            isRemoveCardButtonVisible.observe { isVisible: Boolean ->
                removeCardButton.isVisible = isVisible
            }
            helpButton.isVisible = isHelpButtonVisible
        }
    }

    private fun executeCommand(command: CardsEditorController.Command) {
        when (command) {
            is ShowUnfilledTextInputAt -> {
                cardsViewPager.setCurrentItem(command.position, true)
                showToast(R.string.toast_fill_in_the_card)
            }
            is ShowIntervalsPopup -> {
                showIntervalsPopup(command.intervalItems)
            }
            ShowIntervalsAreOffMessage -> {
                showToast(R.string.toast_text_intervals_are_off)
            }
            ShowCardIsRemovedMessage -> {
                Snackbar
                    .make(
                        coordinatorLayout,
                        R.string.card_is_removed,
                        resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                    )
                    .setAction(
                        R.string.snackbar_action_cancel,
                        { controller?.dispatch(RestoreLastRemovedCardButtonClicked) }
                    )
                    .show()
            }
            AskUserToConfirmExit -> {
                exitDialog.show()
            }
        }
    }

    private fun showIntervalsPopup(intervalItems: List<IntervalItem>) {
        intervalsAdapter.intervalItems = intervalItems
        val content = intervalsPopup.contentView
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val location = IntArray(2)
        gradeButton.getLocationOnScreen(location)
        val x = location[0] + 8.dp
        val y = location[1] + gradeButton.height - 8.dp - content.measuredHeight
        intervalsPopup.showAtLocation(
            gradeButton.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
        hideActionBar()
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
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

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            controller?.dispatch(BackButtonClicked)
            return true
        }
    }
}