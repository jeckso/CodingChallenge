package app.bettermetesttask.movies.sections.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.coroutines.AppDispatchers
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import app.bettermetesttask.movies.model.MovieUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MovieDetailsViewModel @Inject constructor(
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
) : ViewModel() {

    private val _movieStateFlow: MutableStateFlow<MovieUiModel?> =
        MutableStateFlow(null)
    val movieStateFlow: StateFlow<MovieUiModel?>
        get() = _movieStateFlow.asStateFlow()

    fun setMovie(movie: MovieUiModel) {
        _movieStateFlow.value = movie
    }

    fun toggleLike() {
        val currentMovie = _movieStateFlow.value ?: return

        viewModelScope.launch(AppDispatchers.io()) {
                if (currentMovie.liked) {
                    dislikeMovieUseCase(currentMovie.id)
                } else {
                    likeMovieUseCase(currentMovie.id)
                }
                _movieStateFlow.value = currentMovie.copy(liked = !currentMovie.liked)
        }
    }
}