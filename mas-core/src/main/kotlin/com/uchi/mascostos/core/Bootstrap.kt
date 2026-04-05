package com.uchi.mascostos.core

import java.nio.file.Path

object Bootstrap {
    /**
     * Punto de entrada de dominio para levantar el motor base.
     */
    fun createDefaultEngine(sqliteFile: Path): BudgetEngine {
        return BudgetEngine(databasePath = sqliteFile)
    }
}
