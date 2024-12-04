package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var btnLeb: ImageView
    private lateinit var btnItalian: ImageView
    private lateinit var btnMexican: ImageView
    private lateinit var btnPortu: ImageView
    private lateinit var btnFrench: ImageView
    private lateinit var btnJapan: ImageView
    private lateinit var btnSA: ImageView
    private lateinit var btnIndian: ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnLeb = view.findViewById(R.id.btnLebaneseCuisine)
        btnItalian = view.findViewById(R.id.btnItalianCuisine)
        btnMexican = view.findViewById(R.id.btnMexicanCuisine)
        btnPortu = view.findViewById(R.id.btnPortCuisine)
        btnFrench = view.findViewById(R.id.btnFrenchCuisine)
        btnJapan = view.findViewById(R.id.btnJapaneseCuisine)
        btnSA = view.findViewById(R.id.btnSACuisine)
        btnIndian = view.findViewById(R.id.btnIndianCuisine)

        btnLeb.setOnClickListener() {
            replaceFragment(LebaneseFragment())
        }

        btnItalian.setOnClickListener(){
            replaceFragment(ItalianFragment())
        }

        btnMexican.setOnClickListener(){
            replaceFragment(MexicanFragment())
        }

        btnPortu.setOnClickListener(){
            replaceFragment(PortugueseFragment())
        }

        btnFrench.setOnClickListener(){
            replaceFragment(FrenchFragment())
        }

        btnJapan.setOnClickListener(){
            replaceFragment(JapaneseFragment())
        }

        btnSA.setOnClickListener(){
            replaceFragment(SouthafricanFragment())
        }

        btnIndian.setOnClickListener(){
            replaceFragment(IndianFragment())
        }

        //checking if device is online and syncing recipes if it is
        if (isOnline(requireContext())){
            Toast.makeText(requireContext(), "You are online. Syncing your recipes...", Toast.LENGTH_SHORT).show()
            syncRecipes(requireContext())
        }

    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    //methods to enable recipes to be synced if device is online
    public fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(
            NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    public fun syncRecipes(context: Context) {
        val recipeDao = RecipeDatabase.getInstance(context).recipeDao()

        CoroutineScope(Dispatchers.IO).launch {
            //upload all recipes from RoomDB and delete them from RoomDB once theyre uploaded
            val unsyncedRecipes = recipeDao.getAllRecipes()

            //upload each recipe to Firebase and if successful, delete it from RoomDB
            for (recipe in unsyncedRecipes) {
                //getting recipe data
                var recipeToEnter: RecipeData = RecipeData()

                recipeToEnter.name = recipe.name
                recipeToEnter.ingredients = recipe.ingredients
                recipeToEnter.durationMins = recipe.durationMins
                recipeToEnter.durationHrs = recipe.durationHrs
                recipeToEnter.method = recipe.method
                recipeToEnter.isLocked = recipe.isLocked

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null)
                {
                    var database = Firebase.database
                    val recipeRef = database.getReference("Users").child(userId).child("Recipes")

                    recipeRef.push().setValue(recipeToEnter)
                        .addOnSuccessListener {
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    context,
                                    "Your recipes has been successfully backed up online. :)",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            //deleting recipe once its been uploaded
                            CoroutineScope(Dispatchers.IO).launch{
                                recipeDao.deleteRecipe(recipe)
                            }
                        }
                        .addOnFailureListener {
                            CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "An error occurred while adding a recipe:" + it.toString() , Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }

        }

    }
}
