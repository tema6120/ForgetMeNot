package com.odnovolov.forgetmenot.presentation.screen.cardfilterforexercise

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import kotlinx.coroutines.launch

class CardFilterForExerciseFragment : BaseFragment() {
    init {
        CardFilterForExerciseDiScope.reopenIfClosed()
    }

    private var controller: CardFilterForExerciseController? = null
    private lateinit var viewModel: CardFilterForExerciseViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_card_filter_for_exercise, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = CardFilterForExerciseDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {

    }

    private fun observeViewModel() {
        with(viewModel) {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            CardFilterForExerciseDiScope.close()
        }
    }
}