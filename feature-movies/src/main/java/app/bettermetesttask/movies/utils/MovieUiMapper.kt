package app.bettermetesttask.movies.utils

import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.movies.model.MovieUiModel
import javax.inject.Inject

class MovieUiMapper @Inject constructor() {

    val mapToUI: (Movie) -> MovieUiModel = {
        with(it) {
            MovieUiModel(id, title, description, posterPath, liked)
        }
    }

    val mapFromUI: (MovieUiModel) -> Movie = {
        with(it) {
            Movie(id, title, description, posterPath, liked)
        }
    }
}