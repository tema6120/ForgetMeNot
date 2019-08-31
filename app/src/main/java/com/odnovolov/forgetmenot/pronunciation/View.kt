package com.odnovolov.forgetmenot.pronunciation

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.BaseFragment
import com.odnovolov.forgetmenot.common.toFlagEmoji
import com.odnovolov.forgetmenot.common.Speaker
import com.odnovolov.forgetmenot.pronunciation.LanguageRecyclerAdapter.ViewHolder
import com.odnovolov.forgetmenot.pronunciation.PronunciationEvent.*
import com.odnovolov.forgetmenot.pronunciation.PronunciationOrder.DismissAnswerDropdownList
import com.odnovolov.forgetmenot.pronunciation.PronunciationOrder.DismissQuestionDropdownList
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import kotlinx.android.synthetic.main.item_language.view.*
import kotlinx.coroutines.launch
import leakcanary.LeakSentry
import java.util.*

class PronunciationFragment : BaseFragment() {

    private val controller = PronunciationController()
    private val viewModel = PronunciationViewModel()
    private lateinit var questionLanguagesPopup: PopupWindow
    private lateinit var questionLanguagesRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var answerLanguagesPopup: PopupWindow
    private lateinit var answerLanguagesRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var speaker: Speaker

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
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        takeOrders()
    }

    private fun setupView() {
        questionLanguagesPopup = createPopup()
        questionLanguagesRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? ->
                controller.dispatch(QuestionLanguageSelected(language))
            }
        )
        (questionLanguagesPopup.contentView as RecyclerView).adapter =
            questionLanguagesRecyclerAdapter

        answerLanguagesPopup = createPopup()
        answerLanguagesRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? ->
                controller.dispatch(AnswerLanguageSelected(language))
            }
        )
        (answerLanguagesPopup.contentView as RecyclerView).adapter = answerLanguagesRecyclerAdapter
        questionLanguageTextView.setOnClickListener {
            showPopup(questionLanguagesPopup, anchor = questionLanguageTextView)
        }
        questionAutoSpeakButton.setOnClickListener {
            controller.dispatch(QuestionAutoSpeakSwitchClicked)
        }
        answerLanguageTextView.setOnClickListener {
            showPopup(answerLanguagesPopup, anchor = answerLanguageTextView)
        }
        answerAutoSpeakButton.setOnClickListener {
            controller.dispatch(AnswerAutoSpeakSwitchClicked)
        }
    }

    private fun createPopup() = PopupWindow(requireContext()).apply {
        contentView = View.inflate(requireContext(), R.layout.popup_available_languages, null)
        setBackgroundDrawable(ColorDrawable(Color.LTGRAY))
        elevation = 20f
        isOutsideTouchable = true
        isFocusable = true
    }

    private fun observeViewModel() {
        with(viewModel) {
            selectedQuestionLanguage.observe { selectedQuestionLanguage ->
                questionLanguageTextView.text =
                    selectedQuestionLanguage?.displayLanguage ?: "Default"
            }
            dropdownQuestionLanguages
                .observe(onChange = questionLanguagesRecyclerAdapter::submitList)
            questionAutoSpeak.observe(onChange = questionAutoSpeakSwitch::setChecked)
            selectedAnswerLanguage.observe { selectedAnswerLanguage ->
                answerLanguageTextView.text =
                    selectedAnswerLanguage?.displayLanguage ?: "Default"
            }
            dropdownAnswerLanguages
                .observe(onChange = answerLanguagesRecyclerAdapter::submitList)
            answerAutoSpeak.observe(onChange = answerAutoSpeakSwitch::setChecked)
        }
    }

    private fun takeOrders() {
        viewScope!!.launch {
            for (order in controller.orders) {
                when (order) {
                    DismissQuestionDropdownList -> {
                        questionLanguagesPopup.dismiss()
                    }
                    DismissAnswerDropdownList -> {
                        answerLanguagesPopup.dismiss()
                    }
                }
            }
        }
    }

    private fun showPopup(popupWindow: PopupWindow, anchor: View) {
        val size: Int = anchor.width
        popupWindow.width = size
        popupWindow.height = size

        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        speaker.shutdown()
        LeakSentry.refWatcher.watch(this)
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
                languageItemButton.setBackgroundColor(Color.GRAY)
            } else {
                languageItemButton.background = null
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