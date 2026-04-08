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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.api.service.BudgetAppService
import com.uchi.mascostos.shared.ui.BudgetUiLogic
import com.uchi.mascostos.shared.ui.UiCostItem
import java.nio.file.Path
import java.nio.file.Paths

private class FakeBudgetAppService : BudgetAppService {
    private val projects = mutableListOf(ProjectRef("P001", "Proyecto Demo"))
    private val budgets = mutableMapOf(
        "P001" to listOf(BudgetOption("B001", "Presupuesto Base"))
    )
    private val catalog = mapOf(
        "P001:B001" to listOf(
            BudgetCatalogItem("T001", "Obras preliminares", "CU001", "Trazo y replanteo", "m2", 120.0),
            BudgetCatalogItem("T002", "Estructuras", "CU002", "Concreto f'c=210", "m3", 380.0),
        )
    )
    private val projectItems = mutableMapOf<String, MutableList<ProjectCostItem>>()

    override fun listProjects(): List<ProjectRef> = projects.toList()

    override fun createProject(name: String, location: String?): ProjectRef {
        val id = "P" + (projects.size + 1).toString().padStart(3, '0')
        val p = ProjectRef(id, name)
        projects += p
        budgets[id] = listOf(BudgetOption("B$id", "Presupuesto $name"))
        return p
    }

    override fun listBudgetsForProject(projectId: String): List<BudgetOption> = budgets[projectId] ?: emptyList()

    override fun listCatalogForProject(projectId: String, budgetId: String?): List<BudgetCatalogItem> {
        return catalog["$projectId:${budgetId ?: ""}"] ?: emptyList()
    }

    override fun listProjectItems(projectId: String): List<ProjectCostItem> = projectItems[projectId]?.toList() ?: emptyList()

    override fun upsertProjectItem(projectId: String, costCode: String, quantity: Double) {
        val target = projectItems.getOrPut(projectId) { mutableListOf() }
        val item = target.firstOrNull { it.costCode == costCode }
        if (item != null) {
            target[target.indexOf(item)] = item.copy(quantity = quantity)
            return
        }

        val cat = catalog.values.flatten().firstOrNull { it.costCode == costCode } ?: return
        target += ProjectCostItem(cat.titleName, cat.costCode, cat.description, cat.unit, quantity, cat.unitCost)
    }

    override fun exportProjectCsv(projectId: String, outputPath: Path): Path = outputPath
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                var serviceStatus by remember { mutableStateOf("Servicio: remoto") }
                val appService: BudgetAppService = remember {
                    try {
                        RemoteBudgetAppService().also { it.listProjects() }
                    } catch (_: Exception) {
                        serviceStatus = "Servicio: mock (backend no disponible)"
                        FakeBudgetAppService()
                    }
                }
                var projects by remember { mutableStateOf(appService.listProjects()) }
                var selectedProject by remember { mutableStateOf(projects.firstOrNull()) }
                var budgets by remember { mutableStateOf(selectedProject?.let { appService.listBudgetsForProject(it.id) } ?: emptyList()) }
                var selectedBudget by remember { mutableStateOf(budgets.firstOrNull()) }
                var catalog by remember {
                    mutableStateOf(
                        selectedProject?.let { p -> appService.listCatalogForProject(p.id, selectedBudget?.id) } ?: emptyList()
                    )
                }
                var selectedCatalogItem by remember { mutableStateOf<BudgetCatalogItem?>(null) }
                var projectItems by remember { mutableStateOf(selectedProject?.let { appService.listProjectItems(it.id) } ?: emptyList()) }
                var selectedProjectItem by remember { mutableStateOf<ProjectCostItem?>(null) }

                var quantityToAdd by remember { mutableStateOf("1") }
                var quantityToEdit by remember { mutableStateOf("") }
                var exportStatus by remember { mutableStateOf("Reporte: pendiente") }

                val uiItems = projectItems.map { UiCostItem(it.titleName, it.costCode, it.quantity, it.unitCost) }
                val total = BudgetUiLogic.total(uiItems)
                val subtotalsByTitle = BudgetUiLogic.subtotalsByTitle(uiItems)

                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("MASCostos Android (preparado para servicio real)", style = MaterialTheme.typography.titleMedium)
                    Text(serviceStatus)

                    Text("Proyectos")
                    LazyColumn(Modifier.fillMaxWidth().weight(0.7f, false)) {
                        items(projects) { p ->
                            Card(Modifier.fillMaxWidth().clickable {
                                selectedProject = p
                                budgets = appService.listBudgetsForProject(p.id)
                                selectedBudget = budgets.firstOrNull()
                                catalog = appService.listCatalogForProject(p.id, selectedBudget?.id)
                                projectItems = appService.listProjectItems(p.id)
                            }) { Text("${p.name} (${p.id})", Modifier.padding(8.dp)) }
                        }
                    }

                    Text("Presupuesto activo")
                    LazyColumn(Modifier.fillMaxWidth().weight(0.5f, false)) {
                        items(budgets) { b ->
                            Card(Modifier.fillMaxWidth().clickable {
                                selectedBudget = b
                                val pid = selectedProject?.id ?: return@clickable
                                catalog = appService.listCatalogForProject(pid, b.id)
                            }) { Text("${b.name} (${b.id})", Modifier.padding(8.dp)) }
                        }
                    }

                    Text("Estructura")
                    LazyColumn(Modifier.fillMaxWidth().weight(1f, false)) {
                        items(catalog) { c ->
                            Card(Modifier.fillMaxWidth().clickable { selectedCatalogItem = c }) {
                                Text("${c.titleName} | ${c.costCode} - ${c.description} (S/ ${c.unitCost})", Modifier.padding(8.dp))
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(quantityToAdd, { quantityToAdd = it }, label = { Text("Cantidad") })
                        Button(onClick = {
                            val pid = selectedProject?.id ?: return@Button
                            val code = selectedCatalogItem?.costCode ?: return@Button
                            val qty = quantityToAdd.toDoubleOrNull() ?: return@Button
                            appService.upsertProjectItem(pid, code, qty)
                            projectItems = appService.listProjectItems(pid)
                        }) { Text("Agregar selección") }
                    }

                    Text("Ítems")
                    LazyColumn(Modifier.fillMaxWidth().weight(1f, false)) {
                        items(projectItems) { it ->
                            Card(Modifier.fillMaxWidth().clickable { selectedProjectItem = it }) {
                                Text("${it.titleName} | ${it.costCode} | ${it.quantity} x ${it.unitCost} = ${"%.2f".format(it.subtotal)}", Modifier.padding(8.dp))
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(quantityToEdit, { quantityToEdit = it }, label = { Text("Nueva cantidad") })
                        Button(onClick = {
                            val pid = selectedProject?.id ?: return@Button
                            val item = selectedProjectItem ?: return@Button
                            val qty = quantityToEdit.toDoubleOrNull() ?: return@Button
                            appService.upsertProjectItem(pid, item.costCode, qty)
                            projectItems = appService.listProjectItems(pid)
                        }) { Text("Actualizar ítem") }
                        Button(onClick = {
                            val pid = selectedProject?.id ?: "NA"
                            val filename = BudgetUiLogic.reportFileName(pid, "android")
                            appService.exportProjectCsv(pid, Paths.get(filename))
                            exportStatus = "Reporte CSV generado: $filename"
                        }) { Text("Exportar CSV") }
                    }

                    Text("Subtotales por título")
                    subtotalsByTitle.forEach { (title, subtotal) -> Text("$title: ${"%.2f".format(subtotal)}") }
                    Text("Total: ${"%.2f".format(total)}")
                    Text(exportStatus)
                }
            }
        }
    }
}
