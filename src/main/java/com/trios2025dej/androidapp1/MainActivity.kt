package com.trios2025dej.androidapp1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.trios2025dej.androidapp1.ui.theme.AndroidApp1Theme

// ------------------ DATA CLASS -----------------------
data class Expense(
    val id: Int,
    val amount: Double,
    val note: String,
    val category: String
)

// ------------------ MAIN ACTIVITY --------------------
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidApp1Theme {
                AppRoot()
            }
        }
    }
}

// ------------------ ROOT APP (HOME + CHART) ---------
@Composable
fun AppRoot() {
    var showChart by remember { mutableStateOf(false) }
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var monthlyBudget by remember { mutableStateOf("") }

    if (showChart) {
        CategoryChartScreen(
            expenses = expenses,
            monthlyBudget = monthlyBudget.toDoubleOrNull() ?: 0.0,
            onBack = { showChart = false }
        )
    } else {
        ExpenseScreen(
            monthlyBudget = monthlyBudget,
            onBudgetChange = { monthlyBudget = it },
            expenses = expenses,
            onExpensesChange = { expenses = it },
            onShowChart = { showChart = true }
        )
    }
}

// ------------------ MAIN EXPENSE SCREEN --------------
@Composable
fun ExpenseScreen(
    monthlyBudget: String,
    onBudgetChange: (String) -> Unit,
    expenses: List<Expense>,
    onExpensesChange: (List<Expense>) -> Unit,
    onShowChart: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }

    val categories = listOf("General", "Food", "Transport", "Bills", "Shopping", "Other")
    var expanded by remember { mutableStateOf(false) }

    var editingId by remember { mutableStateOf<Int?>(null) }

    val total = expenses.sumOf { it.amount }
    val budgetValue = monthlyBudget.toDoubleOrNull() ?: 0.0
    val remaining = budgetValue - total

    Scaffold(
        topBar = {
            // Custom top bar WITHOUT experimental APIs
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Expense Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
        ) {
            // ---------------- BUDGET ----------------------
            Text(
                text = "Monthly Budget",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            OutlinedTextField(
                value = monthlyBudget,
                onValueChange = onBudgetChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter monthly budget") }
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onBudgetChange("") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Reset Monthly Budget")
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Total Spent: $${"%.2f".format(total)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Remaining: $${"%.2f".format(remaining)}",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = if (remaining < 0) Color.Red else Color(0xFF006400)
            )

            Spacer(Modifier.height(20.dp))

            // ---------------- ADD / EDIT EXPENSE ----------------
            Text(
                text = if (editingId == null) "Add New Expense" else "Edit Expense",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
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

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val a = amount.toDoubleOrNull()
                    if (a != null) {
                        if (editingId == null) {
                            // Add new
                            onExpensesChange(
                                expenses + Expense(
                                    id = (expenses.maxOfOrNull { it.id } ?: 0) + 1,
                                    amount = a,
                                    note = note,
                                    category = category
                                )
                            )
                        } else {
                            // Save edit
                            onExpensesChange(
                                expenses.map {
                                    if (it.id == editingId) it.copy(
                                        amount = a,
                                        note = note,
                                        category = category
                                    ) else it
                                }
                            )
                            editingId = null
                        }
                        // Clear form
                        amount = ""
                        note = ""
                        category = "General"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editingId == null) "Add Expense" else "Save Changes")
            }

            Spacer(Modifier.height(16.dp))

            // ----------- VIEW CHART BUTTON ----------------
            OutlinedButton(
                onClick = onShowChart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.BarChart, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("View Spending Chart")
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- LIST -------------------------
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn {
                items(expenses, key = { it.id }) { e ->
                    ExpenseCard(
                        e = e,
                        onEdit = {
                            editingId = e.id
                            amount = e.amount.toString()
                            note = e.note
                            category = e.category
                        },
                        onDelete = {
                            onExpensesChange(expenses.filter { it.id != e.id })
                            if (editingId == e.id) {
                                editingId = null
                                amount = ""
                                note = ""
                                category = "General"
                            }
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// ------------------ EXPENSE CARD ---------------------
@Composable
fun ExpenseCard(
    e: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$${"%.2f".format(e.amount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = e.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }

            if (e.note.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text(e.note, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// ------------------ BAR CHART SCREEN -----------------
@Composable
fun CategoryChartScreen(
    expenses: List<Expense>,
    monthlyBudget: Double,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onBack) {
                        Text(
                            text = "Back",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Spending Chart",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .padding(16.dp)
        ) {
            val totalsByCategory = expenses
                .groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            val grandTotal = totalsByCategory.values.sum()

            Text(
                text = "Monthly Budget: $${"%.2f".format(monthlyBudget)}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Total Spent: $${"%.2f".format(grandTotal)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(16.dp))

            if (grandTotal <= 0.0) {
                Text("No expenses yet to show chart.")
            } else {
                LazyColumn {
                    items(totalsByCategory.toList(), key = { it.first }) { (category, total) ->
                        val percent = (total / grandTotal * 100.0).toFloat()

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(category, fontWeight = FontWeight.Bold)
                                Text("${"%.1f".format(percent)}%")
                            }

                            Spacer(Modifier.height(4.dp))

                            // Simple bar using Boxes – no Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(16.dp)
                                    .background(
                                        color = Color.LightGray.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percent / 100f)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                            }

                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}
