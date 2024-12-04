package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class MealPlanFragment : Fragment() {

    private lateinit var dayTextViews: List<TextView>
    private lateinit var ivShoppingBag: ImageView
    private lateinit var txtShoppingBag: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_meal_plan, container, false)

        dayTextViews = listOf(
            view.findViewById(R.id.txtMon),
            view.findViewById(R.id.txtTues),
            view.findViewById(R.id.txtWed),
            view.findViewById(R.id.txtThur),
            view.findViewById(R.id.txtFri),
            view.findViewById(R.id.txtSat),
            view.findViewById(R.id.txtSun)
        )

        ivShoppingBag = view.findViewById(R.id.ivShoppingBag)
        txtShoppingBag = view.findViewById(R.id.txtViewShoppingList)

        ivShoppingBag.setOnClickListener(){
            replaceFragment(ShoppingFragment())
        }

        txtShoppingBag.setOnClickListener(){
            replaceFragment(ShoppingFragment())
        }

        dayTextViews.forEachIndexed { index, textView ->
            textView.setOnClickListener {
                val day = when (index) {
                    0 -> "Monday"
                    1 -> "Tuesday"
                    2 -> "Wednesday"
                    3 -> "Thursday"
                    4 -> "Friday"
                    5 -> "Saturday"
                    6 -> "Sunday"
                    else -> ""
                }

                it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                navigateToMealPlanDaysFragment(day)
            }
        }

        return view
    }

    private fun navigateToMealPlanDaysFragment(day: String) {
        val mealPlanDays = MealPlanDaysFragment().apply {
            arguments = Bundle().apply {
                putString("day", day)
            }
        }
        replaceFragment(mealPlanDays)
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
