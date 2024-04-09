package com.example.firedatabase_assis

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MediaDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MediaDatabase.db"
        private const val TABLE_MEDIA = "media"

        // Define table columns
        private const val COLUMN_ID = "id"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_OVERVIEW = "overview"
        private const val COLUMN_STREAMING_SERVICE = "streamingService"
        private const val COLUMN_STREAMING_INFO = "streamingInfo"
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

        // Add other column names as needed
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create media table
        val CREATE_MEDIA_TABLE = ("CREATE TABLE $TABLE_MEDIA ("
                + "$COLUMN_ID INTEGER PRIMARY KEY,"
                + "$COLUMN_TYPE TEXT,"
                + "$COLUMN_TITLE TEXT,"
                + "$COLUMN_OVERVIEW TEXT,"
                + "$COLUMN_STREAMING_SERVICE TEXT,"
                + "$COLUMN_STREAMING_INFO TEXT,"
                + "$COLUMN_YEAR INTEGER,"
                + "$COLUMN_IMDB_ID TEXT,"
                + "$COLUMN_TMDB_ID INTEGER,"
                + "$COLUMN_GENRES TEXT,"
                + "$COLUMN_DIRECTORS TEXT,"
                + "$COLUMN_CREATORS TEXT,"
                + "$COLUMN_STATUS TEXT,"
                + "$COLUMN_SEASON_COUNT INTEGER,"
                + "$COLUMN_EPISODE_COUNT INTEGER,"
                + "$COLUMN_SEASONS TEXT"
                + ")")
        db.execSQL(CREATE_MEDIA_TABLE)
    }

    fun insertMedia(media: Media) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COLUMN_TYPE, media.type)
        values.put(COLUMN_TITLE, media.title)
        values.put(COLUMN_OVERVIEW, media.overview)
        values.put(COLUMN_STREAMING_SERVICE, media.streamingService?.toString())
        values.put(COLUMN_STREAMING_INFO, media.streamingInfo?.toString())
        values.put(COLUMN_YEAR, media.year)
        values.put(COLUMN_IMDB_ID, media.imdbId)
        values.put(COLUMN_TMDB_ID, media.tmdbId)
        values.put(COLUMN_GENRES, media.genres.toString())
        values.put(COLUMN_DIRECTORS, media.directors?.toString())
        values.put(COLUMN_CREATORS, media.creators?.toString())
        values.put(COLUMN_STATUS, media.status?.toString())
        values.put(COLUMN_SEASON_COUNT, media.seasonCount)
        values.put(COLUMN_EPISODE_COUNT, media.episodeCount)
        values.put(COLUMN_SEASONS, media.seasons?.toString())
        // Insert row
        db.insert(TABLE_MEDIA, null, values)
        db.close() // Closing database connection
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEDIA")
        // Create tables again
        onCreate(db)
    }
}
