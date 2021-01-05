package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.PopupWindow
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DeckSettingsDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseDiScope
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationEvent.*
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import kotlinx.coroutines.launch
import java.util.*

class PronunciationFragment : BaseFragment() {
    init {
        DeckSettingsDiScope.reopenIfClosed()
        ExampleExerciseDiScope.reopenIfClosed()
        PronunciationDiScope.reopenIfClosed()
    }

    private var controller: PronunciationController? = null
    private lateinit var viewModel: PronunciationViewModel
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
    private lateinit var exampleFragment: ExampleExerciseFragment

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
        viewCoroutineScope!!.launch {
            val diScope = PronunciationDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        exampleFragment = (childFragmentManager.findFragmentByTag("ExampleExerciseFragment")
                as ExampleExerciseFragment)
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        helpButton.setOnClickListener {
            controller?.dispatch(HelpButtonClicked)
        }
        questionLanguageButton.setOnClickListener {
            questionLanguagePopup.show(anchor = questionLanguageButton)
        }
        questionAutoSpeakButton.setOnClickListener {
            controller?.dispatch(QuestionAutoSpeakSwitchToggled)
        }
        answerLanguageButton.setOnClickListener {
            answerLanguagePopup.show(anchor = answerLanguageButton)
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
                updateLanguageButton(isQuestion = true, selectedQuestionLanguage)
            }
            displayedQuestionLanguages.observe(questionLanguageAdapter::submitList)
            questionAutoSpeaking.observe { questionAutoSpeak: Boolean ->
                questionAutoSpeakSwitch.isChecked = questionAutoSpeak
                questionAutoSpeakSwitch.uncover()
            }
            selectedAnswerLanguage.observe { selectedAnswerLanguage: Locale? ->
                updateLanguageButton(isQuestion = false, selectedAnswerLanguage)
            }
            displayedAnswerLanguages.observe(answerLanguageAdapter::submitList)
            answerAutoSpeaking.observe { answerAutoSpeak: Boolean ->
                answerAutoSpeakSwitch.isChecked = answerAutoSpeak
                answerAutoSpeakSwitch.uncover()
            }
            speakTextInBrackets.observe { speakTextInBrackets: Boolean ->
                speakTextInBracketsSwitch.isChecked = speakTextInBrackets
                speakTextInBracketsSwitch.uncover()
            }
        }
    }

    private fun updateLanguageButton(
        isQuestion: Boolean,
        language: Locale?
    ) {
        val languageTextView = if (isQuestion) questionLanguageTextView else answerLanguageTextView
        val flagTextView = if (isQuestion) questionFlagTextView else answerFlagTextView
        val languageButton = if (isQuestion) questionLanguageButton else answerLanguageButton
        languageTextView.text = language?.displayLanguage
            ?: getString(R.string.default_language)
        val flag: String? = language?.toFlagEmoji()
        val hasFlag = flag != null
        if (hasFlag) {
            flagTextView.text = flag
            flagTextView.isVisible = true
        }
        flagTextView.isVisible = hasFlag
        languageTextView.updatePaddingRelative(start = if (hasFlag) 8.dp else 18.dp)
        languageTextView.updateLayoutParams<ConstraintLayout.LayoutParams> {
            marginStart = if (hasFlag) 0 else 12.dp
        }
        languageButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
            startToStart = if (hasFlag) flagTextView.id else languageTextView.id
        }
    }

    private fun createLanguagePopup(): PopupWindow {
        val content = View.inflate(requireContext(), R.layout.popup_available_languages, null)
        return LightPopupWindow(content)
    }

    private fun navigateToTtsSettings() {
        startActivity(
            Intent().apply {
                action = "com.android.settings.TTS_SETTINGS"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    override fun onResume() {
        super.onResume()
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        behavior.addBottomSheetCallback(bottomSheetCallback)
        exampleFragment.notifyBottomSheetStateChanged(behavior.state)
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        behavior.removeBottomSheetCallback(bottomSheetCallback)
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            PronunciationDiScope.close()
        }
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            exampleFragment.notifyBottomSheetStateChanged(newState)
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }

    private val backPressInterceptor = object : MainActivity.BackPressInterceptor {
        override fun onBackPressed(): Boolean {
            val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
            return if (behavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
                behavior.state = BottomSheetBehavior.STATE_COLLAPSED
                true
            } else {
                false
            }
        }
    }
}