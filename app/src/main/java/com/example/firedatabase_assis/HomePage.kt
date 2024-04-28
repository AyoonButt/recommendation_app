package com.example.firedatabase_assis


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Cache
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.example.firedatabase_assis.databinding.ActivityHomepageBinding
import kotlinx.coroutines.*
import java.util.PriorityQueue
import kotlin.coroutines.CoroutineContext


class HomePage : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityHomepageBinding
    private val imageContainers: PriorityQueue<ImageContainerWrapper> =
        PriorityQueue() // Stores wrappers with priority and container data
    private lateinit var requestQueue: RequestQueue
    private val containerTagsMap: MutableMap<String, ContainerTags> = HashMap()

    // Coroutine Job to handle the loop
    private var loopJob: Job? = null

    // Coroutine context
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + SupervisorJob()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomepageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestQueue = Volley.newRequestQueue(this)
        startLoop()
        loadContainersFromQueue()


// move to a separate view
        binding.bottomNavBar.setOnItemReselectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.bottom_menu_home -> {
                    /*Already on home so no activity needed*/
                }

                R.id.bottom_menu_explore -> {
                    /*val intent = Intent(this, ExploreActivity::class.java)
                    startActivity(intent)*/
                }

                R.id.bottom_menu_communities -> {
                    /*val intent = Intent(this, CommunitiesActivity::class.java)
                    startActivity(intent)*/
                }
                /*R.id.bottom_menu_profile-> {
                    val intent = Intent(this, UserActivity::class.java)
                    startActivity(intent)
                }*/
                R.id.bottom_menu_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun startLoop() {
        loopJob = launch {
            repeat(5) {
                loadImageWithDelay()
                delay(15L)
            }
        }
    }

    private fun updateAndAddToQueue(wrapper: ImageContainerWrapper) {
        wrapper.updatePriority() // Update the priority based on conditions
        imageContainers.add(wrapper) // Add to the priority queue
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
            val request = object : StringRequest(
                Method.GET,
                "$url$externalId?api_key=$key&external_source=$source",

                Response.Listener { response ->
                    Log.d("RequestURL", "$url$externalId?external_source=$source")

                    val poster = parseResponse(response)
                    val baseURL = "https://image.tmdb.org/t/p/w185$poster"

                    // Inflate the image container layout
                    val imageContainer =
                        layoutInflater.inflate(R.layout.item_image_container, null) as LinearLayout
                    val imageView = imageContainer.findViewById<ImageView>(R.id.imageView)
                    val btnLike = imageContainer.findViewById<ToggleButton>(R.id.btnLike)
                    val btnDislike = imageContainer.findViewById<ToggleButton>(R.id.btnDislike)
                    val btnSaved = imageContainer.findViewById<ToggleButton>(R.id.btnSaved)
                    val captionTextView =
                        imageContainer.findViewById<TextView>(R.id.captionTextView)


                    captionTextView.text = caption

                    val serviceTag = extractStreamingService(caption)


                    val genreTag = extractGenres(caption) //List of Strings


                    containerTagsMap[containerLayout.id.toString()] =
                        ContainerTags(serviceTag, genreTag)

                    try {
                        loadImageFromUrl(baseURL, imageView)
                    } catch (e: GlideException) {
                        e.logRootCauses("GlideException")
                    }

                    // Handle like button click
                    btnLike.setOnCheckedChangeListener { buttonView, isChecked ->
                        val containerId = containerLayout.id.toString()
                        val containerTags =
                            containerTagsMap[containerId] ?: ContainerTags(null, null)

                        containerTags.likeState = isChecked // Update liked state

                        // Update the containerTagsMap
                        containerTagsMap[containerId] = containerTags


                    }

                    // Handle dislike button click
                    btnDislike.setOnCheckedChangeListener { buttonView, isChecked ->
                        val containerId = containerLayout.id.toString()
                        val containerTags =
                            containerTagsMap[containerId] ?: ContainerTags(null, null)

                        containerTags.dislikeState = isChecked // Update liked state

                        // Update the containerTagsMap
                        containerTagsMap[containerId] = containerTags

                    }

                    btnSaved.setOnCheckedChangeListener { buttonView, isChecked ->
                        val containerId = containerLayout.id.toString()
                        val containerTags =
                            containerTagsMap[containerId] ?: ContainerTags(null, null)

                        containerTags.savedState = isChecked // Update liked state

                        // Update the containerTagsMap
                        containerTagsMap[containerId] = containerTags


                    }


                    // Add the image container to the layout
                    containerLayout.addView(imageContainer)

                    // Create an ImageContainerWrapper instance
                    val wrapper = ImageContainerWrapper(

                        priority = 0, // Set initial priority (can be updated later)
                        imageView = imageView,
                        btnLike = btnLike,
                        btnDislike = btnDislike,
                        btnSaved = btnSaved,
                        captionTextView = captionTextView,
                        containerLayoutId = containerLayout.id.toString(),
                        containerTagsMap = containerTagsMap
                    )

                    // Update priority and add to the priority queue
                    wrapper.updatePriority() // Update priority based on tags
                    updateAndAddToQueue(wrapper)


                },

                Response.ErrorListener { error ->
                    // Handle error
                }) {
                @Throws(AuthFailureError::class)
                override fun getHeaders(): Map<String, String> {
                    val headers = HashMap<String, String>()
                    headers["accept"] = "application/json"
                    headers["Authorization"] = "Bearer $token"
                    return headers
                }

                override fun getCacheEntry(): Cache.Entry? {
                    return null
                }
            }

            // Add the request to the request queue
            requestQueue.add(request)
            Log.d("Volley", "Added request to queue")

            val last = dbHelper.getLastImdbIdFromDatabase(this@HomePage)
            if (last != null) {
                dbHelper.deleteRowByExternalId(this@HomePage, last)
            }
        }
    }

    private fun loadImageFromUrl(url: String, imageView: ImageView) {
        Glide.with(imageView)
            .load(url)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop()
            .placeholder(R.drawable.lotr) // Placeholder image while loading
            .into(imageView)

    }

    private fun loadContainersFromQueue() {
        // Start a coroutine
        launch {
            while (imageContainers.isNotEmpty()) {
                val wrapper =
                    imageContainers.poll() // Retrieve and remove the container with the highest priority
                // Fetch all attributes from the wrapper
                val imageView = wrapper.imageView
                val btnLike = wrapper.btnLike
                val btnDislike = wrapper.btnDislike
                val btnSaved = wrapper.btnSaved
                val captionTextView = wrapper.captionTextView
                val containerLayoutId = wrapper.containerLayoutId
                val containerTagsMap = wrapper.containerTagsMap
                val serviceTag = containerTagsMap[containerLayoutId]?.service
                val genreTags = containerTagsMap[containerLayoutId]?.genre
                val likeState = containerTagsMap[containerLayoutId]?.likeState
                val dislikeState = containerTagsMap[containerLayoutId]?.dislikeState
                val savedState = containerTagsMap[containerLayoutId]?.savedState

                // Now you can use these attributes to load or manipulate your UI elements
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        loopJob?.cancel() // Cancel the loop job when the activity is destroyed
    }
}
