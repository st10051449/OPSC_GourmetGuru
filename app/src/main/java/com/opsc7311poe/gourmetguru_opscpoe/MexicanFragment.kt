package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.opsc7311poe.gourmetguru_opscpoe.adapters.RecipeAdapter
import com.opsc7311poe.gourmetguru_opscpoe.models.ApiRecipeData
import com.opsc7311poe.gourmetguru_opscpoe.network.ApiClient
import com.opsc7311poe.gourmetguru_opscpoe.network.RecipesApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MexicanFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var recipeAdapter: RecipeAdapter
    private var recipes: List<ApiRecipeData> = listOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_mexican, container, false)
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        recyclerView = view.findViewById(R.id.rvMexicanRecipes)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with an empty list and set click listener
        recipeAdapter = RecipeAdapter(listOf(), "Mexican") { recipe ->
            navigateToAPICuisineRecipeDetailsFragment("Mexican", recipe.name)
        }
        recyclerView.adapter = recipeAdapter

        fetchMexicanRecipes()

        return view
    }


    private fun fetchMexicanRecipes() {
        val apiService = ApiClient.createService(RecipesApiService::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val recipeNames = apiService.getRecipesByCuisine("Mexican")
                val recipesWithImages = mutableListOf<ApiRecipeData>()

                // Iterate through each recipe name and fetch the image URL
                for (recipeName in recipeNames) {
                    val recipeDetails = apiService.getRecipeDetails("Mexican", recipeName)
                    val recipeData = ApiRecipeData(
                        name = recipeName,
                        imageUrl = recipeDetails.image // Get the image URL from the recipe details
                    )
                    recipesWithImages.add(recipeData)
                }

                // Update UI with the fetched data
                withContext(Dispatchers.Main) {
                    recipeAdapter.updateData(recipesWithImages)
                }
            } catch (e: Exception) {
                Log.e("MexicanFragment", "Error fetching recipes", e)
            }
        }
    }

    // Method to navigate to APICuisineRecipeDetailsFragment
    private fun navigateToAPICuisineRecipeDetailsFragment(cuisineName: String, recipeName: String) {
        val recipeDetailsFragment = APICuisineRecipeDetailsFragment().apply {
            arguments = Bundle().apply {
                putString("cuisineName", cuisineName)
                putString("recipeName", recipeName)
            }
        }
        replaceFragment(recipeDetailsFragment)
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }
}