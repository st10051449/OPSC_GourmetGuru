package com.opsc7311poe.gourmetguru_opscpoe

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.media.ExifInterface
import android.graphics.Matrix

class MyProfileFragment : Fragment() {

    private lateinit var btnOpenSettings: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtEmail: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var storageRef: StorageReference
    private lateinit var database: FirebaseDatabase
    private lateinit var profilePic: ImageView
    private lateinit var txtUpdatePfp: TextView
    private var fileUri: Uri? = null

    private val fileName = "profile_picture.jpg"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_profile, container, false)

        // Initialize Firebase references
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        database = FirebaseDatabase.getInstance()

        profilePic = view.findViewById(R.id.imgpfp)
        txtUpdatePfp = view.findViewById(R.id.txtupdatepfp)

        // Load current profile picture if available
        loadProfilePicture()

        // Set up click listener to choose an image
        txtUpdatePfp.setOnClickListener {
            chooseImage()
        }

        // Fetching and displaying user information
        txtName = view.findViewById(R.id.txtUsername)
        txtEmail = view.findViewById(R.id.txtEmail)

        val userId = auth.currentUser?.uid
        val userRef = database.getReference("Users").child(userId!!)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                txtName.text = "${dataSnapshot.child("name").getValue(String::class.java)} ${dataSnapshot.child("surname").getValue(String::class.java)}"
                txtEmail.text = dataSnapshot.child("email").getValue(String::class.java)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    requireContext(),
                    "Error reading from the database: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Handling settings button navigation
        btnOpenSettings = view.findViewById(R.id.btnsettings)
        btnOpenSettings.setOnClickListener {
            replaceFragment(Settings())
        }

        return view
    }

    private fun replaceFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.frame_container, fragment)
            .commit()
    }

    private fun chooseImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Choose image to upload"), PICK_IMAGE_REQUEST)
    }

    // Function to adjust image orientation
    // Function to adjust image orientation and flipping
    private fun getCorrectlyOrientedAndFlippedBitmap(uri: Uri): Bitmap? {
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
        inputStream?.close()  // Close input stream after reading

        // Open new input stream for Exif (needed for some devices)
        val newInputStream = requireActivity().contentResolver.openInputStream(uri)
        val exif = newInputStream?.let { ExifInterface(it) }
        newInputStream?.close()

        val orientation = exif?.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        Log.d("ImageOrientation", "Orientation: $orientation")

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
        }

        // Create a new bitmap with the applied matrix
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    // Function to rotate and flip the image
    private fun rotateAndFlipImage(source: Bitmap, angle: Float, flipHorizontal: Boolean, flipVertical: Boolean): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)

        // Flip horizontally if needed
        if (flipHorizontal) {
            matrix.postScale(-1f, 1f)
        }
        // Flip vertically if needed
        if (flipVertical) {
            matrix.postScale(1f, -1f)
        }

        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            fileUri = data.data
            try {
                // Get correctly oriented bitmap
                val bitmap: Bitmap? = fileUri?.let { getCorrectlyOrientedAndFlippedBitmap(it) }
                bitmap?.let {
                    profilePic.setImageBitmap(it)
                    saveImageToInternalStorage(it) // Save image to internal storage
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) {
        // Get the current user's unique ID
        val userId = auth.currentUser?.uid ?: return

        // Create a unique file name based on the user ID
        val fileName = "${auth.currentUser?.uid}_profile_picture.jpg"
        val file = File(requireContext().filesDir, fileName)

        try {
            FileOutputStream(file).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                Toast.makeText(requireContext(), "Image saved successfully", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
    private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid ?: return
        val fileName = "${auth.currentUser?.uid}_profile_picture.jpg"
        val file = File(requireContext().filesDir, fileName)

        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            profilePic.setImageBitmap(bitmap)
        } else {
            Log.e("Load Image Failed", "Profile picture not found in internal storage")
        }
    }


    /*private fun loadProfilePicture() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val imagePath = "images/$userId.jpg"
            val imageRef = storageRef.child(imagePath)
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri).into(profilePic)
            }.addOnFailureListener { exception ->
                Log.e("Load Image Failed", "Error: ${exception.message}")
            }
        } else {
            Log.e("Load Image Failed", "User ID is null")
        }
    }

    private fun uploadImage(uri: Uri?) {
        uri?.let {
            val progressDialog = ProgressDialog(requireContext())
            progressDialog.setTitle("Uploading Image...")
            progressDialog.setMessage("Processing...")
            progressDialog.show()

            val userId = auth.currentUser?.uid
            if (userId != null) {
                val newImagePath = "images/$userId.jpg"
                val newImageRef: StorageReference = storageRef.child(newImagePath)

                newImageRef.putFile(uri)
                    .addOnSuccessListener {
                        newImageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            database.getReference("Users/$userId/profilePicture").setValue(downloadUri.toString())
                                .addOnSuccessListener {
                                    progressDialog.dismiss()
                                    Toast.makeText(requireContext(), "File uploaded successfully!", Toast.LENGTH_LONG).show()
                                    loadProfilePicture()
                                }
                                .addOnFailureListener { e ->
                                    progressDialog.dismiss()
                                    Log.e("Database Update Failed", "Error: ${e.message}")
                                    Toast.makeText(requireContext(), "Failed to update database", Toast.LENGTH_LONG).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Log.e("Upload Failed", "Error: ${e.message}")
                        Toast.makeText(requireContext(), "File Upload Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
            } else {
                progressDialog.dismiss()
                Log.e("Upload Image Failed", "User ID is null")
                Toast.makeText(requireContext(), "User ID is null", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun uploadImageToStorage(
        uri: Uri,
        newImageRef: StorageReference,
        profilePictureRef: DatabaseReference,
        progressDialog: ProgressDialog
    ) {
        newImageRef.putFile(uri)
            .addOnSuccessListener {
                // Get the uploaded image URL
                newImageRef.downloadUrl.addOnSuccessListener { newUri ->
                    profilePictureRef.setValue(newUri.toString()).addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), "File uploaded successfully!", Toast.LENGTH_LONG).show()
                        loadProfilePicture()
                    }.addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Log.e("Database Update Failed", "Error: ${e.message}")
                        Toast.makeText(requireContext(), "Failed to update database", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Log.e("Image URL Retrieval Failed", "Error: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to retrieve image URL: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.e("Upload Failed", "Error: ${e.message}")
                Toast.makeText(requireContext(), "File Upload Failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
    }*/

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
