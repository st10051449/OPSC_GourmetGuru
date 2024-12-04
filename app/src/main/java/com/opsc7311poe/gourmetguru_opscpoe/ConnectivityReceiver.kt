package com.opsc7311poe.gourmetguru_opscpoe

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Looper
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.logging.Handler

//OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 26 October 2024].
class ConnectivityReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (isOnline(context)) {
            showToast(context, "You're back online. Busy syncing your recipes...")
            //syncing all recipes saved offline to firebase
            syncRecipes(context)

        }
        else {
            showToast(context, "We see you have lost internet connection. Don't worry your recipes will be saved and synced when you are back online :)")
        }
    }

    //function to handle toasts since this can happen at any given point in the app
    private fun showToast(context: Context, message: String)
    {
        android.os.Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    public fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
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
                            Toast.makeText(context, "Your recipes has been successfully backed up online. :)", Toast.LENGTH_LONG).show()
                            //deleting recipe once its been uploaded
                            CoroutineScope(Dispatchers.IO).launch{
                                recipeDao.deleteRecipe(recipe)
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "An error occurred while adding a recipe:" + it.toString() , Toast.LENGTH_LONG).show()
                        }
                }
            }

        }

    }
}