package com.example.firedatabase_assis


import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.firedatabase_assis.MediaDBHelper
import com.example.firedatabase_assis.databinding.ActivityHomepageBinding
import kotlinx.coroutines.*
import java.util.PriorityQueue
import kotlin.coroutines.CoroutineContext


class HomePage : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityHomepageBinding
    private val imageContainers: PriorityQueue<LinearLayout> =
        PriorityQueue() // PriorityQueue to store image containers
    //private lateinit var requestQueue: RequestQueue

    // Coroutine Job to handle the loop
    private var loopJob: Job? = null

    // Coroutine context
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //requestQueue = Volley.newRequestQueue(this)
        startLoop()
    }

    private fun startLoop() {
        loopJob = launch {
            repeat(10) {
                loadImageWithDelay()
                delay(15L)
            }
        }
    }

    private suspend fun loadImageWithDelay() {
        withContext(Dispatchers.IO) {
            val dbHelper = MediaDBHelper(applicationContext)
            val containerLayout = findViewById<LinearLayout>(R.id.containerLayout)

            val source = "imdb_id"
            val url = "https://api.themoviedb.org/3/find/"
            val key = "d9dd9d3ae838db18d51f88cd50e479e4"
            val token =
                "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJkOWRkOWQzYWU4MzhkYjE4ZDUxZjg4Y2Q1MGU0NzllNCIsInN1YiI6IjY2MjZiM2ZkMjU4ODIzMDE2NDkxODliMSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.wIF16waIO_pGrRKxWr4ye8QFYUFMGP6WBDX5Wg2JOpM"

            val externalId = dbHelper.getLastImdbIdFromDatabase(this@HomePage)
            Log.d("ExternalID", "External ID: $externalId")

            val caption = dbHelper.getCaption(applicationContext)

            // Create the request
//            val request = object : StringRequest(
//                Method.GET,
//                "$url$externalId?api_key=$key&external_source=$source",
//
//                Response.Listener { response ->
//                    Log.d("RequestURL", "$url$externalId?external_source=$source")
//
//                    val poster = parseResponse(response)
//                    val baseURL = "https://image.tmdb.org/t/p/w185$poster"
//
//                    // Inflate the image container layout
//                    val imageContainer =
//                        layoutInflater.inflate(R.layout.item_image_container, null) as LinearLayout
//                    val imageView = imageContainer.findViewById<ImageView>(R.id.imageView)
//                    val btnLike = imageContainer.findViewById<Button>(R.id.btnLike)
//                    val btnDislike = imageContainer.findViewById<Button>(R.id.btnDislike)
//                    val captionTextView =
//                        imageContainer.findViewById<TextView>(R.id.captionTextView)
//
//
//                    captionTextView.text = caption
//
//
//
//
//                    try {
//                        loadImageFromUrl(baseURL, imageView)
//                    } catch (e: GlideException) {
//                        e.logRootCauses("GlideException")
//                    }
//
//                    // Handle like button click
//                    btnLike.setOnClickListener {
//                        // Handle like action
//                    }
//
//                    // Handle dislike button click
//                    btnDislike.setOnClickListener {
//                        // Handle dislike action
//                    }
//
//                    // Add the image container to the layout
//                    containerLayout.addView(imageContainer)
//                },
//                Response.ErrorListener { error ->
//                    // Handle error
//                }) {
//                @Throws(AuthFailureError::class)
//                override fun getHeaders(): Map<String, String> {
//                    val headers = HashMap<String, String>()
//                    headers["accept"] = "application/json"
//                    headers["Authorization"] = "Bearer $token"
//                    return headers
//                }
//
//                override fun getCacheEntry(): Cache.Entry? {
//                    return null
//                }
//            }
//
//            // Add the request to the request queue
//            requestQueue.add(request)
//            Log.d("Volley", "Added request to queue")
//
//            val last = dbHelper.getLastImdbIdFromDatabase(this@HomePage)
//            if (last != null) {
//                dbHelper.deleteRowByExternalId(this@HomePage, last)
//            }
//        }
//    }
//
//    private fun loadImageFromUrl(url: String, imageView: ImageView) {
//        Glide.with(imageView)
//            .load(url)
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .centerCrop()
//            .placeholder(R.drawable.lotr) // Placeholder image while loading
//            .into(imageView)
//
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        loopJob?.cancel() // Cancel the loop job when the activity is destroyed
        }
    }
}