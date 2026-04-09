package com.uchi.mascostos.backend

import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val role: String)
data class UserContext(val username: String, val role: String)
data class UserRecord(val username: String, val role: String)
data class CreateUserRequest(val username: String, val password: String, val role: String)
data class AuditRecord(val username: String, val role: String, val endpoint: String, val action: String, val createdAt: String)

class AuthSecurityStore {
    private val sessions = ConcurrentHashMap<String, UserContext>()
    private val conn: Connection by lazy { DriverManager.getConnection("jdbc:sqlite:mascostos-auth.sqlite") }

    init {
        ensureSchema()
        ensureDefaultAdmin()
    }

    fun login(username: String, password: String): LoginResponse? {
        val row = conn.prepareStatement("SELECT username, role FROM users WHERE username = ? AND password = ?").use { ps ->
            ps.setString(1, username)
            ps.setString(2, password)
            ps.executeQuery().use { rs ->
                if (rs.next()) UserContext(rs.getString(1), rs.getString(2)) else null
            }
        } ?: return null

        val token = UUID.randomUUID().toString()
        sessions[token] = row
        return LoginResponse(token = token, role = row.role)
    }

    fun resolveUser(token: String?): UserContext? {
        if (token.isNullOrBlank()) return null
        return sessions[token]
    }

    fun authorize(user: UserContext?, needed: Permission): Boolean {
        if (user == null) return false
        return when (user.role.lowercase()) {
            "admin" -> true
            "editor" -> needed in setOf(Permission.READ, Permission.WRITE)
            "reporter" -> needed in setOf(Permission.READ, Permission.EXPORT)
            "viewer" -> needed == Permission.READ
            else -> false
        }
    }

    fun audit(user: UserContext?, endpoint: String, action: String) {
        conn.prepareStatement(
            "INSERT INTO audit_logs(username, role, endpoint, action, created_at) VALUES (?, ?, ?, ?, strftime('%Y-%m-%dT%H:%M:%fZ','now'))",
        ).use { ps ->
            ps.setString(1, user?.username ?: "anonymous")
            ps.setString(2, user?.role ?: "none")
            ps.setString(3, endpoint)
            ps.setString(4, action)
            ps.executeUpdate()
        }
    }

    fun listUsers(): List<UserRecord> {
        val list = mutableListOf<UserRecord>()
        conn.createStatement().use { st ->
            st.executeQuery("SELECT username, role FROM users ORDER BY username").use { rs ->
                while (rs.next()) list += UserRecord(rs.getString(1), rs.getString(2))
            }
        }
        return list
    }

    fun createUser(username: String, password: String, role: String): Boolean {
        if (username.isBlank() || password.isBlank() || role.isBlank()) return false
        return conn.prepareStatement("INSERT OR IGNORE INTO users(username, password, role) VALUES (?, ?, ?)").use { ps ->
            ps.setString(1, username.trim())
            ps.setString(2, password.trim())
            ps.setString(3, role.trim().lowercase())
            ps.executeUpdate() > 0
        }
    }

    fun listAudit(limit: Int = 200): List<AuditRecord> {
        val list = mutableListOf<AuditRecord>()
        conn.prepareStatement(
            "SELECT username, role, endpoint, action, created_at FROM audit_logs ORDER BY id DESC LIMIT ?",
        ).use { ps ->
            ps.setInt(1, limit.coerceIn(1, 1000))
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    list += AuditRecord(
                        username = rs.getString(1),
                        role = rs.getString(2),
                        endpoint = rs.getString(3),
                        action = rs.getString(4),
                        createdAt = rs.getString(5),
                    )
                }
            }
        }
        return list
    }

    private fun ensureSchema() {
        conn.createStatement().use { st ->
            st.execute(
                "CREATE TABLE IF NOT EXISTS users(" +
                    "username TEXT PRIMARY KEY, password TEXT NOT NULL, role TEXT NOT NULL)",
            )
            st.execute(
                "CREATE TABLE IF NOT EXISTS audit_logs(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT NOT NULL, role TEXT NOT NULL, endpoint TEXT NOT NULL, action TEXT NOT NULL, created_at TEXT NOT NULL)",
            )
        }
    }

    private fun ensureDefaultAdmin() {
        conn.prepareStatement("INSERT OR IGNORE INTO users(username, password, role) VALUES ('admin', 'admin123', 'admin')").use { it.executeUpdate() }
    }
}

enum class Permission { READ, WRITE, EXPORT, ADMIN }
