package com.example.project

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var signInButton: Button
    private lateinit var signInTextView: TextView
    private lateinit var createAccountTextView: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        editTextEmail = findViewById(R.id.editTextTextEmailAddress)
        editTextPassword = findViewById(R.id.editTextTextPassword2)
        signInButton = findViewById(R.id.SignInButton)
        signInTextView = findViewById(R.id.SignTo_textView)
        createAccountTextView = findViewById(R.id.CreateAcc_textView)

        signInButton.setOnClickListener {
            val enteredEmail = editTextEmail.text.toString()
            val enteredPassword = editTextPassword.text.toString()

            if (TextUtils.isEmpty(enteredEmail) || TextUtils.isEmpty(enteredPassword)) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Sign in the user using Firebase authentication
            auth.signInWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        checkUserRole(user?.uid)
                    } else {
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        updateUI(null)
                    }
                }
        }

        createAccountTextView.setOnClickListener {
            val intent = Intent(this, Registration_Screen::class.java)
            startActivity(intent)
        }
    }

    private fun checkUserRole(userId: String?) {
        if (userId == null) {
            Toast.makeText(this, "User ID is null.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document: DocumentSnapshot? = task.result
                    if (document != null && document.exists()) {
                        val role = document.getString("role")
                        if (role != null) {
                            handleUserRole(role)
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Role not found for the user.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.d("Firestore", "No such document")
                        // User not found in 'users' collection, check other collections if needed
                        // ...
                    }
                } else {
                    Log.d("Firestore", "get failed with ", task.exception)
                    Toast.makeText(
                        this@MainActivity,
                        "Firestore error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun handleUserRole(role: String) {
        when (role) {
            "user" -> {
                // Start ClientDashboardActivity for users
                val intent = Intent(this@MainActivity, ClientDashboardActivity::class.java)
                startActivity(intent)
            }
            "manager" -> {
                // Start ManagerBoardActivity for managers
                val intent = Intent(this@MainActivity, ManagerBoardActivity::class.java)
                startActivity(intent)
            }
            "employee" -> {
                // Start EmployeeDashboardActivity for employees
                val intent = Intent(this@MainActivity, EmployeeDashboardActivity::class.java)
                startActivity(intent)
            }
            else -> {
                // Handle unknown role or customize as needed
                Toast.makeText(this@MainActivity, "Unknown role: $role", Toast.LENGTH_SHORT).show()
            }
        }
        finish() // Close the current activity
    }

    private fun updateUI(user: FirebaseUser?) {
        // Handle UI updates if needed
    }
}
