package com.uchi.mascostos.app.api

import com.uchi.mascostos.app.dto.PresupuestoSheetRowDto

interface PresupuestoSheetApi {
    fun listarHojaPresupuesto(
        proyectoId: Long,
        subpresupuestoId: Long,
        subpresupuestoCodigo: String?,
        subpresupuestoNombre: String
    ): List<PresupuestoSheetRowDto>
}