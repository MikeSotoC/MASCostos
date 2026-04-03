package com.uchi.mascostos.app.service

import com.uchi.mascostos.app.api.PresupuestoSheetApi
import com.uchi.mascostos.app.dto.PresupuestoSheetRowDto
import com.uchi.mascostos.app.dto.PresupuestoSheetRowType
import com.uchi.mascostos.core.ports.BaseCostosRepository
import com.uchi.mascostos.core.ports.PartidaJerarquiaNode
import com.uchi.mascostos.core.ports.ProyectoPresupuestoRepository

class PresupuestoSheetApiImpl(
    private val proyectoPresupuestoRepository: ProyectoPresupuestoRepository,
    private val baseCostosRepository: BaseCostosRepository
) : PresupuestoSheetApi {

    override fun listarHojaPresupuesto(
        proyectoId: Long,
        subpresupuestoId: Long,
        subpresupuestoCodigo: String?,
        subpresupuestoNombre: String
    ): List<PresupuestoSheetRowDto> {
        val partidas = proyectoPresupuestoRepository.listarPartidasProyecto(proyectoId, subpresupuestoId)
        val rows = mutableListOf<PresupuestoSheetRowDto>()

        val prefijoSub = (subpresupuestoCodigo?.padStart(2, '0') ?: "01").takeLast(2)

        rows += PresupuestoSheetRowDto(
            tipo = PresupuestoSheetRowType.TITULO,
            itemVisual = prefijoSub,
            descripcion = subpresupuestoNombre.uppercase(),
            unidad = null,
            metrado = null,
            precioUnitario = null,
            parcial = null,
            nivel = 0
        )

        var subtituloActualCodigo: String? = null
        var grupoActualCodigo: String? = null

        var subtituloIndex = 0
        var grupoIndex = 0
        var partidaIndex = 0

        partidas.forEach { partida ->
            val cod = partida.codPartidaBase ?: return@forEach
            val ancestros = baseCostosRepository.listarAncestrosPartida(cod)

            val niveles = resolverNiveles(
                ancestros = ancestros,
                subpresupuestoNombre = subpresupuestoNombre,
                descripcionPartida = partida.descripcion
            )

            val subtitulo = niveles.first
            val grupo = niveles.second

            if (subtitulo != null && subtitulo.codPartida != subtituloActualCodigo) {
                subtituloActualCodigo = subtitulo.codPartida
                grupoActualCodigo = null
                subtituloIndex++
                grupoIndex = 0
                partidaIndex = 0

                rows += PresupuestoSheetRowDto(
                    tipo = PresupuestoSheetRowType.SUBTITULO,
                    itemVisual = "%s.%02d".format(prefijoSub, subtituloIndex),
                    descripcion = subtitulo.descripcion.uppercase(),
                    unidad = null,
                    metrado = null,
                    precioUnitario = null,
                    parcial = null,
                    nivel = 1
                )
            }

            if (grupo != null && grupo.codPartida != grupoActualCodigo) {
                grupoActualCodigo = grupo.codPartida
                grupoIndex++
                partidaIndex = 0

                rows += PresupuestoSheetRowDto(
                    tipo = PresupuestoSheetRowType.GRUPO,
                    itemVisual = "%s.%02d.%02d".format(prefijoSub, subtituloIndex, grupoIndex),
                    descripcion = grupo.descripcion.uppercase(),
                    unidad = null,
                    metrado = null,
                    precioUnitario = null,
                    parcial = null,
                    nivel = 2
                )
            }

            partidaIndex++

            rows += PresupuestoSheetRowDto(
                tipo = PresupuestoSheetRowType.PARTIDA,
                itemVisual = "%s.%02d.%02d.%02d".format(prefijoSub, subtituloIndex, grupoIndex, partidaIndex),
                descripcion = partida.descripcion,
                unidad = partida.unidad,
                metrado = partida.metrado,
                precioUnitario = partida.precioUnitario,
                parcial = partida.parcial,
                nivel = 3,
                codPartidaBase = partida.codPartidaBase,
                proyectoPartidaId = partida.id
            )
        }

        return rows
    }

    private fun resolverNiveles(
        ancestros: List<PartidaJerarquiaNode>,
        subpresupuestoNombre: String,
        descripcionPartida: String
    ): Pair<PartidaJerarquiaNode?, PartidaJerarquiaNode?> {
        val ignorar = setOf(
            "PARTIDAS",
            "PARTIDA",
            "EDIFICACIONES",
            subpresupuestoNombre.trim().uppercase(),
            descripcionPartida.trim().uppercase()
        )

        val utiles = ancestros.filter { node ->
            val desc = node.descripcion.trim().uppercase()
            desc.isNotBlank() && desc !in ignorar
        }

        val subtitulo = utiles.getOrNull(0)
        val grupo = utiles.getOrNull(1) ?: utiles.getOrNull(0)

        return subtitulo to grupo
    }
}