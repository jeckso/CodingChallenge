package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.cache.MoviesCacheStrategy
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.datamovies.utils.withRetry
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.errors.MovieError
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MoviesRepositoryImpl @Inject constructor(
    private val localStore: MoviesLocalStore,
    private val remoteStore: MoviesRestStore,
    private val mapper: MoviesMapper,
    private val cacheStrategy: MoviesCacheStrategy
) : MoviesRepository {

    override suspend fun getMovies(): Result<List<Movie>> = try {
        if (cacheStrategy.shouldFetchFromNetwork()) {
            getMoviesFromRemoteWithRetry()
        } else {
            getMoviesFromLocal()
        }
    } catch (e: Exception) {
        // If network fetch fails, try local as fallback
        when (e) {
            is MovieError.NetworkError -> getMoviesFromLocal()
            else -> Result.Error(e)
        }
    }

    private suspend fun getMoviesFromRemoteWithRetry(): Result<List<Movie>> = try {
        val remoteMovies = withRetry {
            remoteStore.getMovies()
        }
        // Cache the results
        localStore.saveMovies(remoteMovies.map { mapper.mapToLocal(it) })
        cacheStrategy.updateLastFetchTime()
        Result.Success(remoteMovies)
    } catch (e: Exception) {
        throw MovieError.NetworkError(
            message = "Failed to fetch movies from network after multiple attempts",
            cause = e
        )
    }

    private suspend fun getMoviesFromLocal(): Result<List<Movie>> = try {
        val localMovies = localStore.getMovies().map { mapper.mapFromLocal(it) }
        if (localMovies.isEmpty()) {
            Result.Error(MovieError.NoNetworkAndEmptyCache)
        } else {
            Result.Success(localMovies)
        }
    } catch (e: Exception) {
        Result.Error(MovieError.DatabaseError(
            message = "Failed to fetch movies from database",
            cause = e
        ))
    }

    override suspend fun refreshMovies(): Result<List<Movie>> {
        cacheStrategy.invalidateCache()
        return getMovies()
    }

    override suspend fun getMovie(id: Int): Result<Movie> = try {
        val movie = localStore.getMovie(id)
        Result.Success(mapper.mapFromLocal(movie))
    } catch (e: Exception) {
        when (e) {
            is NoSuchElementException -> Result.Error(MovieError.MovieNotFound)
            else -> Result.Error(MovieError.DatabaseError(
                message = "Failed to fetch movie with id: $id",
                cause = e
            ))
        }
    }

    override fun observeLikedMovieIds(): Flow<List<Int>> =
        localStore.observeLikedMoviesIds()

    override suspend fun addMovieToFavorites(movieId: Int): Result<Unit> = try {
        localStore.likeMovie(movieId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(MovieError.DatabaseError(
            message = "Failed to add movie to favorites: $movieId",
            cause = e
        ))
    }

    override suspend fun removeMovieFromFavorites(movieId: Int): Result<Unit> = try {
        localStore.dislikeMovie(movieId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(
            MovieError.DatabaseError(
            message = "Failed to remove movie from favorites: $movieId",
            cause = e
        ))
    }
}