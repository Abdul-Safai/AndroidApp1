package com.trios2025dej.androidapp1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import com.trios2025dej.androidapp1.ui.theme.AndroidApp1Theme

// ----------------------
//  DATA CLASS
// ----------------------
data class Expense(
    val id: Int,
    var amount: Double,
    var note: String,
    var category: String
)

// ----------------------
//  MAIN ACTIVITY
// ----------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var darkMode by remember { mutableStateOf(false) }

            AndroidApp1Theme(darkTheme = darkMode) {
                ExpenseTrackerScreen(
                    darkMode = darkMode,
                    onToggleDark = { darkMode = !darkMode }
                )
            }
        }
    }
}

// ----------------------
//  MAIN SCREEN
// ----------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(darkMode: Boolean, onToggleDark: () -> Unit) {

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    val categories = listOf("General", "Food", "Transport", "Bills", "Shopping", "Other")
    var expanded by remember { mutableStateOf(false) }

    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var editMode by remember { mutableStateOf<Expense?>(null) }

    val total = expenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                actions = {
                    IconButton(onClick = { onToggleDark() }) {
                        Icon(
                            imageVector = if (darkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {

            // Total Amount
            Text(
                text = "Total: $${String.format("%.2f", total)}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Dropdown
            Box {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { expanded = true }
                ) {
                    Text("Category: $category")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    categories.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                category = it
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ADD / SAVE BUTTON
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null) {
                        if (editMode == null) {
                            expenses = expenses + Expense(
                                id = expenses.size + 1,
                                amount = amt,
                                note = note,
                                category = category
                            )
                        } else {
                            val updated = editMode!!.copy(
                                amount = amt,
                                note = note,
                                category = category
                            )
                            expenses = expenses.map { if (it.id == updated.id) updated else it }
                            editMode = null
                        }

                        amount = ""
                        note = ""
                        category = "General"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editMode == null) "Add Expense" else "Save Changes")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // EXPENSE LIST
            LazyColumn {
                items(expenses) { exp ->
                    ExpenseCard(
                        expense = exp,
                        onDelete = {
                            expenses = expenses.filter { it.id != exp.id }
                        },
                        onEdit = {
                            editMode = exp
                            amount = exp.amount.toString()
                            note = exp.note
                            category = exp.category
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

// ----------------------
//  EXPENSE CARD
// ----------------------
@Composable
fun ExpenseCard(expense: Expense, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Amount: $${String.format("%.2f", expense.amount)}",
                style = MaterialTheme.typography.titleLarge
            )

            Text("Category: ${expense.category}", style = MaterialTheme.typography.bodyMedium)

            if (expense.note.isNotEmpty()) {
                Text("Note: ${expense.note}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
