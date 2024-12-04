package com.opsc7311poe.gourmetguru_opscpoe

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.*
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.database.FirebaseDatabase

class Login : AppCompatActivity() {

    private lateinit var txtREmail: EditText
    private lateinit var txtRPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var txtNoAccount: TextView
    private lateinit var txtLoginSSo: TextView

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance()



        txtREmail = findViewById(R.id.txtREmail)
        txtRPassword = findViewById(R.id.txtRPassword)
        btnLogin = findViewById(R.id.btnLogin)
        txtNoAccount = findViewById(R.id.txtNoAccount)
        txtLoginSSo = findViewById(R.id.txtLoginSSo)



        btnLogin.setOnClickListener {
            val email = txtREmail.text.toString().trim()
            val password = txtRPassword.text.toString().trim()

            if (validateLogin(email, password)) {
                // Check if email exists for Google SSO
                mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result?.signInMethods?.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD) == true) {
                        Toast.makeText(this, "Please use Google Sign-In for this account", Toast.LENGTH_SHORT).show()
                    } else {

                        mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {
                                    startActivity(Intent(this, MainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    }
                }
            }
        }

        /// Google SSO login button click
        txtLoginSSo.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(this, gso)

            // Sign out from the previously selected account to force account selection
            googleSignInClient.signOut().addOnCompleteListener {
                signInWithGoogle()
            }
        }



        txtNoAccount.setOnClickListener {
            startActivity(Intent(this, Registration::class.java))
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            checkIfNewUser(account.idToken!!)
        } catch (e: ApiException) {
            val statusCode = e.statusCode
            val errorMessage = CommonStatusCodes.getStatusCodeString(statusCode)
            Log.e("TAG", "Google sign-in failed: $errorMessage")
            Toast.makeText(this, "Google sign-in failed: $errorMessage", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkIfNewUser(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false
                val user = mAuth.currentUser
                if (isNewUser) {
                    // Show password setup dialog
                    showPasswordDialog()

                    //get and store user info
                    val email = user?.email

                    //getting name and surname
                    val displayName = user?.displayName
                    val nameParts = displayName?.split(" ")
                    val fName = nameParts?.getOrNull(0) ?: ""
                    val surname = nameParts?.getOrNull(1) ?: ""

                    storeGoogleUserData(email, fName, surname)

                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            } else {
                Log.e("TAG", "signInWithCredential:failure", task.exception)
                Toast.makeText(this, "SSO login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeGoogleUserData(email: String?, fName: String?, surname: String?) {
        val userId = mAuth.currentUser?.uid
        if (userId != null) {
            val database = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            val userMap = hashMapOf<String, String>(
                "name" to fName!!,
                "surname" to surname!!,
                "email" to email!!
            )
            database.setValue(userMap).addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) { } else { }
            }
        }

    }

    private fun showPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Set a Password")

        val input = EditText(this)
        input.hint = "Enter password"
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val password = input.text.toString()
            if (password.length >= 6) {
                val user = mAuth.currentUser
                user?.updatePassword(password)?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to set password: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                // Optionally, call showPasswordDialog() again to prompt the user
                showPasswordDialog()
            }
        }
        builder.setNegativeButton("Cancel") { _, _ ->
            Toast.makeText(this, "Password setup canceled", Toast.LENGTH_SHORT).show()
        }
        builder.setCancelable(false)
        builder.show()
    }

    fun validateLogin(email: String, password: String): Boolean {
        if (TextUtils.isEmpty(email)) {
            txtREmail.error = "Email is required."
            return false
        }

        if (TextUtils.isEmpty(password)) {
            txtRPassword.error = "Password is required."
            return false
        }

        if (password.length < 6) {
            txtRPassword.error = "Password must be at least 6 characters long."
            return false
        }

        return true
    }
}
