package com.example.firedatabase_assis

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class StreamingAPI : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_window)

        requestQueue = Volley.newRequestQueue(this)
        scheduleRequest()
    }

    private fun scheduleRequest() {
        val url = "https://streaming-availability.p.rapidapi.com/search/filters?services=netflix&country=us&output_language=en&year_max=2024&order_by=original_title&genres_relation=and&year_min=1995&show_type=all"

        val uri = Uri.parse(url).buildUpon().build().toString()

        val stringRequest = object : StringRequest(
            Request.Method.GET, uri,
            Response.Listener { response ->
                Log.d("VolleyResponse", "response: $response")

                try {
                    val media = parseMediaJson(response)
                    val mediaEntity = Media(
                        type = media.type,
                        title = media.title,
                        overview = media.overview,
                        streamingInfo = media.streamingInfo,
                        streamingService = media.streamingService,
                        year = media.year,
                        imdbId = media.imdbId,
                        tmdbId = media.tmdbId,
                        genres = media.genres,
                        directors = media.directors,
                        creators = media.creators,
                        status = media.status,
                        seasonCount = media.seasonCount,
                        episodeCount = media.episodeCount,
                        seasons = media.seasons
                    )

                    val dbHelper = MediaDBHelper(this)
                    dbHelper.insertMedia(mediaEntity)
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
                return params


            }
        }
        requestQueue.add(stringRequest)
    }
}
