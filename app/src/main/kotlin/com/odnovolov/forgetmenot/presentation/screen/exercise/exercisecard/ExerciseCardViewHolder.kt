package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.domain.interactor.exercise.ExerciseCard

abstract class ExerciseCardViewHolder<T : ExerciseCard>(
    itemView: View
) : RecyclerView.ViewHolder(itemView) {
    private val vibrator: Vibrator? by lazy {
        ContextCompat.getSystemService(itemView.context, Vibrator::class.java)
    }

    abstract fun bind(exerciseCard: T)

    protected fun vibrate() {
        vibrator?.let { vibrator: Vibrator ->
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        VIBRATION_DURATION,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(VIBRATION_DURATION)
            }
        }
    }

    companion object {
        private const val VIBRATION_DURATION = 50L
    }
}