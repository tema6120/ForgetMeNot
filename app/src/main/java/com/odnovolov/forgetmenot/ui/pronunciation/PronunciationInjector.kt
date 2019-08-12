package com.odnovolov.forgetmenot.ui.pronunciation

import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.odnovolov.forgetmenot.entity.Speaker

object PronunciationInjector {

    fun viewModel(fragment: PronunciationFragment): PronunciationViewModel {
        val context = fragment.requireContext()
        val speaker = Speaker(context.applicationContext)
        val initPronunciation = fragment.navArgs<PronunciationFragmentArgs>().value.initPronunciation
        val resultCallback: ResultCallback = fragment.navArgs<PronunciationFragmentArgs>().value.resultCallback
        val factory = PronunciationViewModelImpl.Factory(fragment, speaker, initPronunciation, resultCallback)
        val viewModelProvider = ViewModelProviders.of(fragment, factory)
        return viewModelProvider.get(PronunciationViewModelImpl::class.java)
    }

}