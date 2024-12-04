package com.opsc7311poe.gourmetguru_opscpoe

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SelectMealFragment : Fragment() {

    private var mealType: String? = null
    private var day: String? = null
    private lateinit var userId: String
    private lateinit var resultsLayout: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.fragment_select_meal, container, false)

        mealType = arguments?.getString("mealType")
        day = arguments?.getString("day")
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        resultsLayout = rootView.findViewById(R.id.layoutResults)

        rootView.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val searchQuery = rootView.findViewById<EditText>(R.id.txtName).text.toString()
            if (searchQuery.isNotEmpty()) {
                searchRecipes(searchQuery)
            } else {
                Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        rootView.findViewById<Button>(R.id.btnFilter).setOnClickListener {
            showFilterDialog()
        }

        return rootView
    }

    private fun searchRecipes(query: String) {
        resultsLayout.removeAllViews() // Clear previous results
        val lowerCaseQuery = query.lowercase().trim() // Convert the query to lowercase and trim

        // Access the public recipes under cuisines
        FirebaseDatabase.getInstance().reference.child("cuisines")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var foundRecipe = false

                    // Iterate through the cuisines and recipes
                    for (cuisineSnapshot in dataSnapshot.children) {
                        for (recipeSnapshot in cuisineSnapshot.child("recipes").children) {
                            val recipeName = recipeSnapshot.key?.lowercase()?.trim() // Get the recipe name
                            Log.d("RecipeSearch", "Recipe found: $recipeName") // Log the found recipe

                            // Check if the recipe name matches the search query
                            if (recipeName?.contains(lowerCaseQuery) == true) {
                                foundRecipe = true

                                // Extract ingredients
                                val ingredientsList = recipeSnapshot.child("ingredients").children.mapNotNull {
                                    it.getValue(String::class.java)
                                }

                                // Ensure the recipe name (key) is not null before passing it
                                if (recipeName != null) {
                                    createRecipeTextView(recipeName, ingredientsList) // Pass recipe name and ingredients
                                } else {
                                    Log.d("Debug", "Recipe snapshot key is null")
                                }
                            }
                        }
                    }

                    if (!foundRecipe) {
                        searchPersonalRecipes(query)
                    }

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun searchPersonalRecipes(query: String) {
        resultsLayout.removeAllViews() // Clear previous results
        val lowerCaseQuery = query.lowercase().trim() // Convert the query to lowercase and trim

        // Access user's personal recipes
        FirebaseDatabase.getInstance().reference.child("Users").child(userId).child("Recipes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var foundRecipe = false

                    // Iterate through the user's recipes
                    for (recipeSnapshot in dataSnapshot.children) {
                        // Retrieve the recipe name directly from the snapshot
                        val recipeName = recipeSnapshot.child("name").getValue(String::class.java)?.lowercase()?.trim()
                        Log.d("RecipeSearch", "Recipe found: $recipeName") // Log retrieved recipe names

                        // Check if the recipe name contains the search query
                        if (recipeName?.contains(lowerCaseQuery) == true) {
                            foundRecipe = true

                            // Extract ingredients from the recipe
                            val ingredientsList = recipeSnapshot.child("ingredients").children.mapNotNull { ingredientSnapshot ->
                                val ingredientName = ingredientSnapshot.child("name").getValue(String::class.java)
                                val ingredientAmount = ingredientSnapshot.child("amount").getValue(String::class.java)
                                if (ingredientName != null && ingredientAmount != null) "$ingredientAmount $ingredientName" else null
                            }

                            // Create the recipe text view if a valid recipe name is found
                            if (recipeName.isNotEmpty()) {
                                createRecipeTextView(recipeName, ingredientsList)
                            } else {
                                Log.d("Debug", "Recipe snapshot name is empty or null")
                            }
                        }
                    }

                    if (!foundRecipe) {
                        createNoResultsTextView("No personal recipes found for: $query")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun createRecipeTextView(recipeName: String, ingredients: List<String>) {
        val capitalizedRecipeName = capitalizeWords(recipeName) // Capitalize each word
        val textView = TextView(requireContext()).apply {
            text = capitalizedRecipeName
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(16, 16, 16, 16)
            setOnClickListener { saveMealPlan(capitalizedRecipeName, ingredients) } // Save ingredients
        }
        resultsLayout.addView(textView)
    }



    private fun saveMealPlan(recipeName: String, ingredients: List<String>) {
        val mealPath = "Users/$userId/MealPlan/$day/${mealType ?: ""}"
        val capitalizedRecipeName = capitalizeWords(recipeName) // Capitalize each word before saving

        // Create a map to store the recipe ingredients
        val recipeData = hashMapOf<String, Any>(
            "ingredients" to ingredients
        )

        // Before saving, remove any existing meal for the selected day and meal type
        val mealReference = FirebaseDatabase.getInstance().reference.child(mealPath)

        mealReference.setValue(null) // This will clear any previous recipe for the same day and meal type

        // Save the recipe name and ingredients for the selected day and meal type
        mealReference.child(capitalizedRecipeName)
            .setValue(recipeData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "$capitalizedRecipeName saved for $day $mealType", Toast.LENGTH_SHORT).show()
                navigateBackToMealPlanDays()
            }
            .addOnFailureListener { error ->
                Toast.makeText(requireContext(), "Failed to save: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun navigateBackToMealPlanDays() {
        val fragment = MealPlanDaysFragment().apply {
            arguments = Bundle().apply {
                putString("day", day) // Pass the selected day back
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun showFilterDialog() {
        val filterOptions = arrayOf("Italian", "Lebanese", "Mexican", "Portuguese", "French", "Japanese", "South African", "Indian")
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Choose a cuisine")
            setItems(filterOptions) { _, which ->
                filterRecipesByCuisine(filterOptions[which])
            }
            show()
        }
    }

    private fun createNoResultsTextView(message: String) {
        val noResultsTextView = TextView(requireContext()).apply {
            text = message
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.darker_gray)) // You can customize this color
            setPadding(25, 16, 16, 16)
        }
        resultsLayout.addView(noResultsTextView)
    }

    private fun filterRecipesByCuisine(cuisine: String) {
        resultsLayout.removeAllViews()
        FirebaseDatabase.getInstance().reference
            .child("cuisines").child(cuisine).child("recipes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (recipeSnapshot in dataSnapshot.children) {
                            // Ensure the recipe key is not null
                            val recipeName = recipeSnapshot.key
                            if (recipeName != null) {
                                // Extract ingredients from the recipe
                                val ingredientsList = recipeSnapshot.child("ingredients").children.mapNotNull {
                                    it.getValue(String::class.java)
                                }

                                // Create the recipe text view with the recipe name and ingredients
                                createRecipeTextView(recipeName, ingredientsList)
                            } else {
                                Log.d("Debug", "Recipe snapshot key is null")
                            }
                        }
                    } else {
                        createNoResultsTextView("No recipes found for: $cuisine")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    createNoResultsTextView("Error fetching data: ${databaseError.message}")
                }
            })
    }

    private fun capitalizeWords(input: String): String {
        return input.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
