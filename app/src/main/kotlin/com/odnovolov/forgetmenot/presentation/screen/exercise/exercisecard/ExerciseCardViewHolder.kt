package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class ExerciseCardViewHolder<T : ExerciseCard>(
    itemView: View,
    private val coroutineScope: CoroutineScope
) : RecyclerView.ViewHolder(itemView) {
    private var observing: Job? = null

    open fun bind(exerciseCard: T) {
        observing?.cancel()
        observing = coroutineScope.launch {
            bind(exerciseCard, this)
        }
    }

    protected abstract fun bind(exerciseCard: T, coroutineScope: CoroutineScope)
}