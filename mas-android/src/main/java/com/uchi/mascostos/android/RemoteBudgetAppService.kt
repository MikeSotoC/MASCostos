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
import java.util.concurrent.ConcurrentHashMap

class RemoteBudgetAppService(
    private val config: RemoteConfig = RemoteConfig(),
    private val gson: Gson = Gson(),
) : BudgetAppService {
    data class RemoteConfig(
        val baseUrl: String = "http://10.0.2.2:8080",
        val connectTimeoutMs: Int = 5_000,
        val readTimeoutMs: Int = 5_000,
        val maxRetries: Int = 2,
        val cacheTtlMs: Long = 20_000,
        val authToken: String? = null,
    )

    private data class CacheEntry(val createdAt: Long, val json: String)
    private val getCache = ConcurrentHashMap<String, CacheEntry>()

    override fun listProjects(): List<ProjectRef> {
        val json = requestGet("/projects")
        val type = object : TypeToken<List<ProjectRef>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun createProject(name: String, location: String?): ProjectRef {
        val json = request(
            "POST",
            "/projects",
            gson.toJson(mapOf("name" to name, "location" to location)),
        )
        getCache.clear()
        return gson.fromJson(json, ProjectRef::class.java)
    }

    override fun listBudgetsForProject(projectId: String): List<BudgetOption> {
        val json = requestGet("/projects/$projectId/budgets")
        val type = object : TypeToken<List<BudgetOption>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun listCatalogForProject(projectId: String, budgetId: String?): List<BudgetCatalogItem> {
        val suffix = if (budgetId.isNullOrBlank()) "" else "?budgetId=$budgetId"
        val json = requestGet("/projects/$projectId/catalog$suffix")
        val type = object : TypeToken<List<BudgetCatalogItem>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun listProjectItems(projectId: String): List<ProjectCostItem> {
        val json = requestGet("/projects/$projectId/items")
        val type = object : TypeToken<List<ProjectCostItem>>() {}.type
        return gson.fromJson(json, type)
    }

    override fun upsertProjectItem(projectId: String, costCode: String, quantity: Double) {
        request(
            "POST",
            "/projects/$projectId/items",
            gson.toJson(mapOf("code" to costCode, "quantity" to quantity)),
        )
        invalidate("/projects/$projectId/items")
    }

    override fun exportProjectCsv(projectId: String, outputPath: Path): Path {
        request(
            "POST",
            "/projects/$projectId/export-csv",
            gson.toJson(mapOf("outputPath" to outputPath.toString())),
        )
        return outputPath
    }

    override fun exportProjectPdf(projectId: String, outputPath: Path): Path {
        request(
            "POST",
            "/projects/$projectId/export-pdf",
            gson.toJson(mapOf("outputPath" to outputPath.toString())),
        )
        return outputPath
    }

    private fun requestGet(path: String): String {
        val now = System.currentTimeMillis()
        val cached = getCache[path]
        if (cached != null && now - cached.createdAt <= config.cacheTtlMs) return cached.json
        val fresh = request("GET", path)
        getCache[path] = CacheEntry(now, fresh)
        return fresh
    }

    private fun invalidate(pathPrefix: String) {
        getCache.keys.toList().forEach { key ->
            if (key.startsWith(pathPrefix) || key == "/projects") {
                getCache.remove(key)
            }
        }
    }

    private fun request(method: String, path: String, body: String? = null): String {
        var lastError: Exception? = null
        repeat(config.maxRetries + 1) { attempt ->
            try {
                val conn = (URL(config.baseUrl + path).openConnection() as HttpURLConnection).apply {
                    requestMethod = method
                    connectTimeout = config.connectTimeoutMs
                    readTimeout = config.readTimeoutMs
                    setRequestProperty("Content-Type", "application/json")
                    config.authToken?.takeIf { it.isNotBlank() }?.let { setRequestProperty("Authorization", "Bearer $it") }
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
            } catch (ex: Exception) {
                lastError = ex
                if (attempt < config.maxRetries) Thread.sleep(250L * (attempt + 1))
            }
        }
        throw IllegalStateException("Fallo remoto tras reintentos: ${lastError?.message}", lastError)
    }
}

fun defaultReportPath(projectId: String): Path = Paths.get("report-$projectId-android.csv")
