package com.uwi.rideshareapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.uwi.rideshareapp.databinding.ActivityProfileBinding
import com.uwi.rideshareapp.databinding.ActivityUpdateProfileBinding
import com.uwi.rideshareapp.model.User
import kotlinx.android.synthetic.main.activity_update_profile.*
import java.util.*

class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private lateinit var database: DatabaseReference

    var mAuth: FirebaseAuth? = null


    companion object {
        val TAG = "UpdateProfileActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.setTitle("Update Profile")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Profile Photo
        selectphoto_btn.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        //Display Phone Number
        mAuth = FirebaseAuth.getInstance()

        val phoneNumber = findViewById<TextView>(R.id.userPhoneNo)
        phoneNumber.text = mAuth?.currentUser?.phoneNumber.toString()


        //Update user information
        database = FirebaseDatabase.getInstance().getReference("Users")
        database.child(mAuth?.currentUser?.uid!!).get().addOnSuccessListener {
            if (it.exists()) {
                val name = it.child("name").value
                val bio = it.child("bio").value
                val address = it.child("address").value
                val email = it.child("email").value


                binding.userName.text = name.toString()
//                binding.userBio.text = bio.toString()
//                binding.userAddress.text = address.toString()
//                binding.userEmail.text = email.toString()
            } else {
                Toast.makeText(this, "User Doesn't Exist", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }

        binding.saveBtn.setOnClickListener {

//            val name = binding.editTextName.text.toString()
//            val bio = binding.editTextBio.text.toString()
//            val address = binding.editTextAddress.text.toString()
//            val email = binding.editTextEmail.text.toString()
//
//            database = FirebaseDatabase.getInstance().getReference("Users")
//            val userInfo = User(name, bio, address, email)
//            database.child(mAuth?.currentUser?.uid!!).setValue(userInfo).addOnSuccessListener {
//
//                val intent = Intent(this, ProfileActivity::class.java)
//                startActivity(intent)
//
//                Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()
//
//            }.addOnFailureListener {
//
//                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
//
//            }
            uploadImageToFirebaseStorage()
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            profile_image.setImageBitmap(bitmap)

            selectphoto_btn.alpha = 0f

//      val bitmapDrawable = BitmapDrawable(bitmap)
//      selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    Log.d(TAG, "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                Log.d(TAG, "Failed to upload image to storage: ${it.message}")
            }
    }



    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val name = binding.editTextName.text.toString()
        val bio = binding.editTextBio.text.toString()
        val address = binding.editTextAddress.text.toString()
        val email = binding.editTextEmail.text.toString()

        database = FirebaseDatabase.getInstance().getReference("Users")
        val userInfo = User(name, bio, address, email, profileImageUrl)
        database.child(mAuth?.currentUser?.uid!!).setValue(userInfo).addOnSuccessListener {

            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)

            Toast.makeText(this, "Successfully Saved", Toast.LENGTH_SHORT).show()

        }.addOnFailureListener {

            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()

        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}