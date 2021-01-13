package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard

abstract class ExerciseCardViewHolder<T : ExerciseCard>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    abstract fun bind(exerciseCard: T)
}