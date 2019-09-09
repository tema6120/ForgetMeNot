package com.odnovolov.forgetmenot.common.viewcreator

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.viewcreator.SingleChoiceDialogCreator.ItemAdapter.ViewHolder
import kotlinx.android.synthetic.main.item_single_choice.view.*

object SingleChoiceDialogCreator {
    fun <T : Item> create(
        context: Context,
        title: CharSequence,
        onItemClick: (T) -> Unit,
        takeAdapter: (ItemAdapter<T>) -> Unit
    ): Dialog {
        val adapter = ItemAdapter(onItemClick)
        takeAdapter(adapter)
        val recyclerView = View.inflate(context, R.layout.dialog_single_choice, null)
                as RecyclerView
        recyclerView.adapter = adapter
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setView(recyclerView)
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    interface Item {
        val text: String
        val isSelected: Boolean
    }

    class ItemAdapter<T : Item>(
        private val onItemClick: (T) -> Unit
    ) : ListAdapter<T, ViewHolder>(
        DiffCallback()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_single_choice, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            with(viewHolder.itemView) {
                val item = getItem(position)
                radioButton.isChecked = item.isSelected
                radioButton.text = item.text
                button.setOnClickListener { onItemClick(item) }
            }
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        class DiffCallback<T : Item> : DiffUtil.ItemCallback<T>() {
            override fun areItemsTheSame(oldItem: T, newItem: T): Boolean =
                oldItem.text == newItem.text

            @SuppressLint("DiffUtilEquals")
            override fun areContentsTheSame(oldItem: T, newItem: T): Boolean =
                oldItem == newItem
        }
    }
}