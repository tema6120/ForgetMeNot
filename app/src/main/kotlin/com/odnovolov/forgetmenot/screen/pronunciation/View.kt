package com.odnovolov.forgetmenot.screen.pronunciation

import android.animation.LayoutTransition
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.*
import com.odnovolov.forgetmenot.common.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator.PresetRecyclerAdapter
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.customview.InputDialogCreator
import com.odnovolov.forgetmenot.common.customview.PresetPopupCreator
import com.odnovolov.forgetmenot.screen.pronunciation.LanguageRecyclerAdapter.ViewHolder
import com.odnovolov.forgetmenot.screen.pronunciation.PronunciationEvent.*
import com.odnovolov.forgetmenot.screen.pronunciation.PronunciationOrder.SetDialogText
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import kotlinx.android.synthetic.main.item_language.view.*
import java.util.*

class PronunciationFragment : BaseFragment() {

    private val controller = PronunciationController()
    private val viewModel = PronunciationViewModel()
    private lateinit var choosePronunciationPopup: PopupWindow
    private lateinit var pronunciationRecyclerAdapter: PresetRecyclerAdapter
    private lateinit var questionLanguagePopup: PopupWindow
    private lateinit var questionLanguageRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var answerLanguagePopup: PopupWindow
    private lateinit var answerLanguageRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var speaker: Speaker
    private lateinit var presetNameInputDialog: Dialog
    private lateinit var presetNameEditText: EditText

    override fun onAttach(context: Context) {
        super.onAttach(context)
        speaker = Speaker(context, onInit = {
            val availableLanguages: Set<Locale> = speaker.availableLanguages
            controller.dispatch(AvailableLanguagesUpdated(availableLanguages))
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initChoosePronunciationPopup()
        questionLanguagePopup = createLanguagePopup()
        answerLanguagePopup = createLanguagePopup()
        initPresetNameInputDialog()
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
    }

    private fun initChoosePronunciationPopup() {
        choosePronunciationPopup = PresetPopupCreator.create(
            context = requireContext(),
            setPresetButtonClickListener = { id: Long ->
                controller.dispatch(SetPronunciationButtonClicked(id))
            },
            renamePresetButtonClickListener = { id: Long ->
                controller.dispatch(RenamePronunciationButtonClicked(id))
            },
            deletePresetButtonClickListener = { id: Long ->
                controller.dispatch(DeletePronunciationButtonClicked(id))
            },
            addButtonClickListener = {
                controller.dispatch(AddNewPronunciationButtonClicked)
            },
            takeAdapter = { pronunciationRecyclerAdapter = it }
        )
    }

    private fun createLanguagePopup() = PopupWindow(requireContext()).apply {
        contentView = View.inflate(requireContext(), R.layout.popup_available_languages, null)
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 20f
        isOutsideTouchable = true
        isFocusable = true
    }

    private fun initPresetNameInputDialog() {
        presetNameInputDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_pronunciation_name_input_dialog),
            takeEditText = { presetNameEditText = it },
            onTextChanged = { controller.dispatch(DialogTextChanged(it.toString())) },
            onPositiveClick = { controller.dispatch(PositiveDialogButtonClicked) },
            onNegativeClick = { controller.dispatch(NegativeDialogButtonClicked) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.orders.forEach(viewScope!!, ::executeOrder)
    }

    private fun setupView() {
        initAdapters()
        setOnClickListeners()
    }

    private fun initAdapters() {
        questionLanguageRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? ->
                controller.dispatch(QuestionLanguageSelected(language))
                questionLanguagePopup.dismiss()
            }
        )
        (questionLanguagePopup.contentView as RecyclerView).adapter =
            questionLanguageRecyclerAdapter

        answerLanguageRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? ->
                controller.dispatch(AnswerLanguageSelected(language))
                answerLanguagePopup.dismiss()
            }
        )
        (answerLanguagePopup.contentView as RecyclerView).adapter = answerLanguageRecyclerAdapter
    }

    private fun setOnClickListeners() {
        savePronunciationButton.setOnClickListener {
            controller.dispatch(SavePronunciationButtonClicked)
        }
        pronunciationNameTextView.setOnClickListener {
            showChoosePronunciationPopup()
        }
        questionLanguageTextView.setOnClickListener {
            showLanguagePopup(questionLanguagePopup, anchor = questionLanguageTextView)
        }
        questionAutoSpeakButton.setOnClickListener {
            val isOn = questionAutoSpeakSwitch.isChecked.toggle()
            controller.dispatch(QuestionAutoSpeakSwitchToggled(isOn))
        }
        answerLanguageTextView.setOnClickListener {
            showLanguagePopup(answerLanguagePopup, anchor = answerLanguageTextView)
        }
        answerAutoSpeakButton.setOnClickListener {
            val isOn = answerAutoSpeakSwitch.isChecked.toggle()
            controller.dispatch(AnswerAutoSpeakSwitchToggled(isOn))
        }
    }

    private fun showChoosePronunciationPopup() {
        val location = IntArray(2)
        pronunciationNameTextView.getLocationOnScreen(location)
        val x = location[0] + pronunciationNameTextView.width - choosePronunciationPopup.width
        val y = location[1]
        choosePronunciationPopup.showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun showLanguagePopup(popupWindow: PopupWindow, anchor: View) {
        popupWindow.width = anchor.width

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun observeViewModel() {
        with(viewModel) {
            currentPronunciation.observe {
                val pronunciationName = when {
                    it.id == 0L -> getString(R.string.default_name)
                    it.name.isEmpty() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
                }
                pronunciationNameTextView.text = pronunciationName
            }
            isSavePronunciationButtonEnabled.observe(
                onChange = { isSavePronunciationButtonEnabled ->
                    savePronunciationButton.visibility =
                        if (isSavePronunciationButtonEnabled) VISIBLE
                        else GONE
                },
                afterFirst = {
                    header.layoutTransition = LayoutTransition()
                })
            availablePronunciations.observe(onChange = pronunciationRecyclerAdapter::submitList)
            isPresetNameInputDialogVisible.observe { isVisible ->
                presetNameInputDialog.run { if (isVisible) show() else dismiss() }
            }
            dialogInputCheckResult.observe {
                presetNameEditText.error = when (it) {
                    OK -> null
                    EMPTY -> getString(R.string.error_message_empty_name)
                    OCCUPIED -> getString(R.string.error_message_occupied_name)
                }
            }
            selectedQuestionLanguage.observe { selectedQuestionLanguage ->
                questionLanguageTextView.text =
                    selectedQuestionLanguage?.displayLanguage
                        ?: getString(R.string.default_name)
            }
            dropdownQuestionLanguages
                .observe(onChange = questionLanguageRecyclerAdapter::submitList)
            questionAutoSpeak.observe(
                onChange = questionAutoSpeakSwitch::setChecked,
                afterFirst = {
                    questionAutoSpeakSwitch.jumpDrawablesToCurrentState()
                    questionAutoSpeakSwitch.visibility = VISIBLE
                }
            )
            selectedAnswerLanguage.observe { selectedAnswerLanguage ->
                answerLanguageTextView.text =
                    selectedAnswerLanguage?.displayLanguage
                        ?: getString(R.string.default_name)
            }
            dropdownAnswerLanguages
                .observe(onChange = answerLanguageRecyclerAdapter::submitList)
            answerAutoSpeak.observe(
                onChange = answerAutoSpeakSwitch::setChecked,
                afterFirst = {
                    answerAutoSpeakSwitch.jumpDrawablesToCurrentState()
                    answerAutoSpeakSwitch.visibility = VISIBLE
                }
            )
        }
    }

    private fun executeOrder(order: PronunciationOrder) {
        when (order) {
            is SetDialogText -> {
                presetNameEditText.setText(order.text)
                presetNameEditText.selectAll()
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val dialogState = savedInstanceState?.getBundle(STATE_KEY_DIALOG)
        if (dialogState != null) {
            presetNameInputDialog.onRestoreInstanceState(dialogState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(STATE_KEY_DIALOG, presetNameInputDialog.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        speaker.shutdown()
    }

    companion object {
        const val STATE_KEY_DIALOG = "pronunciationNameInputDialog"
    }
}

class LanguageRecyclerAdapter(
    private val onItemClick: (language: Locale?) -> Unit
) : ListAdapter<DropdownLanguage, ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.apply {
            val dropdownLanguage: DropdownLanguage = getItem(position)
            if (dropdownLanguage.language == null) {
                languageNameTextView.text = context.getString(R.string.default_name)
                flagTextView.text = null
            } else {
                languageNameTextView.text = dropdownLanguage.language.displayLanguage
                flagTextView.text = dropdownLanguage.language.toFlagEmoji()
            }
            if (dropdownLanguage.isSelected) {
                val backgroundColor =
                    ContextCompat.getColor(context, R.color.selected_item_background)
                languageFrame.setBackgroundColor(backgroundColor)
            } else {
                languageFrame.background = null
            }
            languageItemButton.setOnClickListener {
                onItemClick(dropdownLanguage.language)
            }
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<DropdownLanguage>() {
        override fun areItemsTheSame(
            oldItem: DropdownLanguage,
            newItem: DropdownLanguage
        ): Boolean {
            return oldItem.language == newItem.language
        }

        override fun areContentsTheSame(
            oldItem: DropdownLanguage,
            newItem: DropdownLanguage
        ): Boolean {
            return oldItem == newItem
        }
    }
}