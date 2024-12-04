package com.opsc7311poe.gourmetguru_opscpoe

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.opsc7311poe.gourmetguru_opscpoe.databinding.FragmentViewSelectedBinding
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener

class ViewSelectedFragment : Fragment() {

    private lateinit var binding: FragmentViewSelectedBinding
    private var recipeName: String? = null
    private var cuisine: String? = null
    private lateinit var btnBack: ImageView
    private lateinit var btnSaveToCollection: ImageView // Add a reference for the save button
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""



    private lateinit var recipeID: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment using view binding
        binding = FragmentViewSelectedBinding.inflate(inflater, container, false)

        // Get the passed recipe data
        cuisine = arguments?.getString("cuisine")
        recipeName = arguments?.getString("recipeName")

        // Fetch and display recipe details only if cuisine and recipeName are not null
        if (cuisine != null && recipeName != null) {
            loadRecipe(cuisine!!, recipeName!!)
        } else {
            Log.e("ViewSelectedFragment", "Cuisine or Recipe Name is null.")
        }

        btnBack = binding.btnBack // Use binding to get btnBack directly
        btnBack.setOnClickListener {
            // Go back to the previous fragment instead of replacing
            parentFragmentManager.popBackStack()
        }

        recipeID = arguments?.getString("recipeID") ?: "" // recipeName will be treated as recipeID

// Ensure recipeID is not empty
        if (recipeID.isEmpty()) {
            Toast.makeText(requireContext(), "Recipe ID not found", Toast.LENGTH_SHORT).show()
            return binding.root
        }

        // Initialize the save button and set its click listener
        btnSaveToCollection = binding.btnSaveToCollection // Ensure you have a corresponding view in your XML layout
        btnSaveToCollection.setOnClickListener {
            // Fetch all collections and wait for the callback
            getAllCollectionsNames { collectionOptions ->
                if (collectionOptions.isNotEmpty()) {
                    // Show an alert dialog with the collection names
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Choose a collection")
                    builder.setItems(collectionOptions.toTypedArray()) { dialog, which ->
                        val selectedCollection = collectionOptions[which]

                        // Save the recipe to the selected collection
                        saveRecipeToCollection(recipeID, selectedCollection)
                    }
                    builder.show()
                } else {
                    Toast.makeText(requireContext(), "No collections found", Toast.LENGTH_SHORT).show()
                }
            }

            // Navigate to the collection view fragment
            replaceFragment(ViewCollectionsFragment())
        }

        return binding.root
    }

    private fun getAllCollectionsNames(callback: (List<String>) -> Unit) {
        val userCollectionsPath = "Users/$userId/Collections"
        val collectionNames: MutableList<String> = mutableListOf()

        // Fetch all collections under the user
        FirebaseDatabase.getInstance().reference.child(userCollectionsPath)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Loop through each collection and extract the 'name' attribute
                    for (collectionSnapshot in dataSnapshot.children) {
                        val collectionName = collectionSnapshot.child("name").getValue(String::class.java)
                        if (collectionName != null) {
                            collectionNames.add(collectionName)
                        }
                    }
                    callback(collectionNames)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("TAG", "Error fetching collections: ${databaseError.message}")
                    Toast.makeText(requireContext(), "Failed to load collections: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveRecipeToCollection(recipeName: String, selectedCollection: String) {
        Log.d("AddRecipeToCollection", "Collection name received: $selectedCollection")
        val collectionPath = "Users/$userId/Collections"
        val capitalizedRecipeName = capitalizeWords(recipeName) // Capitalize each word before saving

        // Fetch recipe list
        // Fetch all collections under the user
        FirebaseDatabase.getInstance().reference.child(collectionPath)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var collectionFound = false

                    // Loop through all collections to find the one with the matching name
                    for (collectionSnapshot in dataSnapshot.children) {
                        val collectionName = collectionSnapshot.child("name").getValue(String::class.java)

                        if (collectionName == selectedCollection) {
                            // Collection found
                            collectionFound = true

                            // Fetch the recipe list within the found collection
                            val recipesRef = collectionSnapshot.ref.child("Recipes")
                            recipesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(recipesSnapshot: DataSnapshot) {
                                    val fetchedRecipeList: MutableList<String> = mutableListOf()

                                    // Initialize the list from existing data
                                    if (recipesSnapshot.exists()) {
                                        for (snapshot in recipesSnapshot.children) {
                                            val recipe = snapshot.getValue(String::class.java)
                                            if (recipe != null) {
                                                fetchedRecipeList.add(recipe)
                                            }
                                        }
                                    }

                                    // Check if the recipe is already in the list before adding
                                    if (!fetchedRecipeList.contains(capitalizedRecipeName)) {
                                        fetchedRecipeList.add(capitalizedRecipeName)

                                        // Save the updated list of recipes
                                        recipesRef.setValue(fetchedRecipeList)
                                            .addOnSuccessListener {
                                                Toast.makeText(requireContext(), "Saved recipe to collection", Toast.LENGTH_SHORT).show()
                                                replaceFragment(ViewCollectionsFragment())
                                            }
                                            .addOnFailureListener { error ->
                                                Toast.makeText(requireContext(), "Failed to save: ${error.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(requireContext(), "Recipe already in collection", Toast.LENGTH_SHORT).show()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    Toast.makeText(requireContext(), "Failed to load recipe list: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                                }
                            })
                            break // Exit loop after finding the collection
                        }
                    }

                    if (!collectionFound) {
                        Toast.makeText(requireContext(), "Collection not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to search collections: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun capitalizeWords(input: String): String {
        return input.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    private fun loadRecipe(cuisine: String, recipeName: String) {
        val database = FirebaseDatabase.getInstance().reference

        database.child("cuisines").child(cuisine).child("recipes").child(recipeName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val duration = dataSnapshot.child("Duration").getValue(String::class.java)
                        val imageUrl = dataSnapshot.child("image").getValue(String::class.java)
                        val ingredients = dataSnapshot.child("ingredients").getValue(object : GenericTypeIndicator<List<String>>() {})
                        val steps = dataSnapshot.child("steps").getValue(object : GenericTypeIndicator<List<String>>() {})

                        // Use the retrieved values to display in your UI
                        binding.txtSelRecpName.text = recipeName
                        binding.txtDuration.text = duration

                        // Load image using Glide if needed
                        if (imageUrl != null) {
                            Glide.with(this@ViewSelectedFragment)
                                .load(imageUrl)
                                .into(binding.imgSelRecipeback)
                        }

                        // Display ingredients
                        if (ingredients != null) {
                            binding.tvIngredientsList.text = ingredients.joinToString("\n")
                        }

                        // Display steps
                        if (steps != null) {
                            binding.tvSteps.text = steps.joinToString("\n")
                        }
                    } else {
                        // Handle the case where the recipe doesn't exist
                        Log.e("ViewSelectedFragment", "Recipe not found.")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("ViewSelectedFragment", "Database error: ${databaseError.message}")
                }
            })
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
