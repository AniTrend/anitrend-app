package com.mxt.anitrend.data.genre

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class GenreItem(
    val name: String,
)

interface GenreRepository {
    fun observeGenres(): Flow<List<GenreItem>>
}

class MockGenreRepository : GenreRepository {
    override fun observeGenres(): Flow<List<GenreItem>> = flow {
        emit(
            listOf(
                GenreItem("Action"),
                GenreItem("Adventure"),
                GenreItem("Comedy"),
                GenreItem("Drama"),
                GenreItem("Fantasy"),
                GenreItem("Horror"),
                GenreItem("Mecha"),
                GenreItem("Mystery"),
                GenreItem("Romance"),
                GenreItem("Sci-Fi"),
                GenreItem("Slice of Life"),
                GenreItem("Sports"),
                GenreItem("Supernatural"),
                GenreItem("Thriller"),
                GenreItem("Isekai"),
            )
        )
    }
}
