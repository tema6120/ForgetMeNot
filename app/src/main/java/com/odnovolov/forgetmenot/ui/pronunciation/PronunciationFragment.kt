package com.odnovolov.forgetmenot.ui.pronunciation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.entity.Pronunciation
import java.io.Serializable

class PronunciationFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pronunciation, container, false)
    }
}

interface ResultCallback : Serializable {
    fun setResult(result: Pronunciation)
}