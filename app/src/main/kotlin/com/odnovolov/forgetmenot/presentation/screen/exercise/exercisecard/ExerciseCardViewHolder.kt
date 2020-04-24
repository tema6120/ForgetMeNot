package com.odnovolov.forgetmenot.presentation.screen.exercise.exercisecard

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat
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
    private val vibrator: Vibrator? =
        ContextCompat.getSystemService(asyncItemView.context, Vibrator::class.java)

    open fun bind(exerciseCard: T) {
        observing?.cancel()
        asyncItemView.invokeWhenInflated {
            observing = coroutineScope.launch {
                bind(exerciseCard, this)
            }
        }
    }

    protected abstract fun bind(exerciseCard: T, coroutineScope: CoroutineScope)

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