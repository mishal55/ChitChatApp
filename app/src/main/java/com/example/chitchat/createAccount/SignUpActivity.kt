package com.example.chitchat.createAccount

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.chitchat.MainActivity
import com.example.chitchat.databinding.ActivitySignUpBinding
import com.example.chitchat.models.Users
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var mauth : FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var progressDialog: ProgressDialog
    private lateinit var verify : Task<Void>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mauth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        supportActionBar?.hide()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Creating Account")
        progressDialog.setMessage("We are Creating you Account")

        binding.AlreadyHaveAccount.setOnClickListener {
            startActivity(Intent(this , SignInActivity::class.java))
        }

        binding.userSignUp.setOnClickListener {

            if(binding.userName.text.toString().isNotEmpty()
                &&
                binding.userEmail.text.toString().isNotEmpty()
                &&
                binding.userPassword.text.toString().isNotEmpty()){

                progressDialog.show()

                mauth.createUserWithEmailAndPassword(
                    binding.userEmail.text.toString(),
                    binding.userPassword.text.toString())
                    .addOnCompleteListener { task ->
                        progressDialog.dismiss()

                        if (task.isSuccessful){

                            verify = mauth.currentUser?.sendEmailVerification()
                                ?.addOnSuccessListener{

                                    Toast.makeText(
                                        this,
                                        "Please Verify your Email",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    val user = Users(
                                        binding.userName
                                            .text
                                            .toString(),
                                        binding.userEmail.text.toString(),
                                        binding.userPassword.text.toString()
                                    )

                                    val id = task.result.user?.uid

                                    if (id != null) {
                                        firebaseDatabase.reference
                                            .child("Users")
                                            .child(id)
                                            .setValue(user)
                                    }

                                    binding.userName.setText("")
                                    binding.userEmail.setText("")
                                    binding.userPassword.setText("")

                                    startActivity(Intent(this,
                                        VerifyEmail::class.java))
                                }
                                ?.addOnFailureListener{
                                    Toast.makeText(this ,
                                        "Exception${it.toString()}",
                                        Toast.LENGTH_SHORT)
                                        .show()
                                } as Task<Void>
                        }
                        else{
                            Toast.makeText(this ,
                                task.exception.toString(),
                                Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }
            else{
                if(binding.userName.text.toString().isEmpty()){
                    binding.userName.error = "Enter your name"
                    return@setOnClickListener
                }
                if (binding.userEmail.text.toString().isEmpty()){
                    binding.userEmail.error = "Enter your email"
                    return@setOnClickListener
                }

                if (binding.userPassword.text.toString().isEmpty()){
                    binding.userPassword.error = "Enter your password"
                    return@setOnClickListener
                }
            }
        }

        if (mauth.currentUser?.email?.isNotEmpty() == true){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}