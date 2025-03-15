package app.bettermetesttask.domainmovies.interactors

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import app.bettermetesttask.domaincore.utils.Result
import app.bettermetesttask.domainmovies.entries.Movie
import app.bettermetesttask.domainmovies.repository.MoviesRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(MockitoExtension::class)
class ObserveMoviesUseCaseTest {

    @Mock
    private lateinit var repository: MoviesRepository

    private lateinit var useCase: ObserveMoviesUseCase

    private val testMovies = listOf(
        Movie(1, "Movie 1", "Description 1", "path1", liked = false),
        Movie(2, "Movie 2", "Description 2", "path2", liked = false),
        Movie(3, "Movie 3", "Description 3", "path3", liked = false)
    )

    @BeforeEach
    fun setup() {
        useCase = ObserveMoviesUseCase(repository)
    }

    @Test
    fun `invoke should return flow with liked movies when repository returns success`() = runTest {
        // Given
        val likedMovieIds = listOf(1, 3)
        val expectedMovies = listOf(
            testMovies[0].copy(liked = true),
            testMovies[1].copy(liked = false),
            testMovies[2].copy(liked = true)
        )

        `when`(repository.getMovies()).thenReturn(Result.Success(testMovies))
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(likedMovieIds))

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedMovies, (result[0] as Result.Success).data)
    }

    @Test
    fun `invoke should return flow with unchanged movies when no movies are liked`() = runTest {
        // Given
        val emptyLikedIds = emptyList<Int>()

        `when`(repository.getMovies()).thenReturn(Result.Success(testMovies))
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(emptyLikedIds))

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(testMovies, (result[0] as Result.Success).data)
    }

    @Test
    fun `invoke should return flow with error when repository returns error`() = runTest {
        // Given
        val error = RuntimeException("Test error")
        `when`(repository.getMovies()).thenReturn(Result.Error(error))

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Error)
        assertEquals(error, (result[0] as Result.Error).error)
    }

    @Test
    fun `invoke should return flow with all movies liked when all IDs are in liked set`() = runTest {
        // Given
        val allLikedIds = testMovies.map { it.id }
        val expectedMovies = testMovies.map { it.copy(liked = true) }

        `when`(repository.getMovies()).thenReturn(Result.Success(testMovies))
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(allLikedIds))

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertEquals(expectedMovies, (result[0] as Result.Success).data)
    }

    @Test
    fun `invoke should handle empty movie list correctly`() = runTest {
        // Given
        val emptyMovies = emptyList<Movie>()
        val someLikedIds = listOf(1, 2)

        `when`(repository.getMovies()).thenReturn(Result.Success(emptyMovies))
        `when`(repository.observeLikedMovieIds()).thenReturn(flowOf(someLikedIds))

        // When
        val result = useCase().toList()

        // Then
        assertEquals(1, result.size)
        assertTrue(result[0] is Result.Success)
        assertTrue((result[0] as Result.Success).data.isEmpty())
    }
}