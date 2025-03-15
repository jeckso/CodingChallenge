package app.bettermetesttask.movies.injection

import app.bettermetesttask.featurecommon.injection.scopes.FragmentScope
import app.bettermetesttask.movies.sections.details.MovieDetailsBottomSheet
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class MoviesDetailsBuildersModule {

    @FragmentScope
    @ContributesAndroidInjector(modules = [MoviesScreenModule::class])
    abstract fun createMoviesDetailsInjector(): MovieDetailsBottomSheet

}