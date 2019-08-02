package com.odnovolov.forgetmenot.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.odnovolov.forgetmenot.R
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckFragment
import com.odnovolov.forgetmenot.ui.adddeck.AddDeckFragment.Request.AddDeck
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorFragment
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorFragment.Request.CreateExercise
import com.odnovolov.forgetmenot.ui.exercisecreator.ExerciseCreatorFragment.Result.ExerciseIsCreated
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.Action.*
import com.odnovolov.forgetmenot.ui.home.HomeViewModel.Event.*
import kotlinx.android.synthetic.main.fragment_home.*
import leakcanary.LeakSentry

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var addDeckFragment: AddDeckFragment
    private lateinit var exerciseCreatorFragment: ExerciseCreatorFragment
    private lateinit var adapter: DecksPreviewRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel = HomeInjector.viewModel(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        val toolbar: Toolbar = rootView.findViewById(R.id.toolbar)
        toolbar.inflateMenu(R.menu.home_actions)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        subscribeToViewModel()
    }

    private fun setupView() {
        setupToolbar()
        initRecyclerAdapter()
    }

    private fun setupToolbar() {
        toolbar.setOnMenuItemClickListener { item: MenuItem? ->
            when (item?.itemId) {
                R.id.action_add_deck -> {
                    viewModel.onEvent(AddDeckMenuItemClicked)
                    true
                }
                R.id.action_sort_by -> {
                    viewModel.onEvent(SortByMenuItemClicked)
                    true
                }
                else -> false
            }
        }
        configureSearchView()
    }

    private fun configureSearchView() {
        val searchItem = toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                viewModel.onEvent(SearchTextChanged(newText))
                return true
            }
        })
    }

    private fun initRecyclerAdapter() {
        adapter = DecksPreviewRecyclerAdapter(viewModel)
        decksPreviewRecycler.adapter = adapter
    }

    private fun subscribeToViewModel() {
        with(viewModel.state) {
            decksPreview.observe(viewLifecycleOwner, Observer(adapter::submitList))
        }

        viewModel.action!!.observe(viewLifecycleOwner, Observer { action ->
            when (action) {
                SendAddDeckRequest -> {
                    addDeckFragment.request(AddDeck)
                }
                ShowDeckSortingBottomSheet -> {
                    DeckSortingBottomSheet().show(childFragmentManager, "DeckSortingBottomSheet Tag")
                }
                is SendCreateExerciseRequest -> {
                    exerciseCreatorFragment.request(CreateExercise(action.deckId))
                }
                NavigateToExercise -> {
                    findNavController().navigate(R.id.action_home_screen_to_exercise_screen)
                }
                is NavigateToDeckSettings -> {
                    val direction = HomeFragmentDirections.actionHomeScreenToDeckSettingsScreen(action.deckId)
                    findNavController().navigate(direction)
                }
                ShowDeckIsDeletedSnackbar -> {
                    Snackbar
                        .make(
                            homeFragmentRootView,
                            getString(R.string.snackbar_message_deck_is_deleted),
                            resources.getInteger(R.integer.duration_deck_is_deleted_snackbar)
                        )
                        .setAction(
                            R.string.snackbar_action_cancel,
                            { viewModel.onEvent(DeckIsDeletedSnackbarCancelActionClicked) }
                        )
                        .show()
                }
            }
        })
    }

    override fun onAttachFragment(childFragment: Fragment) {
        super.onAttachFragment(childFragment)
        when (childFragment) {
            is DeckSortingBottomSheet -> {
                childFragment.viewModel = viewModel
            }
            is AddDeckFragment -> {
                addDeckFragment = childFragment
            }
            is ExerciseCreatorFragment -> {
                exerciseCreatorFragment = childFragment
                exerciseCreatorFragment.result.observe(this, Observer { result ->
                    when (result) {
                        ExerciseIsCreated -> viewModel.onEvent(ExerciseWasCreated)
                    }
                })
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LeakSentry.refWatcher.watch(this)
    }
}