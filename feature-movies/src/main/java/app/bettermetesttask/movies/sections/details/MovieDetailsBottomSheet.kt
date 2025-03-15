package app.bettermetesttask.movies.sections.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import app.bettermetesttask.featurecommon.injection.utils.Injectable
import app.bettermetesttask.featurecommon.injection.viewmodel.SimpleViewModelProviderFactory
import app.bettermetesttask.featurecommon.utils.images.GlideApp
import app.bettermetesttask.movies.R
import app.bettermetesttask.movies.databinding.MovieDetailsBottomSheetBinding
import app.bettermetesttask.movies.model.MovieUiModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class MovieDetailsBottomSheet : BottomSheetDialogFragment(), Injectable {

    private var _binding: MovieDetailsBottomSheetBinding? = null
    private val binding get() = _binding!!

    private val viewModel by viewModels<MovieDetailsViewModel> {
        SimpleViewModelProviderFactory(
            viewModelProvider
        )
    }

    @Inject
    lateinit var viewModelProvider: Provider<MovieDetailsViewModel>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MovieDetailsBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.movieStateFlow.collect(::renderMovie)
            }
        }

        arguments?.getParcelable<MovieUiModel>(ARG_MOVIE)?.let { movie ->
            viewModel.setMovie(movie)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupViews() {
        binding.btnLike.setOnClickListener {
            viewModel.toggleLike()
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun renderMovie(movie: MovieUiModel?) {
        with(binding) {
            titleTv.text = movie?.title
            descriptionTv.text = movie?.description

            GlideApp.with(this@MovieDetailsBottomSheet)
                .load(movie?.posterPath)
                .into(posterIv)

            if (movie != null) {
                btnLike.setImageResource(
                    if (movie.liked) R.drawable.ic_favorite_liked
                    else R.drawable.ic_favorite_not_liked
                )
            }
        }
    }

    companion object {
        private const val ARG_MOVIE = "movie"

        fun createArgs(movie: MovieUiModel) = Bundle().apply {
            putParcelable(ARG_MOVIE, movie)
        }
    }
}