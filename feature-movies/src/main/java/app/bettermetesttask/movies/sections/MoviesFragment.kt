package app.bettermetesttask.movies.sections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import app.bettermetesttask.domainmovies.errors.MovieError
import app.bettermetesttask.featurecommon.injection.utils.Injectable
import app.bettermetesttask.featurecommon.injection.viewmodel.SimpleViewModelProviderFactory
import app.bettermetesttask.featurecommon.utils.views.gone
import app.bettermetesttask.featurecommon.utils.views.visible
import app.bettermetesttask.movies.R
import app.bettermetesttask.movies.databinding.MoviesFragmentBinding
import app.bettermetesttask.movies.model.MovieUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class MoviesFragment : Fragment(R.layout.movies_fragment), Injectable {

    @Inject
    lateinit var viewModelProvider: Provider<MoviesViewModel>

    @Inject
    lateinit var adapter: MoviesAdapter

    private var _binding: MoviesFragmentBinding? = null
    private val binding get() = _binding!!


    private val viewModel by viewModels<MoviesViewModel> {
        SimpleViewModelProviderFactory(
            viewModelProvider
        )
    }
    private val coroutineJobs = mutableListOf<Job>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MoviesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.moviesStateFlow.collect(::renderMoviesState)
            }
        }.also { coroutineJobs.add(it) }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.movies.collect(::submitToAdapter)
            }
        }.also { coroutineJobs.add(it) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadMovies()
    }

    override fun onDestroyView() {
        coroutineJobs.forEach { it.cancel() }
        coroutineJobs.clear()
        _binding = null
        super.onDestroyView()
    }


    private fun setupListeners() {
        adapter.onItemClicked = { movie ->
            viewModel.openMovieDetails(movie)
        }

        adapter.onItemLiked = { movie ->
            viewModel.likeMovie(movie)
        }
    }

    private fun setupRecyclerView() {
        binding.rvList.apply {
            adapter = this@MoviesFragment.adapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            )
            setHasFixedSize(true)
        }
    }

    private fun submitToAdapter(movies: List<MovieUiModel>) {
        adapter.submitList(movies)
    }

    private fun renderMoviesState(state: MoviesState) {
        when (state) {
            MoviesState.Loading -> showLoading()
            is MoviesState.Loaded -> showContent()
            is MoviesState.Error -> {
                when (state.error) { //Add specififc errors handling
                    is MovieError.NetworkError -> showError(state.error)
                    is MovieError.DatabaseError -> showError(state.error)
                    is MovieError.NoNetworkAndEmptyCache -> showError(state.error)
                    is MovieError.CacheError -> showError(state.error)
                    is MovieError.MovieNotFound -> showError(state.error)
                }
            }

            else -> showEmpty()
        }
    }

    private fun showLoading() {
        with(binding) {
            progressBar.visible()
            rvList.gone()
            errorView.gone()
        }
    }

    private fun showContent() {
        with(binding) {
            progressBar.gone()
            rvList.visible()
            errorView.gone()
        }
    }

    private fun showError(error: Throwable) {
        with(binding) {
            progressBar.gone()
            errorView.visible()
            errorView.text = error.localizedMessage
            rvList.gone()
        }
    }

    private fun showEmpty() {
        with(binding) {
            progressBar.gone()
            errorView.visible()
            errorView.setText(R.string.no_movies_found)
            rvList.gone()
        }
    }

}