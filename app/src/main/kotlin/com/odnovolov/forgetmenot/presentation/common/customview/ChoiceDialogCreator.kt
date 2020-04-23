package com.odnovolov.forgetmenot.presentation.common.customview

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemAdapter.ViewHolder
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsCheckBox
import com.odnovolov.forgetmenot.presentation.common.customview.ChoiceDialogCreator.ItemForm.AsRadioButton
import kotlinx.android.synthetic.main.dialog_single_choice.view.*

object ChoiceDialogCreator {
    fun create(
        context: Context,
        title: CharSequence? = null,
        itemForm: ItemForm,
        onItemClick: (Item) -> Unit,
        takeAdapter: (ItemAdapter) -> Unit
    ): Dialog {
        val adapter = ItemAdapter(itemForm, onItemClick)
        takeAdapter(adapter)
        val rootView = View.inflate(context, R.layout.dialog_single_choice, null)
        val recyclerView = rootView.recycler
        recyclerView.adapter = adapter
        return AlertDialog.Builder(context)
            .setTitle(title)
            .setView(rootView)
            .setNegativeButton(R.string.close, null)
            .create()
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