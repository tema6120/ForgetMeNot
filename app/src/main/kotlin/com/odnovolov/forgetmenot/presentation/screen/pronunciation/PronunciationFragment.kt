package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.toFlagEmoji
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationEvent.*
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import kotlinx.coroutines.launch
import java.util.*

class PronunciationFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        PronunciationDiScope.reopenIfClosed()
    }

    private var controller: PronunciationController? = null
    private lateinit var viewModel: PronunciationViewModel
    private lateinit var questionLanguagePopup: PopupWindow
    private lateinit var questionLanguageAdapter: LanguageAdapter
    private lateinit var answerLanguagePopup: PopupWindow
    private lateinit var answerLanguageAdapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewCoroutineScope!!.launch {
            val diScope = PronunciationDiScope.get()
            controller = diScope.controller
            viewModel = diScope.viewModel
            presetView.inject(diScope.presetController, diScope.presetViewModel)
            observeViewModel()
            Looper.myQueue().addIdleHandler {
                finishSetup()
                false
            }
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            selectedQuestionLanguage.observe { selectedQuestionLanguage: Locale? ->
                questionLanguageTextView.text = getSelectedLanguageText(selectedQuestionLanguage)
            }
            questionAutoSpeak.observe { questionAutoSpeak: Boolean ->
                questionAutoSpeakSwitch.isChecked = questionAutoSpeak
                questionAutoSpeakSwitch.uncover()
            }
            selectedAnswerLanguage.observe { selectedAnswerLanguage: Locale? ->
                answerLanguageTextView.text = getSelectedLanguageText(selectedAnswerLanguage)
            }
            answerAutoSpeak.observe { answerAutoSpeak: Boolean ->
                answerAutoSpeakSwitch.isChecked = answerAutoSpeak
                answerAutoSpeakSwitch.uncover()
            }
            speakTextInBrackets.observe { speakTextInBrackets: Boolean ->
                speakTextInBracketsSwitch.isChecked = speakTextInBrackets
                speakTextInBracketsSwitch.uncover()
            }
        }
    }

    private fun getSelectedLanguageText(locale: Locale?): String {
        return if (locale != null) {
            val flagEmoji: String? = locale.toFlagEmoji()
            if (flagEmoji != null) {
                "${locale.displayLanguage} $flagEmoji"
            } else {
                locale.displayLanguage
            }
        } else {
            getString(R.string.default_name)
        }
    }

    private fun finishSetup() {
        questionLanguagePopup = createLanguagePopup()
        answerLanguagePopup = createLanguagePopup()
        initAdapters()
        setOnClickListeners()
        viewModel.displayedQuestionLanguages.observe(questionLanguageAdapter::submitList)
        viewModel.displayedAnswerLanguages.observe(answerLanguageAdapter::submitList)
    }

    private fun createLanguagePopup() = PopupWindow(requireContext()).apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        contentView = View.inflate(requireContext(), R.layout.popup_available_languages, null)
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 20f
        isOutsideTouchable = true
        isFocusable = true
    }

    private fun initAdapters() {
        questionLanguageAdapter = LanguageAdapter(
            onItemClick = { language: Locale? ->
                controller?.dispatch(QuestionLanguageSelected(language))
                questionLanguagePopup.dismiss()
            }
        )
        (questionLanguagePopup.contentView as RecyclerView).adapter = questionLanguageAdapter

        answerLanguageAdapter = LanguageAdapter(
            onItemClick = { language: Locale? ->
                controller?.dispatch(AnswerLanguageSelected(language))
                answerLanguagePopup.dismiss()
            }
        )
        (answerLanguagePopup.contentView as RecyclerView).adapter = answerLanguageAdapter
    }

    private fun setOnClickListeners() {
        questionLanguageTextView.setOnClickListener {
            showLanguagePopup(questionLanguagePopup, anchor = questionLanguageTextView)
        }
        questionAutoSpeakButton.setOnClickListener {
            controller?.dispatch(QuestionAutoSpeakSwitchToggled)
        }
        answerLanguageTextView.setOnClickListener {
            showLanguagePopup(answerLanguagePopup, anchor = answerLanguageTextView)
        }
        answerAutoSpeakButton.setOnClickListener {
            controller?.dispatch(AnswerAutoSpeakSwitchToggled)
        }
        speakTextInBracketsButton.setOnClickListener {
            controller?.dispatch(SpeakTextInBracketsSwitchToggled)
        }
        goToTtsSettingsButton.setOnClickListener {
            navigateToTtsSettings()
        }
    }

    private fun showLanguagePopup(popupWindow: PopupWindow, anchor: View) {
        popupWindow.width = anchor.width
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val x = location[0]
        val y = location[1]
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, x, y)
    }

    private fun navigateToTtsSettings() {
        startActivity(
            Intent().apply {
                action = "com.android.settings.TTS_SETTINGS"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            getBundle(STATE_KEY_PRONUNCIATION_PRESET_VIEW)
                ?.let(presetView::restoreInstanceState)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(
            STATE_KEY_PRONUNCIATION_PRESET_VIEW,
            presetView.saveInstanceState()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        PronunciationDiScope.close()
    }

    companion object {
        const val STATE_KEY_PRONUNCIATION_PRESET_VIEW = "STATE_KEY_PRONUNCIATION_PRESET_VIEW"
    }
}