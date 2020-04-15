package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.preset.PresetFragment
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
    private lateinit var questionLanguagePopup: PopupWindow
    private lateinit var questionLanguageAdapter: LanguageAdapter
    private lateinit var answerLanguagePopup: PopupWindow
    private lateinit var answerLanguageAdapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        questionLanguagePopup = createLanguagePopup()
        answerLanguagePopup = createLanguagePopup()
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = PronunciationDiScope.get()
            controller = diScope.controller
            val presetFragment = childFragmentManager
                .findFragmentByTag("Pronunciation Preset Tag") as PresetFragment
            presetFragment.inject(diScope.presetController, diScope.presetViewModel)
            observeViewModel(diScope.viewModel)
        }
    }

    private fun setupView() {
        initAdapters()
        setOnClickListeners()
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

    private fun observeViewModel(viewModel: PronunciationViewModel) {
        with(viewModel) {
            selectedQuestionLanguage.observe { selectedQuestionLanguage ->
                questionLanguageTextView.text =
                    selectedQuestionLanguage?.displayLanguage ?: getString(R.string.default_name)
            }
            displayedQuestionLanguages.observe(questionLanguageAdapter::submitList)
            questionAutoSpeak.observe { questionAutoSpeak: Boolean ->
                questionAutoSpeakSwitch.isChecked = questionAutoSpeak
                questionAutoSpeakSwitch.uncover()
            }
            selectedAnswerLanguage.observe { selectedAnswerLanguage ->
                answerLanguageTextView.text =
                    selectedAnswerLanguage?.displayLanguage ?: getString(R.string.default_name)
            }
            displayedAnswerLanguages.observe(answerLanguageAdapter::submitList)
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

    override fun onDestroy() {
        super.onDestroy()
        PronunciationDiScope.close()
    }
}