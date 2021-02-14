package com.odnovolov.forgetmenot.presentation.screen.dsvformat

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.isVisible
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.needToCloseDiScope
import com.odnovolov.forgetmenot.presentation.common.openUrl
import com.odnovolov.forgetmenot.presentation.common.setTextWithClickableAnnotations
import kotlinx.android.synthetic.main.fragment_dsv_format.*
import kotlinx.android.synthetic.main.tip.*
import kotlinx.coroutines.launch

class DsvFormatFragment : BaseFragment() {
    init {
        DsvFormatDiScope.reopenIfClosed()
    }

    private var controller: DsvFormatController? = null
    private lateinit var viewModel: DsvFormatViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dsv_format, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = DsvFormatDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            observeViewModel()
        }
    }

    private fun setupView() {
        tipTextView.setTextWithClickableAnnotations(
            stringId = R.string.tip_dsv_format,
            onAnnotationClick = { annotationValue: String ->
                when (annotationValue) {
                    "csv_library" -> openUrl(APACHE_COMMONS_CSV_LIBRARY_URL)
                    "CSVFormat" -> openUrl(CSV_FORMAT_URL)
                }
            },
            linkColor = Color.WHITE
        )
        closeTipButton.setOnClickListener {
            tipLayout.isVisible = false
        }
    }

    private fun observeViewModel() {
        with(viewModel) {

        }
    }

    override fun onResume() {
        super.onResume()
        appBar.post { appBar.isActivated = contentScrollView.canScrollVertically(-1) }
        contentScrollView.viewTreeObserver.addOnScrollChangedListener(scrollListener)
    }

    override fun onPause() {
        super.onPause()
        contentScrollView.viewTreeObserver.removeOnScrollChangedListener(scrollListener)
    }

    private val scrollListener = ViewTreeObserver.OnScrollChangedListener {
        val canScrollUp = contentScrollView.canScrollVertically(-1)
        if (appBar.isActivated != canScrollUp) {
            appBar.isActivated = canScrollUp
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (needToCloseDiScope()) {
            DsvFormatDiScope.close()
        }
    }

    companion object {
        const val APACHE_COMMONS_CSV_LIBRARY_URL = "https://commons.apache.org/proper/commons-csv/"
        const val CSV_FORMAT_URL =
            "https://commons.apache.org/proper/commons-csv/apidocs/org/apache/commons/csv/CSVFormat.html"
    }
}