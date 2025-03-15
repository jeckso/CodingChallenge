package app.bettermetesttask.movies.sections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domaincore.utils.coroutines.AppDispatchers
import app.bettermetesttask.domainmovies.interactors.AddMovieToFavoritesUseCase
import app.bettermetesttask.domainmovies.interactors.ObserveMoviesUseCase
import app.bettermetesttask.domainmovies.interactors.RemoveMovieFromFavoritesUseCase
import app.bettermetesttask.movies.model.MovieUiModel
import app.bettermetesttask.movies.navigation.MoviesNavigator
import app.bettermetesttask.movies.utils.MovieUiMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

class MoviesViewModel @Inject constructor(
    private val observeMoviesUseCase: ObserveMoviesUseCase,
    private val likeMovieUseCase: AddMovieToFavoritesUseCase,
    private val dislikeMovieUseCase: RemoveMovieFromFavoritesUseCase,
    private val mapper: MovieUiMapper,
    private val navigator: MoviesNavigator
) : ViewModel() {

    private val _moviesStateFlow: MutableStateFlow<MoviesState> =
        MutableStateFlow(MoviesState.Initial)

    val moviesStateFlow: StateFlow<MoviesState>
        get() = _moviesStateFlow.asStateFlow()

    private val _movies = MutableSharedFlow<List<MovieUiModel>>(replay = 1)
    val movies = _movies.asSharedFlow()

    fun loadMovies() {
        viewModelScope.launch() {
            _moviesStateFlow.value = MoviesState.Loading
            observeMoviesUseCase().flowOn(AppDispatchers.io()).catch { error ->
                _moviesStateFlow.value = MoviesState.Error(error)
            }
                .collect { result ->
                    _moviesStateFlow.value = when (result) {
                        is Result.Success -> {
                            _movies.emit(result.data.map { mapper.mapToUI(it) })
                            MoviesState.Loaded(result.data)
                        }

                        is Result.Error -> MoviesState.Error(result.error)
                    }

                }
        }
    }

    fun likeMovie(movie: MovieUiModel) {
        viewModelScope.launch(AppDispatchers.io()) {
            try {
                if (movie.liked) {
                    dislikeMovieUseCase(movie.id)
                } else {
                    likeMovieUseCase(movie.id)
                }
            } catch (e: Exception) {
                _moviesStateFlow.value = MoviesState.Error(e)
            }
        }
    }

    fun openMovieDetails(movie: MovieUiModel) {
        viewModelScope.launch {
            navigator.navigateToMovieDetails(movie)
        }
    }
}