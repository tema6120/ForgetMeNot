package com.odnovolov.forgetmenot.presentation.common.customview.preset

import android.content.Context
import com.odnovolov.forgetmenot.R.string

data class Preset(
    val id: Long?,
    val name: String,
    val isSelected: Boolean
)

fun Preset.isOff(): Boolean = id == null
fun Preset.isDefault(): Boolean = id == 0L
fun Preset.isIndividual(): Boolean = !isOff() && !isDefault() && name.isEmpty()
fun Preset.isShared(): Boolean = name.isNotEmpty()

fun Preset.toString(context: Context): String = when {
    isOff() -> context.getString(string.off)
    isDefault() -> context.getString(string.default_name)
    isIndividual() -> context.getString(string.individual_name)
    else -> "'${name}'"
}