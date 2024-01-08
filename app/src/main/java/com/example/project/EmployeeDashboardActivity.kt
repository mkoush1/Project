package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Data class to represent ticket information
data class TicketData(
    val id: String,
    val title: String,
    val status: String,
    var clientName: String // Change to a mutable property
)

// Extension function to show a toast message
fun AppCompatActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

class EmployeeDashboardActivity : AppCompatActivity() {

    private lateinit var currentUser: String
    private lateinit var assignedTicketsList: MutableList<TicketData>
    private lateinit var ticketsRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_dashboard)

        // Initialize UI elements
        val welcomeTextView: TextView = findViewById(R.id.welcomeTextView)
        ticketsRecyclerView = findViewById(R.id.ticketsRecyclerView)

        // Get the current user ID (employee ID)
        currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Set welcome text
        welcomeTextView.text = "My Tickets"

        // Initialize the list to hold assigned tickets
        assignedTicketsList = mutableListOf()

        // Set up the RecyclerView and its adapter
        val adapter = TicketAdapter(assignedTicketsList)
        ticketsRecyclerView.adapter = adapter
        ticketsRecyclerView.layoutManager = LinearLayoutManager(this)

        // Set click listener for RecyclerView items
        adapter.setOnItemClickListener { position ->
            val intent = Intent(this, EmployeeTicketDetailsActivity::class.java)
            intent.putExtra("ticketId", assignedTicketsList[position].id)
            startActivity(intent)
        }

        // Load assigned tickets for the current employee
        loadAssignedTicketsFromFirestore()
    }

    private fun loadAssignedTicketsFromFirestore() {
        FirebaseFirestore.getInstance().collection("Tickets")
            .whereEqualTo("assignedTo", currentUser)
            .get()
            .addOnSuccessListener { querySnapshot ->
                assignedTicketsList.clear()
                for (document in querySnapshot.documents) {
                    val ticket = TicketData(
                        id = document.id,
                        title = document.getString("title") ?: "",
                        status = document.getString("status") ?: "",
                        clientName = document.getString("userName") ?: ""
                    )
                    assignedTicketsList.add(ticket)
                }
                ticketsRecyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                showToast("Failed to load assigned tickets: ${it.message}")
            }
    }

    // RecyclerView adapter for tickets
    inner class TicketAdapter(private val ticketList: List<TicketData>) :
        RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

        private var onItemClickListener: ((Int) -> Unit)? = null

        // Set the item click listener
        fun setOnItemClickListener(listener: (Int) -> Unit) {
            onItemClickListener = listener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
            // Inflate the ticket card layout
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ticket_card_layout, parent, false)
            return TicketViewHolder(view)
        }

        override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
            // Bind data to the ViewHolder
            val ticket = ticketList[position]
            holder.bind(ticket)

            // Set click listener for the item view
            holder.itemView.setOnClickListener {
                onItemClickListener?.invoke(position)
            }
        }

        override fun getItemCount(): Int = ticketList.size

        // ViewHolder for ticket items
        inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // UI elements in the ticket card layout
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
            private val clientTextView: TextView = itemView.findViewById(R.id.clientTextView)

            // Bind data to the ViewHolder
            fun bind(ticket: TicketData) {
                titleTextView.text = ticket.title
                statusTextView.text = "Status: ${ticket.status}"
                clientTextView.text = "Client: ${ticket.clientName}"
            }
        }
    }
}
