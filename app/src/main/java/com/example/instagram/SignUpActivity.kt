package com.example.instagram

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        login_link_btn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        signUp_btn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {

        val fullName = fullName_signUp.text.toString()
        val userName = userName_signUp.text.toString()
        val email = email_signUp.text.toString()
        val password = password_signUp.text.toString()

        when {
            fullName.isEmpty() -> {
                Toast.makeText(this, "Full name is Required", Toast.LENGTH_LONG).show()
                return
            }
            userName.isEmpty() -> {
                Toast.makeText(this, "User Name is Required", Toast.LENGTH_LONG).show()
                return
            }
            email.isEmpty() -> {
                Toast.makeText(this, "Email is Required", Toast.LENGTH_LONG).show()
                return
            }
            password.isEmpty() -> {
                Toast.makeText(this, "Password is Required", Toast.LENGTH_LONG).show()
                return
            }
            else -> {
                Log.d("SignUpActivity", "Email is $email")
                Log.d("SignUpActivity", "Password is $password ")

                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("Sign Up")
                progressDialog.setMessage("Please wait")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d("SignUpActivity", "addOnCompleteListener")
                            saveUserInfo(fullName, userName, email, progressDialog)
                        }
                    }
                    .addOnFailureListener {
                        Log.d("SignUpActivity", "addOnFailureListener")
                        Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
                        progressDialog.dismiss()
                    }
            }
        }
    }

    private fun saveUserInfo(
        fullName: String,
        userName: String,
        email: String,
        progressDialog: ProgressDialog
    ) {
        Log.d("SignUpActivity", "saveUserInfo")
        val uid = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = uid
        userMap["fullName"] = fullName.toLowerCase()
        userMap["userName"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "Hey , i am using Saqr Instagram App."
        userMap["image"] =
            "https://firebasestorage.googleapis.com/v0/b/instgram-9b3b1.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=f9c4e366-83a7-436c-b63e-b2c867b413ad"

        ref.child(uid).setValue(userMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("SignUpActivity", "saveUserInfo CompleteListener")
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account Created", Toast.LENGTH_LONG).show()

                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(uid)
                        .child("Following").child(uid).setValue(true)


                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
            }
            .addOnFailureListener {
                Log.d("SignUpActivity", "saveUserInfo FailureListener")
                Toast.makeText(this, "${it.message}", Toast.LENGTH_LONG).show()
                progressDialog.dismiss()
            }

    }
}
