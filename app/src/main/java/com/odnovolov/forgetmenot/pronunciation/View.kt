package com.odnovolov.forgetmenot.pronunciation

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
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
import com.odnovolov.forgetmenot.common.database.Pronunciation
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
    private lateinit var pronunciationPopup: PopupWindow
    private lateinit var pronunciationRecyclerAdapter: PronunciationRecyclerAdapter
    private lateinit var questionLanguagePopup: PopupWindow
    private lateinit var questionLanguageRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var answerLanguagePopup: PopupWindow
    private lateinit var answerLanguageRecyclerAdapter: LanguageRecyclerAdapter
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
        pronunciationPopup = createPronunciationPopup()
        questionLanguagePopup = createLanguagePopup()
        answerLanguagePopup = createLanguagePopup()
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
    }

    private fun createPronunciationPopup() = PopupWindow(requireContext()).apply {
        contentView = View.inflate(requireContext(), R.layout.popup_choose_pronunciation, null)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        takeOrders()
    }

    private fun setupView() {
        initAdapters()
        setOnClickListeners()
    }

    private fun initAdapters() {
        pronunciationRecyclerAdapter = PronunciationRecyclerAdapter()
        val sharedPronunciationRecyclerView = pronunciationPopup.contentView
            .findViewById<RecyclerView>(R.id.sharedPronunciationRecyclerView)
        sharedPronunciationRecyclerView.adapter = pronunciationRecyclerAdapter

        questionLanguageRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? ->
                controller.dispatch(QuestionLanguageSelected(language))
            }
        )
        (questionLanguagePopup.contentView as RecyclerView).adapter =
            questionLanguageRecyclerAdapter

        answerLanguageRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? ->
                controller.dispatch(AnswerLanguageSelected(language))
            }
        )
        (answerLanguagePopup.contentView as RecyclerView).adapter = answerLanguageRecyclerAdapter
    }

    private fun setOnClickListeners() {
        pronunciationTitleTextView.setOnClickListener {
            showChoosePronunciationPopup()
        }
        savePronunciationButton.setOnClickListener {
            controller.dispatch(SavePronunciationButtonClicked)
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
        pronunciationPopup.width = 200.dp

        val location = IntArray(2)
        pronunciationTitleTextView.getLocationOnScreen(location)
        val x = location[0] + pronunciationTitleTextView.width - pronunciationPopup.width
        val y = location[1]
        pronunciationPopup.showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
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
            isSavePronunciationButtonEnabled.observe { isSavePronunciationButtonEnabled ->
                savePronunciationButton.visibility =
                    if (isSavePronunciationButtonEnabled) View.VISIBLE
                    else View.GONE
            }
            sharedPronunciations.observe(onChange = pronunciationRecyclerAdapter::submitList)
            selectedQuestionLanguage.observe { selectedQuestionLanguage ->
                questionLanguageTextView.text =
                    selectedQuestionLanguage?.displayLanguage ?: "Default"
            }
            dropdownQuestionLanguages
                .observe(onChange = questionLanguageRecyclerAdapter::submitList)
            questionAutoSpeak.observe(onChange = questionAutoSpeakSwitch::setChecked)
            selectedAnswerLanguage.observe { selectedAnswerLanguage ->
                answerLanguageTextView.text =
                    selectedAnswerLanguage?.displayLanguage ?: "Default"
            }
            dropdownAnswerLanguages
                .observe(onChange = answerLanguageRecyclerAdapter::submitList)
            answerAutoSpeak.observe(onChange = answerAutoSpeakSwitch::setChecked)
        }
    }

    private fun takeOrders() {
        viewScope!!.launch {
            for (order in controller.orders) {
                when (order) {
                    DismissQuestionDropdownList -> {
                        questionLanguagePopup.dismiss()
                    }
                    DismissAnswerDropdownList -> {
                        answerLanguagePopup.dismiss()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.dispose()
        speaker.shutdown()
        LeakSentry.refWatcher.watch(this)
    }
}

class PronunciationRecyclerAdapter
    : ListAdapter<Pronunciation, PronunciationRecyclerAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shared_pronunciation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val pronunciation = getItem(position)
        val textView = viewHolder.itemView as TextView
        textView.text = pronunciation.name
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    class DiffCallback : DiffUtil.ItemCallback<Pronunciation>() {
        override fun areItemsTheSame(
            oldItem: Pronunciation,
            newItem: Pronunciation
        ): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: Pronunciation,
            newItem: Pronunciation
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
                languageItemButton.setBackgroundColor(translucentColor)
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