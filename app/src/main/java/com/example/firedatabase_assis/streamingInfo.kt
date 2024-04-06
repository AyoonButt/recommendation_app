package com.example.firedatabase_assis

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley



class streamingInfo : AppCompatActivity() {

    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        queue = Volley.newRequestQueue(this)

        // Schedule multiple requests
        scheduleRequest("https://streaming-availability.p.rapidapi.com/search/filters?services=netflix&country=us&output_language=en&year_max=2024&order_by=original_title&genres_relation=and&year_min=1995&show_type=all")
        scheduleRequest("https://streaming-availability.p.rapidapi.com/search/filters?services=prime.subscription&country=us&output_language=en&year_max=2024&order_by=original_title&genres_relation=and&year_min=1995&show_type=all")

        // Add more requests as needed
    }

    private fun scheduleRequest(url: String) {
        val stringRequest = object : StringRequest(Request.Method.GET, url,
            Response.Listener { response ->
                // Handle response
                Log.d("Response", response)
            },
            Response.ErrorListener { error ->
                // Handle error
                Log.e("Error", "Error occurred", error)
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                // Add your RapidAPI authorization key
                headers["X-RapidAPI-Key"] = "0614af3779msh2d614123e805945p1d3410jsn4b82927cc9e1"
                headers["X-RapidAPI-Host"] = "streaming-availability.p.rapidapi.com"
                return headers
            }
        }


        // Add the request to the queue
        queue.add(stringRequest)
    }
}
