package com.odnovolov.forgetmenot.presentation.screen.home.choosepreset

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.string
import com.odnovolov.forgetmenot.domain.entity.ExercisePreference
import com.odnovolov.forgetmenot.domain.entity.isDefault
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.common.dp
import com.odnovolov.forgetmenot.presentation.common.uncover
import com.odnovolov.forgetmenot.presentation.screen.deckeditor.decksettings.DisplayingDeckSetting
import kotlinx.android.synthetic.main.item_preset_for_multiple_decks.view.*

class PresetAdapter(
    private val onPresetButtonClicked: (exercisePreferenceId: Long) -> Unit
) : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    var items: List<SelectablePreset> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_preset_for_multiple_decks, parent, false)
        return SimpleRecyclerViewHolder(itemView)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val (exercisePreference: ExercisePreference, isSelected: Boolean) = items[position]
        with (viewHolder.itemView) {
            presetRadioButton.isChecked = isSelected
            presetRadioButton.uncover()
            presetNameTextView.text = when {
                exercisePreference.isDefault() -> context.getString(string.preset_name_default)
                else -> "'${exercisePreference.name}'"
            }
            presetDescription.text = composePresetDescription(exercisePreference, context)
            presetButton.setOnClickListener {
                onPresetButtonClicked(exercisePreference.id)
            }
            updateLayoutParams<MarginLayoutParams> {
                bottomMargin = if (position == itemCount - 1) 16.dp else 0
            }
        }
    }

    private fun composePresetDescription(
        exercisePreference: ExercisePreference,
        context: Context
    ): CharSequence {
        return DisplayingDeckSetting.values().joinTo(
            SpannableStringBuilder(),
            separator = "      "
        ) { displayingDeckSetting: DisplayingDeckSetting ->
            val title: String = context.getString(displayingDeckSetting.titleRes)
            val value: String = displayingDeckSetting.getDisplayText(exercisePreference, context)
            val spannableValue = SpannableString(value)
            val boldSpan = StyleSpan(Typeface.BOLD)
            val valueColor: Int = ContextCompat.getColor(context, R.color.text_medium_emphasis)
            val colorSpan = ForegroundColorSpan(valueColor)
            spannableValue.setSpan(boldSpan, 0, value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableValue.setSpan(colorSpan, 0, value.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            buildSpannedString {
                append(title)
                append(": ")
                append(spannableValue)
            }
        }
    }
}