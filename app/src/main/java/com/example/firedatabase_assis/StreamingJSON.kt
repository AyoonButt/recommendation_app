package com.example.firedatabase_assis

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import com.google.gson.JsonParser

data class Media(
    val type: String,
    val title: String,
    val overview: String,
    val cast: List<String>,
    val year: Int,
    val imdbId: String,
    val tmdbId: Int,
    val originalTitle: String,
    val genres: List<Genre>,
    val directors: List<String>,
    val creators: List<String>?, // Nullable for series
    val status: Status?, // Nullable for series
    val seasonCount: Int?, // Nullable for series
    val episodeCount: Int?, // Nullable for series
    val seasons: List<Season>? // Nullable for series
)

data class StreamingInfo(
    val us: List<StreamingService>
)

data class StreamingService(
    val service: String,
    val streamingType: String,
    val quality: String,
    val link: String,
    val videoLink: String,
    val audios: List<Audio>,
    val subtitles: List<Subtitle>,
    val availableSince: Long
)

data class Audio(
    val language: String,
    val region: String
)

data class Subtitle(
    val locale: Locale,
    val closedCaptions: Boolean
)

data class Locale(
    val language: String,
    val region: String
)

data class Genre(
    val id: Int,
    val name: String
)

data class Status(
    val statusCode: Int,
    val statusText: String
)

data class Season(
    val number: Int,
    val episodes: List<Episode>
)

data class Episode(
    val title: String,
    val overview: String,
    val episodeNumber: Int
)

data class PaginationInfo(
    val hasMore: Boolean?,
    val nextCursor: String?
)

fun splitMediaJson(jsonResponse: String): List<JsonObject> {
    val jsonObject = JsonParser.parseString(jsonResponse).asJsonObject
    val resultArray = jsonObject.getAsJsonArray("result")
    val mediaJsonObjects = mutableListOf<JsonObject>()
    var lastJsonObject: JsonObject? = null

    resultArray?.forEach { mediaElement ->
        val mediaObject = mediaElement.asJsonObject
        if (mediaObject.has("type")) {
            lastJsonObject = mediaObject
        } else {
            lastJsonObject?.let {
                val hasMoreJsonElement = mediaObject.get("hasMore")
                val nextCursorJsonElement = mediaObject.get("nextCursor")

                if (hasMoreJsonElement != null && nextCursorJsonElement != null) {
                    it.add("hasMore", hasMoreJsonElement)
                    it.add("nextCursor", nextCursorJsonElement)
                }
            }
        }

        // Check if the media object is not already in the list before adding it
        if (lastJsonObject != null && !mediaJsonObjects.contains(lastJsonObject)) {
            mediaJsonObjects.add(lastJsonObject!!)
            Log.d("SplitJsonObject", lastJsonObject.toString())
        }
    }

    return mediaJsonObjects
}


fun parseMediaJson(jsonResponse: String): Media {

    val jsonObject = JsonParser.parseString(jsonResponse).asJsonObject

    val type = jsonObject.get("type")?.asString ?: ""
    val title = jsonObject.get("title")?.asString ?: ""
    val overview = jsonObject.get("overview")?.asString ?: ""
    val year = jsonObject.get("year")?.asInt ?: 0
    val imdbId = jsonObject.get("imdbId")?.asString ?: ""
    val tmdbId = jsonObject.get("tmdbId")?.asInt ?: 0
    val originalTitle = jsonObject.get("originalTitle")?.asString ?: ""

    val genresJsonArray = jsonObject.getAsJsonArray("genres")
    val genres = mutableListOf<Genre>()
    genresJsonArray?.forEach { genreElement ->
        val genreJsonObject = genreElement.asJsonObject
        val genreId = genreJsonObject.get("id")?.asInt ?: 0
        val genreName = genreJsonObject.get("name")?.asString ?: ""
        genres.add(Genre(genreId, genreName))
    }


    val directorsJsonArray = jsonObject.getAsJsonArray("directors")
    val directors = mutableListOf<String>()
    directorsJsonArray?.forEach { directorElement ->
        directors.add(directorElement.asString)
    }

    val castJsonArray = jsonObject.getAsJsonArray("cast")
    val cast = mutableListOf<String>()
    castJsonArray?.forEach { castElement ->
        cast.add(castElement.asString)
    }

    val creatorsJsonArray = jsonObject.getAsJsonArray("creators")
    val creators = mutableListOf<String>()
    creatorsJsonArray?.forEach { creatorElement ->
        creators.add(creatorElement.asString)
    }

    val statusJsonObject = jsonObject.getAsJsonObject("status")
    val status: Status? = statusJsonObject?.let {
        Status(it.get("statusCode")?.asInt ?: 0, it.get("statusText")?.asString ?: "")
    }

    val seasonCount: Int? = jsonObject.get("seasonCount")?.asInt
    val episodeCount: Int? = jsonObject.get("episodeCount")?.asInt

    val seasonsJsonArray = jsonObject.getAsJsonArray("seasons")
    val seasons: List<Season>? = seasonsJsonArray?.map { seasonElement ->
        val seasonObject = seasonElement.asJsonObject
        val seasonNumber = seasonObject.get("number")?.asInt ?: 0
        val episodesJsonArray = seasonObject.getAsJsonArray("episodes")
        val episodes = mutableListOf<Episode>()
        episodesJsonArray?.forEach { episodeElement ->
            val episodeObject = episodeElement.asJsonObject
            val episodeTitle = episodeObject.get("title")?.asString ?: ""
            val episodeOverview = episodeObject.get("overview")?.asString ?: ""
            val episodeNumber = episodeObject.get("episodeNumber")?.asInt ?: 0
            episodes.add(Episode(episodeTitle, episodeOverview, episodeNumber))
        }
        Season(seasonNumber, episodes)
    }

    return Media(
        type,
        title,
        overview,
        cast,
        year,
        imdbId,
        tmdbId,
        originalTitle,
        genres,
        directors,
        creators,
        status,
        seasonCount,
        episodeCount,
        seasons
    )
}

fun parseStreamingInfo(streamingInfoJsonObject: JsonObject): StreamingInfo {
    val usServicesJsonArray = streamingInfoJsonObject.getAsJsonArray("us")
    val usServices = mutableListOf<StreamingService>()
    usServicesJsonArray?.forEach { serviceElement ->
        val serviceObject = serviceElement.asJsonObject
        val service = serviceObject.get("service")?.asString ?: ""
        val streamingType = serviceObject.get("streamingType")?.asString ?: ""
        val quality = serviceObject.get("quality")?.asString ?: ""
        val link = serviceObject.get("link")?.asString ?: ""
        val videoLink = serviceObject.get("videoLink")?.asString ?: ""

        val audios = mutableListOf<Audio>()
        val audiosJsonArray = serviceObject.getAsJsonArray("audios")
        audiosJsonArray?.forEach { audioElement ->
            val audioObject = audioElement.asJsonObject
            val language = audioObject.get("language")?.asString ?: ""
            val region = audioObject.get("region")?.asString ?: ""
            audios.add(Audio(language, region))
        }

        val subtitles = mutableListOf<Subtitle>()
        val subtitlesJsonArray = serviceObject.getAsJsonArray("subtitles")
        subtitlesJsonArray?.forEach { subtitleElement ->
            val subtitleObject = subtitleElement.asJsonObject
            val localeObject = subtitleObject.getAsJsonObject("locale")
            val localeLanguage = localeObject.get("language")?.asString ?: ""
            val localeRegion = localeObject.get("region")?.asString ?: ""
            val closedCaptions = subtitleObject.get("closedCaptions")?.asBoolean ?: false
            subtitles.add(Subtitle(Locale(localeLanguage, localeRegion), closedCaptions))
        }

        val availableSince = serviceObject.get("availableSince")?.asLong ?: 0L

        val streamingServiceInfo = StreamingService(
            service = service,
            streamingType = streamingType,
            quality = quality,
            link = link,
            videoLink = videoLink,
            audios = audios,
            subtitles = subtitles,
            availableSince = availableSince
        )

        usServices.add(streamingServiceInfo)
    }
    return StreamingInfo(us = usServices)
}

fun paginationInfoParser(response: String): PaginationInfo {
    val jsonObject = JsonParser.parseString(response).asJsonObject
    val hasMoreJsonElement = jsonObject.getAsJsonPrimitive("hasMore")
    val nextCursorJsonElement = jsonObject.getAsJsonPrimitive("nextCursor")
    val hasMore = hasMoreJsonElement?.asBoolean ?: false
    val nextCursor = nextCursorJsonElement?.asString ?: ""
    return PaginationInfo(hasMore, nextCursor)
}

fun parseAndInsertMedia(response: String, context: Context) {
    val mediaJsonObjects = splitMediaJson(response)
    var paginationInfo = PaginationInfo(null, null) // Default pagination info

    mediaJsonObjects.forEachIndexed { index, mediaJsonObject ->
        if (!isValidMediaObject(mediaJsonObject)) {
            return@forEachIndexed
        }
        val media = parseMediaJson(mediaJsonObject.toString())

        val mediaEntity = Media(
            type = media.type,
            title = media.title,
            overview = media.overview,
            cast = media.cast,
            year = media.year,
            imdbId = media.imdbId,
            tmdbId = media.tmdbId,
            originalTitle = media.originalTitle,
            genres = media.genres,
            directors = media.directors,
            creators = media.creators,
            status = media.status,
            seasonCount = media.seasonCount,
            episodeCount = media.episodeCount,
            seasons = media.seasons
        )

        val streamingInfoJsonObject = mediaJsonObject.getAsJsonObject("streamingInfo")
        val usServicesList = parseStreamingInfo(streamingInfoJsonObject).us
        val serviceList = mutableListOf<StreamingService>()

        for (service in usServicesList) {
            serviceList.add(
                StreamingService(
                    service = service.service,
                    streamingType = service.streamingType,
                    quality = service.quality,
                    link = service.link,
                    videoLink = service.videoLink,
                    audios = service.audios,
                    subtitles = service.subtitles,
                    availableSince = service.availableSince
                )
            )
        }

        val dbHelper = MediaDBHelper(context.applicationContext)

        if (index == mediaJsonObjects.size - 1) {
            paginationInfo = paginationInfoParser(response)
            dbHelper.insertRow(mediaEntity, usServicesList, paginationInfo)
        } else {
            dbHelper.insertRow(mediaEntity, usServicesList, paginationInfo)
        }
    }
}


private fun isValidMediaObject(jsonObject: JsonObject): Boolean {
    // Check if the JSON object contains all necessary fields for a complete media object
    return jsonObject.has("type") && jsonObject.has("title") && jsonObject.has("overview")
    // Add more checks for other required fields if necessary
}


fun getLastJsonObject(response: String): JsonObject? {
    val mediaJsonObjects = splitMediaJson(response)
    return if (mediaJsonObjects.isNotEmpty()) {
        mediaJsonObjects.lastOrNull()
    } else {
        null
    }
}