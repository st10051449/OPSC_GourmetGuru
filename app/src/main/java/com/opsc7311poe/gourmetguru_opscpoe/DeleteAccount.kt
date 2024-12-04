package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class DeleteAccount : Fragment() {
    private lateinit var txtconfirmPass: TextView
    private lateinit var btnDelete: Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delete_account, container, false)

        //handle when delete button is tapped
        txtconfirmPass = view.findViewById(R.id.edtconfirmpass)
        btnDelete = view.findViewById(R.id.btnDelete)

        btnDelete.setOnClickListener(){
            val password = txtconfirmPass.text.toString()

            if(password.isNullOrBlank()){
                Toast.makeText(requireContext(), "Please enter your current password to confirm account deletion!", Toast.LENGTH_SHORT).show()
            }
            else{
                deleteAccount (password)
            }
        }


        // Inflate the layout for this fragment
        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("Delete Account Fragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun deleteAccount(currentPassword: String){
        val auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.delete()
                            .addOnCompleteListener { deleteTask ->
                                if (deleteTask.isSuccessful) {
                                    // Account deletion successful, sending user back to login page
                                    Toast.makeText(requireContext(), "Your account has been deleted.", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(activity, Login::class.java)
                                    startActivity(intent)
                                    activity?.finish()

                                } else {
                                    // Account deletion failed
                                    Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(requireContext(), "The password you entered is incorrect!", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // User is not authenticated, handle this case accordingly
            Toast.makeText(requireContext(), "Oh no! Something went wrong. Try again later.", Toast.LENGTH_SHORT).show()
        }
    }
}