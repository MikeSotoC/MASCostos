package com.uchi.mascostos.android

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.api.service.BudgetAppService
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

class RemoteBudgetAppService(
    private val baseUrl: String = "http://10.0.2.2:8080",
    private val gson: Gson = Gson(),
) : BudgetAppService {

    override fun listProjects(): List<ProjectRef> {
        val json = request("GET", "/projects")
        val type = object : TypeToken<List<ProjectRef>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun createProject(name: String, location: String?): ProjectRef {
        val json = request(
            "POST",
            "/projects",
            gson.toJson(mapOf("name" to name, "location" to location)),
        )
        return gson.fromJson(json, ProjectRef::class.java)
    }

    override fun listBudgetsForProject(projectId: String): List<BudgetOption> {
        val json = request("GET", "/projects/$projectId/budgets")
        val type = object : TypeToken<List<BudgetOption>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun listCatalogForProject(projectId: String, budgetId: String?): List<BudgetCatalogItem> {
        val suffix = if (budgetId.isNullOrBlank()) "" else "?budgetId=$budgetId"
        val json = request("GET", "/projects/$projectId/catalog$suffix")
        val type = object : TypeToken<List<BudgetCatalogItem>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun listProjectItems(projectId: String): List<ProjectCostItem> {
        val json = request("GET", "/projects/$projectId/items")
        val type = object : TypeToken<List<ProjectCostItem>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun upsertProjectItem(projectId: String, costCode: String, quantity: Double) {
        request(
            "POST",
            "/projects/$projectId/items",
            gson.toJson(mapOf("code" to costCode, "quantity" to quantity)),
        )
    }

    override fun exportProjectCsv(projectId: String, outputPath: Path): Path {
        request(
            "POST",
            "/projects/$projectId/export-csv",
            gson.toJson(mapOf("outputPath" to outputPath.toString())),
        )
        return outputPath
    }

    private fun request(method: String, path: String, body: String? = null): String {
        val conn = (URL(baseUrl + path).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 5_000
            readTimeout = 5_000
            setRequestProperty("Content-Type", "application/json")
            if (body != null) {
                doOutput = true
            }
        }

        if (body != null) {
            OutputStreamWriter(conn.outputStream).use { it.write(body) }
        }

        val status = conn.responseCode
        val stream = if (status in 200..299) conn.inputStream else conn.errorStream
        val text = BufferedReader(stream.reader()).readText()
        if (status !in 200..299) error("HTTP $status: $text")
        return text
    }
}

fun defaultReportPath(projectId: String): Path = Paths.get("report-$projectId-android.csv")
