package com.odnovolov.forgetmenot.presentation.screen.cardappearance.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.domain.entity.Card
import com.odnovolov.forgetmenot.presentation.common.SimpleRecyclerViewHolder
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_ANSWER_TEXT_ALIGNMENT
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_ANSWER_TEXT_SIZE
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_QUESTION_TEXT_ALIGNMENT
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardAppearance.Companion.DEFAULT_QUESTION_TEXT_SIZE
import com.odnovolov.forgetmenot.presentation.screen.cardappearance.CardTextAlignment
import kotlinx.android.synthetic.main.item_card_in_card_appearance_settings.view.*

class CardAdapter : RecyclerView.Adapter<SimpleRecyclerViewHolder>() {
    init {
        stateRestorationPolicy = PREVENT_WHEN_EMPTY
    }

    private val itemViews = ArrayList<View>()

    var questionTextAlignment: CardTextAlignment = DEFAULT_QUESTION_TEXT_ALIGNMENT
        set(value) {
            field = value
            itemViews.forEach { itemView: View ->
                itemView.questionTextView.gravity = value.gravity
            }
        }

    var questionTextSize: Int = DEFAULT_QUESTION_TEXT_SIZE
        set(value) {
            field = value
            itemViews.forEach { itemView: View ->
                itemView.questionTextView.textSize = value.toFloat()
            }
        }

    var answerTextAlignment: CardTextAlignment = DEFAULT_ANSWER_TEXT_ALIGNMENT
        set(value) {
            field = value
            itemViews.forEach { itemView: View ->
                itemView.answerTextView.gravity = value.gravity
            }
        }

    var answerTextSize: Int = DEFAULT_ANSWER_TEXT_SIZE
        set(value) {
            field = value
            itemViews.forEach { itemView: View ->
                itemView.answerTextView.textSize = value.toFloat()
            }
        }

    var items: List<Card> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleRecyclerViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_in_card_appearance_settings, parent, false)
        itemViews.add(itemView)
        // if we don't post, it causes incorrect work of 'fitsSystemWindows'
        parent.post { applyCardAppearance(itemView) }
        return SimpleRecyclerViewHolder(itemView)
    }

    private fun applyCardAppearance(itemView: View) {
        itemView.questionTextView.gravity = questionTextAlignment.gravity
        itemView.questionTextView.textSize = questionTextSize.toFloat()
        itemView.answerTextView.gravity = answerTextAlignment.gravity
        itemView.answerTextView.textSize = answerTextSize.toFloat()
    }

    override fun onBindViewHolder(viewHolder: SimpleRecyclerViewHolder, position: Int) {
        val card: Card = items[position]
        with(viewHolder.itemView) {
            questionTextView.text = card.question
            answerTextView.text = card.answer
        }
    }
}