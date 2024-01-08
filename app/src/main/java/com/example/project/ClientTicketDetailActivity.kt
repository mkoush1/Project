package com.example.project

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClientTicketDetailActivity : AppCompatActivity() {

    private lateinit var commentEditText: EditText
    private lateinit var submitCommentButton: Button
    private lateinit var ticketCommentsTextView: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: String
    private lateinit var ticketId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_ticket_detail)

        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        ticketId = intent.getStringExtra("ticketId") ?: ""

        commentEditText = findViewById(R.id.commentEditText)
        submitCommentButton = findViewById(R.id.submitCommentButton)
        ticketCommentsTextView = findViewById(R.id.ticketCommentsTextView)

        submitCommentButton.setOnClickListener {
            val newComment = commentEditText.text.toString()
            addCommentToFirestore(newComment)
            commentEditText.text.clear()
        }

        loadTicketDetailsFromFirestore()
        loadCommentsFromFirestore()
    }

    private fun loadTicketDetailsFromFirestore() {
        firestore.collection("Tickets")
            .document(ticketId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val ticketTitle = documentSnapshot.getString("title")
                    val ticketDescription = documentSnapshot.getString("description")
                    val ticketStatus = documentSnapshot.getString("status")

                    Log.d("TicketDetails", "Title: $ticketTitle, Description: $ticketDescription, Status: $ticketStatus")

                    // Update UI with ticket details
                    updateUITicketDetails(ticketTitle, ticketDescription, ticketStatus)
                } else {
                    // Document does not exist, handle accordingly
                    // For now, log an error message and finish the activity
                    Log.e("ClientTicketDetailActivity", "Ticket document does not exist.")
                    finish()
                }
            }
            .addOnFailureListener { e ->
                // Handle failures
                Log.e("ClientTicketDetailActivity", "Error loading ticket details", e)
                finish()
            }
    }

    private fun addCommentToFirestore(newComment: String) {
        val comment = hashMapOf(
            "comment" to newComment,
            "ticketId" to ticketId,
            "userId" to currentUser
        )

        firestore.collection("Comments")
            .add(comment)
            .addOnSuccessListener {
                loadCommentsFromFirestore()
            }
            .addOnFailureListener {
                // Handle failures
            }
    }

    private fun loadCommentsFromFirestore() {
        firestore.collection("Comments")
            .whereEqualTo("ticketId", ticketId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val comments = mutableListOf<String>()
                for (document in querySnapshot.documents) {
                    val comment = document.getString("comment")
                    if (comment != null) {
                        comments.add(comment)
                    }
                }
                displayComments(comments)
            }
            .addOnFailureListener {
                // Handle failures
            }
    }

    private fun displayComments(comments: List<String>) {
        val commentsTextView: TextView = findViewById(R.id.ticketCommentsTextView)
        commentsTextView.text = comments.joinToString("\n")
    }

    private fun updateUITicketDetails(title: String?, description: String?, status: String?) {
        // Update UI with ticket details (replace with your UI elements)
        val titleTextView: TextView = findViewById(R.id.ticketTitleTextView)
        val descriptionTextView: TextView = findViewById(R.id.ticketDescriptionTextView)
        val statusTextView: TextView = findViewById(R.id.ticketStatusTextView)

        titleTextView.text = title
        descriptionTextView.text = description
        statusTextView.text = "Status: $status"
    }
}
