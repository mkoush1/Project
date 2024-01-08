package com.example.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class TicketDetailsActivity : AppCompatActivity() {

    private lateinit var spinnerEmployee: Spinner
    private lateinit var btnSave: Button

    private lateinit var ticketTitle: String
    private lateinit var ticketStatus: String
    private lateinit var ticketClient: String
    private lateinit var currentEmployeeId: String

    // Firebase
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Map to store employee names and their corresponding IDs
    private val employeeNameIdMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_details)

        // Retrieve data from the intent
        ticketTitle = intent.getStringExtra(EXTRA_TICKET_TITLE) ?: ""
        ticketStatus = intent.getStringExtra(EXTRA_TICKET_STATUS) ?: ""
        ticketClient = intent.getStringExtra(EXTRA_TICKET_CLIENT) ?: ""
        currentEmployeeId = intent.getStringExtra(EXTRA_TICKET_EMPLOYEE) ?: ""

        // Initialize UI elements
        val tvTitle: TextView = findViewById(R.id.tvTitle)
        val tvStatus: TextView = findViewById(R.id.tvStatus)
        val tvClient: TextView = findViewById(R.id.tvClient)
        val tvEmployee: TextView = findViewById(R.id.tvEmployee)
        spinnerEmployee = findViewById(R.id.spinnerEmployee)
        btnSave = findViewById(R.id.btnSave)

        // Set text for TextViews
        tvTitle.text = ticketTitle
        tvStatus.text = ticketStatus
        tvClient.text = ticketClient
        tvEmployee.text = currentEmployeeId

        // Fetch employee data for the Spinner
        fetchEmployeeData()

        // Set click listener for Save button
        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    // Function to fetch employee data from Firestore
    private fun fetchEmployeeData() {
        firestore.collection("users")
            .whereEqualTo("role", "employee")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val employeeName = document.getString("name") ?: ""
                    val employeeId = document.id
                    employeeNameIdMap[employeeName] = employeeId
                }
                setupEmployeeSpinner()
            }
            .addOnFailureListener { exception ->
                // Handle errors
                Toast.makeText(this, "Error fetching employee data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to set up the Spinner with employee names
    private fun setupEmployeeSpinner() {
        val employees = employeeNameIdMap.keys.toList()
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, employees)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEmployee.adapter = adapter

        // Set the current employee as the selected item in the Spinner
        val currentPosition = employees.indexOf(currentEmployeeId)
        if (currentPosition != -1) {
            spinnerEmployee.setSelection(currentPosition)
        }
    }

    // Function to save changes when the Save button is clicked
    private fun saveChanges() {
        val selectedEmployeeName = spinnerEmployee.selectedItem.toString()

        // Update the employee information in the UI
        findViewById<TextView>(R.id.tvEmployee).text = selectedEmployeeName

        // Find the corresponding employee ID from the map
        val selectedEmployeeId = employeeNameIdMap[selectedEmployeeName] ?: ""

        // Update the assignedTo field in Firestore
        val ticketRef = firestore.collection("Tickets").whereEqualTo("title", ticketTitle)
        ticketRef.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentId = querySnapshot.documents[0].id
                    firestore.collection("Tickets").document(documentId)
                        .update("assignedTo", selectedEmployeeId)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Ticket updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error updating ticket: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching ticket: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        // Finish the activity
        finish()
    }

    companion object {
        // Companion object to create an Intent with extras for starting this activity
        fun newIntent(context: Context, title: String, status: String, client: String, employeeId: String?): Intent {
            return Intent(context, TicketDetailsActivity::class.java).apply {
                putExtra(EXTRA_TICKET_TITLE, title)
                putExtra(EXTRA_TICKET_STATUS, status)
                putExtra(EXTRA_TICKET_CLIENT, client)
                putExtra(EXTRA_TICKET_EMPLOYEE, employeeId)
            }
        }
    }
}
