package app.bettermetesttask.movies.navigation

import androidx.navigation.NavController
import app.bettermetesttask.featurecommon.utils.navigation.executeSafeNavAction
import app.bettermetesttask.movies.R
import app.bettermetesttask.movies.model.MovieUiModel
import app.bettermetesttask.movies.sections.details.MovieDetailsBottomSheet
import dagger.Lazy
import javax.inject.Inject

interface MoviesNavigator {
    fun navigateToMovieDetails(movie: MovieUiModel)
}

class MoviesNavigatorImpl @Inject constructor(
    private val navController: Lazy<NavController>
) : MoviesNavigator {

    override fun navigateToMovieDetails(movie: MovieUiModel) {
        executeSafeNavAction {
            navController.get().navigate(
                R.id.action_show_movie_details,
                MovieDetailsBottomSheet.createArgs(movie)
            )
        }
    }
}