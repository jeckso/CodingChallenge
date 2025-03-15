package app.bettermetesttask.domainmovies.errors

sealed class MovieError : Exception() {
    data class NetworkError(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : MovieError()

    data class DatabaseError(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : MovieError()

    data class CacheError(
        override val message: String? = null,
        override val cause: Throwable? = null
    ) : MovieError()

    object MovieNotFound : MovieError() {
        override val message: String = "Movie not found"
    }

    object NoNetworkAndEmptyCache : MovieError() {
        override val message: String = "No network connection and cache is empty"
    }
}