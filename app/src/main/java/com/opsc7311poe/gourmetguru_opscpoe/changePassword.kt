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

class changePassword : Fragment() {

    private lateinit var txtCurPass: TextView
    private lateinit var txtNewPass: TextView
    private lateinit var txtConfirmNewPass: TextView
    private lateinit var btnChangePass: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        txtCurPass = view.findViewById(R.id.txtentercurpass)
        txtNewPass = view.findViewById(R.id.txtenternewpass)
        txtConfirmNewPass = view.findViewById(R.id.txtenterconfirm)
        btnChangePass = view.findViewById(R.id.btnChange)
        //functionality for change pass button
        btnChangePass.setOnClickListener(){
            val oldPass = txtCurPass.text.toString()
            val newPass = txtNewPass.text.toString()
            val retypePass = txtConfirmNewPass.text.toString()

            if (oldPass.isNullOrBlank() || newPass.isNullOrBlank() || retypePass.isNullOrBlank()) {
                Toast.makeText(requireContext(), "Please make sure you have filled in all fields!", Toast.LENGTH_SHORT).show()
            } else if (newPass != retypePass) {
                Toast.makeText(requireContext(), "Your passwords do not match!", Toast.LENGTH_SHORT).show()
            } else {
                passwordChange(oldPass, newPass, retypePass)
                replaceFragment(Settings())
            }

        }



        // Inflate the layout for this fragment
        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d("Settings Fragment", "Replacing fragment: ${fragment::class.java.simpleName}")
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .addToBackStack(null)
            .commit()
    }
    fun passwordChange(oldPassword: String, newPassword: String, newPasswordRetype: String){
        val auth = FirebaseAuth.getInstance()
        val user: FirebaseUser? = auth.currentUser

        if(user!= null){
            val credential = EmailAuthProvider.getCredential(user.email!!, oldPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {

                        if (newPassword == newPasswordRetype) {
                            user.updatePassword(newPassword)
                                .addOnCompleteListener { updatePasswordTask ->
                                    if (updatePasswordTask.isSuccessful) {
                                        Toast.makeText(requireContext(), "Password has been successfully changed!", Toast.LENGTH_SHORT).show()
                                        replaceFragment(Settings())
                                    } else {
                                        // Password change failed
                                        Toast.makeText(requireContext(), "Failed to change password", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(requireContext(), "Your passwords do not match!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Your current password is incorrect!", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            activity?.finish()
        }
    }
}