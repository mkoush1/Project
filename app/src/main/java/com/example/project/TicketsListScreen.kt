package com.example.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

const val EXTRA_TICKET_TITLE = "extra_ticket_title"
const val EXTRA_TICKET_STATUS = "extra_ticket_status"
const val EXTRA_TICKET_CLIENT = "extra_ticket_client"
const val EXTRA_TICKET_EMPLOYEE = "extra_ticket_employee"

class TicketsListScreen : AppCompatActivity() {

    private lateinit var recyclerViewTickets: RecyclerView
    private lateinit var ticketAdapter: TicketAdapter
    private val ticketList = ArrayList<Ticket>()

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tickets_list_screen)

        recyclerViewTickets = findViewById(R.id.recyclerViewTickets)
        val btnFilterTickets: Button = findViewById(R.id.btnFilterTickets)

        ticketAdapter = TicketAdapter(ticketList, this)
        recyclerViewTickets.layoutManager = LinearLayoutManager(this)
        recyclerViewTickets.adapter = ticketAdapter

        fetchTickets()

        btnFilterTickets.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun fetchTickets() {
        firestore.collection("Tickets")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val title = document.getString("title") ?: ""
                    val status = document.getString("status") ?: ""
                    val clientId = document.getString("createdBy") ?: ""
                    val employeeId = document.getString("assignedTo")

                    fetchUserDetails(clientId, "user") { clientName ->
                        fetchUserDetails(employeeId, "employee") { employeeName ->
                            ticketList.add(Ticket(title, status, clientName, employeeName))
                            ticketAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching tickets: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserDetails(userId: String?, userType: String, callback: (String) -> Unit) {
        if (userId != null && userId.isNotBlank()) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    val userName = userDoc.getString("name") ?: "Unknown $userType"
                    callback(userName)
                }
                .addOnFailureListener {
                    callback("Unknown $userType")
                }
        } else {
            callback("Unknown $userType")
        }
    }

    private fun fetchDistinctValues(collection: String, field: String, roleFilter: String? = null, callback: (List<String>) -> Unit) {
        firestore.collection(collection)
            .get()
            .addOnSuccessListener { result ->
                val distinctValues = result.documents
                    .filter { doc ->
                        roleFilter.isNullOrBlank() || (doc.getString("role") == roleFilter)
                    }
                    .mapNotNull { it.getString(field) }
                callback(distinctValues.distinct())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error fetching distinct $field: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Inside your showFilterDialog() function:

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter_tickets, null)

        val spinnerStatus: Spinner = dialogView.findViewById(R.id.spinnerStatus)
        val spinnerEmployee: Spinner = dialogView.findViewById(R.id.spinnerEmployee)
        val spinnerClient: Spinner = dialogView.findViewById(R.id.spinnerClient)

        fetchDistinctValues("Tickets", "status") { distinctStatus ->
            fetchDistinctValues("users", "name", roleFilter = "user") { distinctUsers ->
                fetchDistinctValues("users", "name", roleFilter = "employee") { distinctEmployees ->
                    val statusArray = mutableListOf("All").apply { addAll(distinctStatus) }
                    val userArray = mutableListOf("All").apply { addAll(distinctUsers) }
                    val employeeArray = mutableListOf("All").apply { addAll(distinctEmployees) }

                    val adapters = listOf(
                        ArrayAdapter(this, android.R.layout.simple_spinner_item, statusArray),
                        ArrayAdapter(this, android.R.layout.simple_spinner_item, userArray),
                        ArrayAdapter(this, android.R.layout.simple_spinner_item, employeeArray)
                    )

                    adapters.forEachIndexed { index, adapter ->
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        when (index) {
                            0 -> spinnerStatus.adapter = adapter
                            1 -> spinnerEmployee.adapter = adapter
                            2 -> spinnerClient.adapter = adapter
                        }
                    }
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Filter Tickets")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                val statusFilter = spinnerStatus.selectedItem.toString()
                val employeeFilter = spinnerEmployee.selectedItem.toString()
                val clientFilter = spinnerClient.selectedItem.toString()

                filterTickets(statusFilter, employeeFilter, clientFilter)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }


    private fun filterTickets(statusFilter: String, employeeFilter: String, clientFilter: String) {
        val filteredTickets = ticketList.filter { ticket ->
            val statusMatch = statusFilter.equals("All", ignoreCase = true) || ticket.status.equals(statusFilter, ignoreCase = true)
            val clientMatch = clientFilter.equals("All", ignoreCase = true) || ticket.client.equals(clientFilter, ignoreCase = true)
            val employeeMatch = employeeFilter.equals("All", ignoreCase = true) || ticket.employee?.equals(employeeFilter, ignoreCase = true) ?: false


            statusMatch && clientMatch && employeeMatch
        }

        ticketAdapter.updateList(filteredTickets)
    }





    private class TicketAdapter(
        private var ticketList: List<Ticket>,
        private val context: Context
    ) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.ticket_card, parent, false)
            return TicketViewHolder(view)
        }

        override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
            val ticket = ticketList[position]
            holder.bind(ticket)

            holder.itemView.setOnClickListener {
                val intent = Intent(context, TicketDetailsActivity::class.java).apply {
                    putExtra(EXTRA_TICKET_TITLE, ticket.title)
                    putExtra(EXTRA_TICKET_STATUS, ticket.status)
                    putExtra(EXTRA_TICKET_CLIENT, ticket.client)
                    putExtra(EXTRA_TICKET_EMPLOYEE, ticket.employee)
                }
                context.startActivity(intent)
            }
        }

        override fun getItemCount(): Int = ticketList.size

        fun updateList(newList: List<Ticket>) {
            ticketList = newList
            notifyDataSetChanged()
        }

        class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
            private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
            private val tvClient: TextView = itemView.findViewById(R.id.tvClient)
            private val tvEmployee: TextView = itemView.findViewById(R.id.tvEmployee)

            fun bind(ticket: Ticket) {
                tvTitle.text = ticket.title
                tvStatus.text = ticket.status
                tvClient.text = ticket.client
                tvEmployee.text = ticket.employee ?: "No Employee Assigned"
            }
        }
    }

    data class Ticket(
        val title: String,
        val status: String,
        val client: String,
        val employee: String?
    )
}
