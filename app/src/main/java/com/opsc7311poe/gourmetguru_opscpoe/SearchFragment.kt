package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.app.AlertDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.opsc7311poe.gourmetguru_opscpoe.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    lateinit var binding: FragmentSearchBinding
    private lateinit var btnBack: ImageView
    private var mealType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using view binding
        binding = com.opsc7311poe.gourmetguru_opscpoe.databinding.FragmentSearchBinding.inflate(inflater, container, false)

        mealType = arguments?.getString("mealType")

        // Back button action
        btnBack = binding.btnBack
        btnBack.setOnClickListener {
            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
            parentFragmentManager.popBackStack()
        }

        // Search button action
        binding.btnSearch.setOnClickListener {
            val searchQuery = binding.txtName.text.toString()
            performSearch(searchQuery)
        }

        // Filter button action
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        return binding.root
    }

    fun performSearch(query: String) {
        val database = FirebaseDatabase.getInstance().reference

        // Clear previous results
        binding.layoutResults.removeAllViews()

        database.child("cuisines")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val resultList = mutableListOf<String>()

                    for (cuisineSnapshot in dataSnapshot.children) {
                        val cuisine = cuisineSnapshot.key
                        val recipeSnapshot = cuisineSnapshot.child("recipes")

                        for (recipe in recipeSnapshot.children) {
                            val recipeName = recipe.key
                            if (recipeName?.contains(query, ignoreCase = true) == true) {
                                resultList.add("$cuisine: $recipeName")

                                // Create a text box for each recipe
                                val textView = TextView(requireContext()).apply {
                                    text = "$recipeName ($cuisine)"
                                    textSize = 18f
                                    setTextColor(resources.getColor(android.R.color.white))
                                    setPadding(30, 16, 16, 16)
                                    background = resources.getDrawable(R.drawable.textbox_background, null)
                                    setOnClickListener {
                                        // Handle the click to navigate to ViewSelectedFragment and pass data
                                        onRecipeClicked(cuisine ?: "", recipeName ?: "")
                                    }
                                }
                                binding.layoutResults.addView(textView)
                            }
                        }
                    }

                    if (resultList.isEmpty()) {
                        val textView = TextView(requireContext()).apply {
                            text = "No results found for: $query"
                            textSize = 18f
                            setTextColor(resources.getColor(android.R.color.white))
                            setPadding(16, 16, 16, 16)
                        }
                        binding.layoutResults.addView(textView)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    val textView = TextView(requireContext()).apply {
                        text = "Error fetching data: ${databaseError.message}"
                        textSize = 18f
                        setTextColor(resources.getColor(android.R.color.white))
                        setPadding(16, 16, 16, 16)
                    }
                    binding.layoutResults.addView(textView)
                }
            })
    }


    //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 1 October 2024].
    private fun onRecipeClicked(cuisine: String, recipeName: String) {
        // Create a new instance of ViewSelectedFragment
        val viewSelectedFragment = ViewSelectedFragment()

        // Create a bundle to pass the recipe data
        val bundle = Bundle().apply {
            putString("cuisine", cuisine)
            putString("recipeName", recipeName)
            putString("recipeID", recipeName) // Use recipeName as recipeID
        }

        // Attach the bundle to the fragment
        viewSelectedFragment.arguments = bundle

        // Perform the fragment transaction to switch to ViewSelectedFragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, viewSelectedFragment)
            .addToBackStack(null)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

    private fun showFilterDialog() {
        val filterOptions = arrayOf("Italian", "Lebanese", "Mexican", "Portuguese", "French", "Indian", "Japanese", "South African")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose a cuisine")
        builder.setItems(filterOptions) { dialog, which ->
            val selectedCuisine = filterOptions[which]
            filterRecipesByCuisine(selectedCuisine)
        }
        builder.show()
    }

    private fun filterRecipesByCuisine(cuisine: String) {
        val database = FirebaseDatabase.getInstance().reference

        // Clear previous results
        binding.layoutResults.removeAllViews()

        database.child("cuisines").child(cuisine).child("recipes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val recipeList = mutableListOf<String>()

                    for (recipeSnapshot in dataSnapshot.children) {
                        val recipeName = recipeSnapshot.key
                        recipeList.add(recipeName ?: "")

                        // Dynamically add results to ScrollView
                        val textView = TextView(requireContext()).apply {
                            text = recipeName
                            textSize = 18f
                            setTextColor(resources.getColor(android.R.color.white))
                            setPadding(16, 16, 16, 16)
                            background = resources.getDrawable(R.drawable.textbox_background, null)
                            setOnClickListener {
                                onRecipeClicked(cuisine, recipeName ?: "")
                            }
                        }
                        binding.layoutResults.addView(textView)
                    }

                    if (recipeList.isEmpty()) {
                        val textView = TextView(requireContext()).apply {
                            text = "No recipes found for: $cuisine"
                            textSize = 18f
                            setTextColor(resources.getColor(android.R.color.white))
                            setPadding(16, 16, 16, 16)
                        }
                        binding.layoutResults.addView(textView)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    val textView = TextView(requireContext()).apply {
                        text = "Error fetching data: ${databaseError.message}"
                        textSize = 18f
                        setTextColor(resources.getColor(android.R.color.white))
                        setPadding(16, 16, 16, 16)
                    }
                    binding.layoutResults.addView(textView)
                }
            })
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}
