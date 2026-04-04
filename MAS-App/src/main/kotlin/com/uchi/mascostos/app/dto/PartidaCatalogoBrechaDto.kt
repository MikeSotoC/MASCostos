package com.uchi.mascostos.app.dto

data class PartidaCatalogoBrechaDto(
    val proyectoPartidaId: Long,
    val codPartidaBase: String,
    val descripcionProyecto: String,
    val descripcionCatalogo: String?,
    val unidadProyecto: String?,
    val unidadCatalogo: String?,
    val catalogoEncontrado: Boolean,
    val requiereNormalizacion: Boolean
)
