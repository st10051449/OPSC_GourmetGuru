package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class NewCollectionFragment : Fragment() {

    private lateinit var btnSaveCol: Button
    private lateinit var btnBack: ImageView
    private lateinit var txtColName: EditText

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
        val view = inflater.inflate(R.layout.fragment_new_collection, container, false)

        btnSaveCol = view.findViewById(R.id.btnSave)
        btnBack = view.findViewById(R.id.btnBack)
        txtColName = view.findViewById(R.id.txtCollectionName)

        btnSaveCol.setOnClickListener {
            saveCollection()
        }

        btnBack.setOnClickListener(){
            replaceFragment(MyRecipesFragment())
        }
        return view

    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun saveCollection() {
        val collectionName = txtColName.text.toString().trim()

        if (collectionName.isEmpty()) {
            Toast.makeText(requireContext(), "Collection name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }


        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Create a unique ID for the collection
        val collectionId = UUID.randomUUID().toString()

        // Reference to the Firebase Database
        val database = FirebaseDatabase.getInstance().reference
        val collectionsRef = database.child("Users").child(userId).child("Collections").child(collectionId)

        // Save the collection name to Firebase
        collectionsRef.child("name").setValue(collectionName)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Collection saved successfully", Toast.LENGTH_SHORT).show()
                    txtColName.text.clear() // Clear the input field after saving
                } else {
                    Toast.makeText(requireContext(), "Failed to save collection", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
