package com.opsc7311poe.gourmetguru_opscpoe

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.concurrent.Executor

class ViewYourRecipesFrgament : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var svAllRec: ScrollView
    private lateinit var linlayAllRec: LinearLayout

    // Biometric authentication variables
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var currentRecipeKey: String? = null  // To store the current recipe key to open after auth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_view_your_recipes_frgament, container, false)

        // Back button functionality
        btnBack = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            replaceFragment(MyRecipesFragment())
        }

        // ScrollView and LinearLayout for displaying recipes
        svAllRec = view.findViewById(R.id.svAllRecipes)
        linlayAllRec = view.findViewById(R.id.linlayRecipes)

        // Initialize biometric prompt and dialog
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(requireContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show()
                // If authentication succeeds, open the locked recipe
                currentRecipeKey?.let { openRecipe(it) }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(requireContext(), "Authentication failed", Toast.LENGTH_SHORT).show()
            }
        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric authentication required")
            .setSubtitle("Use your fingerprint or face to access this recipe")
            .setNegativeButtonText("Cancel")
            .build()

        // Fetch and display recipes
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val database = Firebase.database
            val recRef = database.getReference("Users").child(userId).child("Recipes")

            recRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    linlayAllRec.removeAllViews()

                    for (pulledOrder in snapshot.children) {
                        val recName: String? = pulledOrder.child("name").getValue(String::class.java)
                        val isLocked: Boolean = pulledOrder.child("locked").getValue(Boolean::class.java) ?: false

                        if (recName != null) {
                            // Create TextView for each recipe
                            val textView = TextView(requireContext())
                            textView.text = recName
                            textView.textSize = 20f
                            textView.setTextColor(Color.parseColor("#FFFFFF"))
                            textView.typeface = ResourcesCompat.getFont(requireContext(), R.font.lora)
                            textView.height = 100

                            // Click listener for each recipe
                            textView.setOnClickListener {
                                if (isLocked) {
                                    // Store the recipe key and trigger biometric authentication
                                    currentRecipeKey = pulledOrder.key
                                    biometricPrompt.authenticate(promptInfo)
                                } else {
                                    // If recipe is not locked, open it directly
                                    openRecipe(pulledOrder.key)
                                }
                            }

                            linlayAllRec.addView(textView)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error reading from the database: $error", Toast.LENGTH_SHORT).show()
                }
            })
        }

        return view
    }


    private fun openRecipe(recipeID: String?) {
        val viewSelectedRecipeFrag = ViewSelectedRecipeFragment()
        val bundle = Bundle()
        bundle.putString("recipeID", recipeID)
        viewSelectedRecipeFrag.arguments = bundle
        replaceFragment(viewSelectedRecipeFrag)
    }


    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
