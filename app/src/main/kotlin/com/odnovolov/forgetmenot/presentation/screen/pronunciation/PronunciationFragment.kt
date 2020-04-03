package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.preset.PresetFragment
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel
import java.util.*

class PronunciationFragment : BaseFragment() {
    private val koinScope =
        getKoin().getOrCreateScope<PronunciationViewModel>(PRONUNCIATION_SCOPE_ID)
    private val viewModel: PronunciationViewModel by koinScope.viewModel(this)
    private val controller: PronunciationController by koinScope.inject()
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

    override fun onAttachFragment(childFragment: Fragment) {
        if (childFragment is PresetFragment) {
            childFragment.controller = koinScope.get()
            childFragment.viewModel = koinScope.get()
        }
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
        observeViewModel()
    }

    private fun setupView() {
        initAdapters()
        setOnClickListeners()
    }

    private fun initAdapters() {
        questionLanguageAdapter = LanguageAdapter(
            onItemClick = { language: Locale? ->
                controller.onQuestionLanguageSelected(language)
                questionLanguagePopup.dismiss()
            }
        )
        (questionLanguagePopup.contentView as RecyclerView).adapter = questionLanguageAdapter

        answerLanguageAdapter = LanguageAdapter(
            onItemClick = { language: Locale? ->
                controller.onAnswerLanguageSelected(language)
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
            controller.onQuestionAutoSpeakSwitchToggled()
        }
        answerLanguageTextView.setOnClickListener {
            showLanguagePopup(answerLanguagePopup, anchor = answerLanguageTextView)
        }
        answerAutoSpeakButton.setOnClickListener {
            controller.onAnswerAutoSpeakSwitchToggled()
        }
        doNotSpeakTextInBracketsButton.setOnClickListener {
            controller.onDoNotSpeakTextInBracketsSwitchToggled()
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

    private fun observeViewModel() {
        with(viewModel) {
            selectedQuestionLanguage.observe { selectedQuestionLanguage ->
                questionLanguageTextView.text =
                    selectedQuestionLanguage?.displayLanguage
                        ?: getString(R.string.default_name)
            }
            dropdownQuestionLanguages.observe(questionLanguageAdapter::submitList)
            questionAutoSpeak.observe { questionAutoSpeak: Boolean ->
                questionAutoSpeakSwitch.isChecked = questionAutoSpeak
                if (questionAutoSpeakSwitch.visibility == INVISIBLE) {
                    questionAutoSpeakSwitch.jumpDrawablesToCurrentState()
                    questionAutoSpeakSwitch.visibility = VISIBLE
                }
            }
            selectedAnswerLanguage.observe { selectedAnswerLanguage ->
                answerLanguageTextView.text =
                    selectedAnswerLanguage?.displayLanguage
                        ?: getString(R.string.default_name)
            }
            dropdownAnswerLanguages.observe(answerLanguageAdapter::submitList)
            answerAutoSpeak.observe { answerAutoSpeak: Boolean ->
                answerAutoSpeakSwitch.isChecked = answerAutoSpeak
                if (answerAutoSpeakSwitch.visibility == INVISIBLE) {
                    answerAutoSpeakSwitch.jumpDrawablesToCurrentState()
                    answerAutoSpeakSwitch.visibility = VISIBLE
                }
            }
            doNotSpeakTextInBrackets.observe { doNotSpeakTextInBrackets: Boolean ->
                doNotSpeakTextInBracketsSwitch.isChecked = doNotSpeakTextInBrackets
                if (doNotSpeakTextInBracketsSwitch.visibility == INVISIBLE) {
                    doNotSpeakTextInBracketsSwitch.jumpDrawablesToCurrentState()
                    doNotSpeakTextInBracketsSwitch.visibility = VISIBLE
                }
            }
        }
    }
}