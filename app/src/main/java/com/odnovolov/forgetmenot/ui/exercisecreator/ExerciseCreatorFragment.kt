package com.odnovolov.forgetmenot.ui.exercisecreator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.odnovolov.forgetmenot.R
import kotlinx.android.synthetic.main.progress_bar.*

class ExerciseCreatorFragment : Fragment() {

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
    }

}