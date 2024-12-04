package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MealPlanDaysFragment : Fragment() {

    private lateinit var txtDayOfWeek: TextView
    private lateinit var txtBreakfast: TextView
    private lateinit var txtLunch: TextView
    private lateinit var txtDinner: TextView
    private lateinit var txtSelDinner: TextView
    private lateinit var txtSelBreakfast: TextView
    private lateinit var txtSelLunch: TextView
    private lateinit var userId: String
    private lateinit var btnBack: ImageView
    private var day: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meal_plan_days, container, false)

        txtDayOfWeek = view.findViewById(R.id.txtMainDay)
        txtBreakfast = view.findViewById(R.id.txtBreak)
        txtLunch = view.findViewById(R.id.txtLunch)
        txtDinner = view.findViewById(R.id.txtDinner)
        txtSelDinner = view.findViewById(R.id.txtDinnerSelected)
        txtSelBreakfast = view.findViewById(R.id.txtBreakfastSelected)
        txtSelLunch = view.findViewById(R.id.txtLunchSelected)

        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener(){
            replaceFragment(MealPlanFragment())
        }


        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        day = arguments?.getString("day") ?: ""

        txtDayOfWeek.text = day
        fetchSelectedMeals()

        txtBreakfast.setOnClickListener { navigateToSelectMealFragment("breakfast") }
        txtLunch.setOnClickListener { navigateToSelectMealFragment("lunch") }
        txtDinner.setOnClickListener { navigateToSelectMealFragment("dinner") }

        return view
    }


    private fun fetchSelectedMeals() {
        val mealPlanPath = "Users/$userId/MealPlan/$day"

        FirebaseDatabase.getInstance().reference.child(mealPlanPath)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Handle breakfast meal
                    val breakfastSnapshot = dataSnapshot.child("breakfast")
                    txtSelBreakfast.text = fetchMealWithIngredients(breakfastSnapshot) ?: "No Breakfast selected"

                    // Handle lunch meal
                    val lunchSnapshot = dataSnapshot.child("lunch")
                    txtSelLunch.text = fetchMealWithIngredients(lunchSnapshot) ?: "No Lunch selected"

                    // Handle dinner meal
                    val dinnerSnapshot = dataSnapshot.child("dinner")
                    txtSelDinner.text = fetchMealWithIngredients(dinnerSnapshot) ?: "No Dinner selected"
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching meals: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun fetchMealWithIngredients(snapshot: DataSnapshot): String? {
        if (!snapshot.exists() || !snapshot.hasChildren()) {
            return null
        }


        val recipeSnapshot = snapshot.children.firstOrNull()
        if (recipeSnapshot != null && recipeSnapshot.key != null) {
            val recipeName = recipeSnapshot.key
            val ingredients = recipeSnapshot.child("ingredients").children.mapNotNull { it.getValue(String::class.java) }


            return "$recipeName"
        }
        return null
    }

    //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 24 September 2024].
    private fun navigateToSelectMealFragment(mealType: String) {
        val selectMealFragment = SelectMealFragment().apply {
            arguments = Bundle().apply {
                putString("day", day)
                putString("mealType", mealType)
            }
        }
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.frame_container, selectMealFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
