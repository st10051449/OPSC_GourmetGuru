package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.FirebaseDatabase

class ViewSelectedRecipeFragment : Fragment() {
    private lateinit var btnBack: ImageView
    private lateinit var txtDuration: TextView
    private lateinit var txtIngredients: TextView
    private lateinit var txtMethods: TextView
    private lateinit var txtRecipeName: TextView

    private lateinit var fetchedRecipe: RecipeData

    private lateinit var btnsavincol: ImageView
    private var recipeID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_selected_recipe, container, false)

        // Back button functionality
        btnBack = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            replaceFragment(MyRecipesFragment())
        }

        btnsavincol = view.findViewById(R.id.btnsavesel)

        // Retrieve recipeID from arguments
        recipeID = arguments?.getString("recipeID")


       // OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 1 October 2024].
        btnsavincol.setOnClickListener {
            // Check if recipeID is available before proceeding
            recipeID?.let { id ->
                val viewCollectionsFrag = ViewCollectionsFragment()
                // Pass the recipeID as an argument
                val bundle = Bundle()
                bundle.putString("recipeID", id)
                viewCollectionsFrag.arguments = bundle
                replaceFragment(viewCollectionsFrag)
            } ?: run {
                Toast.makeText(requireContext(), "Recipe ID is missing", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetching and populating information
        txtRecipeName = view.findViewById(R.id.txtRecipeName)
        txtDuration = view.findViewById(R.id.txtDuration)
        txtIngredients = view.findViewById(R.id.txtIngredients)
        txtMethods = view.findViewById(R.id.txtMethods)

        val database = FirebaseDatabase.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val recRef = database.getReference("Users").child(userId!!).child("Recipes")

        // Fetching service data from DB
        recipeID?.let { id ->
            // Query the database to find the recipe with the matching ID
            val query = recRef.child(id)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Directly fetch the recipe object without looping
                    fetchedRecipe = dataSnapshot.getValue(RecipeData::class.java)!!

                    if (fetchedRecipe != null) {
                        // Assign fetched recipe info to text views
                        txtRecipeName.text = fetchedRecipe.name
                        txtDuration.text = "${fetchedRecipe.durationHrs} Hours ${fetchedRecipe.durationMins} Minutes"
                        // Displaying all ingredients
                        var allIngreString: String = ""
                        for (ingredient in fetchedRecipe.ingredients!!) {
                            allIngreString += "${ingredient.amount}             ${ingredient.name}\n"
                        }
                        txtIngredients.text = allIngreString
                        // Displaying all steps in method
                        var allStepsString: String = ""
                        for (step in fetchedRecipe.method!!) {
                            allStepsString += "$step\n"
                        }
                        txtMethods.text = allStepsString
                    } else {
                        Toast.makeText(requireContext(), "Recipe not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error reading from the database: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("ServicesFragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
