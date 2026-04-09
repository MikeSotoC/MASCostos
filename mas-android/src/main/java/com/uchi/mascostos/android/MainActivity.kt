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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.api.service.BudgetAppService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uchi.mascostos.shared.ui.BudgetUiLogic
import com.uchi.mascostos.shared.ui.MasDesignSystem
import com.uchi.mascostos.shared.ui.UiCostItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.file.Paths

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val runtimeConfig = remember { AndroidConfigLoader.load() }
                val appService: BudgetAppService = remember {
                    if (runtimeConfig.useRemote) {
                        RemoteBudgetAppService(
                            config = RemoteBudgetAppService.RemoteConfig(
                                baseUrl = runtimeConfig.baseUrl,
                                maxRetries = runtimeConfig.maxRetries,
                                authToken = runtimeConfig.token,
                            ),
                        )
                    } else {
                        LocalBudgetAppService(this@MainActivity)
                    }
                }
                val scope = rememberCoroutineScope()
                val gson = remember { Gson() }
                val prefs = remember { getSharedPreferences("mascostos_cache", MODE_PRIVATE) }

                var projects by remember { mutableStateOf<List<ProjectRef>>(emptyList()) }
                var selectedProject by remember { mutableStateOf<ProjectRef?>(null) }
                var budgets by remember { mutableStateOf<List<BudgetOption>>(emptyList()) }
                var selectedBudget by remember { mutableStateOf<BudgetOption?>(null) }
                var catalog by remember { mutableStateOf<List<BudgetCatalogItem>>(emptyList()) }
                var selectedCatalogItem by remember { mutableStateOf<BudgetCatalogItem?>(null) }
                var projectItems by remember { mutableStateOf<List<ProjectCostItem>>(emptyList()) }
                var selectedProjectItem by remember { mutableStateOf<ProjectCostItem?>(null) }

                var quantityToAdd by remember { mutableStateOf("1") }
                var quantityToEdit by remember { mutableStateOf("") }
                var exportStatus by remember { mutableStateOf("Reporte: pendiente") }
                var serviceStatus by remember {
                    mutableStateOf(
                        if (runtimeConfig.useRemote) "Servicio: remoto (opcional)" else "Servicio: SQLite local (predeterminado)"
                    )
                }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var isLoading by remember { mutableStateOf(false) }
                var selectedScreen by remember { mutableStateOf(0) }
                var syncStatus by remember { mutableStateOf("Sync: pendiente") }

                fun saveCache(projectId: String?) {
                    if (!runtimeConfig.useRemote) return
                    prefs.edit()
                        .putString("projects", gson.toJson(projects))
                        .putString("budgets", gson.toJson(budgets))
                        .putString("catalog", gson.toJson(catalog))
                        .putString("items:$projectId", gson.toJson(projectItems))
                        .apply()
                }

                fun loadCache() {
                    if (!runtimeConfig.useRemote) return
                    val projectType = object : TypeToken<List<ProjectRef>>() {}.type
                    val budgetType = object : TypeToken<List<BudgetOption>>() {}.type
                    val catalogType = object : TypeToken<List<BudgetCatalogItem>>() {}.type
                    projects = gson.fromJson<List<ProjectRef>>(prefs.getString("projects", "[]"), projectType) ?: emptyList()
                    budgets = gson.fromJson<List<BudgetOption>>(prefs.getString("budgets", "[]"), budgetType) ?: emptyList()
                    catalog = gson.fromJson<List<BudgetCatalogItem>>(prefs.getString("catalog", "[]"), catalogType) ?: emptyList()
                    selectedProject = projects.firstOrNull()
                    selectedBudget = budgets.firstOrNull()
                    selectedProject?.let { p ->
                        val itemType = object : TypeToken<List<ProjectCostItem>>() {}.type
                        projectItems = gson.fromJson<List<ProjectCostItem>>(prefs.getString("items:${p.id}", "[]"), itemType) ?: emptyList()
                    }
                }

                fun runSafely(block: suspend () -> Unit) {
                    scope.launch {
                        isLoading = true
                        try {
                            block()
                            errorMessage = null
                        } catch (ex: Exception) {
                            errorMessage = ex.message ?: "Error de conexión"
                        } finally {
                            isLoading = false
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    loadCache()
                    runSafely {
                        projects = withContext(Dispatchers.IO) { appService.listProjects() }
                        selectedProject = projects.firstOrNull()
                        selectedProject?.let { p ->
                            budgets = withContext(Dispatchers.IO) { appService.listBudgetsForProject(p.id) }
                            selectedBudget = budgets.firstOrNull()
                            catalog = withContext(Dispatchers.IO) { appService.listCatalogForProject(p.id, selectedBudget?.id) }
                            projectItems = withContext(Dispatchers.IO) { appService.listProjectItems(p.id) }
                            saveCache(p.id)
                        }
                    }
                }

                val uiItems = projectItems.map { UiCostItem(it.titleName, it.costCode, it.quantity, it.unitCost) }
                val total = BudgetUiLogic.total(uiItems)
                val subtotalsByTitle = BudgetUiLogic.subtotalsByTitle(uiItems)

                Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(MasDesignSystem.Spacing.section.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    ) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("MASCostos Android", style = MaterialTheme.typography.titleMedium)
                            Text("Diseño moderno base + servicio real", style = MaterialTheme.typography.bodyMedium)
                            Text(MasDesignSystem.StatusText.parityAligned, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(serviceStatus)
                    if (errorMessage != null) Text("Error: $errorMessage")
                    if (isLoading) CircularProgressIndicator()
                    Divider()
                    val screens = listOf("Proyectos", "Catálogo", "Ítems")
                    TabRow(selectedTabIndex = selectedScreen) {
                        screens.forEachIndexed { index, label ->
                            Tab(
                                selected = selectedScreen == index,
                                onClick = { selectedScreen = index },
                                text = { Text(label) },
                            )
                        }
                    }

                    if (selectedScreen == 0) {
                        Text("Proyectos", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(Modifier.fillMaxWidth().weight(1f, false)) {
                            if (isLoading && projects.isEmpty()) {
                                items((1..3).toList()) {
                                    Card(Modifier.fillMaxWidth()) { Text("Cargando proyecto...", Modifier.padding(10.dp)) }
                                }
                            }
                            items(projects) { p ->
                                Card(
                                    Modifier.fillMaxWidth().clickable {
                                    selectedProject = p
                                    runSafely {
                                        budgets = withContext(Dispatchers.IO) { appService.listBudgetsForProject(p.id) }
                                        selectedBudget = budgets.firstOrNull()
                                        catalog = withContext(Dispatchers.IO) { appService.listCatalogForProject(p.id, selectedBudget?.id) }
                                        projectItems = withContext(Dispatchers.IO) { appService.listProjectItems(p.id) }
                                        saveCache(p.id)
                                    }
                                },
                                ) { Text("${p.name} (${p.id})", Modifier.padding(10.dp)) }
                            }
                        }
                    } else if (selectedScreen == 1) {
                        Text("Presupuesto activo", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(Modifier.fillMaxWidth().weight(0.4f, false)) {
                            if (isLoading && budgets.isEmpty()) {
                                items((1..2).toList()) {
                                    Card(Modifier.fillMaxWidth()) { Text("Cargando presupuesto...", Modifier.padding(10.dp)) }
                                }
                            }
                            items(budgets) { b ->
                                Card(Modifier.fillMaxWidth().clickable {
                                    selectedBudget = b
                                    val pid = selectedProject?.id ?: return@clickable
                                    runSafely {
                                        catalog = withContext(Dispatchers.IO) { appService.listCatalogForProject(pid, b.id) }
                                        saveCache(pid)
                                    }
                                }) { Text("${b.name} (${b.id})", Modifier.padding(10.dp)) }
                            }
                        }
                        Text("Estructura", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(Modifier.fillMaxWidth().weight(1f, false)) {
                            if (isLoading && catalog.isEmpty()) {
                                items((1..4).toList()) {
                                    Card(Modifier.fillMaxWidth()) { Text("Cargando catálogo...", Modifier.padding(10.dp)) }
                                }
                            }
                            items(catalog) { c ->
                                Card(Modifier.fillMaxWidth().clickable { selectedCatalogItem = c }) {
                                    Text("${c.titleName} | ${c.costCode} - ${c.description} (S/ ${c.unitCost})", Modifier.padding(10.dp))
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(quantityToAdd, { quantityToAdd = it }, label = { Text("Cantidad") })
                            Button(onClick = {
                                val pid = selectedProject?.id ?: return@Button
                                val code = selectedCatalogItem?.costCode ?: return@Button
                                val qty = quantityToAdd.toDoubleOrNull() ?: return@Button
                                runSafely {
                                    withContext(Dispatchers.IO) { appService.upsertProjectItem(pid, code, qty) }
                                    projectItems = withContext(Dispatchers.IO) { appService.listProjectItems(pid) }
                                    saveCache(pid)
                                }
                            }) { Text("Agregar selección") }
                        }
                    } else {
                        val localService = appService as? LocalBudgetAppService
                        Text("Ítems", style = MaterialTheme.typography.titleSmall)
                        LazyColumn(Modifier.fillMaxWidth().weight(1f, false)) {
                            if (isLoading && projectItems.isEmpty()) {
                                items((1..3).toList()) {
                                    Card(Modifier.fillMaxWidth()) { Text("Cargando ítems...", Modifier.padding(10.dp)) }
                                }
                            }
                            items(projectItems) { it ->
                                Card(Modifier.fillMaxWidth().clickable { selectedProjectItem = it }) {
                                    Text("${it.titleName} | ${it.costCode} | ${it.quantity} x ${it.unitCost} = ${"%.2f".format(it.subtotal)}", Modifier.padding(10.dp))
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(quantityToEdit, { quantityToEdit = it }, label = { Text("Nueva cantidad") })
                            Button(onClick = {
                                val pid = selectedProject?.id ?: return@Button
                                val item = selectedProjectItem ?: return@Button
                                val qty = quantityToEdit.toDoubleOrNull() ?: return@Button
                                runSafely {
                                    withContext(Dispatchers.IO) { appService.upsertProjectItem(pid, item.costCode, qty) }
                                    projectItems = withContext(Dispatchers.IO) { appService.listProjectItems(pid) }
                                    saveCache(pid)
                                }
                            }) { Text("Actualizar ítem") }
                            Button(onClick = {
                                val pid = selectedProject?.id ?: return@Button
                                val filename = BudgetUiLogic.reportFileName(pid, "android")
                                runSafely {
                                    withContext(Dispatchers.IO) { appService.exportProjectCsv(pid, Paths.get(filename)) }
                                    exportStatus = "Reporte CSV generado: $filename"
                                }
                            }) { Text("Exportar CSV") }
                            Button(onClick = {
                                val pid = selectedProject?.id ?: return@Button
                                val filename = "report-$pid-android.pdf"
                                runSafely {
                                    withContext(Dispatchers.IO) { appService.exportProjectPdf(pid, Paths.get(filename)) }
                                    exportStatus = "Reporte PDF generado: $filename"
                                }
                            }) { Text("Exportar PDF") }
                            if (localService != null) {
                                Button(onClick = {
                                    runSafely {
                                        val remote = RemoteBudgetAppService(
                                            RemoteBudgetAppService.RemoteConfig(
                                                baseUrl = runtimeConfig.baseUrl,
                                                maxRetries = runtimeConfig.maxRetries,
                                                authToken = runtimeConfig.token,
                                            ),
                                        )
                                        val report = withContext(Dispatchers.IO) { localService.syncPendingOperations(remote) }
                                        syncStatus = "Sync: ${report.synced}/${report.processed}, conflictos ${report.conflicts}"
                                    }
                                }) { Text("Sincronizar pendientes") }
                            }
                        }

                        Divider()
                        Text("Subtotales por título", style = MaterialTheme.typography.titleSmall)
                        subtotalsByTitle.forEach { (title, subtotal) -> Text("$title: ${"%.2f".format(subtotal)}") }
                        Text("Total: ${"%.2f".format(total)}")
                        Text(exportStatus)
                        if (localService != null) {
                            Text("Outbox pendiente: ${localService.pendingOutboxCount()} | Conflictos: ${localService.conflictCount()}")
                        }
                        Text(syncStatus)
                        Text(MasDesignSystem.StatusText.pendingRoadmap, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
