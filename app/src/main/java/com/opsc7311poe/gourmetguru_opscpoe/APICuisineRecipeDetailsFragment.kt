package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.opsc7311poe.gourmetguru_opscpoe.adapters.IngredientsAdapter
import com.opsc7311poe.gourmetguru_opscpoe.adapters.StepsAdapter
import com.opsc7311poe.gourmetguru_opscpoe.network.RecipesApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class APICuisineRecipeDetailsFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var recipeImageView: ImageView
    private lateinit var recipeNameTextView: TextView
    private lateinit var recipeDurationTextView: TextView
    private lateinit var ingredientsRecyclerView: RecyclerView
    private lateinit var stepsRecyclerView: RecyclerView
    private lateinit var ingredientsAdapter: IngredientsAdapter
    private lateinit var stepsAdapter: StepsAdapter
    private lateinit var apiService: RecipesApiService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://gourmet-guru-rest-api-hs.onrender.com/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(RecipesApiService::class.java)
        val view = inflater.inflate(R.layout.fragment_a_p_i_cuisine_recipe_details, container, false)

        btnBack = view.findViewById(R.id.btnBack)
        recipeImageView = view.findViewById(R.id.imgCuisineRecipe)
        recipeNameTextView = view.findViewById(R.id.txtSelectedRecipeName)
        recipeDurationTextView = view.findViewById(R.id.txtRecipeDuration)
        ingredientsRecyclerView = view.findViewById(R.id.rvRecipeIngredients)
        stepsRecyclerView = view.findViewById(R.id.rvRecipeSteps)

        btnBack.setOnClickListener {
            //replaceFragment(HomeFragment())
            parentFragmentManager.popBackStack()
        }

        ingredientsRecyclerView.layoutManager = LinearLayoutManager(context)
        stepsRecyclerView.layoutManager = LinearLayoutManager(context)

        /*val args = APICuisineRecipeDetailsFragmentArgs.fromBundle(requireArguments())
        val cuisineName = args.cuisineName
        val recipeName = args.recipeName*/

        // Retrieve the arguments from the Bundle
        val args = arguments
        val cuisineName = args?.getString("cuisineName") ?: "Unknown Cuisine"
        val recipeName = args?.getString("recipeName") ?: "Unknown Recipe"

        fetchRecipeDetails(cuisineName, recipeName)

        return view
    }

    private fun fetchRecipeDetails(cuisineName: String, recipeName: String) {
        lifecycleScope.launch {
            try {
                // Log the request details
                Log.d("RecipeDetails", "Fetching details for: $cuisineName - $recipeName")

                val recipeDetails = apiService.getRecipeDetails(cuisineName, recipeName)

                // Log the received recipe details
                Log.d("RecipeDetails", "Fetched details: $recipeDetails")

                // Check if duration is null or empty and log it
                if (recipeDetails.Duration.isNullOrEmpty()) {
                    Log.d("RecipeDuration", "No duration available for the recipe.")
                    recipeDurationTextView.text = "Duration not available"
                } else {
                    Log.d("RecipeDuration", "Recipe duration: ${recipeDetails.Duration}")
                    recipeDurationTextView.text = recipeDetails.Duration
                }

                Glide.with(this@APICuisineRecipeDetailsFragment)
                    .load(recipeDetails.image)
                    .into(recipeImageView)

                recipeNameTextView.text = recipeName

                recipeDurationTextView.text = recipeDetails.Duration

                ingredientsAdapter = IngredientsAdapter(recipeDetails.ingredients)
                ingredientsRecyclerView.adapter = ingredientsAdapter

                stepsAdapter = StepsAdapter(recipeDetails.steps)
                stepsRecyclerView.adapter = stepsAdapter

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}
