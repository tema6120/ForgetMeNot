package com.odnovolov.forgetmenot.pronunciation

import android.animation.LayoutTransition
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.*
import com.odnovolov.forgetmenot.common.NameCheckResult.*
import com.odnovolov.forgetmenot.common.database.asBoolean
import com.odnovolov.forgetmenot.pronunciation.LanguageRecyclerAdapter.ViewHolder
import com.odnovolov.forgetmenot.pronunciation.PronunciationEvent.*
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import kotlinx.android.synthetic.main.item_language.view.*
import leakcanary.LeakSentry
import java.util.*


class PronunciationFragment : BaseFragment() {

    private val controller = PronunciationController()
    private val viewModel = PronunciationViewModel()
    private lateinit var choosePronunciationPopup: PopupWindow
    private lateinit var pronunciationRecyclerAdapter: PronunciationRecyclerAdapter
    private lateinit var questionLanguagePopup: PopupWindow
    private lateinit var questionLanguageRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var answerLanguagePopup: PopupWindow
    private lateinit var answerLanguageRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var speaker: Speaker
    private lateinit var nameInputDialog: AlertDialog
    private lateinit var nameInput: EditText

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
        choosePronunciationPopup = createChoosePronunciationPopup()
        questionLanguagePopup = createLanguagePopup()
        answerLanguagePopup = createLanguagePopup()
        initDialog()
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
    }

    private fun createChoosePronunciationPopup() = PopupWindow(requireContext()).apply {
        contentView = View.inflate(requireContext(), R.layout.popup_choose_pronunciation, null)
        val addNewPronunciationButton: ImageButton =
            contentView.findViewById(R.id.addNewPronunciationButton)
        addNewPronunciationButton.setOnClickListener {
            controller.dispatch(AddNewPronunciationButtonClicked)
        }
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 20f
        isOutsideTouchable = true
        isFocusable = true
    }

    private fun createLanguagePopup() = PopupWindow(requireContext()).apply {
        contentView = View.inflate(requireContext(), R.layout.popup_available_languages, null)
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 20f
        isOutsideTouchable = true
        isFocusable = true
    }

    private fun initDialog() {
        nameInputDialog = createInputDialog(
            title = getString(R.string.title_pronunciation_name_input_dialog),
            takeEditText = { nameInput = it },
            onTextChanged = { controller.dispatch(DialogTextChanged(it.toString())) },
            onPositiveClick = { controller.dispatch(PositiveDialogButtonClicked) },
            onNegativeClick = { controller.dispatch(NegativeDialogButtonClicked) }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        initAdapters()
        setOnClickListeners()
    }

    private fun initAdapters() {
        pronunciationRecyclerAdapter = PronunciationRecyclerAdapter(
            onItemClick = { pronunciationId ->
                controller.dispatch(PronunciationButtonClicked(pronunciationId))
                choosePronunciationPopup.dismiss()
            }
        )
        val availablePronunciationsRecyclerView = choosePronunciationPopup.contentView
            .findViewById<RecyclerView>(R.id.availablePronunciationsRecyclerView)
        availablePronunciationsRecyclerView.adapter = pronunciationRecyclerAdapter

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
        pronunciationTitleTextView.setOnClickListener {
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
        choosePronunciationPopup.width = 196.dp

        val location = IntArray(2)
        pronunciationTitleTextView.getLocationOnScreen(location)
        val x = location[0] + pronunciationTitleTextView.width - choosePronunciationPopup.width
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
                    it.id == 0L -> getString(R.string.default_pronunciation_name)
                    it.name.isEmpty() -> getString(R.string.individual_pronunciation_name)
                    else -> it.name
                }
                pronunciationTitleTextView.text = pronunciationName
            }
            isSavePronunciationButtonEnabled.observe(
                onChange = { isSavePronunciationButtonEnabled ->
                    savePronunciationButton.visibility =
                        if (isSavePronunciationButtonEnabled) View.VISIBLE
                        else View.GONE
                },
                afterFirst = {
                    header.layoutTransition = LayoutTransition()
                })
            availablePronunciations.observe(onChange = pronunciationRecyclerAdapter::submitList)
            isDialogVisible.observe { isDialogVisible ->
                if (isDialogVisible) {
                    nameInputDialog.show()
                } else {
                    nameInputDialog.dismiss()
                }
            }
            dialogInputCheckResult.observe {
                nameInput.error = when (it) {
                    OK -> null
                    EMPTY -> getString(R.string.error_message_empty_name)
                    OCCUPIED -> getString(R.string.error_message_occupied_name)
                }
            }
            selectedQuestionLanguage.observe { selectedQuestionLanguage ->
                questionLanguageTextView.text =
                    selectedQuestionLanguage?.displayLanguage ?: "Default"
            }
            dropdownQuestionLanguages
                .observe(onChange = questionLanguageRecyclerAdapter::submitList)
            questionAutoSpeak.observe(
                onChange = questionAutoSpeakSwitch::setChecked,
                afterFirst = {
                    questionAutoSpeakSwitch.jumpDrawablesToCurrentState()
                    questionAutoSpeakSwitch.visibility = View.VISIBLE
                }
            )
            selectedAnswerLanguage.observe { selectedAnswerLanguage ->
                answerLanguageTextView.text =
                    selectedAnswerLanguage?.displayLanguage ?: "Default"
            }
            dropdownAnswerLanguages
                .observe(onChange = answerLanguageRecyclerAdapter::submitList)

            answerAutoSpeak.observe(
                onChange = answerAutoSpeakSwitch::setChecked,
                afterFirst = {
                    answerAutoSpeakSwitch.jumpDrawablesToCurrentState()
                    answerAutoSpeakSwitch.visibility = View.VISIBLE
                }
            )
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val dialogState = savedInstanceState?.getBundle(STATE_KEY_DIALOG)
        if (dialogState != null) {
            nameInputDialog.onRestoreInstanceState(dialogState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(STATE_KEY_DIALOG, nameInputDialog.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        speaker.shutdown()
        LeakSentry.refWatcher.watch(this)
    }

    companion object {
        const val STATE_KEY_DIALOG = "pronunciationNameInputDialog"
    }
}

class PronunciationRecyclerAdapter(
    private val onItemClick: (pronunciationId: Long) -> Unit
) :
    ListAdapter<AvailablePronunciation, PronunciationRecyclerAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shared_pronunciation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val availablePronunciation = getItem(position)
        val textView = viewHolder.itemView as TextView
        textView.text = when {
            availablePronunciation.id == 0L ->
                textView.context.getString(R.string.default_pronunciation_name)
            availablePronunciation.name.isEmpty() ->
                textView.context.getString(R.string.individual_pronunciation_name)
            else ->
                "'${availablePronunciation.name}'"
        }
        if (availablePronunciation.isSelected.asBoolean()) {
            val backgroundColor =
                ContextCompat.getColor(textView.context, R.color.selected_item_background)
            textView.setBackgroundColor(backgroundColor)
        } else {
            textView.background = null
        }
        textView.setOnClickListener {
            onItemClick(availablePronunciation.id)
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<AvailablePronunciation>() {
        override fun areItemsTheSame(
            oldItem: AvailablePronunciation,
            newItem: AvailablePronunciation
        ): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: AvailablePronunciation,
            newItem: AvailablePronunciation
        ): Boolean {
            return oldItem == newItem
        }
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
                languageNameTextView.text = "Default"
                flagTextView.text = null
            } else {
                languageNameTextView.text = dropdownLanguage.language.displayLanguage
                flagTextView.text = dropdownLanguage.language.toFlagEmoji()
            }
            if (dropdownLanguage.isSelected) {
                val backgroundColor = ContextCompat.getColor(context, R.color.colorAccent)
                val translucentColor = with(backgroundColor) {
                    Color.argb(alpha / 2, red, green, blue)
                }
                languageFrame.setBackgroundColor(translucentColor)
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