package com.example.project

import android.os.Bundle
import android.text.TextUtils
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class Registration_Screen : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextIdNumber: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var registerButton: Button
    private lateinit var spinnerRole: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration_screen)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        editTextName = findViewById(R.id.editTextName)
        editTextIdNumber = findViewById(R.id.editTextIdNumber)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        registerButton = findViewById(R.id.registerButton)
        spinnerRole = findViewById(R.id.spinnerRole)

        // Populate the spinner with roles
        ArrayAdapter.createFromResource(
            this,
            R.array.roles_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerRole.adapter = adapter
        }

        registerButton.setOnClickListener {
            val enteredName = editTextName.text.toString()
            val enteredIdNumber = editTextIdNumber.text.toString()
            val enteredEmail = editTextEmail.text.toString()
            val enteredPassword = editTextPassword.text.toString()
            val selectedRole = spinnerRole.selectedItem.toString()

            if (TextUtils.isEmpty(enteredName) ||
                TextUtils.isEmpty(enteredIdNumber) ||
                TextUtils.isEmpty(enteredEmail) ||
                TextUtils.isEmpty(enteredPassword)
            ) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create a user in Firebase Authentication
            auth.createUserWithEmailAndPassword(enteredEmail, enteredPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        // Insert user data into the Firestore with selected role
                        insertUserData(enteredName, enteredIdNumber, enteredEmail, selectedRole)
                        updateUI(user)
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
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Account creation success, navigate to the appropriate activity
            // For example:
            // val intent = Intent(this, NextActivity::class.java)
            // startActivity(intent)
            Toast.makeText(this, "Successfully Registered", Toast.LENGTH_SHORT).show()
            finish() // close the current activity
        }
    }

    private fun insertUserData(name: String, idNumber: String, email: String, role: String) {
        // Insert user data into the Firestore based on the selected role
        val userId = auth.currentUser?.uid ?: return

        val user = hashMapOf(
            "name" to name,
            "idNumber" to idNumber,
            "email" to email,
            "role" to role
            // Add other user-related data as needed
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                // Successfully added user data to Firestore
            }
            .addOnFailureListener { e ->
                Toast.makeText(baseContext, "Error adding user: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
