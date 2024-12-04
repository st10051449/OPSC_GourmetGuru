package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


class MyRecipesFragment : Fragment() {

    // Declare ImageView variables
    private lateinit var imgTimer: ImageView
    private lateinit var imgAddRecipes: ImageView
    private lateinit var imgViewRecipes: ImageView
    private  lateinit var imgViewBack: ImageView

    //nav
    private lateinit var imgbtnAddCollection: ImageView
    private lateinit var imgbtnViewCollection: ImageView

    private lateinit var txtAddCollection: TextView
    private lateinit var txtViewCollection: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_recipes, container, false)

        // Initialize ImageView variables
        imgTimer = view.findViewById(R.id.imgTimer)

        // Set onClickListener for imgTimer
        imgTimer.setOnClickListener {
            replaceFragment(TimerFragment())
        }

        imgbtnAddCollection = view.findViewById(R.id.imgAddCollections)
        imgbtnViewCollection = view.findViewById(R.id.imgCollections)
        txtViewCollection = view.findViewById(R.id.txtViewCollections)
        txtAddCollection = view.findViewById(R.id.txtCreateCollection)


        imgbtnAddCollection.setOnClickListener(){
            replaceFragment(NewCollectionFragment())
        }

        imgbtnViewCollection.setOnClickListener(){
            replaceFragment(ViewCollectionsFragment())
        }


        //nav to viewing
        txtAddCollection.setOnClickListener(){
            replaceFragment(NewCollectionFragment())
        }


        txtViewCollection.setOnClickListener(){
            replaceFragment(ViewCollectionsFragment())
        }

        //handling add recipe navigation
        imgAddRecipes = view.findViewById(R.id.imgAddRecipes)
        imgAddRecipes.setOnClickListener {
            replaceFragment(AddYourRecipeFregment())
        }

        //handling view recipe navigation
        imgViewRecipes = view.findViewById(R.id.imgViewRecipes)
        imgViewRecipes.setOnClickListener {
            replaceFragment(ViewYourRecipesFrgament())
        }

        //handling back button functionality
        imgViewBack = view.findViewById(R.id.btnBack)
        imgViewBack.setOnClickListener {
            replaceFragment(HomeFragment())
        }

        return view
    }

    // Function to replace the current fragment
    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
