package com.odnovolov.forgetmenot.presentation.screen.deckcontent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.TooltipCompat
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.screen.deckcontent.DeckContentEvent.AddCardButtonClicked
import kotlinx.android.synthetic.main.fragment_deck_content.*
import kotlinx.coroutines.launch

class DeckContentFragment : BaseFragment() {
    init {
        DeckContentDiScope.reopenIfClosed()
    }

    private var controller: DeckContentController? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_deck_content, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DeckContentDiScope.get()
            controller = diScope.controller
            val adapter = CardOverviewAdapter(diScope.controller)
            cardsRecycler.adapter = adapter
            diScope.viewModel.cards.observe(adapter::submitList)
        }
    }

    private fun setupView() {
        addCardButton.run {
            setOnClickListener { controller?.dispatch(AddCardButtonClicked) }
            TooltipCompat.setTooltipText(this, contentDescription)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DeckContentDiScope.close()
        }
    }
}