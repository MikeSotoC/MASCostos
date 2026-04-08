package com.uchi.mascostos.backend

import com.uchi.mascostos.core.Bootstrap
import com.uchi.mascostos.core.asBudgetAppService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.gson.gson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.nio.file.Paths

data class CreateProjectRequest(val name: String, val location: String?)
data class UpsertItemRequest(val code: String, val quantity: Double)
data class ExportCsvRequest(val outputPath: String?)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val appService = Bootstrap.bootWithSqlite(
        sqliteFile = Paths.get("mascostos.sqlite"),
        schemaScript = Paths.get("SQLDelphin_basica.sql"),
    ).asBudgetAppService()

    install(ContentNegotiation) {
        gson()
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        route("/projects") {
            get {
                call.respond(appService.listProjects())
            }

            post {
                val req = call.receive<CreateProjectRequest>()
                call.respond(appService.createProject(req.name, req.location))
            }

            route("/{projectId}") {
                get("/budgets") {
                    val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    call.respond(appService.listBudgetsForProject(projectId))
                }

                get("/catalog") {
                    val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val budgetId = call.request.queryParameters["budgetId"]
                    call.respond(appService.listCatalogForProject(projectId, budgetId))
                }

                get("/items") {
                    val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    call.respond(appService.listProjectItems(projectId))
                }

                post("/items") {
                    val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<UpsertItemRequest>()
                    appService.upsertProjectItem(projectId, req.code, req.quantity)
                    call.respond(HttpStatusCode.NoContent)
                }

                post("/export-csv") {
                    val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<ExportCsvRequest>()
                    val path = req.outputPath ?: "report-$projectId.csv"
                    val file = appService.exportProjectCsv(projectId, Paths.get(path))
                    call.respond(mapOf("output" to file.toAbsolutePath().toString()))
                }
            }
        }
    }
}
