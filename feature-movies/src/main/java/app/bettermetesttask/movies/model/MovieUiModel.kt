package app.bettermetesttask.movies.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MovieUiModel(
    val id: Int,
    val title: String,
    val description: String,
    val posterPath: String?,
    val liked: Boolean = false
) : Parcelable