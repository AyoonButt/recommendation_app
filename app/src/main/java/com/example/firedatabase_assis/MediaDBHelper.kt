package com.example.firedatabase_assis


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log


class MediaDBHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "MediaDatabase.db"
        private const val TABLE_STREAMING_INFO = "StreamingInfo"

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
        private const val COLUMN_HAS_MORE = "hasMore"
        private const val COLUMN_NEXT_CURSOR = "nextCursor"


    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create media table
        val CREATE_STREAMING_INFO_TABLE = ("CREATE TABLE $TABLE_STREAMING_INFO ("
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
                + "$COLUMN_IMDB_ID TEXT UNIQUE,"
                + "$COLUMN_TMDB_ID INTEGER ,"
                + "$COLUMN_GENRES TEXT,"
                + "$COLUMN_DIRECTORS TEXT,"
                + "$COLUMN_CREATORS TEXT,"
                + "$COLUMN_STATUS TEXT,"
                + "$COLUMN_SEASON_COUNT INTEGER ,"
                + "$COLUMN_EPISODE_COUNT INTEGER,"
                + "$COLUMN_SEASONS TEXT,"
                + "$COLUMN_HAS_MORE TEXT,"
                + "$COLUMN_NEXT_CURSOR TEXT"
                + ")")
        db.execSQL(CREATE_STREAMING_INFO_TABLE)
    }


    fun insertRow(media: Media, streamingServices: List<StreamingService>, paging: PaginationInfo) {
        val db = this.writableDatabase
        try {

            val streamingValues = ContentValues().apply {
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


                for (service in streamingServices) {

                    put(COLUMN_STREAMING_SERVICE, service.service)
                    put(COLUMN_STREAMING_TYPE, service.streamingType)
                    put(COLUMN_QUALITY, service.quality)
                    put(COLUMN_LINK, service.link)
                    put(COLUMN_VIDEO_LINK, service.videoLink)
                    put(COLUMN_AUDIOS, service.audios.toString()) // Convert list to string
                    put(COLUMN_SUBTITLES, service.subtitles.toString()) // Convert list to string
                    put(COLUMN_AVAILABLE_SINCE, service.availableSince)
                }



                put(COLUMN_HAS_MORE, paging.hasMore)
                put(COLUMN_NEXT_CURSOR, paging.nextCursor)

            }



            db.insert(TABLE_STREAMING_INFO, null, streamingValues)

        } catch (e: Exception) {
            // Handle exceptions
            e.printStackTrace()
        } finally {
            // Close database connection
            db.close()
        }

    }

    fun getMostRecentNextCursor(streamingService: String): String? {
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT $COLUMN_NEXT_CURSOR FROM $TABLE_STREAMING_INFO WHERE $COLUMN_STREAMING_SERVICE = ?",
            arrayOf(streamingService)
        )
        val cursorIndex = cursor.getColumnIndex(COLUMN_NEXT_CURSOR)
        val mostRecentCursor = if (cursor.moveToFirst()) {
            cursor.getString(cursorIndex)
        } else {
            null
        }
        cursor.close()
        db.close()
        return mostRecentCursor
    }

    fun isNextCursorEmpty(): Boolean {
        val db = this.readableDatabase
        val cursor = db.query(
            TABLE_STREAMING_INFO,
            arrayOf(COLUMN_NEXT_CURSOR),
            "$COLUMN_NEXT_CURSOR IS NULL OR $COLUMN_NEXT_CURSOR = ''",
            null,
            null,
            null,
            null
        )

        val isEmpty = cursor.count == 0
        cursor.close()
        db.close()

        return isEmpty
    }

    fun getCaption(context: Context): String? {
        // Create an instance of your MediaDBHelper class
        val dbHelper = MediaDBHelper(context)

        // Get a readable database
        val db = dbHelper.readableDatabase

        // Define the projection that specifies the columns from which to retrieve data
        val projection = arrayOf(
            COLUMN_TYPE,
            COLUMN_TITLE,
            COLUMN_OVERVIEW,
            COLUMN_STREAMING_SERVICE,
            COLUMN_STREAMING_TYPE,
            COLUMN_QUALITY,
            COLUMN_LINK,
            COLUMN_VIDEO_LINK,
            COLUMN_AUDIOS,
            COLUMN_SUBTITLES,
            COLUMN_AVAILABLE_SINCE,
            COLUMN_YEAR,
            COLUMN_IMDB_ID,
            COLUMN_TMDB_ID,
            COLUMN_GENRES,
            COLUMN_DIRECTORS,
            COLUMN_CREATORS,
            COLUMN_STATUS,
            COLUMN_SEASON_COUNT,
            COLUMN_EPISODE_COUNT,
            COLUMN_SEASONS,
            COLUMN_HAS_MORE,
            COLUMN_NEXT_CURSOR
        )

        // Define the sorting order to get the latest row
        val sortOrder = "ROWID DESC" // Assuming ROWID represents the order of insertion

        // Perform the query to retrieve the last row
        val cursor = db.query(
            TABLE_STREAMING_INFO, // The table name to query
            projection, // The columns to return
            null, // The columns for the WHERE clause
            null, // The values for the WHERE clause
            null, // Don't group the rows
            null, // Don't filter by row groups
            sortOrder, // The sort order
            "1" // Limit to one row
        )

        // Row retrieved from the database
        var row: String? = null

        // Check if there is at least one row in the cursor
        if (cursor.moveToFirst()) {
            // Iterate over the columns and append the values to the row string
            val rowBuilder = StringBuilder()
            for (i in 0 until cursor.columnCount) {
                rowBuilder.append("${cursor.getColumnName(i)}: ${cursor.getString(i)}\n")
            }
            row = rowBuilder.toString()
        } else {
            // If the cursor is empty, print a message indicating no data found
            Log.d("getLastRow", "No row found in the database.")
        }

        // Close the cursor and the database
        cursor.close()
        db.close()

        // Return the last row retrieved from the database
        return row
    }


    /* fun deleteRowsWithNullTypeAndService(db: SQLiteDatabase, tableName: String) {
         // Perform the delete operation
         val deletedRows = db.delete(tableName, "type IS NULL AND streamingType IS NULL", null)

         // Check if any rows were deleted
         if (deletedRows > 0) {
             // Rows deleted successfully
             println("$deletedRows rows deleted where type and streaming_service are null")
         } else {
             // No rows deleted
             println("No rows deleted where type and streaming_service are null")
         }
     }

     */

    fun getLastImdbIdFromDatabase(context: Context): String? {
        // Create an instance of your MediaDBHelper class
        val dbHelper = MediaDBHelper(context)

        // Get a readable database
        val db = dbHelper.readableDatabase

        // Define the column from which to retrieve IMDb ID
        val projection = arrayOf(COLUMN_IMDB_ID)

        // Define the sorting order to get the latest row
        val sortOrder = "ROWID DESC" // Assuming ROWID represents the order of insertion

        // Perform the query to retrieve the latest IMDb ID
        val cursor = db.query(
            TABLE_STREAMING_INFO, // The table name to query
            projection, // The columns to return
            null, // The columns for the WHERE clause
            null, // The values for the WHERE clause
            null, // Don't group the rows
            null, // Don't filter by row groups
            sortOrder, // The sort order
            "1" // Limit to one row
        )

        // IMDb ID retrieved from the database
        var imdbId: String? = null

        // Check if there is at least one row in the cursor
        if (cursor.moveToFirst()) {
            // Retrieve the IMDb ID from the cursor
            val columnIndex = cursor.getColumnIndex(COLUMN_IMDB_ID)
            if (columnIndex >= 0) {
                imdbId = cursor.getString(columnIndex)
            } else {
                Log.e("getColumnIndex", "Column '$COLUMN_IMDB_ID' not found in cursor")
            }
        } else {
            // If the cursor is empty, print a message indicating no data found
            Log.d("getLastImdbId", "No IMDb ID found in the database.")
        }

        // Close the cursor and the database
        cursor.close()
        db.close()

        // Return the last IMDb ID retrieved from the database
        return imdbId
    }


    fun deleteRowByExternalId(context: Context, externalId: String): Boolean {
        val dbHelper = MediaDBHelper(context)
        val db = dbHelper.writableDatabase

        return try {
            val rowsAffected =
                db.delete(TABLE_STREAMING_INFO, "$COLUMN_IMDB_ID = ?", arrayOf(externalId))
            rowsAffected > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            db.close()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STREAMING_INFO")
        // Create tables again
        onCreate(db)
    }

}
