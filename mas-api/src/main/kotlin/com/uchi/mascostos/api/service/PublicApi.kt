package com.uchi.mascostos.api.service

import com.uchi.mascostos.api.model.BudgetResult
import com.uchi.mascostos.api.model.ProjectRef

interface ProjectGateway {
    fun createProject(name: String, location: String?): ProjectRef
    fun listProjects(): List<ProjectRef>
    fun addProjectItem(projectId: String, costCode: String, quantity: Double)
}

interface BudgetGateway {
    fun estimate(projectId: String): BudgetResult
}
