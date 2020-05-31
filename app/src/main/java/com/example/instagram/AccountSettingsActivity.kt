package com.example.instagram

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.instagram.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfileRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfileRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")

        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SignInActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        change_image_text_btn.setOnClickListener {
            checker = "clicked"
            CropImage.activity().setAspectRatio(1, 1)
                .start(this@AccountSettingsActivity)
        }


        save_info_profile_btn.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }

        userInfo()

    }

    private fun uploadImageAndUpdateInfo() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("please wait ,Updating....")
        progressDialog.show()

        when {
            fullName_profileSettings_frag.text.toString() == "" -> {
                Toast.makeText(
                        this@AccountSettingsActivity,
                        "Full name is Required",
                        Toast.LENGTH_LONG
                    )
                    .show()
                progressDialog.dismiss()
                return
            }
            userName_profileSettings_frag.text.toString() == "" -> {
                Toast.makeText(
                        this@AccountSettingsActivity,
                        "User Name is Required",
                        Toast.LENGTH_LONG
                    )
                    .show()
                progressDialog.dismiss()
                return
            }
            bio_profileSettings_frag.text.toString() == "" -> {
                Toast.makeText(this@AccountSettingsActivity, "Bio is Required", Toast.LENGTH_LONG)
                    .show()
                progressDialog.dismiss()
                return
            }
            imageUri == null -> {
                Toast.makeText(this@AccountSettingsActivity, "Image is Required", Toast.LENGTH_LONG)
                    .show()
                progressDialog.dismiss()
                return
            }
            else -> {
                val fileRef = storageProfileRef!!.child(firebaseUser!!.uid + "jpg")
                val uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
                    if (it.isSuccessful) {
                        it.exception?.let { it1 ->
                            throw it1
                            progressDialog.dismiss()
                        }
                    }
                    return@Continuation fileRef.downloadUrl
                }).addOnCompleteListener {
                    if (it.isSuccessful) {
                        myUrl = it.result.toString()
                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullName"] =
                            fullName_profileSettings_frag.text.toString().toLowerCase()
                        userMap["userName"] =
                            userName_profileSettings_frag.text.toString().toLowerCase()
                        userMap["bio"] = bio_profileSettings_frag.text.toString().toLowerCase()
                        userMap["image"] = myUrl
                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        progressDialog.dismiss()
                        Toast.makeText(this, "Account Updated", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }.addOnFailureListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri
            profile_settings_imageView.setImageURI(imageUri)
        }
    }

    private fun updateUserInfoOnly() {

        when {
            fullName_profileSettings_frag.text.toString() == "" -> {
                Toast.makeText(
                        this@AccountSettingsActivity,
                        "Full name is Required",
                        Toast.LENGTH_LONG
                    )
                    .show()
                return
            }
            userName_profileSettings_frag.text.toString() == "" -> {
                Toast.makeText(
                        this@AccountSettingsActivity,
                        "User Name is Required",
                        Toast.LENGTH_LONG
                    )
                    .show()
                return
            }
            bio_profileSettings_frag.text.toString() == "" -> {
                Toast.makeText(this@AccountSettingsActivity, "Bio is Required", Toast.LENGTH_LONG)
                    .show()
                return
            }
            else -> {
                val userRef = FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullName"] = fullName_profileSettings_frag.text.toString().toLowerCase()
                userMap["userName"] = userName_profileSettings_frag.text.toString().toLowerCase()
                userMap["bio"] = bio_profileSettings_frag.text.toString().toLowerCase()
                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(this, "Account Updated", Toast.LENGTH_LONG).show()
                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    private fun userInfo() {

        val userRef =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user = p0.getValue(User::class.java)

                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_settings_imageView)

                    userName_profileSettings_frag.setText(user.getUserName())
                    fullName_profileSettings_frag.setText(user.getFullName())
                    bio_profileSettings_frag.setText(user.getBio())
                }
            }
        })
    }
}
