package com.uchi.mascostos.data.sqlite

import java.sql.Connection
import java.sql.DriverManager

class SQLiteConnector {

    fun openBaseCostos(): Connection {
        return DriverManager.getConnection(DbPaths.BASE_COSTOS)
    }

    fun openProyectos(): Connection {
        return DriverManager.getConnection(DbPaths.PROYECTOS)
    }
}