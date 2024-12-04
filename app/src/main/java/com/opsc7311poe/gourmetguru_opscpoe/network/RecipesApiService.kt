package com.opsc7311poe.gourmetguru_opscpoe.network

import com.opsc7311poe.gourmetguru_opscpoe.models.ApiRecipeData
import com.opsc7311poe.gourmetguru_opscpoe.models.ApiRecipeDetailsData
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipesApiService {
    @GET("recipes/cuisines")
    suspend fun getCuisines(): List<String>

    @GET("cuisines/{cuisineName}/recipes")
    //suspend fun getRecipesByCuisine(@Path("cuisineName") cuisineName: String): List<ApiRecipeData>
    suspend fun getRecipesByCuisine(@Path("cuisineName") cuisineName: String): List<String>

    @GET("cuisines/{cuisineName}/recipes/{recipeName}")
    suspend fun getRecipeDetails(
        @Path("cuisineName") cuisineName: String,
        @Path("recipeName") recipeName: String
    ):  ApiRecipeDetailsData
}
