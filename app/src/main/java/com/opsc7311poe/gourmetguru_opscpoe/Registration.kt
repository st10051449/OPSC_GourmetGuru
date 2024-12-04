package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Registration : AppCompatActivity() {

    // Declare Firebase Authentication and Database Reference
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    // Declare EditText fields
    private lateinit var nameEditText: EditText
    private lateinit var surnameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize the EditText and Button views
        nameEditText = findViewById(R.id.txtRName)
        surnameEditText = findViewById(R.id.txtRSurname)
        emailEditText = findViewById(R.id.txtREmail)
        passwordEditText = findViewById(R.id.txtRPassword)
        registerButton = findViewById(R.id.btnRegister)

        // Handle button click to register the user
        registerButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val surname = surnameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(name, surname, email, password)
            }
        }
    }

    // Function to register a user with Firebase
    private fun registerUser(name: String, surname: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Store user details in Firebase Realtime Database
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        database = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                        val userMap = hashMapOf<String, String>(
                            "name" to name,
                            "surname" to surname,
                            "email" to email
                        )
                        database.setValue(userMap).addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                // Registration successful, navigate to MainActivity
                                Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish() // Close the registration activity
                            } else {
                                // If storing user details in the database fails
                                Toast.makeText(this, "Failed to save user info", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // If registration fails
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
