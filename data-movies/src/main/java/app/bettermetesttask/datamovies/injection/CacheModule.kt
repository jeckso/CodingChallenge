package app.bettermetesttask.datamovies.injection

import android.content.Context
import android.content.SharedPreferences
import app.bettermetesttask.datamovies.cache.MoviesCacheStrategy
import app.bettermetesttask.datamovies.cache.TimeBasedMoviesCacheStrategy
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class CacheModule {

    @Provides
    @Singleton
    fun provideMoviesCacheStrategy(preferences: SharedPreferences): MoviesCacheStrategy {
        return TimeBasedMoviesCacheStrategy(preferences)
    }
}