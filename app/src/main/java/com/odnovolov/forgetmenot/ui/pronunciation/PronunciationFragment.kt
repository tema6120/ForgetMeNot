package com.odnovolov.forgetmenot.ui.pronunciation

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.entity.Pronunciation
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.Action.*
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.Event.*
import com.odnovolov.forgetmenot.ui.pronunciation.PronunciationViewModel.State.DropdownLanguage
import kotlinx.android.synthetic.main.fragment_pronunciation.*
import java.io.Serializable
import java.util.*

class PronunciationFragment : Fragment() {

    private lateinit var viewModel: PronunciationViewModel
    private lateinit var questionLanguagesPopup: PopupWindow
    private lateinit var questionLanguagesRecyclerAdapter: LanguageRecyclerAdapter
    private lateinit var answerLanguagesPopup: PopupWindow
    private lateinit var answerLanguagesRecyclerAdapter: LanguageRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = PronunciationInjector.viewModel(this)
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
        subscribeToViewModel(isFirstCreated = savedInstanceState == null)
    }

    private fun setupView() {
        questionLanguagesPopup = createPopup()
        questionLanguagesRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? -> viewModel.onEvent(QuestionLanguageSelected(language)) }
        )
        (questionLanguagesPopup.contentView as RecyclerView).adapter = questionLanguagesRecyclerAdapter
        answerLanguagesPopup = createPopup()
        answerLanguagesRecyclerAdapter = LanguageRecyclerAdapter(
            onItemClick = { language: Locale? -> viewModel.onEvent(AnswerLanguageSelected(language)) }
        )
        (answerLanguagesPopup.contentView as RecyclerView).adapter = answerLanguagesRecyclerAdapter
        pronunciationNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.onEvent(NameInputChanged(text.toString()))
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        questionLanguageTextView.setOnClickListener {
            viewModel.onEvent(QuestionLanguageButtonClicked)
        }
        questionAutoSpeakButton.setOnClickListener {
            viewModel.onEvent(QuestionAutoSpeakSwitchClicked)
        }
        answerLanguageTextView.setOnClickListener {
            viewModel.onEvent(AnswerLanguageButtonClicked)
        }
        answerAutoSpeakButton.setOnClickListener {
            viewModel.onEvent(AnswerAutoSpeakSwitchClicked)
        }
        doneFab.setOnClickListener { viewModel.onEvent(DoneFabClicked) }
    }

    private fun createPopup() = PopupWindow(requireContext()).apply {
        contentView = View.inflate(requireContext(), R.layout.popup_available_languages, null)
        setBackgroundDrawable(ColorDrawable(Color.LTGRAY))
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            elevation = 20f
        }
        isOutsideTouchable = true
        isFocusable = true
    }

    private fun subscribeToViewModel(isFirstCreated: Boolean) {
        with(viewModel.state) {
            // Unfortunately, EditText owns state and make its own changes ( https://youtu.be/VsStyq4Lzxo?t=482 )
            if (isFirstCreated) {
                name.observe(viewLifecycleOwner, Observer {
                    pronunciationNameEditText.setText(it)
                    name.removeObservers(viewLifecycleOwner)
                })
            }
            dropdownQuestionLanguages.observe(viewLifecycleOwner, Observer { dropdownLanguages: List<DropdownLanguage> ->
                questionLanguagesRecyclerAdapter.submitList(dropdownLanguages)
            })
            selectedQuestionLanguage.observe(viewLifecycleOwner, Observer { selectedQuestionLanguage ->
                questionLanguageTextView.text = selectedQuestionLanguage?.displayLanguage ?: "Default"
            })
            questionAutoSpeak.observe(viewLifecycleOwner, Observer { questionAutoSpeak ->
                questionAutoSpeakSwitch.isChecked = questionAutoSpeak
            })
            selectedAnswerLanguage.observe(viewLifecycleOwner, Observer { selectedAnswerLanguage ->
                answerLanguageTextView.text = selectedAnswerLanguage?.displayLanguage ?: "Default"
            })
            dropdownAnswerLanguages.observe(viewLifecycleOwner, Observer { dropdownLanguages: List<DropdownLanguage> ->
                answerLanguagesRecyclerAdapter.submitList(dropdownLanguages)
            })
            answerAutoSpeak.observe(viewLifecycleOwner, Observer { answerAutoSpeak ->
                answerAutoSpeakSwitch.isChecked = answerAutoSpeak
            })
        }

        viewModel.action!!.observe(viewLifecycleOwner, Observer { action ->
            when (action) {
                ShowQuestionDropdownList -> {
                    showPopup(questionLanguagesPopup, anchor = questionLanguageTextView)
                }
                DismissQuestionDropdownList -> {
                    questionLanguagesPopup.dismiss()
                }
                ShowAnswerDropdownList -> {
                    showPopup(answerLanguagesPopup, anchor = answerLanguageTextView)
                }
                DismissAnswerDropdownList -> {
                    answerLanguagesPopup.dismiss()
                }
                is SetNameErrorText -> {
                    pronunciationNameEditText.error = action.errorText
                }
                NavigateUp -> {
                    findNavController().navigateUp()
                }
            }
        })
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
}

interface ResultCallback : Serializable {
    fun setResult(result: Pronunciation)
}