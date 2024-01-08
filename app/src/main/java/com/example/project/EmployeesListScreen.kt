package com.example.project

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class EmployeesListScreen : AppCompatActivity() {

    private lateinit var recyclerViewEmployees: RecyclerView
    private lateinit var employeeAdapter: EmployeeAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employees_list_screen)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerViewEmployees = findViewById(R.id.recyclerViewEmployees)
        val btnAddEmployee: Button = findViewById(R.id.btnAddEmployee)

        employeeAdapter = EmployeeAdapter()
        recyclerViewEmployees.layoutManager = LinearLayoutManager(this)
        recyclerViewEmployees.adapter = employeeAdapter

        btnAddEmployee.setOnClickListener {
            showAddEmployeeDialog()
        }

        // Fetch employees from Firestore
        fetchEmployees()
    }

    private fun fetchEmployees() {
        firestore.collection("users").whereEqualTo("role", "employee").addSnapshotListener { snapshot, _ ->
            val employees = mutableListOf<Employee>()

            snapshot?.documents?.forEach { document ->
                val name = document.getString("name")
                val email = document.getString("email")
                val idNumber = document.getString("idNumber")
                val role = document.getString("role")

                if (name != null && email != null && idNumber != null && role != null) {
                    employees.add(Employee(name, email, idNumber, role))
                }
            }

            employeeAdapter.setEmployees(employees)
        }
    }

    private fun showAddEmployeeDialog() {
        // Inflating the layout for the custom dialog
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_employee, null)

        // Retrieving UI components from the dialog layout
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etIdNumber = dialogView.findViewById<EditText>(R.id.etIdNumber) // Corrected field

        // Creating an AlertDialog with the custom dialog layout
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add New Employee")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                // Add a new employee to Firestore with the default role "employee"
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val idNumber = etIdNumber.text.toString() // Corrected field
                val role = "employee" // Set the default role

                val newEmployee = Employee(name, email, idNumber, role)
                addEmployeeToFirestore(newEmployee)
            }
            .setNegativeButton("Cancel", null)
            .create()

        // Show the custom dialog
        dialog.show()
    }

    private fun addEmployeeToFirestore(employee: Employee) {
        firestore.collection("users").add(employee)
    }

    inner class EmployeeAdapter : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>() {
        private var employees = listOf<Employee>()

        fun setEmployees(newList: List<Employee>) {
            employees = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.employee_card, parent, false)
            return EmployeeViewHolder(view)
        }

        override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
            val employee = employees[position]
            holder.bind(employee)
        }

        override fun getItemCount(): Int {
            return employees.size
        }

        inner class EmployeeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvEmployeeName: TextView = itemView.findViewById(R.id.tvEmployeeName)
            private val tvEmployeeEmail: TextView = itemView.findViewById(R.id.tvEmployeeEmail)
            private val tvEmployeeIdNumber: TextView = itemView.findViewById(R.id.tvEmployeeIdNumber)

            fun bind(employee: Employee) {
                tvEmployeeName.text = employee.name
                tvEmployeeEmail.text = employee.email
                tvEmployeeIdNumber.text = employee.idNumber
            }
        }
    }

    data class Employee(val name: String, val email: String, val idNumber: String, val role: String)
}
