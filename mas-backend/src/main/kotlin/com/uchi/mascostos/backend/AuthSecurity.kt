package com.uchi.mascostos.backend

import java.sql.Connection
import java.sql.DriverManager
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String, val role: String)
data class UserContext(val username: String, val role: String)

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

enum class Permission { READ, WRITE, EXPORT }
