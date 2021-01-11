package com.odnovolov.forgetmenot.presentation.screen.home

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.View.MeasureSpec.makeMeasureSpec
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.*
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.*
import com.odnovolov.forgetmenot.presentation.common.customview.AsyncFrameLayout
import com.odnovolov.forgetmenot.presentation.screen.home.DeckListItem.DeckPreview
import com.odnovolov.forgetmenot.presentation.screen.home.HomeEvent.*
import kotlinx.android.synthetic.main.item_deck_preview.view.*

class DeckPreviewAdapter(
    private val controller: HomeController,
    private val setupHeader: (View) -> Unit
) : ListAdapter<DeckListItem, SimpleRecyclerViewHolder>(DiffCallback()) {
    init {
        stateRestorationPolicy = PREVENT_WHEN_EMPTY
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
        private const val TYPE_FOOTER = 2
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            DeckListItem.Header -> TYPE_HEADER
            DeckListItem.Footer -> TYPE_FOOTER
            else -> TYPE_ITEM
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        prepareMeasuring(parent)

        val layoutInflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            TYPE_HEADER -> {
                layoutInflater.inflate(R.layout.item_deck_preview_header, parent, false)
                    .also(setupHeader)
            }
            TYPE_FOOTER -> {
                layoutInflater.inflate(R.layout.item_deck_preview_footer, parent, false)
            }
            else -> {
                val layoutParams = LayoutParams(MATCH_PARENT, 0)
                AsyncFrameLayout(layoutParams, parent.context).apply {
                    inflateAsync(R.layout.item_deck_preview)
                    invokeWhenInflated {
                        // strangely enough, text size in xml sometimes differs from text size that
                        // set programmatically. It may cause different height of real item and
                        // premeasured height of AsyncFrameLayout
                        deckNameTextView.textSize = 14.5f
                        // with async inflation, parameter 'fontFamily' in xml causes InflateException
                        // so we set font on ui thread programmatically
                        deckNameTextView.setFont(R.font.comfortaa, Typeface.BOLD)
                    }
                }
            }
        }
        return SimpleRecyclerViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val deckListItem = getItem(position)
        when (deckListItem) {
            DeckListItem.Header, DeckListItem.Footer -> return
        }
        val asyncFrameLayout = viewHolder.itemView as AsyncFrameLayout
        val deckPreview = deckListItem as DeckPreview
        measureAsyncFrameLayoutHeight(asyncFrameLayout, deckPreview.deckName)
        asyncFrameLayout.invokeWhenInflated {
            deckButton.setOnClickListener {
                controller.dispatch(DeckButtonClicked(deckPreview.deckId))
            }
            deckButton.setOnLongClickListener {
                controller.dispatch(DeckButtonLongClicked(deckPreview.deckId))
                true
            }
            deckNameTextView.text =
                if (deckPreview.searchMatchingRanges != null)
                    deckPreview.deckName.highlight(deckPreview.searchMatchingRanges, context) else
                    deckPreview.deckName
            deckOptionButton.setOnClickListener {
                controller.dispatch(DeckOptionButtonClicked(deckPreview.deckId))
            }
            deckSelector.setOnClickListener {
                controller.dispatch(DeckSelectorClicked(deckPreview.deckId))
            }
            avgLapsValueTextView.text = deckPreview.averageLaps
            learnedValueTextView.text = "${deckPreview.learnedCount}/${deckPreview.totalCount}"
            taskValueTextView.text = deckPreview.numberOfCardsReadyForExercise?.toString() ?: "-"
            taskValueTextView.setTextColor(
                getTaskColor(deckPreview.numberOfCardsReadyForExercise, context)
            )
            val isDeckNew = deckPreview.lastTestedAt == null
            lastTestedValueTextView.isVisible = !isDeckNew
            newDeckLabelTextView.isVisible = isDeckNew
            if (!isDeckNew) {
                lastTestedValueTextView.text = deckPreview.lastTestedAt
            }
            updateDeckItemSelectionState(itemView = this, deckPreview.deckId)
        }
    }

    private var colorNotHasTask: Int? = null
    private var colorHasTask: Int? = null

    private fun getTaskColor(numberOfCardsReadyForExercise: Int?, context: Context): Int {
        return if (numberOfCardsReadyForExercise == null || numberOfCardsReadyForExercise == 0) {
            if (colorNotHasTask == null) {
                colorNotHasTask = ContextCompat.getColor(context, R.color.textPrimary)
            }
            colorNotHasTask!!
        } else {
            if (colorHasTask == null) {
                colorHasTask = ContextCompat.getColor(context, R.color.text_task)
            }
            colorHasTask!!
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DeckListItem>() {
        override fun areItemsTheSame(oldItem: DeckListItem, newItem: DeckListItem): Boolean {
            return when {
                oldItem === newItem -> true
                oldItem is DeckPreview && newItem is DeckPreview -> {
                    oldItem.deckId == newItem.deckId
                }
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: DeckListItem, newItem: DeckListItem): Boolean {
            return oldItem == newItem
        }
    }

    // Deck selection

    var deckSelection: DeckSelection? = null
        set(value) {
            field = value
            itemViewDeckIdMap.forEach { (itemView: View, deckId: Long) ->
                updateDeckItemSelectionState(itemView, deckId)
            }
        }

    private var itemViewDeckIdMap = HashMap<View, Long>()

    private fun updateDeckItemSelectionState(itemView: View, deckId: Long) {
        itemViewDeckIdMap[itemView] = deckId

        val isItemSelected: Boolean? = deckSelection?.run {
            deckId in selectedDeckIds
        }
        itemView.isSelected = isItemSelected == true
        itemView.deckOptionButton.isVisible = isItemSelected == null
        itemView.deckSelector.isVisible = isItemSelected != null
    }

    // end Deck selection

    // Measuring

    private var parentWidth = -1
    private var textViewForMeasure: TextView? = null

    private fun prepareMeasuring(parent: ViewGroup) {
        if (parentWidth != parent.width) {
            parentWidth = parent.width
            textViewForMeasure = TextView(parent.context).apply {
                layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
                textSize = 14.5f
                setFont(R.font.comfortaa, Typeface.BOLD)
            }
        }
    }

    private fun measureAsyncFrameLayoutHeight(
        asyncFrameLayout: AsyncFrameLayout,
        deckName: String
    ) {
        if (asyncFrameLayout.isInflated) {
            if (asyncFrameLayout.layoutParams.height != WRAP_CONTENT) {
                asyncFrameLayout.updateLayoutParams {
                    height = WRAP_CONTENT
                }
            }
            return
        }
        val widthForDeckNameTextView: Int = parentWidth - 116.dp

        val textViewForMeasure = textViewForMeasure!!
        textViewForMeasure.text = deckName
        textViewForMeasure.measure(
            makeMeasureSpec(widthForDeckNameTextView, MeasureSpec.EXACTLY),
            makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val asyncFrameLayoutHeight = 57.dp + 29.sp + textViewForMeasure.measuredHeight
        asyncFrameLayout.updateLayoutParams {
            height = asyncFrameLayoutHeight
        }
    }

    // end Measuring
}