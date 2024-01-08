package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class ClientDashboardActivity : AppCompatActivity() {

    private lateinit var ticketList: MutableList<String>
    private lateinit var ticketListView: ListView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_dashboard)

        firestore = FirebaseFirestore.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        ticketList = mutableListOf()
        ticketListView = findViewById(R.id.ticketListView)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ticketList)
        ticketListView.adapter = adapter

        val addTicketButton: Button = findViewById(R.id.addTicketButton)
        addTicketButton.setOnClickListener {
            showAddTicketDialog()
        }

        loadTicketsFromFirestore()

        ticketListView.setOnItemClickListener { _, _, position, _ ->
            getTicketId(position) // Now handled inside getTicketId
        }
    }

    private fun loadTicketsFromFirestore() {
        firestore.collection("Tickets")
            .whereEqualTo("createdBy", currentUser)
            .get()
            .addOnSuccessListener { querySnapshot ->
                ticketList.clear()
                for (document in querySnapshot.documents) {
                    val title = document.getString("title")
                    val description = document.getString("description")

                    if (title != null && description != null) {
                        val ticketString = "$title - $description"
                        ticketList.add(ticketString)
                    }
                }
                val adapter = ticketListView.adapter as ArrayAdapter<String>
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                showToast("Failed to load tickets: ${it.message}")
            }
    }

    private fun showAddTicketDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_ticket, null)

        val titleEditText: EditText = dialogView.findViewById(R.id.titleEditText)
        val descriptionEditText: EditText = dialogView.findViewById(R.id.descriptionEditText)
        val submitTicketButton: Button = dialogView.findViewById(R.id.submitTicketButton)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        submitTicketButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()

            addTicketToFirestore(title, description)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addTicketToFirestore(title: String, description: String) {
        val ticketId = generateRandomTicketId()

        // Retrieve user name from Firestore
        firestore.collection("users")
            .document(currentUser)
            .get()
            .addOnSuccessListener { userDocument ->
                if (userDocument.exists()) {
                    val userName = userDocument.getString("name") ?: "Unknown User"

                    // Create ticket data
                    val ticket = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "createdBy" to currentUser,
                        "status" to "open",
                        "assignedTo" to "",
                        "userName" to userName
                    )

                    // Add the ticket to Firestore
                    firestore.collection("Tickets").document(ticketId)
                        .set(ticket)
                        .addOnSuccessListener {
                            loadTicketsFromFirestore()
                        }
                        .addOnFailureListener { e ->
                            showToast("Failed to add ticket: ${e.message}")
                        }
                } else {
                    showToast("User document not found.")
                }
            }
            .addOnFailureListener {
                showToast("Failed to retrieve user data: ${it.message}")
            }
    }

    private fun getTicketId(position: Int) {
        if (position < ticketList.size) {
            val ticketString = ticketList[position]
            val parts = ticketString.split(" - ")
            val title = parts[0]
            val description = parts[1]

            firestore.collection("Tickets")
                .whereEqualTo("title", title)
                .whereEqualTo("description", description)
                .whereEqualTo("createdBy", currentUser)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val document = querySnapshot.documents.first()
                        val ticketId = document.id
                        startTicketDetailActivity(ticketId)
                    }
                }
                .addOnFailureListener {
                    showToast("Failed to get ticket ID: ${it.message}")
                }
        }
    }

    private fun generateRandomTicketId(): String {
        return UUID.randomUUID().toString()
    }

    private fun showToast(message: String) {
        // Handle showing toast message
    }

    private fun startTicketDetailActivity(ticketId: String) {
        val intent = Intent(this, ClientTicketDetailActivity::class.java)
        intent.putExtra("ticketId", ticketId)
        startActivity(intent)
    }
}
