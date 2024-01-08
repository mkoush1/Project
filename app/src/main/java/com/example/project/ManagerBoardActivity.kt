package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class ManagerBoardActivity : AppCompatActivity() {

    // Properties to store references to ImageView elements
    private lateinit var imageViewEmployees: ImageView
    private lateinit var imageViewClients: ImageView
    private lateinit var imageViewTickets: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manager_board)

        // Initialize references to ImageView elements
        imageViewEmployees = findViewById(R.id.imageView)
        imageViewClients = findViewById(R.id.imageView2)
        imageViewTickets = findViewById(R.id.imageView3)

        // Set up click listeners for ImageView elements

        // Click listener for the Employees ImageView
        imageViewEmployees.setOnClickListener {
            // Create an intent to navigate to the EmployeesListScreen
            val intent = Intent(this, EmployeesListScreen::class.java)

            // Start the EmployeesListScreen activity
            startActivity(intent)
        }

        // Click listener for the Clients ImageView
        imageViewClients.setOnClickListener {
            // Create an intent to navigate to the ClientsListScreen
            val intent = Intent(this, ClientsListScreen::class.java)

            // Start the ClientsListScreen activity
            startActivity(intent)
        }

        // Click listener for the Tickets ImageView
        imageViewTickets.setOnClickListener {
            // Create an intent to navigate to the TicketsListScreen
            val intent = Intent(this, TicketsListScreen::class.java)

            // Start the TicketsListScreen activity
            startActivity(intent)
        }
    }
}
