package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.Locale

class AddYourRecipeFregment : Fragment() {
    private lateinit var txtName: EditText
    private lateinit var txtDurationHrs: EditText
    private lateinit var txtDurationMins: EditText
    private lateinit var txtAllIngredients: TextView
    lateinit var txtIngrName: EditText
    private lateinit var txtIngrAmount: EditText
    private lateinit var txtMethod: TextView
    private lateinit var txtStep: EditText
    private lateinit var swLock: Switch
    private lateinit var btnAdd: Button
    private lateinit var btnAddIngr: Button
    private lateinit var btnAddStep: Button
    private lateinit var btnBack: ImageView

    var recipeEntered: RecipeData = RecipeData()
    var ingredientsEntered: MutableList<Ingredient> = mutableListOf()
    var method: MutableList<String> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add_your_recipe_fregment, container, false)

        //connecting all elements
        txtName = view.findViewById(R.id.edtRecipeName)
        txtDurationHrs = view.findViewById(R.id.txtDurationHrs)
        txtDurationMins = view.findViewById(R.id.txtDurationMins)
        txtAllIngredients = view.findViewById(R.id.txtAllIngredients)
        txtIngrName = view.findViewById(R.id.txtIngrName)
        txtIngrAmount = view.findViewById(R.id.txtIngrAmount)
        txtMethod = view.findViewById(R.id.txtMethod)
        swLock = view.findViewById(R.id.swLockRecipe)
        btnAdd = view.findViewById(R.id.btnAddRecipe)
        btnAddIngr = view.findViewById(R.id.btnAddIngredient)
        btnAddStep = view.findViewById(R.id.btnAddStep)
        txtStep = view.findViewById(R.id.txtStep)

        //handling back button functionality
        btnBack = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener{
            replaceFragment(MyRecipesFragment())
        }

        //handling add step to method functionality

        btnAddStep.setOnClickListener{

            //checking all fields are filled
            if(txtStep.text.toString().isBlank())
            {
                Toast.makeText( requireContext(), "Please enter the next step for the method.", Toast.LENGTH_SHORT).show()
            }
            else
            {

                method.add(txtStep.text.toString())
                txtStep.text.clear()

                //displaying updated list to user
                var allStepsString: String = ""
                for(step in method)
                {
                    allStepsString += step
                    allStepsString += "\n"
                }

                txtMethod.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                txtMethod.text = allStepsString
            }

        }

        //handling add ingredients  functionality
        btnAddIngr.setOnClickListener{

            //checking all fields are filled
            if(txtIngrName.text.toString().isBlank() || txtIngrAmount.text.toString().isBlank())
            {
                Toast.makeText( requireContext(), "Please enter ingredient name and amount.", Toast.LENGTH_SHORT).show()
            }
            else
            {

                ingredientsEntered.add(Ingredient(txtIngrName.text.toString(), txtIngrAmount.text.toString()))
                txtIngrName.text.clear()
                txtIngrAmount.text.clear()

                //displaying updated list to user
                var allIngreString: String = ""
                for(Ingredient in ingredientsEntered)
                {
                    allIngreString += "${Ingredient.amount}             ${Ingredient.name}"
                    allIngreString += "\n"
                }

                txtAllIngredients.textAlignment = View.TEXT_ALIGNMENT_VIEW_START
                txtAllIngredients.text = allIngreString
            }

        }

        //handling save button functionality
        btnAdd.setOnClickListener{
            //check if all fields are filled
            if(txtName.text.toString().isBlank() ||
                txtAllIngredients.text.toString().isBlank() ||
                txtMethod.text.toString().isBlank() ||
                txtDurationHrs.text.toString().isBlank() ||
                txtDurationMins.text.toString().isBlank())
            {
                Toast.makeText( requireContext(), "Please ensure all recipe information is filled correctly.", Toast.LENGTH_SHORT).show()
            }
            else {
                //assigning recipe data to recipe object

                recipeEntered.name = txtName.text.toString()
                recipeEntered.method = method
                recipeEntered.ingredients = ingredientsEntered
                recipeEntered.isLocked = swLock.isChecked

                //allowing user to enter duration amount
                recipeEntered.durationHrs = txtDurationHrs.text.toString().toDouble()
                recipeEntered.durationMins = txtDurationMins.text.toString().toDouble()

                //checking if user is online, if so push data to Firebase, else save locally
                if (isOnline(requireContext())) {

                    //pushing recipe data to DB
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    if (userId != null)
                    {
                        var database = Firebase.database
                        val recipeRef = database.getReference("Users").child(userId).child("Recipes")

                        recipeRef.push().setValue(recipeEntered)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Your recipe has been successfully backed up online. :)", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "An error occurred while adding a recipe:" + it.toString() , Toast.LENGTH_LONG).show()
                            }
                    }

                    //go back to service landing page
                    it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    replaceFragment(MyRecipesFragment())
                }
                else{
                    //saving recipe locally
                    //getting instance of DB
                    val db = RecipeDatabase.getInstance(requireContext())
                    val recipeDao = db.recipeDao()
                    CoroutineScope(Dispatchers.IO).launch {
                        //generate an id to use for local db
                        recipeDao.insertRecipe(recipeEntered)
                        //display message to user that theyre offline and recipe is save offline
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                requireContext(),
                                "You are currently offline. Don't worry your recipe will be saved when you return online :)",
                                Toast.LENGTH_LONG
                            ).show()
                            //go back to service landing page
                            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            replaceFragment(MyRecipesFragment())
                        }
                    }
                }
            }
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

    //method to check if phone is connected to the internet
    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}