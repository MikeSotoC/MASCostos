package com.uchi.mascostos.android

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.uchi.mascostos.api.model.BudgetCatalogItem
import com.uchi.mascostos.api.model.BudgetOption
import com.uchi.mascostos.api.model.ProjectCostItem
import com.uchi.mascostos.api.model.ProjectRef
import com.uchi.mascostos.api.service.BudgetAppService
import java.nio.file.Path
import java.util.UUID

data class SyncReport(val processed: Int, val synced: Int, val conflicts: Int)

class LocalBudgetAppService(context: Context) : BudgetAppService {
    private val db = LocalDb(context).writableDatabase

    override fun listProjects(): List<ProjectRef> {
        val rows = mutableListOf<ProjectRef>()
        db.rawQuery("SELECT id, name FROM projects ORDER BY name", null).use { c ->
            while (c.moveToNext()) rows += ProjectRef(c.getString(0), c.getString(1))
        }
        return rows
    }

    override fun createProject(name: String, location: String?): ProjectRef {
        val id = UUID.randomUUID().toString()
        db.execSQL("INSERT INTO projects(id, name, location) VALUES (?, ?, ?)", arrayOf(id, name, location))
        return ProjectRef(id, name)
    }

    override fun listBudgetsForProject(projectId: String): List<BudgetOption> {
        return listOf(BudgetOption("LOCAL-BASE", "Presupuesto local base"))
    }

    override fun listCatalogForProject(projectId: String, budgetId: String?): List<BudgetCatalogItem> {
        val rows = mutableListOf<BudgetCatalogItem>()
        db.rawQuery(
            "SELECT title_id, title_name, cost_code, description, unit, unit_cost FROM catalog ORDER BY title_name, cost_code",
            null,
        ).use { c ->
            while (c.moveToNext()) {
                rows += BudgetCatalogItem(
                    titleId = c.getString(0),
                    titleName = c.getString(1),
                    costCode = c.getString(2),
                    description = c.getString(3),
                    unit = c.getString(4),
                    unitCost = c.getDouble(5),
                )
            }
        }
        return rows
    }

    override fun listProjectItems(projectId: String): List<ProjectCostItem> {
        val rows = mutableListOf<ProjectCostItem>()
        db.rawQuery(
            """
            SELECT c.title_name, i.cost_code, c.description, c.unit, i.quantity, c.unit_cost
            FROM project_items i
            JOIN catalog c ON c.cost_code = i.cost_code
            WHERE i.project_id = ?
            ORDER BY c.title_name, i.cost_code
            """.trimIndent(),
            arrayOf(projectId),
        ).use { c ->
            while (c.moveToNext()) {
                rows += ProjectCostItem(
                    titleName = c.getString(0),
                    costCode = c.getString(1),
                    description = c.getString(2),
                    unit = c.getString(3),
                    quantity = c.getDouble(4),
                    unitCost = c.getDouble(5),
                )
            }
        }
        return rows
    }

    override fun upsertProjectItem(projectId: String, costCode: String, quantity: Double) {
        val previous = db.rawQuery(
            "SELECT quantity, version FROM project_items WHERE project_id = ? AND cost_code = ?",
            arrayOf(projectId, costCode),
        ).use { c ->
            if (c.moveToFirst()) c.getDouble(0) to c.getInt(1) else null
        }
        val nextVersion = (previous?.second ?: 0) + 1
        db.execSQL(
            "INSERT INTO project_items(project_id, cost_code, quantity, version) VALUES (?, ?, ?, ?) " +
                "ON CONFLICT(project_id, cost_code) DO UPDATE SET quantity = excluded.quantity, version = excluded.version",
            arrayOf(projectId, costCode, quantity, nextVersion),
        )
        db.execSQL(
            "INSERT INTO outbox(project_id, cost_code, quantity, operation, client_version, status, created_at) VALUES (?, ?, ?, 'UPSERT_ITEM', ?, 'PENDING', strftime('%Y-%m-%dT%H:%M:%fZ','now'))",
            arrayOf(projectId, costCode, quantity, nextVersion),
        )
        if (previous != null && previous.first != quantity) {
            db.execSQL(
                "INSERT INTO conflicts(project_id, cost_code, local_quantity, attempted_quantity, detected_at) VALUES (?, ?, ?, ?, strftime('%Y-%m-%dT%H:%M:%fZ','now'))",
                arrayOf(projectId, costCode, previous.first, quantity),
            )
        }
    }

    override fun exportProjectCsv(projectId: String, outputPath: Path): Path {
        val header = "titulo,codigo,descripcion,unidad,cantidad,costo_unitario,subtotal"
        val rows = listProjectItems(projectId).joinToString("\n") {
            "\"${it.titleName}\",\"${it.costCode}\",\"${it.description}\",\"${it.unit}\",${it.quantity},${it.unitCost},${it.subtotal}"
        }
        outputPath.toFile().writeText("$header\n$rows")
        return outputPath
    }

    override fun exportProjectPdf(projectId: String, outputPath: Path): Path {
        // PDF simplificado local: se delega a un txt con extensión pdf para mantener modo offline sin dependencias pesadas en Android.
        val text = buildString {
            appendLine("MASCostos - Reporte local $projectId")
            listProjectItems(projectId).forEach { appendLine("${it.costCode} | ${it.description} | ${it.quantity} x ${it.unitCost}") }
        }
        outputPath.toFile().writeText(text)
        return outputPath
    }

    fun pendingOutboxCount(): Int = db.rawQuery("SELECT COUNT(*) FROM outbox WHERE status = 'PENDING'", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    fun conflictCount(): Int = db.rawQuery("SELECT COUNT(*) FROM conflicts", null).use {
        if (it.moveToFirst()) it.getInt(0) else 0
    }

    fun syncPendingOperations(remote: RemoteBudgetAppService): SyncReport {
        val rows = mutableListOf<Triple<Long, String, String>>()
        db.rawQuery(
            "SELECT id, project_id, cost_code FROM outbox WHERE status = 'PENDING' ORDER BY id",
            null,
        ).use { c ->
            while (c.moveToNext()) rows += Triple(c.getLong(0), c.getString(1), c.getString(2))
        }

        var synced = 0
        var conflicts = 0
        val remoteCache = mutableMapOf<String, Map<String, Double>>()
        rows.forEach { (id, projectId, costCode) ->
            try {
                val qty = db.rawQuery(
                    "SELECT quantity FROM project_items WHERE project_id = ? AND cost_code = ?",
                    arrayOf(projectId, costCode),
                ).use { c -> if (c.moveToFirst()) c.getDouble(0) else 0.0 }
                val remoteByCode = remoteCache.getOrPut(projectId) {
                    remote.listProjectItems(projectId).associate { it.costCode to it.quantity }
                }
                val remoteQty = remoteByCode[costCode]
                if (remoteQty != null && kotlin.math.abs(remoteQty - qty) > 0.0001) {
                    db.execSQL(
                        "INSERT INTO conflicts(project_id, cost_code, local_quantity, attempted_quantity, detected_at) VALUES (?, ?, ?, ?, strftime('%Y-%m-%dT%H:%M:%fZ','now'))",
                        arrayOf(projectId, costCode, qty, remoteQty),
                    )
                    db.execSQL("UPDATE outbox SET status = 'CONFLICT' WHERE id = ?", arrayOf(id))
                    conflicts++
                    return@forEach
                }
                remote.upsertProjectItem(projectId, costCode, qty)
                db.execSQL("UPDATE outbox SET status = 'SYNCED' WHERE id = ?", arrayOf(id))
                synced++
            } catch (_: Exception) {
                db.execSQL("UPDATE outbox SET status = 'CONFLICT' WHERE id = ?", arrayOf(id))
                conflicts++
            }
        }
        return SyncReport(processed = rows.size, synced = synced, conflicts = conflicts)
    }
}

private class LocalDb(context: Context) : SQLiteOpenHelper(context, "mascostos_local.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE projects(id TEXT PRIMARY KEY, name TEXT NOT NULL, location TEXT)")
        db.execSQL(
            "CREATE TABLE catalog(" +
                "cost_code TEXT PRIMARY KEY, title_id TEXT, title_name TEXT NOT NULL, description TEXT NOT NULL, unit TEXT NOT NULL, unit_cost REAL NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE project_items(" +
                "project_id TEXT NOT NULL, cost_code TEXT NOT NULL, quantity REAL NOT NULL, version INTEGER NOT NULL DEFAULT 1, PRIMARY KEY(project_id, cost_code))"
        )
        db.execSQL(
            "CREATE TABLE outbox(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, project_id TEXT NOT NULL, cost_code TEXT NOT NULL, quantity REAL NOT NULL, operation TEXT NOT NULL, client_version INTEGER NOT NULL, status TEXT NOT NULL, created_at TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE conflicts(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, project_id TEXT NOT NULL, cost_code TEXT NOT NULL, local_quantity REAL NOT NULL, attempted_quantity REAL NOT NULL, detected_at TEXT NOT NULL)"
        )
        db.execSQL(
            "INSERT INTO catalog(cost_code, title_id, title_name, description, unit, unit_cost) VALUES " +
                "('A.01', 'A', 'Movimiento de tierra', 'Excavación manual', 'm3', 120.0), " +
                "('B.10', 'B', 'Concreto', 'Concreto f''c=210', 'm3', 410.0), " +
                "('C.05', 'C', 'Acabados', 'Pintura interior', 'm2', 28.0)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE project_items ADD COLUMN version INTEGER NOT NULL DEFAULT 1")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS outbox(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, project_id TEXT NOT NULL, cost_code TEXT NOT NULL, quantity REAL NOT NULL, operation TEXT NOT NULL, client_version INTEGER NOT NULL, status TEXT NOT NULL, created_at TEXT NOT NULL)"
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS conflicts(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, project_id TEXT NOT NULL, cost_code TEXT NOT NULL, local_quantity REAL NOT NULL, attempted_quantity REAL NOT NULL, detected_at TEXT NOT NULL)"
            )
        }
    }
}
