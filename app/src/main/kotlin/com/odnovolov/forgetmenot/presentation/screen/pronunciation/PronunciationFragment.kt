package com.odnovolov.forgetmenot.presentation.screen.pronunciation

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.View.*
import android.widget.EditText
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.customview.InputDialogCreator
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator
import com.odnovolov.forgetmenot.presentation.common.customview.PresetPopupCreator.PresetAdapter
import com.odnovolov.forgetmenot.domain.entity.NameCheckResult.*
import com.odnovolov.forgetmenot.domain.isDefault
import com.odnovolov.forgetmenot.domain.isIndividual
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.screen.pronunciation.PronunciationController.Command.SetNamePresetDialogText
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import org.koin.android.ext.android.getKoin
import org.koin.androidx.viewmodel.scope.viewModel
import java.util.*

class PronunciationFragment : BaseFragment() {
    private val koinScope =
        getKoin().getOrCreateScope<PronunciationViewModel>(PRONUNCIATION_SCOPE_ID)
    private val viewModel: PronunciationViewModel by koinScope.viewModel(this)
    private val controller: PronunciationController by koinScope.inject()
    private lateinit var choosePronunciationPopup: PopupWindow
    private lateinit var pronunciationAdapter: PresetAdapter
    private lateinit var questionLanguagePopup: PopupWindow
    private lateinit var questionLanguageAdapter: LanguageAdapter
    private lateinit var answerLanguagePopup: PopupWindow
    private lateinit var answerLanguageAdapter: LanguageAdapter
    private lateinit var presetNameInputDialog: Dialog
    private lateinit var presetNameEditText: EditText

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
            setPresetButtonClickListener = { pronunciationId: Long? ->
                controller.onSetPronunciationButtonClicked(pronunciationId!!)
            },
            renamePresetButtonClickListener = { pronunciationId: Long ->
                controller.onRenamePronunciationButtonClicked(pronunciationId)
            },
            deletePresetButtonClickListener = { pronunciationId: Long ->
                controller.onDeletePronunciationButtonClicked(pronunciationId)
            },
            addButtonClickListener = {
                controller.onAddNewPronunciationButtonClicked()
            },
            takeAdapter = { pronunciationAdapter = it }
        )
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

    private fun initPresetNameInputDialog() {
        presetNameInputDialog = InputDialogCreator.create(
            context = requireContext(),
            title = getString(R.string.title_pronunciation_name_input_dialog),
            takeEditText = { presetNameEditText = it },
            onTextChanged = { controller.onDialogTextChanged(it) },
            onPositiveClick = { controller.onNamePresetPositiveDialogButtonClicked() },
            onNegativeClick = { controller.onNamePresetNegativeDialogButtonClicked() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
        controller.commands.observe(::executeCommand)
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
        savePronunciationButton.setOnClickListener {
            controller.onSavePronunciationButtonClicked()
        }
        pronunciationNameTextView.setOnClickListener {
            showChoosePronunciationPopup()
        }
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
            pronunciation.observe {
                val pronunciationName = when {
                    it.isDefault() -> getString(R.string.default_name)
                    it.isIndividual() -> getString(R.string.individual_name)
                    else -> "'${it.name}'"
                }
                pronunciationNameTextView.text = pronunciationName
            }
            isSavePronunciationButtonEnabled.observe { isEnabled: Boolean ->
                savePronunciationButton.visibility = if (isEnabled) VISIBLE else GONE
            }
            availablePronunciations.observe(pronunciationAdapter::submitList)
            isNamePresetDialogVisible.observe { isVisible ->
                presetNameInputDialog.run { if (isVisible) show() else dismiss() }
            }
            namePresetInputCheckResult.observe {
                presetNameEditText.error = when (it) {
                    Ok -> null
                    Empty -> getString(R.string.error_message_empty_name)
                    Occupied -> getString(R.string.error_message_occupied_name)
                }
            }
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

    private fun executeCommand(command: PronunciationController.Command) {
        when (command) {
            is SetNamePresetDialogText -> {
                presetNameEditText.setText(command.text)
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

    override fun onPause() {
        super.onPause()
        controller.onFragmentPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle(STATE_KEY_DIALOG, presetNameInputDialog.onSaveInstanceState())
    }

    companion object {
        const val STATE_KEY_DIALOG = "pronunciationNameInputDialog"
    }
}