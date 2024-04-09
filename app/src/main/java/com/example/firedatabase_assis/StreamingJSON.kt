package com.example.firedatabase_assis

import com.google.gson.Gson

data class Media(
        val type: String,
        val title: String,
        val overview: String?,
        val streamingService: StreamingService?,
        val streamingInfo: StreamingInfo?,
        val year: Int?,
        val imdbId: String?,
        val tmdbId: Int?,
        val genres: List<Genre>,
        val directors: List<String>?,
        val creators: List<String>?,
        val status: Status?,
        val seasonCount: Int?,
        val episodeCount: Int?,
        val seasons: List<Season>?
        )

data class Genre(
        val id: Int,
        val name: String
        )

data class Status(
        val statusCode: Int,
        val statusText: String
    )

data class StreamingInfo(
        val country: Map<String, StreamingService>
    )

data class StreamingService(
        val streamingType: String,
        val quality: String,
        val link: String,
        val audios: List<String>
    )

data class Season(
        val type: String,
        val title: String,
        val firstAirYear: Int?,
        val lastAirYear: Int?,
        val streamingInfo: StreamingInfo?
    )

    // Function to parse JSON string into Media object

fun parseMediaJson(jsonString: String): Media {
    return Gson().fromJson(jsonString, Media::class.java)
}

