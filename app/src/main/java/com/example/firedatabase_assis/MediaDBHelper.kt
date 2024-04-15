package com.example.firedatabase_assis

import Media
import StreamingService
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MediaDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MediaDatabase.db"
        private const val TABLE_MEDIA = "media"

        // Define table columns
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_OVERVIEW = "overview"
        private const val COLUMN_STREAMING_SERVICE = "streamingService"
        private const val COLUMN_STREAMING_TYPE = "streamingType"
        private const val COLUMN_QUALITY = "quality"
        private const val COLUMN_LINK = "link"
        private const val COLUMN_VIDEO_LINK = "videoLink"
        private const val COLUMN_AUDIOS = "audios"
        private const val COLUMN_SUBTITLES = "subtitles"
        private const val COLUMN_AVAILABLE_SINCE = "availableSince"
        private const val COLUMN_YEAR = "year"
        private const val COLUMN_GENRES = "genres"
        private const val COLUMN_DIRECTORS = "directors"
        private const val COLUMN_CREATORS = "creators"
        private const val COLUMN_STATUS = "status"
        private const val COLUMN_IMDB_ID = "imdbId"
        private const val COLUMN_TMDB_ID = "tmdbId"
        private const val COLUMN_SEASON_COUNT = "seasonCount"
        private const val COLUMN_EPISODE_COUNT = "episodeCount"
        private const val COLUMN_SEASONS = "seasons"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create media table
        val CREATE_MEDIA_TABLE = ("CREATE TABLE $TABLE_MEDIA ("
                + "$COLUMN_TYPE TEXT,"
                + "$COLUMN_TITLE TEXT,"
                + "$COLUMN_OVERVIEW TEXT,"
                + "$COLUMN_STREAMING_SERVICE TEXT,"
                + "$COLUMN_STREAMING_TYPE TEXT,"
                + "$COLUMN_QUALITY TEXT,"
                + "$COLUMN_LINK TEXT,"
                + "$COLUMN_VIDEO_LINK TEXT,"
                + "$COLUMN_AUDIOS TEXT,"
                + "$COLUMN_SUBTITLES TEXT,"
                + "$COLUMN_AVAILABLE_SINCE TEXT,"
                + "$COLUMN_YEAR INTEGER,"
                + "$COLUMN_IMDB_ID TEXT,"
                + "$COLUMN_TMDB_ID INTEGER,"
                + "$COLUMN_GENRES TEXT,"
                + "$COLUMN_DIRECTORS TEXT,"
                + "$COLUMN_CREATORS TEXT,"
                + "$COLUMN_STATUS TEXT,"
                + "$COLUMN_SEASON_COUNT INTEGER,"
                + "$COLUMN_EPISODE_COUNT INTEGER,"
                + "$COLUMN_SEASONS TEXT,"
                + "PRIMARY KEY ($COLUMN_TITLE, $COLUMN_YEAR)" // Define primary key
                + ")")
        db.execSQL(CREATE_MEDIA_TABLE)
    }

    fun insertRow(media: Media, streamingServices: List<StreamingService>) {
        val db = this.writableDatabase
        val mediaValues = ContentValues().apply {
            put(COLUMN_TYPE, media.type)
            put(COLUMN_TITLE, media.title)
            put(COLUMN_OVERVIEW, media.overview)
            put(COLUMN_YEAR, media.year)
            put(COLUMN_IMDB_ID, media.imdbId)
            put(COLUMN_TMDB_ID, media.tmdbId)
            put(COLUMN_GENRES, media.genres.toString())
            put(COLUMN_DIRECTORS, media.directors.toString())
            put(COLUMN_CREATORS, media.creators?.toString())
            put(COLUMN_STATUS, media.status?.toString())
            put(COLUMN_SEASON_COUNT, media.seasonCount)
            put(COLUMN_EPISODE_COUNT, media.episodeCount)
            put(COLUMN_SEASONS, media.seasons?.toString())
        }
        // Insert media row
        db.insert(TABLE_MEDIA, null, mediaValues)

        for (service in streamingServices) {
            val streamingValues = ContentValues().apply {
                put(COLUMN_TITLE, media.title)
                put(COLUMN_YEAR, media.year)
                put(COLUMN_STREAMING_SERVICE, service.service)
                put(COLUMN_STREAMING_TYPE, service.streamingType)
                put(COLUMN_QUALITY, service.quality)
                put(COLUMN_LINK, service.link)
                put(COLUMN_VIDEO_LINK, service.videoLink)
                put(COLUMN_AUDIOS, service.audios.toString()) // Convert list to string
                put(COLUMN_SUBTITLES, service.subtitles.toString()) // Convert list to string
                put(COLUMN_AVAILABLE_SINCE, service.availableSince)
            }
            // Insert streaming info row
            db.insert(TABLE_MEDIA, null, streamingValues)
        }
        db.close() // Closing database connection
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEDIA")
        // Create tables again
        onCreate(db)
    }
}
