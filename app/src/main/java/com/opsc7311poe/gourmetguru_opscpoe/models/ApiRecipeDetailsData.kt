package com.opsc7311poe.gourmetguru_opscpoe.models

data class ApiRecipeDetailsData(
    val ingredients: List<String>,
    val steps: List<String>,
    val Duration: String,
    val image: String
)
