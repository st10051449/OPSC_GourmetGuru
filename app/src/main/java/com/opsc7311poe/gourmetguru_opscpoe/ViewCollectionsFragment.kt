package com.opsc7311poe.gourmetguru_opscpoe

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.graphics.Color
import android.view.HapticFeedbackConstants
import androidx.core.content.res.ResourcesCompat

class ViewCollectionsFragment : Fragment() {

    private lateinit var colLinLay: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var userId: String

    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_collections, container, false)

        // Initialize views
        colLinLay = view.findViewById(R.id.colLinLay)
        btnBack = view.findViewById(R.id.btnBack)

        btnBack.setOnClickListener(){
        replaceFragment(MyRecipesFragment())
        }

        // Initialize Firebase references
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        database = FirebaseDatabase.getInstance().reference

        // Load collections
        loadCollections()

        return view
    }

    private fun replaceFragment(fragment: Fragment) {

        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    private fun loadCollections() {
        if (userId.isEmpty()) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Reference to the user's collections in Firebase
        database.child("Users").child(userId).child("Collections")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    colLinLay.removeAllViews() // Clear previous views

                    for (collectionSnapshot in dataSnapshot.children) {
                        val collectionName = collectionSnapshot.child("name").value as String

                        //OpenAI, 2024. ChatGPT. [online]. Available at: https://chat.openai.com [Accessed 26 October 2024].
                        // Create a TextView for each collection
                        val collectionTextView = TextView(requireContext()).apply {
                            text = collectionName
                            textSize = dpToPx(24f) / requireContext().resources.displayMetrics.density // Convert to sp
                            typeface = ResourcesCompat.getFont(requireContext(), R.font.lora) // Use ResourcesCompat to load the font
                            setPadding(25, 18, 16, 16) // Adjust padding as needed
                            setTextColor(Color.WHITE) // Set text color to white
                        }

                        collectionTextView.setOnClickListener {
                            val viewSelectedCollFrag = ViewCollectionFragment()
                            //transferring collection info using a bundle
                            val bundle = Bundle()
                            bundle.putString("collectionID", collectionSnapshot.key)
                            viewSelectedCollFrag.arguments = bundle
                            //changing to recipe info fragment
                            it.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            replaceFragment(viewSelectedCollFrag)
                        }

                        // Add the TextView to the LinearLayout
                        colLinLay.addView(collectionTextView)
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to load collections: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun dpToPx(dp: Float): Float {
        val density = requireContext().resources.displayMetrics.density
        return dp * density
    }
}
