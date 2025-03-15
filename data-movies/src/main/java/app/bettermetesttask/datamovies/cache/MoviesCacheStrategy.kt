package app.bettermetesttask.datamovies.cache

import android.content.SharedPreferences
import java.util.concurrent.TimeUnit
import javax.inject.Inject

interface MoviesCacheStrategy {
    fun shouldFetchFromNetwork(): Boolean
    fun updateLastFetchTime()
    fun invalidateCache()
}

class TimeBasedMoviesCacheStrategy @Inject constructor(
    private val preferences: SharedPreferences
) : MoviesCacheStrategy {
    companion object {
        private const val KEY_LAST_FETCH_TIME = "last_fetch_time"
        private const val CACHE_DURATION_HOURS = 1L
    }

    override fun shouldFetchFromNetwork(): Boolean {
        val lastFetchTime = preferences.getLong(KEY_LAST_FETCH_TIME, 0)
        val currentTime = System.currentTimeMillis()
        return currentTime - lastFetchTime > TimeUnit.HOURS.toMillis(CACHE_DURATION_HOURS)
    }

    override fun updateLastFetchTime() {
        preferences.edit().putLong(KEY_LAST_FETCH_TIME, System.currentTimeMillis()).apply()
    }

    override fun invalidateCache() {
        preferences.edit().remove(KEY_LAST_FETCH_TIME).apply()
    }
}