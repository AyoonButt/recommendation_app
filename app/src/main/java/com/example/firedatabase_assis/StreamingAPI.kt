package com.example.firedatabase_assis


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
import kotlinx.coroutines.*
import org.json.JSONException


class StreamingAPI : AppCompatActivity() {


    private lateinit var requestQueue: RequestQueue
    private var requestCount = 0
    private var schedulerCount = 0
    private val maxRequestsPerDay = 90
    private val handler = Handler(Looper.getMainLooper())
    private val minYear = 1995


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_homepage)


        requestQueue = Volley.newRequestQueue(this)

        val netflix = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=netflix&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=$minYear" +
                "&show_type=all"

        val prime = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=prime.subscription&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=$minYear" +
                "&show_type=all"

        val hboMax = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=hbo,hulu.addon.hbo,prime.addon.hbomaxus&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=$minYear" +
                "&show_type=all"

        val hulu = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=hulu.subscription,hulu.addon.hbo&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=$minYear" +
                "&show_type=all"

        val apple = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=apple.addon&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=$minYear" +
                "&show_type=all"

        val peacock = "https://streaming-availability.p.rapidapi.com/search/filters" +
                "?services=peacock.free&country=us&output_language=en" +
                "&order_by=original_title&genres_relation=and&year_min=$minYear" +
                "&show_type=all"


        scheduleRequest(netflix, 1995)
        scheduleRequest(prime, 1995)
        scheduleRequest(hboMax, 1995)
        scheduleRequest(hulu, 1995)
        scheduleRequest(apple, 1995)
        scheduleRequest(peacock, 1995)

        // Reset request counter to zero after each day
        requestCount = 0

    }


    private fun scheduleRequest(url: String, minYear: Int) {

        schedulerCount++

        if (schedulerCount == 6) {
            schedulerCount = 0
        }

        handler.postDelayed({
            makeRequest(
                "$url&timestamp=${System.currentTimeMillis()}",
                handler,
                minYear,
                30000
            )
        }, 30000)

    }

    private fun makeRequest(
        url: String, handler: Handler, minYear: Int, delayMillis: Long = 30000
    ) {

        if (!shouldScheduleRequest()) {
            Log.d("StreamingAPI", "Max requests per day reached")
            return
        }

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

                    parseAndInsertMedia(response, applicationContext)

                    getLastJsonObject(response)


                    val jsonObject = JsonParser.parseString(response).asJsonObject
                    val hasMore = jsonObject.get("hasMore").asBoolean

                    if (hasMore) {
                        val nextCursor = jsonObject?.get("nextCursor")?.asString


                        // Schedule the delayed request using coroutines
                        handler.postDelayed(
                            {
                                makeRequest(
                                    "$url&cursor=$nextCursor",
                                    Handler(Looper.getMainLooper()),
                                    minYear,
                                    delayMillis + 30000
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


    // handle the case when no responses can be found and store nextCursor for use
    private fun handleResponse(response: String, url: String, minYear: Int) {
        // Parse the response and check if hasMore is false
        val jsonObject = JsonParser.parseString(response).asJsonObject
        val hasMore = jsonObject.get("hasMore").asBoolean
        if (!hasMore) {
            // If no more responses available, adjust minYear to go back 5 years
            val newMinYear = minYear - 5
            scheduleRequest(url, newMinYear)

        }

    }


    private fun shouldScheduleRequest(): Boolean {
        return requestCount < maxRequestsPerDay
    }


}


/*  If I were to convert to a service

fun getServiceName(url: String): String {
        val servicesStartIndex = url.indexOf("services=")
        if (servicesStartIndex != -1) {
            val servicesEndIndex = url.indexOf("&", startIndex = servicesStartIndex)
            if (servicesEndIndex != -1) {
                val servicesSubstring = url.substring(servicesStartIndex + 9, servicesEndIndex)
                return when {
                    servicesSubstring.contains("netflix") -> "netflix"
                    servicesSubstring.contains("prime.subscription") -> "prime"
                    servicesSubstring.contains("hbo,hulu.addon.hbo,prime.addon.hbomaxus") -> "hbo"
                    servicesSubstring.contains("hulu.subscription,hulu.addon.hbo") -> "hulu"
                    servicesSubstring.contains("apple.addon") -> "apple"
                    servicesSubstring.contains("peacock.free") -> "peacock"
                    else -> ""
                }
            }
        }
        return ""
    }


if (schedulerCount != 0 && !storedCursors) {
                            val streamingService = getServiceName(url)
                            val mostRecentCursor =
                                dbHelper.getMostRecentNextCursor(streamingService)

                            if (mostRecentCursor != null) {
                                handler.postDelayed(
                                    {
                                        makeRequest(
                                            "$url&cursor=$mostRecentCursor",
                                            Handler(Looper.getMainLooper()),
                                            minYear,
                                            delayMillis + 30000
                                        )
                                    },
                                    delayMillis
                                )
                            }
                        } else {
else {
    // Schedule the next request without considering the cursor
    handler.postDelayed(
        {
            makeRequest(
                url,
                Handler(Looper.getMainLooper()),
                minYear,
                delayMillis + 30000
            )
        },
        delayMillis
    ) // Delay of 5 minutes (1000 milliseconds)
}

 */