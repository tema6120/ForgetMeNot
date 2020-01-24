package com.odnovolov.forgetmenot.screen.repetition.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.common.base.BaseFragment
import com.odnovolov.forgetmenot.common.firstBlocking
import com.odnovolov.forgetmenot.screen.repetition.service.RepetitionService
import com.odnovolov.forgetmenot.screen.repetition.view.RepetitionCardAdapter.ViewHolder
import com.odnovolov.forgetmenot.screen.repetition.view.RepetitionViewEvent.ShowAnswerButtonClicked
import kotlinx.android.synthetic.main.fragment_repetition.*
import kotlinx.android.synthetic.main.item_repetition_card.view.*

class RepetitionFragment : BaseFragment() {

    private val controller = RepetitionViewController()
    private val viewModel = RepetitionViewModel()
    private val repetitionCardAdapter = RepetitionCardAdapter(controller)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.run {
            setShowHideAnimationEnabled(false)
            hide()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_repetition, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        observeViewModel()
    }

    private fun setupView() {
        repetitionViewPager.adapter = repetitionCardAdapter
    }

    private fun observeViewModel() {
        with(viewModel) {
            isPlaying.observe { isPlaying ->
                if (isPlaying) {
                    val intent = Intent(context, RepetitionService::class.java)
                    ContextCompat.startForegroundService(requireContext(), intent)
                }
            }
            repetitionCardItems.observe { repetitionCardItems: List<RepetitionCardItem> ->
                repetitionCardAdapter.submitList(repetitionCardItems)
                val currentId = currentRepetitionCardId.firstBlocking()
                updateViewPagerPosition(currentId)
            }
            currentRepetitionCardId.observe(onChange = ::updateViewPagerPosition)
        }
    }

    private fun updateViewPagerPosition(id: Long) {
        val position = repetitionCardAdapter.currentList.indexOfFirst { it.id == id }
        repetitionViewPager.currentItem = position
    }

    override fun onDestroyView() {
        super.onDestroyView()
        repetitionViewPager.adapter = null
    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as AppCompatActivity).supportActionBar?.show()
        if (isRemoving) {
            val intent = Intent(context, RepetitionService::class.java)
            requireContext().stopService(intent)
        }
    }

}

class RepetitionCardAdapter(
    val controller: RepetitionViewController
) : ListAdapter<RepetitionCardItem, ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_repetition_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item: RepetitionCardItem = getItem(position)
        with(viewHolder.itemView) {
            questionTextView.text = item.question
            answerTextView.text = item.answer
            if (item.isAnswered) {
                answerScrollView.visibility = VISIBLE
                showAnswerButton.visibility = GONE
                showAnswerButton.setOnClickListener(null)
            } else {
                answerScrollView.visibility = GONE
                showAnswerButton.visibility = VISIBLE
                showAnswerButton.setOnClickListener {
                    controller.dispatch(ShowAnswerButtonClicked(item.id))
                }
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DiffCallback : DiffUtil.ItemCallback<RepetitionCardItem>() {
        override fun areItemsTheSame(
            oldItem: RepetitionCardItem,
            newItem: RepetitionCardItem
        ): Boolean {
            return oldItem.id == newItem.id
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(
            oldItem: RepetitionCardItem,
            newItem: RepetitionCardItem
        ): Boolean {
            return oldItem == newItem
        }
    }
}