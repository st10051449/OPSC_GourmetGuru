package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ShoppingFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var resultsLayout: LinearLayout
    private lateinit var btnFilterByDay: Button
    private lateinit var btnAlphabeticalOrder: Button
    private lateinit var userId: String
    private val daysOfWeek =
        listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private val allIngredients = mutableListOf<Pair<String, String>>() // Pair of (day, ingredient)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_shopping, container, false)

        btnBack = rootView.findViewById(R.id.btnBack)
        resultsLayout = rootView.findViewById(R.id.resultsLayout)
        btnFilterByDay = rootView.findViewById(R.id.btnFilterByDay)
        btnAlphabeticalOrder = rootView.findViewById(R.id.btnAlphabeticalOrder)
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        loadIngredients()

        btnFilterByDay.setOnClickListener {
            displayIngredientsByDay()
        }

        btnAlphabeticalOrder.setOnClickListener {
            displayIngredientsAlphabetically()
        }

        btnBack.setOnClickListener(){
            replaceFragment(MealPlanFragment()) //directing user back
        }

        return rootView
    }

    private fun loadIngredients() {
        val mealPlanPath = "Users/$userId/MealPlan"

        FirebaseDatabase.getInstance().reference.child(mealPlanPath)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    allIngredients.clear() // Clear previous data
                    for (day in daysOfWeek) {
                        val daySnapshot = dataSnapshot.child(day)
                        if (daySnapshot.exists()) {
                            // For each meal type (breakfast, lunch, dinner)
                            for (mealType in listOf("breakfast", "lunch", "dinner")) {
                                val mealSnapshot = daySnapshot.child(mealType)
                                if (mealSnapshot.exists()) {
                                    val recipeSnapshot = mealSnapshot.children.firstOrNull()
                                    if (recipeSnapshot != null && recipeSnapshot.hasChild("ingredients")) {
                                        val ingredientsList =
                                            recipeSnapshot.child("ingredients").children.mapNotNull {
                                                it.getValue(String::class.java)
                                            }
                                        ingredientsList.forEach { ingredient ->
                                            allIngredients.add(
                                                Pair(
                                                    day,
                                                    ingredient
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            allIngredients.add(
                                Pair(
                                    day,
                                    "Meals not selected"
                                )
                            )
                        }
                    }
                    displayIngredientsByDay()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        requireContext(),
                        "Error fetching meals: ${databaseError.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


   // OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 28 September 2024].
    private fun parseIngredient(ingredient: String): Pair<Int, String> {
        val parts = ingredient.split(" ", limit = 2)
        val name =
            if (parts.size == 2) parts[1].capitalize() else ingredient.capitalize() // Capitalize the ingredient name
        val quantity = parts[0].toIntOrNull() ?: 1 // Default to 1 if no valid quantity
        return Pair(quantity, name)
    }


    //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 28 September 2024].
    private fun groupIngredients(ingredients: List<String>): Map<String, Int> {
        val groupedIngredients = mutableMapOf<String, Int>()

        ingredients.forEach { ingredient ->
            val (quantity, name) = parseIngredient(ingredient)
            groupedIngredients[name] = groupedIngredients.getOrDefault(name, 0) + quantity
        }

        return groupedIngredients
    }

    private fun displayIngredientsByDay() {
        resultsLayout.removeAllViews()

        for (day in daysOfWeek) {
            val dayIngredients = allIngredients.filter { it.first == day }.map { it.second }

            val dayHeader = TextView(requireContext()).apply {
                text = day
                textSize = 30f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(16, 16, 16, 16)
                setTextColor(resources.getColor(android.R.color.white))
            }
            resultsLayout.addView(dayHeader)

            if (dayIngredients.isEmpty() || (dayIngredients.size == 1 && dayIngredients.first() == "Meals not selected")) {
                val noMealText = TextView(requireContext()).apply {
                    text = "Meals not selected"
                    setTextColor(resources.getColor(android.R.color.white))
                    setPadding(16, 8, 16, 8)
                }
                resultsLayout.addView(noMealText)
            } else {
                val grouped = groupIngredients(dayIngredients)
                grouped.forEach { (ingredientName, quantity) ->
                    val ingredientText = TextView(requireContext()).apply {
                        text = "$quantity $ingredientName" // Display grouped ingredients
                        setPadding(16, 8, 16, 8)
                        setTextColor(resources.getColor(android.R.color.white))
                    }
                    resultsLayout.addView(ingredientText)
                }
            }
        }
    }

    private fun displayIngredientsAlphabetically() {
        resultsLayout.removeAllViews()

        val groupedIngredients =
            groupIngredients(allIngredients.filter { it.second != "Meals not selected" }
                .map { it.second })
        val sortedIngredients =
            groupedIngredients.toList().sortedBy { it.first }

        sortedIngredients.forEach { (ingredientName, quantity) ->
            val ingredientText = TextView(requireContext()).apply {
                text = "$quantity $ingredientName"
                setPadding(16, 8, 16, 8)
                setTextColor(resources.getColor(android.R.color.white))
            }
            resultsLayout.addView(ingredientText)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

}
