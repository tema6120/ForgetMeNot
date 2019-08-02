package com.odnovolov.forgetmenot.ui.exercisecreator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.InteractableFragment
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorFragment.Request
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorFragment.Request.CreateExercise
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorFragment.Result
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorFragment.Result.ExerciseIsCreated
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.Action.NotifyParentViewThatExerciseIsCreated
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorViewModel.Event.CreateExerciseWasRequested
import kotlinx.android.synthetic.main.progress_bar.*
import leakcanary.LeakSentry

class ExerciseCreatorFragment : InteractableFragment<Request, Result>() {

    sealed class Request {
        data class CreateExercise(val deckId: Int) : Request()
    }

    sealed class Result {
        object ExerciseIsCreated : Result()
    }

    lateinit var viewModel: ExerciseCreatorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = ExerciseCreatorInjector.viewModel(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.progress_bar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToViewModel()
    }

    private fun subscribeToViewModel() {
        with(viewModel.state) {
            isProcessing.observe(viewLifecycleOwner, Observer { isProcessing ->
                progressBar.visibility = if (isProcessing) View.VISIBLE else View.GONE
            })
        }

        viewModel.action!!.observe(viewLifecycleOwner, Observer { action ->
            when (action) {
                NotifyParentViewThatExerciseIsCreated -> sendResult(ExerciseIsCreated)
            }
        })
    }

    override fun request(request: Request) {
        when (request) {
            is CreateExercise -> viewModel.onEvent(CreateExerciseWasRequested(request.deckId))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }

}