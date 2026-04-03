package com.uchi.mascostos.ui.desktop.ui

import com.uchi.mascostos.app.api.AppApi
import com.uchi.mascostos.app.command.CrearProyectoCommand
import com.uchi.mascostos.app.dto.PartidaCatalogoBrechaDto
import com.uchi.mascostos.app.dto.PresupuestoSheetRowDto
import com.uchi.mascostos.app.dto.PresupuestoSheetRowType
import com.uchi.mascostos.app.dto.ProyectoDto
import com.uchi.mascostos.app.dto.ProyectoPartidaDetalleDto
import com.uchi.mascostos.app.dto.SubpresupuestoDto
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox

class MainWindowController {

    @FXML
    private lateinit var btnNuevoProyecto: Button

    @FXML
    private lateinit var btnActualizar: Button

    @FXML
    private lateinit var explorerTree: TreeView<BudgetExplorerNode>

    @FXML
    private lateinit var sheetContainer: AnchorPane

    @FXML
    private lateinit var detailTabs: TabPane

    private lateinit var appApi: AppApi

    private val partidasTable = TableView<PresupuestoSheetRowDto>()
    private val analisisTable = TableView<ProyectoPartidaDetalleDto>()
    private val brechasTable = TableView<PartidaCatalogoBrechaDto>()
    private val resumenArea = TextArea()

    private val partidasData: ObservableList<PresupuestoSheetRowDto> = FXCollections.observableArrayList()
    private val analisisData: ObservableList<ProyectoPartidaDetalleDto> = FXCollections.observableArrayList()
    private val brechasData: ObservableList<PartidaCatalogoBrechaDto> = FXCollections.observableArrayList()

    private var currentProyecto: ProyectoDto? = null
    private var currentSubpresupuesto: SubpresupuestoDto? = null

    fun setAppApi(appApi: AppApi) {
        this.appApi = appApi
        configureShell()
        loadData()
    }

    @FXML
    fun initialize() {
        btnNuevoProyecto.setOnAction { mostrarDialogoNuevoProyecto() }
        btnActualizar.setOnAction { loadData(currentProyecto?.id) }
    }

    private fun configureShell() {
        buildExplorer()
        buildPartidasTable()
        buildAnalisisTable()
        buildBrechasTable()
        buildDetailTabs()

        sheetContainer.children.setAll(partidasTable)
        AnchorPane.setTopAnchor(partidasTable, 0.0)
        AnchorPane.setRightAnchor(partidasTable, 0.0)
        AnchorPane.setBottomAnchor(partidasTable, 0.0)
        AnchorPane.setLeftAnchor(partidasTable, 0.0)
    }

    private fun buildExplorer() {
        explorerTree.isShowRoot = false
        explorerTree.cellFactory = javafx.util.Callback { BudgetExplorerTreeCell() }
        explorerTree.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            onExplorerSelected(selected)
        }
    }

    private fun buildPartidasTable() {
        partidasTable.items = partidasData
        partidasTable.isEditable = false
        partidasTable.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        partidasTable.styleClass.add("sheet-table")

        val colItem = TableColumn<PresupuestoSheetRowDto, String>("Item").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.itemVisual)
            }
            prefWidth = 90.0
        }

        val colDescripcion = TableColumn<PresupuestoSheetRowDto, String>("Descripción").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.descripcion)
            }
            prefWidth = 430.0
            setCellFactory {
                object : TableCell<PresupuestoSheetRowDto, String>() {
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)

                        if (empty || item == null || index < 0 || index >= tableView.items.size) {
                            text = null
                            style = ""
                            graphic = null
                            return
                        }

                        val row = tableView.items[index]
                        text = "    ".repeat(row.nivel) + item

                        style = when (row.tipo) {
                            PresupuestoSheetRowType.TITULO ->
                                "-fx-font-weight: bold; -fx-text-fill: #c0392b;"
                            PresupuestoSheetRowType.SUBTITULO ->
                                "-fx-font-weight: bold; -fx-text-fill: #1f4ed8;"
                            PresupuestoSheetRowType.GRUPO ->
                                "-fx-font-weight: bold; -fx-text-fill: #e67e22;"
                            PresupuestoSheetRowType.PARTIDA ->
                                ""
                        }
                    }
                }
            }
        }

        val colUnidad = TableColumn<PresupuestoSheetRowDto, String>("Und").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.unidad ?: "")
            }
            prefWidth = 70.0
        }

        val colMetrado = TableColumn<PresupuestoSheetRowDto, String>("Metrado").apply {
            setCellValueFactory { cell ->
                val row = cell.value
                SimpleStringProperty(
                    if (row.tipo == PresupuestoSheetRowType.PARTIDA) (row.metrado ?: 0.0).toString() else ""
                )
            }
            prefWidth = 95.0
        }

        val colPrecio = TableColumn<PresupuestoSheetRowDto, String>("Precio (S/.)").apply {
            setCellValueFactory { cell ->
                val row = cell.value
                SimpleStringProperty(
                    if (row.tipo == PresupuestoSheetRowType.PARTIDA) (row.precioUnitario ?: 0.0).toString() else ""
                )
            }
            prefWidth = 115.0
        }

        val colParcial = TableColumn<PresupuestoSheetRowDto, String>("Parcial (S/.)").apply {
            setCellValueFactory { cell ->
                val row = cell.value
                SimpleStringProperty(
                    if (row.tipo == PresupuestoSheetRowType.PARTIDA) (row.parcial ?: 0.0).toString() else ""
                )
            }
            prefWidth = 125.0
        }

        partidasTable.columns.setAll(
            colItem,
            colDescripcion,
            colUnidad,
            colMetrado,
            colPrecio,
            colParcial
        )

        partidasTable.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            onSheetRowSelected(selected)
        }
    }

    private fun buildAnalisisTable() {
        analisisTable.items = analisisData
        analisisTable.isEditable = true
        analisisTable.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        analisisTable.styleClass.add("sheet-table")

        val colCodigo = TableColumn<ProyectoPartidaDetalleDto, String>("Código").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.codInsumoBase ?: "")
            }
            prefWidth = 140.0
        }

        val colDescripcion = TableColumn<ProyectoPartidaDetalleDto, String>("Descripción / Recurso").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.descripcion)
            }
            prefWidth = 430.0
        }

        val colUnidad = TableColumn<ProyectoPartidaDetalleDto, String>("Und").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.unidad ?: "")
            }
            prefWidth = 70.0
        }

        val colCantidad = TableColumn<ProyectoPartidaDetalleDto, Double>("Cantidad").apply {
            setCellValueFactory { cell ->
                SimpleObjectProperty(cell.value.cantidad)
            }
            cellFactory = TextFieldTableCell.forTableColumn(DoubleStringConverter())
            prefWidth = 95.0
            setOnEditCommit { event ->
                val row = event.rowValue
                val nuevaCantidad = event.newValue ?: 0.0
                val nuevoParcial = nuevaCantidad * row.precioUnitario

                val actualizada = row.copy(
                    cantidad = nuevaCantidad,
                    parcial = nuevoParcial
                )

                val index = event.tablePosition.row
                analisisData[index] = actualizada
                recalcularPartidaDesdeAnalisis()
            }
        }

        val colPrecio = TableColumn<ProyectoPartidaDetalleDto, Double>("Precio (S/.)").apply {
            setCellValueFactory { cell ->
                SimpleObjectProperty(cell.value.precioUnitario)
            }
            cellFactory = TextFieldTableCell.forTableColumn(DoubleStringConverter())
            prefWidth = 115.0
            setOnEditCommit { event ->
                val row = event.rowValue
                val nuevoPrecio = event.newValue ?: 0.0
                val nuevoParcial = row.cantidad * nuevoPrecio

                val actualizada = row.copy(
                    precioUnitario = nuevoPrecio,
                    parcial = nuevoParcial
                )

                val index = event.tablePosition.row
                analisisData[index] = actualizada
                recalcularPartidaDesdeAnalisis()
            }
        }

        val colParcial = TableColumn<ProyectoPartidaDetalleDto, Double>("Parcial (S/.)").apply {
            setCellValueFactory { cell ->
                SimpleObjectProperty(cell.value.parcial)
            }
            prefWidth = 125.0
        }

        analisisTable.columns.setAll(
            colCodigo,
            colDescripcion,
            colUnidad,
            colCantidad,
            colPrecio,
            colParcial
        )

        resumenArea.isEditable = false
        resumenArea.isWrapText = true
        resumenArea.styleClass.add("summary-area")
    }

    private fun buildBrechasTable() {
        brechasTable.items = brechasData
        brechasTable.isEditable = false
        brechasTable.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        brechasTable.styleClass.add("sheet-table")

        val colCodigo = TableColumn<PartidaCatalogoBrechaDto, String>("Código").apply {
            setCellValueFactory { SimpleStringProperty(it.value.codPartidaBase) }
            prefWidth = 120.0
        }

        val colCatalogo = TableColumn<PartidaCatalogoBrechaDto, String>("Catálogo").apply {
            setCellValueFactory { SimpleStringProperty(it.value.descripcionCatalogo ?: "(sin catálogo)") }
            prefWidth = 320.0
        }

        val colProyecto = TableColumn<PartidaCatalogoBrechaDto, String>("Proyecto (técnico)").apply {
            setCellValueFactory { SimpleStringProperty(it.value.descripcionProyecto) }
            prefWidth = 200.0
        }

        val colEstado = TableColumn<PartidaCatalogoBrechaDto, String>("Estado").apply {
            setCellValueFactory { cell ->
                val value = when {
                    !cell.value.catalogoEncontrado -> "SIN CATALOGO"
                    cell.value.requiereNormalizacion -> "REQUIERE NORMALIZACION"
                    else -> "OK"
                }
                SimpleStringProperty(value)
            }
            prefWidth = 180.0
        }

        brechasTable.columns.setAll(colCodigo, colCatalogo, colProyecto, colEstado)
    }

    private fun buildDetailTabs() {
        detailTabs.tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        detailTabs.tabs.clear()

        val analisisTab = Tab("Análisis")
        analisisTab.content = VBox(analisisTable).apply {
            VBox.setVgrow(analisisTable, Priority.ALWAYS)
        }

        val resumenTab = Tab("Resumen")
        resumenTab.content = VBox(resumenArea).apply {
            VBox.setVgrow(resumenArea, Priority.ALWAYS)
        }

        val brechasTab = Tab("Brechas catálogo")
        brechasTab.content = VBox(brechasTable).apply {
            VBox.setVgrow(brechasTable, Priority.ALWAYS)
        }

        detailTabs.tabs.addAll(analisisTab, brechasTab, resumenTab)
    }

    private fun loadData(selectProjectId: Long? = null) {
        if (!::appApi.isInitialized) return

        partidasData.clear()
        analisisData.clear()
        currentProyecto = null
        currentSubpresupuesto = null
        resumenArea.text = ""

        val root = TreeItem<BudgetExplorerNode>(BudgetExplorerNode.Root)

        val proyectos = appApi.proyectoApi.listarProyectos()
        proyectos.forEach { proyecto ->
            val proyectoItem = TreeItem<BudgetExplorerNode>(BudgetExplorerNode.ProyectoNode(proyecto))
            proyectoItem.isExpanded = true

            val subs = appApi.presupuestoApi.listarSubpresupuestosProyecto(proyecto.id)
            subs.forEach { sub ->
                proyectoItem.children += TreeItem(
                    BudgetExplorerNode.SubpresupuestoNode(proyecto, sub)
                )
            }

            root.children += proyectoItem
        }

        explorerTree.root = root

        if (root.children.isEmpty()) {
            resumenArea.text = "No hay proyectos."
            return
        }

        val selectedProjectItem =
            if (selectProjectId != null) {
                root.children.firstOrNull { item ->
                    val node = item.value as? BudgetExplorerNode.ProyectoNode
                    node?.proyecto?.id == selectProjectId
                }
            } else {
                null
            } ?: root.children.first()

        if (selectedProjectItem.children.isNotEmpty()) {
            explorerTree.selectionModel.select(selectedProjectItem.children.first())
        } else {
            explorerTree.selectionModel.select(selectedProjectItem)
        }
    }

    private fun onExplorerSelected(treeItem: TreeItem<BudgetExplorerNode>?) {
        val node = treeItem?.value ?: return

        when (node) {
            is BudgetExplorerNode.ProyectoNode -> {
                currentProyecto = node.proyecto
                currentSubpresupuesto = null
                partidasData.clear()
                analisisData.clear()
                brechasData.clear()

                resumenArea.text = buildString {
                    appendLine("Proyecto: ${node.proyecto.nombre}")
                    appendLine("Cliente: ${node.proyecto.cliente ?: "-"}")
                    appendLine("Ubicación: ${node.proyecto.ubicacion ?: "-"}")
                    appendLine("Estado: ${node.proyecto.estado}")
                }
            }

            is BudgetExplorerNode.SubpresupuestoNode -> {
                currentProyecto = node.proyecto
                currentSubpresupuesto = node.subpresupuesto
                analisisData.clear()

                val subId = node.subpresupuesto.id ?: return
                val rows = appApi.presupuestoSheetApi.listarHojaPresupuesto(
                    proyectoId = node.proyecto.id,
                    subpresupuestoId = subId,
                    subpresupuestoCodigo = node.subpresupuesto.codSubpresupuesto,
                    subpresupuestoNombre = node.subpresupuesto.nombre
                )
                partidasData.setAll(rows)

                val brechas = appApi.presupuestoApi.detectarBrechasCatalogo(node.proyecto.id, subId)
                brechasData.setAll(brechas)

                resumenArea.text = buildString {
                    appendLine("Proyecto: ${node.proyecto.nombre}")
                    appendLine("Subpresupuesto: ${node.subpresupuesto.nombre}")
                    appendLine("Código: ${node.subpresupuesto.codSubpresupuesto ?: "-"}")
                    appendLine("Partidas: ${rows.count { it.tipo == PresupuestoSheetRowType.PARTIDA }}")
                    appendLine("Brechas catálogo: ${brechas.count { it.requiereNormalizacion || !it.catalogoEncontrado }}")
                }

                if (rows.isNotEmpty()) {
                    partidasTable.selectionModel.selectFirst()
                }
            }

            BudgetExplorerNode.Root -> Unit
        }
    }

    private fun onSheetRowSelected(row: PresupuestoSheetRowDto?) {
        analisisData.clear()

        if (row == null) return

        if (row.tipo != PresupuestoSheetRowType.PARTIDA) {
            resumenArea.text = buildString {
                appendLine("Item: ${row.itemVisual}")
                appendLine("Descripción: ${row.descripcion}")
                appendLine("Tipo: ${row.tipo}")
            }
            return
        }

        val partidaId = row.proyectoPartidaId ?: return
        val detalle = appApi.partidaApi.listarDetallePartidaProyecto(partidaId)
        analisisData.setAll(detalle)

        actualizarResumenSheetRow(row)
    }

    private fun actualizarResumenSheetRow(row: PresupuestoSheetRowDto) {
        val totalAnalisis = analisisData.sumOf { it.parcial }

        resumenArea.text = buildString {
            appendLine("Proyecto: ${currentProyecto?.nombre ?: "-"}")
            appendLine("Subpresupuesto: ${currentSubpresupuesto?.nombre ?: "-"}")
            appendLine("Item: ${row.itemVisual}")
            appendLine("Código partida: ${row.codPartidaBase ?: "-"}")
            appendLine("Descripción: ${row.descripcion}")
            appendLine("Unidad: ${row.unidad ?: "-"}")
            appendLine("Metrado: ${row.metrado ?: 0.0}")
            appendLine("Precio unitario: ${row.precioUnitario ?: 0.0}")
            appendLine("Parcial: ${row.parcial ?: 0.0}")
            appendLine()
            appendLine("Recursos: ${analisisData.size}")
            appendLine("Total análisis: $totalAnalisis")
        }
    }

    private fun recalcularPartidaDesdeAnalisis() {
        val partida = partidasTable.selectionModel.selectedItem ?: return
        val index = partidasTable.selectionModel.selectedIndex
        if (index < 0) return
        if (partida.tipo != PresupuestoSheetRowType.PARTIDA) return

        val nuevoPrecio = analisisData.sumOf { it.parcial }
        val nuevoParcial = (partida.metrado ?: 0.0) * nuevoPrecio

        val actualizada = partida.copy(
            precioUnitario = nuevoPrecio,
            parcial = nuevoParcial
        )

        partidasData[index] = actualizada
        partidasTable.selectionModel.select(index)
        actualizarResumenSheetRow(actualizada)
    }

    private fun mostrarDialogoNuevoProyecto() {
        if (!::appApi.isInitialized) return

        val dialog = Dialog<CrearProyectoCommand>()
        dialog.title = "Nuevo proyecto"
        dialog.headerText = "Crear nuevo proyecto"

        val btnCrear = ButtonType("Crear", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.addAll(btnCrear, ButtonType.CANCEL)

        val txtCodigo = TextField()
        val txtNombre = TextField()
        val txtCliente = TextField()
        val txtUbicacion = TextField()

        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            add(Label("Código"), 0, 0)
            add(txtCodigo, 1, 0)
            add(Label("Nombre"), 0, 1)
            add(txtNombre, 1, 1)
            add(Label("Cliente"), 0, 2)
            add(txtCliente, 1, 2)
            add(Label("Ubicación"), 0, 3)
            add(txtUbicacion, 1, 3)
        }

        dialog.dialogPane.content = grid

        dialog.setResultConverter { button ->
            if (button == btnCrear) {
                val codigo = txtCodigo.text.trim()
                val nombre = txtNombre.text.trim()

                if (codigo.isBlank() || nombre.isBlank()) {
                    null
                } else {
                    CrearProyectoCommand(
                        codigo = codigo,
                        nombre = nombre,
                        cliente = txtCliente.text.trim().ifBlank { null },
                        ubicacion = txtUbicacion.text.trim().ifBlank { null }
                    )
                }
            } else {
                null
            }
        }

        val result = dialog.showAndWait()
        if (result.isPresent) {
            val creado = appApi.proyectoApi.crearProyecto(result.get())
            loadData(creado.id)
        }
    }
}