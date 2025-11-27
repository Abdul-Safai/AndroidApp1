package com.trios2025dej.androidapp1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

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
        setContent { App() }
    }
}

// ------------------ ROOT APP -------------------------
@Composable
fun App() {
    var showChart by remember { mutableStateOf(false) }
    var expenses by remember { mutableStateOf(listOf<Expense>()) }
    var monthlyBudget by remember { mutableStateOf("") }

    MaterialTheme {
        if (showChart) {
            ChartScreen(
                expenses = expenses,
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
}

// ------------------ MAIN EXPENSE SCREEN --------------
@OptIn(ExperimentalMaterial3Api::class)
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

    val total = expenses.sumOf { it.amount }
    val budgetValue = monthlyBudget.toFloatOrNull() ?: 0f
    val remaining = budgetValue - total.toFloat()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Expense Tracker",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp)
        ) {
            // ---------------- BUDGET ----------------------
            Text("Monthly Budget", fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = monthlyBudget,
                onValueChange = onBudgetChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { onBudgetChange("") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Reset Monthly Budget")
            }

            Spacer(Modifier.height(14.dp))

            Text(
                "Remaining: $${"%.2f".format(remaining)}",
                fontWeight = FontWeight.Bold,
                color = if (remaining < 0) Color.Red else Color(0xFF006400)
            )

            Spacer(Modifier.height(20.dp))

            // ---------------- ADD EXPENSE ----------------
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
                        onExpensesChange(
                            expenses + Expense(
                                id = expenses.size + 1,
                                amount = a,
                                note = note,
                                category = category
                            )
                        )
                        amount = ""
                        note = ""
                        category = "General"
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Expense")
            }

            Spacer(Modifier.height(16.dp))

            // ----------- VIEW CHART BUTTON ----------------
            OutlinedButton(
                onClick = onShowChart,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.BarChart, null)
                Spacer(Modifier.width(6.dp))
                Text("View Spending Chart")
            }

            Spacer(Modifier.height(20.dp))

            // ---------------- LIST -------------------------
            LazyColumn {
                items(expenses) { e ->
                    ExpenseCard(e)
                    Spacer(Modifier.height(10.dp))
                }
            }
        }
    }
}

// ------------------ EXPENSE CARD ---------------------
@Composable
fun ExpenseCard(e: Expense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Amount: $${"%.2f".format(e.amount)}")
            Text("Category: ${e.category}")
            if (e.note.isNotEmpty()) Text("Note: ${e.note}")
        }
    }
}

// ------------------ PIE CHART SCREEN -----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(expenses: List<Expense>, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spending Chart") },
                navigationIcon = {
                    Button(onClick = onBack) { Text("Back") }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier.padding(pad).padding(16.dp)
        ) {
            Text("Category Breakdown", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))

            val grouped = expenses.groupBy { it.category }
            val total = expenses.sumOf { it.amount }.toFloat()

            val chartData: List<Pair<String, Float>> =
                grouped.map { entry ->
                    val categoryTotal = entry.value.sumOf { it.amount }.toFloat()
                    val sweep = if (total > 0f) (categoryTotal / total) * 360f else 0f
                    entry.key to sweep
                }

            Canvas(
                modifier = Modifier.size(300.dp)
            ) {
                var start = 0f
                val colors = listOf(
                    Color.Red, Color.Blue, Color.Green,
                    Color.Magenta, Color.Cyan, Color.Yellow
                )

                chartData.forEachIndexed { i, (_, sweep) ->
                    drawPieArc(
                        color = colors[i % colors.size],
                        start = start,
                        sweep = sweep
                    )
                    start += sweep
                }
            }
        }
    }
}

// CUSTOM ARC WITHOUT drawArc ERROR
fun DrawScope.drawPieArc(color: Color, start: Float, sweep: Float) {
    val rect = Rect(0f, 0f, size.width, size.height)
    drawArc(
        color = color,
        startAngle = start,
        sweepAngle = sweep,
        useCenter = true,
        topLeft = rect.topLeft,
        size = rect.size
    )
}
