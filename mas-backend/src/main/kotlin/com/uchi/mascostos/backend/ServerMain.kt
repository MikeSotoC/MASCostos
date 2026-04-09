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
data class ExportFileRequest(val outputPath: String?)

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

fun Application.module() {
    val auth = AuthSecurityStore()
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
        post("/auth/login") {
            val req = call.receive<LoginRequest>()
            val result = auth.login(req.username, req.password)
                ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))
            auth.audit(UserContext(req.username, result.role), "/auth/login", "LOGIN")
            call.respond(result)
        }
        get("/auth/users") {
            val user = authorizeCall(call, auth, Permission.ADMIN) ?: return@get call.respond(HttpStatusCode.Forbidden)
            auth.audit(user, "/auth/users", "LIST_USERS")
            call.respond(auth.listUsers())
        }
        post("/auth/users") {
            val user = authorizeCall(call, auth, Permission.ADMIN) ?: return@post call.respond(HttpStatusCode.Forbidden)
            val req = call.receive<CreateUserRequest>()
            val created = auth.createUser(req.username, req.password, req.role)
            auth.audit(user, "/auth/users", "CREATE_USER")
            if (!created) return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "No se pudo crear usuario"))
            call.respond(HttpStatusCode.Created)
        }

        route("/projects") {
            get {
                val user = authorizeCall(call, auth, Permission.READ) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                auth.audit(user, "/projects", "LIST_PROJECTS")
                call.respond(appService.listProjects())
            }

            post {
                val user = authorizeCall(call, auth, Permission.WRITE) ?: return@post call.respond(HttpStatusCode.Forbidden)
                auth.audit(user, "/projects", "CREATE_PROJECT")
                val req = call.receive<CreateProjectRequest>()
                call.respond(appService.createProject(req.name, req.location))
            }

            route("/{projectId}") {
                get("/budgets") {
                    val user = authorizeCall(call, auth, Permission.READ) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    auth.audit(user, "/projects/$projectId/budgets", "LIST_BUDGETS")
                    call.respond(appService.listBudgetsForProject(projectId))
                }

                get("/catalog") {
                    val user = authorizeCall(call, auth, Permission.READ) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val budgetId = call.request.queryParameters["budgetId"]
                    auth.audit(user, "/projects/$projectId/catalog", "LIST_CATALOG")
                    call.respond(appService.listCatalogForProject(projectId, budgetId))
                }

                get("/items") {
                    val user = authorizeCall(call, auth, Permission.READ) ?: return@get call.respond(HttpStatusCode.Unauthorized)
                    val projectId = call.parameters["projectId"] ?: return@get call.respond(HttpStatusCode.BadRequest)
                    auth.audit(user, "/projects/$projectId/items", "LIST_ITEMS")
                    call.respond(appService.listProjectItems(projectId))
                }

                post("/items") {
                    val user = authorizeCall(call, auth, Permission.WRITE) ?: return@post call.respond(HttpStatusCode.Forbidden)
                    val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<UpsertItemRequest>()
                    appService.upsertProjectItem(projectId, req.code, req.quantity)
                    auth.audit(user, "/projects/$projectId/items", "UPSERT_ITEM")
                    call.respond(HttpStatusCode.NoContent)
                }

                post("/export-csv") {
                    val user = authorizeCall(call, auth, Permission.EXPORT) ?: return@post call.respond(HttpStatusCode.Forbidden)
                    val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<ExportFileRequest>()
                    val path = req.outputPath ?: "report-$projectId.csv"
                    val file = appService.exportProjectCsv(projectId, Paths.get(path))
                    auth.audit(user, "/projects/$projectId/export-csv", "EXPORT_CSV")
                    call.respond(mapOf("output" to file.toAbsolutePath().toString()))
                }

                post("/export-pdf") {
                    val user = authorizeCall(call, auth, Permission.EXPORT) ?: return@post call.respond(HttpStatusCode.Forbidden)
                    val projectId = call.parameters["projectId"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    val req = call.receive<ExportFileRequest>()
                    val path = req.outputPath ?: "report-$projectId.pdf"
                    val file = appService.exportProjectPdf(projectId, Paths.get(path))
                    auth.audit(user, "/projects/$projectId/export-pdf", "EXPORT_PDF")
                    call.respond(mapOf("output" to file.toAbsolutePath().toString()))
                }
            }
        }
    }
}

private fun authorizeCall(
    call: io.ktor.server.application.ApplicationCall,
    auth: AuthSecurityStore,
    permission: Permission,
): UserContext? {
    val bearer = call.request.headers["Authorization"]?.removePrefix("Bearer ")?.trim()
    val userFromSession = auth.resolveUser(bearer)
    if (auth.authorize(userFromSession, permission)) return userFromSession

    // compatibilidad: token único por entorno
    val expected = System.getenv("MASCOSTOS_API_TOKEN")?.trim().orEmpty()
    if (expected.isNotBlank() && bearer == expected) return UserContext("env-token", "admin")
    return null
}
