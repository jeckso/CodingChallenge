package app.bettermetesttask.datamovies.repository

import app.bettermetesttask.datamovies.cache.MoviesCacheStrategy
import app.bettermetesttask.datamovies.database.entities.MovieEntity
import app.bettermetesttask.datamovies.repository.stores.MoviesLocalStore
import app.bettermetesttask.datamovies.repository.stores.MoviesMapper
import app.bettermetesttask.datamovies.repository.stores.MoviesRestStore
import app.bettermetesttask.domaincore.utils.Result
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class MoviesRepositoryTest {

    @Mock
    private lateinit var localStore: MoviesLocalStore

    @Mock
    private lateinit var restStore: MoviesRestStore

    private lateinit var mapper: MoviesMapper
    private lateinit var repository: MoviesRepositoryImpl
    private lateinit var cacheStrategy: MoviesCacheStrategy

    @BeforeEach
    fun setup() {
        mapper = MoviesMapper()
        repository = MoviesRepositoryImpl(localStore, restStore, mapper, cacheStrategy)

        // Clear DB before each test
        runTest {
            localStore.clearMovies()
        }
    }

    @Test
    fun `getMovies should return remote data and cache it when remote call succeeds`() = runTest {
        val result = repository.getMovies()

        // Then
        // Verify DB was cleared and new data was saved
        verify(localStore, times(1)).clearMovies()
        verify(localStore).saveMovies(anyList())

        assert(result is Result.Success)
        val successResult = result as Result.Success
        // Verify the structure without knowing exact data
        successResult.data.forEach { movie ->
            assert(movie.id >= 0)
            assert(movie.title.isNotEmpty())
            assert(movie.description.isNotEmpty())
        }
    }

    @Test
    fun `getMovie should return mapped movie from local store`() = runTest {
        // Given
        val movieId = 1
        val localMovie = MovieEntity(movieId, "Test Movie", "Test Desc", "test/path")
        `when`(localStore.getMovie(movieId)).thenReturn(localMovie)

        // When
        val result = repository.getMovie(movieId)

        // Then
        verify(localStore).getMovie(movieId)
        assert(result is Result.Success)
        val movie = (result as Result.Success).data
        assert(movie.id == movieId)
        assert(movie.title.isNotEmpty())
        assert(movie.description.isNotEmpty())
    }

    @Test
    fun `observeLikedMovieIds should return flow from local store`() = runTest {
        // Given
        val likedIds = listOf(1, 2, 3)
        `when`(localStore.observeLikedMoviesIds()).thenReturn(flowOf(likedIds))

        // When
        val result = repository.observeLikedMovieIds()

        // Then
        result.collect { ids ->
            assert(ids.size == 3)
            assert(ids.all { it > 0 })
        }
    }

    @Test
    fun `addMovieToFavorites should call local store likeMovie`() = runTest {
        // Given
        val movieId = 1

        // When
        repository.addMovieToFavorites(movieId)

        // Then
        verify(localStore).likeMovie(movieId)
    }

    @Test
    fun `removeMovieFromFavorites should call local store dislikeMovie`() = runTest {
        // Given
        val movieId = 1

        // When
        repository.removeMovieFromFavorites(movieId)

        // Then
        verify(localStore).dislikeMovie(movieId)
    }

    @Test
    fun `getMovie should return error when local store fails`() = runTest {
        // Given
        val movieId = 1
        val exception = RuntimeException("DB error")
        `when`(localStore.getMovie(movieId)).thenThrow(exception)

        // When
        val result = repository.getMovie(movieId)

        // Then
        assert(result is Result.Error)
        assert((result as Result.Error).error.message?.contains("DB") == true)
    }
}