package com.opsc7311poe.gourmetguru_opscpoe

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AddRecipeToCollectionFragment : Fragment() {

    private lateinit var resultsLayout: LinearLayout
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private lateinit var collectionID: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_recipe_to_collection, container, false)
        collectionID = arguments?.getString("collectionID")!!



        resultsLayout = view.findViewById(R.id.layoutResults)

        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val searchQuery = view.findViewById<EditText>(R.id.txtName).text.toString()
            if (searchQuery.isNotEmpty()) {
                performSearch(searchQuery)
            } else {
                Toast.makeText(requireContext(), "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        view.findViewById<Button>(R.id.btnFilter).setOnClickListener {
            showFilterDialog()
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("AddRecipeToCollectionFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun performSearch(query: String) {
        resultsLayout.removeAllViews()
        val lowerCaseQuery = query.lowercase() // Convert the query to lowercase

        // First, search through the cuisines
        FirebaseDatabase.getInstance().reference.child("cuisines")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var foundRecipe = false

                    // Check cuisines
                    for (cuisineSnapshot in dataSnapshot.children) {
                        val recipeSnapshot = cuisineSnapshot.child("recipes")
                        for (recipe in recipeSnapshot.children) {
                            val recipeName = recipe.key?.lowercase() // Convert recipe name to lowercase
                            if (recipeName?.contains(lowerCaseQuery) == true) {
                                foundRecipe = true
                                createRecipeTextView(recipe.key ?: "", recipe.key ?: "")
                            }
                        }
                    }

                    // Now, search through personal recipes if no cuisine recipes found
                    if (!foundRecipe) {
                        searchPersonalRecipes(lowerCaseQuery)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun createRecipeTextView(recipeName: String, recipeId: String) {
        val capitalizedRecipeName = capitalizeWords(recipeName) // Capitalize each word
        val textView = TextView(requireContext()).apply {
            text = capitalizedRecipeName // Use the capitalized recipe name
            textSize = 18f
            setTextColor(resources.getColor(android.R.color.white))
            setPadding(16, 16, 16, 16)
            setOnClickListener { saveRecipeToCollection(capitalizedRecipeName) } // Pass the capitalized recipe name to saveMealPlan
        }
        resultsLayout.addView(textView)
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
                        val recipeName = recipeSnapshot.child("name").getValue(String::class.java)?.lowercase()?.trim() // Trim and convert to lowercase
                        Log.d("RecipeSearch", "Recipe found: $recipeName") // Log retrieved recipe names

                        // Check if the recipe name contains the search query
                        if (recipeName?.contains(lowerCaseQuery) == true) {
                            foundRecipe = true
                            // Ensure key is not null before passing it
                            if (recipeSnapshot.key != null) {
                                createRecipeTextView(recipeName, recipeSnapshot.key!!)
                            } else {
                                Log.d("Debug", "Recipe snapshot key is null")
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

    private fun saveRecipeToCollection(recipeName: String) {
        Log.d("AddRecipeToCollection", "Collection ID received: $collectionID")
        val collectionPath = "Users/$userId/Collections/$collectionID"
        val capitalizedRecipeName = capitalizeWords(recipeName) // Capitalize each word before saving

        // Fetch recipe list
        FirebaseDatabase.getInstance().reference.child(collectionPath).child("Recipes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val fetchedRecipeList: MutableList<String> = mutableListOf()

                    // Initialize the list from existing data
                    if (dataSnapshot.exists()) {
                        for (snapshot in dataSnapshot.children) {
                            val recipe = snapshot.getValue(String::class.java)
                            if (recipe != null) {
                                fetchedRecipeList.add(recipe)
                            }
                        }
                    }

                    //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 26 October 2024].
                    // Check if the recipe is already in the list before adding
                    if (!fetchedRecipeList.contains(capitalizedRecipeName)) {
                        fetchedRecipeList.add(capitalizedRecipeName)
                    } else {
                        Toast.makeText(requireContext(), "Recipe already in collection", Toast.LENGTH_SHORT).show()
                    }

                    // Saving updated list of recipes
                    FirebaseDatabase.getInstance().reference.child(collectionPath).child("Recipes").setValue(fetchedRecipeList)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Saved recipe to collection", Toast.LENGTH_SHORT).show()
                            replaceFragment(ViewCollectionsFragment())
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(requireContext(), "Failed to save: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load recipe list: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
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
                            // Ensure key is not null before passing it
                            if (recipeSnapshot.key != null) {
                                createRecipeTextView(recipeSnapshot.key!!, recipeSnapshot.key!!)
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