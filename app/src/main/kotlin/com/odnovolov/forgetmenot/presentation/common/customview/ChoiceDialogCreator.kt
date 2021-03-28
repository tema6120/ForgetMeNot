package com.odnovolov.forgetmenot.presentation.common.customview

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.CompoundButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.R.drawable
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import com.odnovolov.forgetmenot.presentation.common.dp
import kotlinx.android.synthetic.main.dialog_choice.view.*
import kotlinx.android.synthetic.main.dialog_title.view.*

object ChoiceDialogCreator {
    fun create(
        context: Context,
        itemForm: ItemForm,
        takeTitle: (TextView) -> Unit,
        onItemClick: (Item) -> Unit,
        takeAdapter: (ItemAdapter) -> Unit
    ): AlertDialog {
        val adapter = ItemAdapter(itemForm, onItemClick)
        takeAdapter(adapter)
        val customTitleLayout = View.inflate(context, R.layout.dialog_title, null)
        takeTitle(customTitleLayout.dialogTitle)
        val contentView = View.inflate(context, R.layout.dialog_choice, null)
        contentView.choiceRecycler.adapter = adapter
        val divider = customTitleLayout.divider
        divider.isVisible = contentView.choiceRecycler.canScrollVertically(-1)
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val canScrollUp = recyclerView.canScrollVertically(-1)
                if (divider.isVisible != canScrollUp) {
                    divider.isVisible = canScrollUp
                }
            }
        }
        contentView.choiceRecycler.addOnScrollListener(scrollListener)
        return AlertDialog.Builder(context)
            .setCustomTitle(customTitleLayout)
            .setView(contentView)
            .create()
            .apply {
                window?.setBackgroundDrawable(
                    ContextCompat.getDrawable(context, drawable.background_dialog)
                )
                customTitleLayout.closeButton.setOnClickListener { dismiss() }
                setOnDismissListener {
                    contentView.choiceRecycler.removeOnScrollListener(scrollListener)
                }
            }
    }

    enum class ItemForm {
        AsRadioButton,
        AsCheckBox
    }

    interface Item {
        val text: String
        val isSelected: Boolean
    }

    class ItemAdapter(
        private val itemForm: ItemForm,
        private val onItemClick: (Item) -> Unit
    ) : ListAdapter<Item, ViewHolder>(DiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val layoutRes: Int = when (itemForm) {
                AsRadioButton -> R.layout.item_single_choice
                AsCheckBox -> R.layout.item_multiple_choice
            }
            val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val item: Item = getItem(position)
            viewHolder.shamButton.isChecked = item.isSelected
            viewHolder.shamButton.text = item.text
            viewHolder.button.setOnClickListener { onItemClick(item) }
            viewHolder.itemView.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = if (position == itemCount - 1) 16.dp else 0
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val shamButton: CompoundButton = view.findViewById(R.id.shamButton)
            val button: View = view.findViewById(R.id.button)
        }

        class DiffCallback : ItemCallback<Item>() {
            override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem.text == newItem.text
            }

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
                return oldItem == newItem
            }
        }
    }
}