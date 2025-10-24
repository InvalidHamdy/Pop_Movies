    package com.InvalidHamdy.moviezshow

    data class Genre(
        val id: Int,
        val name: String
    )

    data class GenreResponse(
        val genres: List<Genre>
    )

    data class MediaItem(
        val id: Int,
        val poster_path: String?,
        val backdrop_path: String?,
        val title: String?,
        val name: String?,
        val overview: String?,
        val number_of_seasons: Int?,
        val first_air_date: String?,
        val release_date: String?
    )

    data class MediaResponse(
        val results: List<MediaItem>
    )
    data class CastMember(
        val id: Int,
        val name: String?,
        val character: String?,
        val profile_path: String?
    )

    data class CreditsResponse(
        val id: Int,
        val cast: List<CastMember> = emptyList()
    )