package com.uchi.mascostos.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class AndroidProject(val id: String, val name: String)
data class AndroidBudget(val id: String, val name: String)
data class AndroidCatalogItem(val title: String, val code: String, val description: String, val unitCost: Double)
data class AndroidProjectItem(val title: String, val code: String, val description: String, var quantity: Double, val unitCost: Double) {
    val subtotal: Double get() = quantity * unitCost
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val projects = remember { mutableStateListOf(AndroidProject("P001", "Proyecto Demo")) }
                val budgets = remember { mutableStateListOf(AndroidBudget("B001", "Presupuesto Base")) }
                val catalog = remember {
                    mutableStateListOf(
                        AndroidCatalogItem("Obras preliminares", "CU001", "Trazo y replanteo", 120.0),
                        AndroidCatalogItem("Estructuras", "CU002", "Concreto f'c=210", 380.0),
                    )
                }
                val projectItems = remember { mutableStateListOf<AndroidProjectItem>() }

                var selectedProject by remember { mutableStateOf<AndroidProject?>(projects.firstOrNull()) }
                var selectedBudget by remember { mutableStateOf<AndroidBudget?>(budgets.firstOrNull()) }
                var selectedCatalogItem by remember { mutableStateOf<AndroidCatalogItem?>(null) }
                var selectedProjectItem by remember { mutableStateOf<AndroidProjectItem?>(null) }

                var quantityToAdd by remember { mutableStateOf("1") }
                var quantityToEdit by remember { mutableStateOf("") }
                var exportStatus by remember { mutableStateOf("Reporte: pendiente") }

                val total = projectItems.sumOf { it.subtotal }
                val subtotalsByTitle = projectItems.groupBy { it.title }.mapValues { (_, v) -> v.sumOf { it.subtotal } }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("MASCostos Android (UI alineada con Desktop)", style = MaterialTheme.typography.titleMedium)

                    Text("Proyectos")
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(0.8f, fill = false)) {
                        items(projects) { p ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { selectedProject = p }) {
                                Text("${p.name} (${p.id})", modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    Text("Proyecto activo: ${selectedProject?.name ?: "-"}")

                    Text("Presupuesto activo")
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(0.6f, fill = false)) {
                        items(budgets) { b ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { selectedBudget = b }) {
                                Text("${b.name} (${b.id})", modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                    Text("Presupuesto seleccionado: ${selectedBudget?.name ?: "-"}")

                    Text("Estructura (equivalente al árbol desktop)")
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                        items(catalog) { c ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { selectedCatalogItem = c }) {
                                Text("${c.title} | ${c.code} - ${c.description} (S/ ${c.unitCost})", modifier = Modifier.padding(8.dp))
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quantityToAdd, onValueChange = { quantityToAdd = it }, label = { Text("Cantidad") })
                        Button(onClick = {
                            val sel = selectedCatalogItem ?: return@Button
                            val qty = quantityToAdd.toDoubleOrNull() ?: return@Button
                            val existing = projectItems.find { it.code == sel.code }
                            if (existing != null) {
                                existing.quantity = qty
                            } else {
                                projectItems.add(
                                    AndroidProjectItem(sel.title, sel.code, sel.description, qty, sel.unitCost)
                                )
                            }
                        }) {
                            Text("Agregar selección")
                        }
                    }

                    Text("Ítems agregados")
                    LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                        items(projectItems) { it ->
                            Card(modifier = Modifier.fillMaxWidth().clickable { selectedProjectItem = it }) {
                                Text(
                                    "${it.title} | ${it.code} - ${it.description} | ${it.quantity} x ${it.unitCost} = ${"%.2f".format(it.subtotal)}",
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = quantityToEdit, onValueChange = { quantityToEdit = it }, label = { Text("Nueva cantidad") })
                        Button(onClick = {
                            val item = selectedProjectItem ?: return@Button
                            val qty = quantityToEdit.toDoubleOrNull() ?: return@Button
                            item.quantity = qty
                        }) {
                            Text("Actualizar ítem")
                        }
                        Button(onClick = {
                            exportStatus = "Reporte CSV generado (mock): report-${selectedProject?.id ?: "NA"}.csv"
                        }) {
                            Text("Exportar CSV")
                        }
                    }

                    Text("Subtotales por título")
                    subtotalsByTitle.forEach { (title, subtotal) ->
                        Text("$title: ${"%.2f".format(subtotal)}")
                    }

                    Text("Total estimado: ${"%.2f".format(total)}")
                    Text(exportStatus)
                    Text("Siguiente acción: conectar esta UI a gateways reales compartidos con desktop")
                }
            }
        }
    }
}
