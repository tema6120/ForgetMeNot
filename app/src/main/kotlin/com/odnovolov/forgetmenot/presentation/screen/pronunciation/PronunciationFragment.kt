package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.MeasureSpec
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.SpeakerImpl.Event.SpeakError
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.decksetup.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationEvent.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.ReasonForInabilityToSpeak.*
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.SpeakingStatus.*
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import kotlinx.android.synthetic.main.popup_speak_error.view.*
import kotlinx.coroutines.launch
import java.util.*

class PronunciationFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        PronunciationDiScope.reopenIfClosed()
    }

    private var controller: PronunciationController? = null
    private lateinit var viewModel: PronunciationViewModel
    private val speakErrorPopup: PopupWindow by lazy { createSpeakErrorPopup() }
    private val speakErrorToast: Toast by lazy {
        Toast.makeText(requireContext(), R.string.error_message_failed_to_speak, Toast.LENGTH_SHORT)
    }
    private val questionLanguagePopup: PopupWindow by lazy {
        createLanguagePopup().apply {
            (contentView as RecyclerView).adapter = questionLanguageAdapter
        }
    }
    private val questionLanguageAdapter = LanguageAdapter(
        onItemClick = { language: Locale? ->
            controller?.dispatch(QuestionLanguageSelected(language))
            questionLanguagePopup.dismiss()
        }
    )
    private val answerLanguagePopup: PopupWindow by lazy {
        createLanguagePopup().apply {
            (contentView as RecyclerView).adapter = answerLanguageAdapter
        }
    }
    private val answerLanguageAdapter = LanguageAdapter(
        onItemClick = { language: Locale? ->
            controller?.dispatch(AnswerLanguageSelected(language))
            answerLanguagePopup.dismiss()
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = PronunciationDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            presetView.inject(diScope.presetController, diScope.presetViewModel)
            observeViewModel()
        }
    }

    private fun setupView() {
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

    private fun observeViewModel() {
        with(viewModel) {
            selectedQuestionLanguage.observe { selectedQuestionLanguage: Locale? ->
                questionLanguageTextView.text = getSelectedLanguageText(selectedQuestionLanguage)
            }
            displayedQuestionLanguages.observe(questionLanguageAdapter::submitList)
            questionAutoSpeak.observe { questionAutoSpeak: Boolean ->
                questionAutoSpeakSwitch.isChecked = questionAutoSpeak
                questionAutoSpeakSwitch.uncover()
            }
            isQuestionPreparingToBePronounced.observe { isPreparing: Boolean ->
                testPronunciationOfQuestionProgressBar.visibility =
                    if (isPreparing) View.VISIBLE else View.INVISIBLE
            }
            questionSpeakingStatus.observe { speakingStatus: SpeakingStatus ->
                with(testPronunciationOfQuestionButton) {
                    setImageResource(getSpeakIconResId(speakingStatus))
                    setOnClickListener {
                        when (speakingStatus) {
                            Speaking -> {
                                controller?.dispatch(StopSpeakButtonClicked)
                            }
                            NotSpeaking -> {
                                controller?.dispatch(TestPronunciationOfQuestionButtonClicked)
                            }
                            CannotSpeak -> {
                                val reasonForInabilityToSpeak: ReasonForInabilityToSpeak? =
                                    viewModel.reasonForInabilityToSpeakQuestion.firstBlocking()
                                showSpeakErrorPopup(
                                    testPronunciationOfQuestionButton,
                                    reasonForInabilityToSpeak
                                )
                            }
                        }
                    }
                    setTooltipTextForSpeakButton(this, speakingStatus)
                }
            }
            selectedAnswerLanguage.observe { selectedAnswerLanguage: Locale? ->
                answerLanguageTextView.text = getSelectedLanguageText(selectedAnswerLanguage)
            }
            displayedAnswerLanguages.observe(answerLanguageAdapter::submitList)
            answerAutoSpeak.observe { answerAutoSpeak: Boolean ->
                answerAutoSpeakSwitch.isChecked = answerAutoSpeak
                answerAutoSpeakSwitch.uncover()
            }
            isAnswerPreparingToBePronounced.observe { isPreparing: Boolean ->
                testPronunciationOfAnswerProgressBar.visibility =
                    if (isPreparing) View.VISIBLE else View.INVISIBLE
            }
            answerSpeakingStatus.observe { speakingStatus: SpeakingStatus ->
                with(testPronunciationOfAnswerButton) {
                    setImageResource(getSpeakIconResId(speakingStatus))
                    setOnClickListener {
                        when (speakingStatus) {
                            Speaking -> {
                                controller?.dispatch(StopSpeakButtonClicked)
                            }
                            NotSpeaking -> {
                                controller?.dispatch(TestPronunciationOfAnswerButtonClicked)
                            }
                            CannotSpeak -> {
                                val reasonForInabilityToSpeak: ReasonForInabilityToSpeak? =
                                    viewModel.reasonForInabilityToSpeakAnswer.firstBlocking()
                                showSpeakErrorPopup(
                                    testPronunciationOfAnswerButton,
                                    reasonForInabilityToSpeak
                                )
                            }
                        }
                    }
                    setTooltipTextForSpeakButton(this, speakingStatus)
                }
            }
            speakTextInBrackets.observe { speakTextInBrackets: Boolean ->
                speakTextInBracketsSwitch.isChecked = speakTextInBrackets
                speakTextInBracketsSwitch.uncover()
            }
            speakerEvents.observe { event: SpeakerImpl.Event ->
                when (event) {
                    SpeakError -> speakErrorToast.show()
                }
            }
        }
    }

    private fun getSpeakIconResId(speakingStatus: SpeakingStatus): Int {
        return when (speakingStatus) {
            Speaking -> R.drawable.ic_volume_off_dark_24dp
            NotSpeaking -> R.drawable.ic_volume_up_dark_24dp
            CannotSpeak -> R.drawable.ic_volume_error_24
        }
    }

    private fun setTooltipTextForSpeakButton(speakButton: View, speakingStatus: SpeakingStatus) {
        speakButton.contentDescription = getString(
            when (speakingStatus) {
                Speaking -> R.string.description_stop_speak_button
                NotSpeaking -> R.string.description_test_pronunciation_button
                CannotSpeak -> R.string.description_cannot_speak_button
            }
        )
        TooltipCompat.setTooltipText(speakButton, speakButton.contentDescription)
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

    private fun createSpeakErrorPopup(): PopupWindow {
        val content = View.inflate(requireContext(), R.layout.popup_speak_error, null).apply {
            goToTtsSettingsButton.setOnClickListener {
                navigateToTtsSettings()
                speakErrorPopup.dismiss()
            }
        }
        return PopupWindow(context).apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            contentView = content
            setBackgroundDrawable(
                ColorDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.exercise_control_panel_popup_background
                    )
                )
            )
            elevation = 20f.dp
            isOutsideTouchable = true
            isFocusable = true
        }
    }

    private fun showSpeakErrorPopup(
        anchor: View,
        reasonForInabilityToSpeak: ReasonForInabilityToSpeak?
    ) {
        speakErrorPopup.dismiss()
        val content: View = speakErrorPopup.contentView
        content.speakErrorDescriptionTextView.text =
            getSpeakErrorDescription(reasonForInabilityToSpeak)
        content.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val anchorLocation = IntArray(2).also { anchor.getLocationOnScreen(it) }
        val x: Int = maxOf(8.dp, anchorLocation[0] + anchor.width - content.measuredWidth)
        val y: Int = anchorLocation[1]
        speakErrorPopup.showAtLocation(
            anchor.rootView,
            Gravity.NO_GRAVITY,
            x,
            y
        )
    }

    private fun getSpeakErrorDescription(
        reasonForInabilityToSpeak: ReasonForInabilityToSpeak?
    ): String? {
        return when (reasonForInabilityToSpeak) {
            null -> null
            is FailedToInitializeSpeaker -> {
                if (reasonForInabilityToSpeak.ttsEngine == null) {
                    getString(R.string.speak_error_description_failed_to_initialized)
                } else {
                    getString(
                        R.string.speak_error_description_failed_to_initialized_with_specifying_tts_engine,
                        reasonForInabilityToSpeak.ttsEngine
                    )
                }
            }
            is LanguageIsNotSupported -> {
                if (reasonForInabilityToSpeak.ttsEngine == null) {
                    getString(
                        R.string.speak_error_description_language_is_not_supported,
                        reasonForInabilityToSpeak.language.displayLanguage
                    )
                } else {
                    getString(
                        R.string.speak_error_description_language_is_not_supported_with_specifying_tts_engine,
                        reasonForInabilityToSpeak.ttsEngine,
                        reasonForInabilityToSpeak.language.displayLanguage
                    )
                }
            }
            is MissingDataForLanguage -> {
                getString(
                    R.string.speak_error_description_missing_data_for_language,
                    reasonForInabilityToSpeak.language.displayLanguage
                )
            }
        }
    }

    private fun createLanguagePopup() = PopupWindow(requireContext()).apply {
        width = WindowManager.LayoutParams.WRAP_CONTENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        contentView = View.inflate(requireContext(), R.layout.popup_available_languages, null)
        setBackgroundDrawable(ColorDrawable(Color.WHITE))
        elevation = 20f.dp
        isOutsideTouchable = true
        isFocusable = true
    }

    private fun showLanguagePopup(popupWindow: PopupWindow, anchor: View) {
        popupWindow.width = anchor.width
        val content: View = popupWindow.contentView
        content.measure(anchor.width, MeasureSpec.UNSPECIFIED)
        val anchorLocation = IntArray(2).also { anchor.getLocationOnScreen(it) }
        val x = anchorLocation[0]
        val y = anchorLocation[1] + anchor.height / 2 - content.measuredHeight / 2
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

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            PronunciationDiScope.close()
        }
    }
}