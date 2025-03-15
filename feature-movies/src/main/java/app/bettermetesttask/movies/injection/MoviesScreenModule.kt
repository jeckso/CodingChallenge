package app.bettermetesttask.movies.injection

import androidx.navigation.NavController
import app.bettermetesttask.movies.navigation.MoviesNavigator
import app.bettermetesttask.movies.navigation.MoviesNavigatorImpl
import dagger.Module
import dagger.Provides
import dagger.Lazy

@Module
class MoviesScreenModule {

    @Provides
    fun provideMoviesNavigator(
        navController: Lazy<NavController>
    ): MoviesNavigator = MoviesNavigatorImpl(navController)

}