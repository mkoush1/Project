package com.example.project

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EmployeeTicketDetailsActivity : AppCompatActivity() {

    // UI elements
    private lateinit var commentEditText: EditText
    private lateinit var submitCommentButton: Button
    private lateinit var ticketCommentsTextView: TextView
    private lateinit var statusSpinner: Spinner
    private lateinit var ticketId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_ticket_details)

        // Retrieve the ticket ID from the intent
        ticketId = intent.getStringExtra("ticketId") ?: ""

        // Check if the ticket ID is valid
        if (ticketId.isNotEmpty()) {
            // Load comments for the ticket
            loadComments(ticketId)

            // Initialize UI elements for comments
            commentEditText = findViewById(R.id.commentEditText)
            submitCommentButton = findViewById(R.id.submitCommentButton)

            // Listener for submit comment button
            submitCommentButton.setOnClickListener {
                val newComment = commentEditText.text.toString()
                addComment(newComment)
                commentEditText.text.clear()
            }

            // Initialize status spinner
            statusSpinner = findViewById(R.id.statusSpinner)

            // Load ticket details
            loadTicketDetailsFromFirestore()

            // Load initial comments
            loadComments(ticketId)

            // Set listener for spinner changes
            statusSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    // Update ticket status in Firestore only when a different status is selected
                    val newStatus = statusSpinner.getItemAtPosition(position).toString()
                    updateTicketStatus(ticketId, newStatus)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
        }
    }

    private fun loadTicketDetailsFromFirestore() {
        FirebaseFirestore.getInstance().collection("Tickets")
            .document(ticketId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val ticketTitle = documentSnapshot.getString("title") ?: ""
                    val ticketDescription = documentSnapshot.getString("description") ?: ""
                    val ticketStatus = documentSnapshot.getString("status") ?: ""

                    // Update UI with ticket details
                    updateUITicketDetails(ticketTitle, ticketDescription, ticketStatus)

                    // Set the selected item in the spinner
                    val statusAdapter = ArrayAdapter.createFromResource(
                        this,
                        R.array.ticket_statuses,
                        android.R.layout.simple_spinner_item
                    )
                    statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    statusSpinner.adapter = statusAdapter
                    val position = statusAdapter.getPosition(ticketStatus)
                    statusSpinner.setSelection(position)
                } else {
                    showToast("Ticket details not found.")
                    finish()
                }
            }
            .addOnFailureListener {
                showToast("Failed to load ticket details: ${it.message}")
                finish()
            }
    }

    private fun updateUITicketDetails(title: String, description: String, status: String) {
        val titleTextView: TextView = findViewById(R.id.ticketTitleTextView)
        val descriptionTextView: TextView = findViewById(R.id.ticketDescriptionTextView)
        val statusTextView: TextView = findViewById(R.id.ticketStatusTextView)

        titleTextView.text = title
        descriptionTextView.text = description
        statusTextView.text = "Status: $status"
    }

    private fun loadComments(ticketId: String) {
        FirebaseFirestore.getInstance().collection("Comments")
            .whereEqualTo("ticketId", ticketId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val comments = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val comment = document.getString("comment") ?: ""
                    comments.add(comment)
                }
                displayComments(comments)
            }
            .addOnFailureListener {
                showToast("Failed to load comments: ${it.message}")
            }
    }

    private fun addComment(newComment: String) {
        val commentData = hashMapOf(
            "comment" to newComment,
            "ticketId" to ticketId
        )

        FirebaseFirestore.getInstance().collection("Comments")
            .add(commentData)
            .addOnSuccessListener {
                // Comment added successfully
                loadComments(ticketId) // Reload comments
            }
            .addOnFailureListener {
                showToast("Failed to add comment: ${it.message}")
            }
    }

    private fun displayComments(comments: List<String>) {
        ticketCommentsTextView = findViewById(R.id.ticketCommentsTextView)
        ticketCommentsTextView.text = comments.joinToString("\n")
    }

    private fun updateTicketStatus(ticketId: String, newStatus: String) {
        val ticketData = hashMapOf("status" to newStatus)

        FirebaseFirestore.getInstance().collection("Tickets")
            .document(ticketId)
            .update(ticketData as Map<String, Any>)
            .addOnSuccessListener {
                showToast("Ticket status updated to $newStatus")
            }
            .addOnFailureListener {
                showToast("Failed to update ticket status: ${it.message}")
            }
    }
}
