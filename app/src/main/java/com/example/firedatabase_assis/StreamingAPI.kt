package com.example.firedatabase_assis

import Media
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.JsonParser
import org.json.JSONException
import parseMediaJson
import parseStreamingInfo


class StreamingAPI : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private val handler = Handler(Looper.getMainLooper())
    private var requestCount = 0
    private val maxRequestsPerDay = 90

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_window)

        requestQueue = Volley.newRequestQueue(this)


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



        scheduleRequest(netflix, 1995)
        scheduleRequest(prime, 1995)
        scheduleRequest(hboMax, 1995)
        scheduleRequest(hulu, 1995)
        scheduleRequest(apple, 1995)
        scheduleRequest(peacock, 1995)


    }

    private fun scheduleRequest(url: String, minYear: Int) {
        if (requestCount < maxRequestsPerDay) {
            requestCount++
            handler.postDelayed({
                makeRequest(
                    "$url&timestamp=${System.currentTimeMillis()}",
                    handler,
                    minYear,
                    300000
                )
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
                        // Delay before making the next request to avoid rate limiting
                        handler.postDelayed(
                            {
                                makeRequest(
                                    "$url&cursor=$nextCursor",
                                    Handler(Looper.getMainLooper()),
                                    minYear, delayMillis + 300000
                                )
                            },
                            delayMillis
                        ) // Delay of 5 minutes (1000 milliseconds)
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

    private fun handleResponse(response: String, url: String, minYear: Int) {
        // Parse the response and check if hasMore is false
        val jsonObject = JsonParser.parseString(response).asJsonObject
        val hasMore = jsonObject.get("hasMore").asBoolean
        if (!hasMore) {
            // If no more responses available, adjust minYear to go back 5 years
            val newMinYear = minYear - 5
            // Reschedule the request with the adjusted minYear
            scheduleRequest(url, newMinYear)
        } else {
            // If hasMore is true, continue with pagination or other processing
        }
    }
}

