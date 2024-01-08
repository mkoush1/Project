package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class ClientsListScreen : AppCompatActivity() {

    // UI elements
    private lateinit var recyclerViewClients: RecyclerView
    private lateinit var clientAdapter: ClientAdapter
    private val clientList = ArrayList<Client>()

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clients_list_screen)

        // Initialize RecyclerView and its adapter
        recyclerViewClients = findViewById(R.id.recyclerViewClients)

        // Set up RecyclerView with the ClientAdapter
        clientAdapter = ClientAdapter(clientList)
        recyclerViewClients.layoutManager = LinearLayoutManager(this)
        recyclerViewClients.adapter = clientAdapter

        // Fetch clients from Firestore with the "user" role
        fetchClientsFromFirestore()
    }

    private fun fetchClientsFromFirestore() {
        firestore.collection("users").whereEqualTo("role", "user").get()
            .addOnSuccessListener { querySnapshot: QuerySnapshot ->
                val clients = querySnapshot.documents.mapNotNull { document ->
                    val name = document.getString("name")
                    val email = document.getString("email")
                    val phone = document.getString("idNumber")
                    if (name != null && email != null && phone != null) {
                        Client(name, email, phone)
                    } else {
                        null
                    }
                }
                clientList.addAll(clients)
                clientAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle errors
                // You might want to add proper error handling here
            }
    }

    // RecyclerView adapter for clients
    private class ClientAdapter(private val clientList: List<Client>) :
        RecyclerView.Adapter<ClientAdapter.ClientViewHolder>() {

        // Create ViewHolder for each client item
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.client_card, parent, false)
            return ClientViewHolder(view)
        }

        // Bind data to the ViewHolder
        override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
            val client = clientList[position]
            holder.bind(client)
        }

        // Return the number of items in the list
        override fun getItemCount(): Int {
            return clientList.size
        }

        // ViewHolder for client items
        class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            // UI elements in the client card layout
            private val tvClientName: TextView = itemView.findViewById(R.id.tvClientName)
            private val tvClientEmail: TextView = itemView.findViewById(R.id.tvClientEmail)
            private val tvClientPhone: TextView = itemView.findViewById(R.id.tvClientId)

            // Bind data to the ViewHolder
            fun bind(client: Client) {
                tvClientName.text = client.name
                tvClientEmail.text = client.email
                tvClientPhone.text = client.phone
            }
        }
    }

    // Data class to represent client information
    data class Client(val name: String, val email: String, val phone: String)
}
