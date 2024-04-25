package com.example.firedatabase_assis

import org.json.JSONException
import org.json.JSONObject

fun parseResponse(jsonString: String): String? {
    var posterPath: String? = null

    try {
        // Parse the JSON string
        val jsonObject = JSONObject(jsonString)

        // Iterate through all fields in the JSON response
        for (key in jsonObject.keys()) {
            // Get the JSON array corresponding to the current field
            val resultsArray = jsonObject.optJSONArray(key)

            // If the field is an array, iterate through its elements
            resultsArray?.let {
                for (i in 0 until resultsArray.length()) {
                    // Get the JSON object at the current index
                    val resultObject = resultsArray.getJSONObject(i)

                    // Extract the poster path if it exists
                    if (resultObject.has("poster_path")) {
                        posterPath = resultObject.getString("poster_path")
                        // If there's only one poster path, return it immediately
                        return posterPath
                    }
                }
            }
        }
    } catch (e: JSONException) {
        e.printStackTrace()
        // Handle parsing error, maybe return null or throw an exception
    }

    return posterPath
}
