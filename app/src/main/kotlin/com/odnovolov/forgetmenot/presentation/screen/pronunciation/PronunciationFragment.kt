package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.os.Bundle
import android.view.*
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
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.Tip
import com.odnovolov.forgetmenot.presentation.screen.exampleexercise.ExampleExerciseFragment
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationEvent.*
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.android.synthetic.main.tip.view.*
import kotlinx.coroutines.launch
import java.util.*

class PronunciationFragment : BaseFragment() {
    init {
        PronunciationDiScope.reopenIfClosed()
    }

    private var controller: PronunciationController? = null
    private lateinit var viewModel: PronunciationViewModel
    private var questionLanguagePopup: PopupWindow? = null
    private val questionLanguageAdapter = LanguageAdapter(
        onItemClicked = { language: Locale? ->
            controller?.dispatch(QuestionLanguageSelected(language))
            questionLanguagePopup?.dismiss()
        },
        onMarkLanguageAsFavoriteButtonClicked = { language: Locale ->
            controller?.dispatch(MarkedLanguageAsFavorite(language))
        },
        onUnmarkLanguageAsFavoriteButtonClicked = { language: Locale ->
            controller?.dispatch(UnmarkedLanguageAsFavorite(language))
        }
    )
    private var answerLanguagePopup: PopupWindow? = null
    private val answerLanguageAdapter = LanguageAdapter(
        onItemClicked = { language: Locale? ->
            controller?.dispatch(AnswerLanguageSelected(language))
            answerLanguagePopup?.dismiss()
        },
        onMarkLanguageAsFavoriteButtonClicked = { language: Locale ->
            controller?.dispatch(MarkedLanguageAsFavorite(language))
        },
        onUnmarkLanguageAsFavoriteButtonClicked = { language: Locale ->
            controller?.dispatch(UnmarkedLanguageAsFavorite(language))
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
            requireQuestionLanguagePopup().show(anchor = questionLanguageButton, Gravity.CENTER)
        }
        questionAutoSpeakButton.setOnClickListener {
            controller?.dispatch(QuestionAutoSpeakSwitchToggled)
        }
        answerLanguageButton.setOnClickListener {
            requireAnswerLanguagePopup().show(anchor = answerLanguageButton, Gravity.CENTER)
        }
        answerAutoSpeakButton.setOnClickListener {
            controller?.dispatch(AnswerAutoSpeakSwitchToggled)
        }
        speakTextInBracketsButton.setOnClickListener {
            controller?.dispatch(SpeakTextInBracketsSwitchToggled)
        }
        goToTtsSettingsButton.setOnClickListener {
            openTtsSettings()
        }
    }

    private fun requireQuestionLanguagePopup(): PopupWindow {
        if (questionLanguagePopup == null) {
            questionLanguagePopup = createLanguagePopup().apply {
                (contentView as RecyclerView).adapter = questionLanguageAdapter
            }
        }
        return questionLanguagePopup!!
    }

    private fun requireAnswerLanguagePopup(): PopupWindow {
        if (answerLanguagePopup == null) {
            answerLanguagePopup = createLanguagePopup().apply {
                (contentView as RecyclerView).adapter = answerLanguageAdapter
            }
        }
        return answerLanguagePopup!!
    }

    private fun observeViewModel() {
        with(viewModel) {
            tip.observe { tip: Tip? ->
                if (tip != null) {
                    if (tipStub != null) {
                        tipStub.inflate()
                        closeTipButton.setOnClickListener {
                            controller?.dispatch(CloseTipButtonClicked)
                        }
                    }
                    val tipLayout = rootView.findViewById<View>(R.id.tipLayout)
                    tipLayout.tipTextView.setText(tip.stringId)
                    tipLayout.isVisible = true
                } else {
                    if (tipStub == null) {
                        val tipLayout = rootView.findViewById<View>(R.id.tipLayout)
                        tipLayout.isVisible = false
                    }
                }
            }
            selectedQuestionLanguage.observe { selectedQuestionLanguage: Locale? ->
                updateLanguageButton(isQuestion = true, selectedQuestionLanguage)
            }
            displayedQuestionLanguages.observe { displayedQuestionLanguages ->
                questionLanguageAdapter.items = displayedQuestionLanguages
            }
            questionAutoSpeaking.observe { questionAutoSpeak: Boolean ->
                questionAutoSpeakSwitch.isChecked = questionAutoSpeak
                questionAutoSpeakSwitch.uncover()
            }
            selectedAnswerLanguage.observe { selectedAnswerLanguage: Locale? ->
                updateLanguageButton(isQuestion = false, selectedAnswerLanguage)
            }
            displayedAnswerLanguages.observe { displayedQuestionLanguages ->
                answerLanguageAdapter.items = displayedQuestionLanguages
            }
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

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
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

    override fun onDestroyView() {
        super.onDestroyView()
        questionLanguagePopup?.dismiss()
        questionLanguagePopup = null
        answerLanguagePopup?.dismiss()
        answerLanguagePopup = null
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

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        val behavior = BottomSheetBehavior.from(exampleFragmentContainerView)
        if (behavior.state != BottomSheetBehavior.STATE_COLLAPSED) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            true
        } else {
            false
        }
    }
}