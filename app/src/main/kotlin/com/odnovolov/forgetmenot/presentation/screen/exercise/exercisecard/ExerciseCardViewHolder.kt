package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class ExerciseCardViewHolder<T : ExerciseCard>(
    private val asyncItemView: AsyncFrameLayout,
    private val coroutineScope: CoroutineScope
) : RecyclerView.ViewHolder(asyncItemView) {
    private var observing: Job? = null

    open fun bind(exerciseCard: T) {
        observing?.cancel()
        asyncItemView.invokeWhenInflated {
            observing = coroutineScope.launch {
                bind(exerciseCard, this)
            }
        }
    }

    protected abstract fun bind(exerciseCard: T, coroutineScope: CoroutineScope)
}