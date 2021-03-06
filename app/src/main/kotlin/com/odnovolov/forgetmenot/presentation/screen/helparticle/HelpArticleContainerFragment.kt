package com.odnovolov.forgetmenot.presentation.screen.helparticle

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.presentation.common.base.BaseFragment
import com.odnovolov.forgetmenot.presentation.common.firstBlocking
import com.odnovolov.forgetmenot.presentation.common.mainactivity.MainActivity
import com.odnovolov.forgetmenot.presentation.common.isFinishing
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleController.Command.OpenArticle
import com.odnovolov.forgetmenot.presentation.screen.helparticle.HelpArticleEvent.*
import kotlinx.android.synthetic.main.fragment_help_article_container.*
import kotlinx.coroutines.launch
import java.util.*

class HelpArticleContainerFragment : BaseFragment() {
    init {
        HelpArticleDiScope.reopenIfClosed()
    }

    private var controller: HelpArticleController? = null
    private lateinit var viewModel: HelpArticleViewModel
    private var needToResetScrollView = false
    private var pendingActions: MutableList<() -> Unit> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help_article_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        viewCoroutineScope!!.launch {
            val diScope = HelpArticleDiScope.getAsync() ?: return@launch
            controller = diScope.controller
            viewModel = diScope.viewModel
            initAdapter()
            observeViewModel()
            controller!!.commands.observe(::executeCommand)
        }
    }

    private fun observeViewModel() {
        with(viewModel) {
            if (isViewFirstCreated) {
                openArticle(currentArticle.firstBlocking(), needToClearBackStack = true)
            }
            currentArticle.observe { currentArticle: HelpArticle ->
                articleTitleTextView.setText(currentArticle.titleRes)
            }
            isPreviousArticleButtonEnabled.observe { isEnabled: Boolean ->
                previousButton.isEnabled = isEnabled
            }
            isNextArticleButtonEnabled.observe { isEnabled: Boolean ->
                nextButton.isEnabled = isEnabled
            }
        }
    }

    private fun executeCommand(command: HelpArticleController.Command) {
        when (command) {
            is OpenArticle -> openArticle(command.article, command.needToClearBackStack)
        }
    }

    private fun setupView() {
        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
        showTableOfContentsButton.setOnClickListener {
            helpDrawerLayout.openDrawer(GravityCompat.END)
        }
        helpDrawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerClosed(drawerView: View) {
                for (action in pendingActions) action()
                pendingActions.clear()
            }

            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
        })
        toggler.setOnClickListener {
            // just to be able to register click which in turn enables click sound
            // maybe it's important for blind persons
        }
        toggler.setOnTouchListener(object : View.OnTouchListener {
            private var tiltAngle = 0f

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                val isTouchPointInsideToggler: Boolean =
                    motionEvent.x >= 0 && motionEvent.x <= toggler.width
                            && motionEvent.y >= 0 && motionEvent.y <= toggler.height

                val tiltAngle = when {
                    !isTouchPointInsideToggler -> {
                        0f
                    }
                    motionEvent.action == MotionEvent.ACTION_UP -> {
                        when (this.tiltAngle) {
                            -5f -> {
                                if (previousButton.isEnabled) {
                                    controller?.dispatch(PreviousArticleButtonClicked)
                                    view.performClick()
                                }
                            }
                            5f -> {
                                if (nextButton.isEnabled) {
                                    controller?.dispatch(NextArticleButtonClicked)
                                    view.performClick()
                                }
                            }
                        }
                        0f
                    }
                    motionEvent.x > toggler.width / 2 -> {
                        if (nextButton.isEnabled) 5f else 0f
                    }
                    else -> {
                        if (previousButton.isEnabled) -5f else 0f
                    }
                }

                if (this.tiltAngle != tiltAngle) {
                    this.tiltAngle = tiltAngle
                    previousButton.isPressed = tiltAngle == -5f
                    nextButton.isPressed = tiltAngle == 5f
                    if (tiltAngle == 0f) animateSettling() else animatePressing()
                }

                return isTouchPointInsideToggler
            }

            private fun animatePressing() {
                ObjectAnimator.ofFloat(toggler, "rotationY", tiltAngle).apply {
                    duration = 100
                    interpolator = LinearInterpolator()
                    setAutoCancel(true)
                    start()
                }
            }

            private fun animateSettling() {
                ObjectAnimator.ofFloat(toggler, "rotationY", 0f).apply {
                    duration = 400
                    interpolator = DecelerateInterpolator()
                    setAutoCancel(true)
                    start()
                }
            }
        })
    }

    private fun initAdapter() {
        val onItemSelected: (HelpArticle) -> Unit = { helpArticle: HelpArticle ->
            helpDrawerLayout.closeDrawer(GravityCompat.END)
            controller?.dispatch(ArticleSelected(helpArticle))
        }
        val adapter = HelpArticleAdapter(onItemSelected)
        tableOfContentsRecycler.adapter = adapter
        viewModel.articleItems.observe(adapter::submitList)
    }

    private fun openArticle(helpArticle: HelpArticle, needToClearBackStack: Boolean) {
        if (needToClearBackStack) {
            childFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.articleFrame, helpArticle.createFragment())
            .apply { if (!needToClearBackStack) addToBackStack(null) }
            .commit()
        needToResetScrollView = true
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        if (needToResetScrollView) {
            contentScrollView.scrollTo(0, 0)
            needToResetScrollView = false
        }
    }

    fun doWhenDrawerClosed(action: () -> Unit) {
        if (helpDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            pendingActions.add(action)
        } else {
            action()
        }
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).registerBackPressInterceptor(backPressInterceptor)
    }

    override fun onPause() {
        super.onPause()
        (activity as MainActivity).unregisterBackPressInterceptor(backPressInterceptor)
    }

    private val backPressInterceptor = MainActivity.BackPressInterceptor {
        if (childFragmentManager.backStackEntryCount > 0) {
            childFragmentManager.popBackStack()
            true
        } else {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing()) {
            HelpArticleDiScope.close()
        }
    }
}