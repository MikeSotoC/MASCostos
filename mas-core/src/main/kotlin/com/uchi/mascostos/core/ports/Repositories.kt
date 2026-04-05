package com.uchi.mascostos.core.ports

import com.uchi.mascostos.core.domain.CostItem
import com.uchi.mascostos.core.domain.Project
import com.uchi.mascostos.core.domain.ProjectItem

interface ProjectRepository {
    fun create(name: String, location: String?): Project
    fun list(): List<Project>
}

interface ProjectItemRepository {
    fun findByProject(projectId: String): List<ProjectItem>
    fun addItem(projectId: String, code: String, quantity: Double)
}

interface CostCatalogRepository {
    fun findByCodes(codes: Set<String>): List<CostItem>
}
