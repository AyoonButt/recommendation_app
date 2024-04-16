package com.example.firedatabase_assis

import Media
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonParser
import org.json.JSONException
import parseMediaJson
import parseStreamingInfo


class StreamingAPI : JobService() {

    private lateinit var requestQueue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())
    private var requestCount = 0
    private val maxRequestsPerDay = 90
    private lateinit var sharedPreferences: SharedPreferences


    override fun onStartJob(params: JobParameters?): Boolean {


        requestQueue = Volley.newRequestQueue(applicationContext)
        sharedPreferences =
            applicationContext.getSharedPreferences("StreamingAPI", Context.MODE_PRIVATE)

        val netflix = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=netflix&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=%d" +
                "&show_type=all"

        val prime = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=prime.subscription&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=%d" +
                "&show_type=all"

        val hboMax = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=hbo,hulu.addon.hbo,prime.addon.hbomaxus&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=%d" +
                "&show_type=all"

        val hulu = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=prime.subscription&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=%d" +
                "&show_type=all"

        val apple = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=prime.subscription&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=%d" +
                "&show_type=all"

        val peacock = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=prime.subscription&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=%d" +
                "&show_type=all"


        /*val startTime = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
        startTime.set(Calendar.HOUR_OF_DAY, 0)
        startTime.set(Calendar.MINUTE, 0)
        startTime.set(Calendar.SECOND, 0)
        startTime.set(Calendar.MILLISECOND, 0)
         */


        // Call your functions here
        scheduleRequest(netflix, 1995)
        scheduleRequest(prime, 1995)
        scheduleRequest(hboMax, 1995)
        scheduleRequest(hulu, 1995)
        scheduleRequest(apple, 1995)
        scheduleRequest(peacock, 1995)

        // Reset request counter to zero after each day
        requestCount = 0


        return false // Return false because your job is not offloaded to a separate thread
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        requestQueue.cancelAll(this)
        requestQueue.stop() // Stop the request queue to release system resources
        return true // Return true to indicate that the job should be rescheduled
    }


    private fun scheduleRequest(url: String, minYear: Int) {
        if (requestCount < maxRequestsPerDay) {
            handler.postDelayed({
                val fullUrl = "$url&timestamp=${System.currentTimeMillis()}"
                makeRequest(fullUrl, handler, minYear, 300000)
            }, 300000)
        } else {
            Log.d("StreamingAPI", "Max requests per day reached")
        }
    }

    private fun makeRequest(
        url: String,
        handler: Handler,
        minYear: Int,
        delayMillis: Long = 300000
    ) {

        requestCount++

        val stringRequest = object : StringRequest(

            Method.GET, url,

            Response.Listener { response ->
                Log.d("VolleyResponse", "response: $response")
                val maxLength = 4000
                for (i in 0..response.length step maxLength) {
                    val end =
                        if (i + maxLength < response.length) i + maxLength else response.length
                    Log.d(
                        "VolleyResponse",
                        "raw JSON response (${i / maxLength}): ${response.substring(i, end)}"
                    )
                }


                try {
                    // Parse the response into Media object
                    val media = parseMediaJson(response)
                    // Create MediaEntity from Media object
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

                    // Convert the response string to a JsonObject
                    val itemObject = JsonParser.parseString(response).asJsonObject
                    // Access the "items" array from the jsonObject
                    val itemsArray = itemObject.getAsJsonArray("items")


                    itemsArray?.forEach { item ->
                        // Access the "streamingInfo" object from each item
                        val streamingInfoJsonObject =
                            item.asJsonObject.getAsJsonObject("streamingInfo")

                        // Parse streaming info
                        val usServices = parseStreamingInfo(streamingInfoJsonObject)

                        val dbHelper = MediaDBHelper(this)

                        dbHelper.insertRow(mediaEntity, usServices.us)


                    }


                    // Convert the response string to a JsonObject
                    val jsonObject = JsonParser.parseString(response).asJsonObject
                    val hasMore = jsonObject.get("hasMore").asBoolean

                    if (hasMore) {
                        val nextCursor = jsonObject.get("nextCursor").asString

                        val storedCursors = getStoredCursors()
                        if (requestCount == 1 && storedCursors.isNotEmpty()) {
                            val storedCursor = getMostRecentCursor()
                            handler.postDelayed(
                                {
                                    makeRequest(
                                        "$url&cursor=$storedCursor",
                                        Handler(Looper.getMainLooper()),
                                        minYear, delayMillis + 300000
                                    )
                                },
                                delayMillis
                            )
                        } else {
                            // If it's not the first request of the day or no cursor stored
                            handler.postDelayed(
                                {
                                    makeRequest(
                                        "$url&cursor=$nextCursor",
                                        Handler(Looper.getMainLooper()),
                                        minYear, delayMillis + 300000
                                    )
                                },
                                delayMillis
                            )
                        }
                    }

                    handleResponse(response, url, minYear)


                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error -> Log.e("VolleyError", error.toString()) }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["X-RapidAPI-Host"] = "streaming-availability.p.rapidapi.com"
                params["X-RapidAPI-Key"] = "0614af3779msh2d614123e805945p1d3410jsn4b82927cc9e1"
                params["accept"] = "application/json, text/plain, */*"
                params["cache-control"] = "no-store"
                params["usequerystring"] = "true"
                params["accept-language"] = "en-US,en;q=0.9"
                params["keep-alive"] = "timeout=60"

                return params
            }
        }

        // Disable caching
        stringRequest.setShouldCache(false)

        // Add the request to the request queue
        requestQueue.add(stringRequest)
    }

    // handle the case when no responses can be found and store nextCursor for use
    private fun handleResponse(response: String, url: String, minYear: Int) {
        // Parse the response and check if hasMore is false
        val jsonObject = JsonParser.parseString(response).asJsonObject
        val hasMore = jsonObject.get("hasMore").asBoolean
        if (!hasMore) {
            // If no more responses available, adjust minYear to go back 5 years
            val newMinYear = minYear - 5
            scheduleRequest(url, newMinYear)

        } else {
            // If hasMore is true, continue with pagination or other processing
            val nextCursor = jsonObject.get("nextCursor").asString
            updateStoredCursors(nextCursor)

        }

    }


    private fun updateStoredCursors(newCursor: String) {
        val sharedPreferences = getSharedPreferences("StreamingAPI", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Get the current list of stored cursors
        val storedCursors =
            sharedPreferences.getStringSet("storedCursors", HashSet<String>()) ?: HashSet()

        // Create a new set to hold the updated cursors
        val updatedCursors = HashSet(storedCursors)

        // Add the new cursor to the updated set
        updatedCursors.add(newCursor)

        // Check if the number of stored cursors exceeds the limit
        if (updatedCursors.size > 50) {
            // Remove the oldest cursors until the limit is met
            val iterator = updatedCursors.iterator()
            repeat(updatedCursors.size - 50) {
                iterator.next()
                iterator.remove()
            }
        }

        // Save the updated list of stored cursors
        editor.putStringSet("storedCursors", updatedCursors)
        editor.apply()
    }


    private fun getMostRecentCursor(): String? {
        val sharedPreferences = getSharedPreferences("StreamingAPI", Context.MODE_PRIVATE)

        // Get the set of stored cursors
        val storedCursors = sharedPreferences.getStringSet("storedCursors", HashSet()) ?: HashSet()

        // Return the last (most recent) cursor from the set
        return storedCursors.lastOrNull()
    }

    private fun getStoredCursors(): Set<String> {
        val sharedPreferences = getSharedPreferences("StreamingAPI", Context.MODE_PRIVATE)
        return sharedPreferences.getStringSet("storedCursors", HashSet()) ?: HashSet()
    }


}