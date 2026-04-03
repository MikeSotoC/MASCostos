package com.uchi.mascostos.ui.desktop.ui

import com.uchi.mascostos.app.api.AppApi
import com.uchi.mascostos.app.command.CrearProyectoCommand
import com.uchi.mascostos.app.dto.ProyectoDto
import com.uchi.mascostos.app.dto.ProyectoPartidaDetalleDto
import com.uchi.mascostos.app.dto.ProyectoPartidaDto
import com.uchi.mascostos.app.dto.SubpresupuestoDto
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.control.cell.TextFieldTableCell
import javafx.scene.layout.*
import javafx.scene.text.FontWeight
import javafx.scene.text.Text
import javafx.scene.text.TextFlow

class MainWindow(
    private val appApi: AppApi
) : BorderPane() {

    private val explorerTree = TreeView<BudgetExplorerNode>()

    private val partidasTable = TableView<ProyectoPartidaDto>()
    private val analisisTable = TableView<ProyectoPartidaDetalleDto>()

    private val partidasData: ObservableList<ProyectoPartidaDto> = FXCollections.observableArrayList()
    private val analisisData: ObservableList<ProyectoPartidaDetalleDto> = FXCollections.observableArrayList()

    private val txtCodigo = Label("-")
    private val txtNombre = Label("-")
    private val txtCliente = Label("-")
    private val txtUbicacion = Label("-")
    private val txtSubpresupuesto = Label("-")
    private val txtPartidas = Label("0")
    private val txtSeleccion = Label("-")

    private val resumenArea = TextArea()

    private var currentProyecto: ProyectoDto? = null
    private var currentSubpresupuesto: SubpresupuestoDto? = null

    init {
        styleClass.add("main-window")
        buildUi()
        loadData()
    }

    private fun buildUi() {
        top = buildToolbar()
        center = buildShell()
    }

    private fun buildToolbar(): Node {
        val title = Label("MASCostos").apply {
            styleClass.add("app-title")
        }

        val nuevoBtn = Button("Nuevo proyecto").apply {
            styleClass.add("module-button")
            setOnAction { mostrarDialogoNuevoProyecto() }
        }

        val refreshBtn = Button("Actualizar").apply {
            styleClass.add("module-button")
            setOnAction { loadData(currentProyecto?.id) }
        }

        return HBox(8.0, title, nuevoBtn, refreshBtn).apply {
            alignment = Pos.CENTER_LEFT
            styleClass.add("window-toolbar")
        }
    }

    private fun buildShell(): Node {
        buildExplorer()
        buildPartidasTable()
        buildAnalisisTable()

        val leftSidebar = VBox(
            10.0,
            buildModuleStrip(),
            buildPanel("Presupuestos", explorerTree)
        ).apply {
            prefWidth = 300.0
            minWidth = 260.0
            styleClass.add("sidebar")
            VBox.setVgrow(children[1], Priority.ALWAYS)
        }

        val rightTop = VBox(
            10.0,
            buildGeneralPane(),
            buildPanel("Hoja del presupuesto", partidasTable)
        ).apply {
            VBox.setVgrow(children[1], Priority.ALWAYS)
        }

        val analisisTab = Tab("Análisis / APU", buildPanelBody(analisisTable)).apply {
            isClosable = false
        }

        val resumenTab = Tab("Resumen", buildPanelBody(resumenArea)).apply {
            isClosable = false
        }

        val bottomTabs = TabPane(analisisTab, resumenTab).apply {
            styleClass.add("detail-tabs")
            tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
        }

        val rightSplit = SplitPane().apply {
            orientation = Orientation.VERTICAL
            items.addAll(rightTop, bottomTabs)
            setDividerPositions(0.58)
        }

        val shell = SplitPane().apply {
            items.addAll(leftSidebar, rightSplit)
            setDividerPositions(0.22)
        }

        HBox.setHgrow(shell, Priority.ALWAYS)
        VBox.setVgrow(shell, Priority.ALWAYS)

        return shell
    }

    private fun buildModuleStrip(): Node {
        val btnProyectos = Button("Proyectos").apply { styleClass.add("module-button") }
        val btnPresupuesto = Button("Presupuesto").apply { styleClass.add("module-button") }
        val btnAnalisis = Button("Análisis").apply { styleClass.add("module-button") }
        val btnPrecios = Button("Precios").apply { styleClass.add("module-button") }
        val btnReportes = Button("Reportes").apply { styleClass.add("module-button") }

        return VBox(6.0, btnProyectos, btnPresupuesto, btnAnalisis, btnPrecios, btnReportes).apply {
            styleClass.add("module-strip")
        }
    }

    private fun buildGeneralPane(): Node {
        val grid = GridPane().apply {
            styleClass.add("general-pane")
            hgap = 14.0
            vgap = 6.0
            add(buildGeneralLabel("Código"), 0, 0)
            add(buildGeneralValue(txtCodigo), 1, 0)

            add(buildGeneralLabel("Descripción"), 2, 0)
            add(buildGeneralValue(txtNombre), 3, 0)

            add(buildGeneralLabel("Cliente"), 0, 1)
            add(buildGeneralValue(txtCliente), 1, 1)

            add(buildGeneralLabel("Ubicación"), 2, 1)
            add(buildGeneralValue(txtUbicacion), 3, 1)

            add(buildGeneralLabel("Subpresupuesto"), 0, 2)
            add(buildGeneralValue(txtSubpresupuesto), 1, 2)

            add(buildGeneralLabel("Partidas"), 2, 2)
            add(buildGeneralValue(txtPartidas), 3, 2)

            add(buildGeneralLabel("Selección"), 0, 3)
            add(buildGeneralValue(txtSeleccion), 1, 3, 3, 1)

            columnConstraints.addAll(
                ColumnConstraints().apply { prefWidth = 90.0 },
                ColumnConstraints().apply { hgrow = Priority.ALWAYS },
                ColumnConstraints().apply { prefWidth = 90.0 },
                ColumnConstraints().apply { hgrow = Priority.ALWAYS }
            )
        }

        return grid
    }

    private fun buildGeneralLabel(text: String): Label {
        return Label(text).apply {
            styleClass.add("general-label")
        }
    }

    private fun buildGeneralValue(label: Label): Node {
        label.styleClass.add("general-value")
        return label
    }

    private fun buildExplorer() {
        explorerTree.isShowRoot = false
        explorerTree.cellFactory = javafx.util.Callback {
            BudgetExplorerTreeCell()
        }
        explorerTree.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            onExplorerSelected(selected)
        }
    }

    private fun buildPartidasTable() {
        partidasTable.items = partidasData
        partidasTable.isEditable = true
        partidasTable.columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        partidasTable.styleClass.add("sheet-table")

        val colCodigo = TableColumn<ProyectoPartidaDto, String>("Código").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.codPartidaBase ?: "")
            }
            prefWidth = 130.0
        }

        val colDescripcion = TableColumn<ProyectoPartidaDto, String>("Descripción").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.descripcion)
            }
            prefWidth = 420.0
        }

        val colUnidad = TableColumn<ProyectoPartidaDto, String>("Und").apply {
            setCellValueFactory { cell ->
                SimpleStringProperty(cell.value.unidad ?: "")
            }
            prefWidth = 70.0
        }

        val colMetrado = TableColumn<ProyectoPartidaDto, Double>("Metrado").apply {
            setCellValueFactory { cell ->
                SimpleObjectProperty(cell.value.metrado)
            }
            cellFactory = TextFieldTableCell.forTableColumn(DoubleStringConverter())
            prefWidth = 90.0
            styleClass.add("numeric-column")
            setOnEditCommit { event ->
                val row = event.rowValue
                val nuevoMetrado = event.newValue ?: 0.0
                val nuevoParcial = nuevoMetrado * row.precioUnitario

                val actualizada = row.copy(
                    metrado = nuevoMetrado,
                    parcial = nuevoParcial
                )

                val index = event.tablePosition.row
                partidasData[index] = actualizada
                partidasTable.selectionModel.select(index)
                actualizarResumenPartida(actualizada)
            }
        }

        val colPrecio = TableColumn<ProyectoPartidaDto, Double>("Precio (S/.)").apply {
            setCellValueFactory { cell ->
                SimpleObjectProperty(cell.value.precioUnitario)
            }
            prefWidth = 110.0
            styleClass.add("numeric-column")
        }

        val colParcial = TableColumn<ProyectoPartidaDto, Double>("Parcial (S/.)").apply {
            setCellValueFactory { cell ->
                SimpleObjectProperty(cell.value.parcial)
            }
            prefWidth = 120.0
            styleClass.add("numeric-column")
        }

        partidasTable.columns.setAll(
            colCodigo,
            colDescripcion,
            colUnidad,
            colMetrado,
            colPrecio,
            colParcial
        )

        partidasTable.selectionModel.selectedItemProperty().addListener { _, _, selected ->
            onPartidaSelected(selected)
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
            prefWidth = 420.0
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
            prefWidth = 90.0
            styleClass.add("numeric-column")
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
            prefWidth = 110.0
            styleClass.add("numeric-column")
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
            prefWidth = 120.0
            styleClass.add("numeric-column")
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

    private fun buildPanel(title: String, body: Node): Node {
        return VBox(
            Label(title).apply { styleClass.add("section-title") },
            buildPanelBody(body)
        ).apply {
            styleClass.add("panel-pane")
            VBox.setVgrow(children[1], Priority.ALWAYS)
        }
    }

    private fun buildPanelBody(body: Node): Node {
        return StackPane(body).apply {
            styleClass.add("panel-body")
            padding = Insets(8.0)
            if (body is Region) {
                body.maxWidth = Double.MAX_VALUE
                body.maxHeight = Double.MAX_VALUE
            }
        }
    }

    private fun loadData(selectProjectId: Long? = null) {
        val proyectos = appApi.proyectoApi.listarProyectos()

        partidasData.clear()
        analisisData.clear()
        currentProyecto = null
        currentSubpresupuesto = null

        val root = TreeItem<BudgetExplorerNode>(BudgetExplorerNode.Root)

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
            clearGeneralInfo()
            resumenArea.text = "No hay proyectos."
            return
        }

        val selectedTreeItem =
            if (selectProjectId != null) {
                root.children.firstOrNull { item ->
                    val node = item.value as? BudgetExplorerNode.ProyectoNode
                    node?.proyecto?.id == selectProjectId
                }
            } else {
                null
            } ?: root.children.first()

        if (selectedTreeItem.children.isNotEmpty()) {
            explorerTree.selectionModel.select(selectedTreeItem.children.first())
        } else {
            explorerTree.selectionModel.select(selectedTreeItem)
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

                updateGeneralInfo(
                    proyecto = node.proyecto,
                    sub = null,
                    partidas = 0,
                    seleccion = "Proyecto"
                )

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
                val partidas = appApi.presupuestoApi.listarPartidasProyecto(node.proyecto.id, subId)
                partidasData.setAll(partidas)

                updateGeneralInfo(
                    proyecto = node.proyecto,
                    sub = node.subpresupuesto,
                    partidas = partidas.size,
                    seleccion = "Subpresupuesto"
                )

                resumenArea.text = buildString {
                    appendLine("Proyecto: ${node.proyecto.nombre}")
                    appendLine("Subpresupuesto: ${node.subpresupuesto.nombre}")
                    appendLine("Código: ${node.subpresupuesto.codSubpresupuesto ?: "-"}")
                    appendLine("Partidas: ${partidas.size}")
                }

                if (partidas.isNotEmpty()) {
                    partidasTable.selectionModel.selectFirst()
                }
            }

            BudgetExplorerNode.Root -> Unit
        }
    }

    private fun onPartidaSelected(partida: ProyectoPartidaDto?) {
        analisisData.clear()

        if (partida == null) {
            return
        }

        val detalle = appApi.partidaApi.listarDetallePartidaProyecto(partida.id)
        analisisData.setAll(detalle)

        updateGeneralInfo(
            proyecto = currentProyecto,
            sub = currentSubpresupuesto,
            partidas = partidasData.size,
            seleccion = partida.codPartidaBase ?: "-"
        )

        actualizarResumenPartida(partida)
    }

    private fun actualizarResumenPartida(partida: ProyectoPartidaDto) {
        val totalAnalisis = analisisData.sumOf { it.parcial }

        val title = Text("${partida.codPartidaBase ?: "-"}  ").apply {
            style = "-fx-font-weight: bold;"
        }
        val desc = Text(partida.descripcion)
        val flow = TextFlow(title, desc)

        val texto = buildString {
            appendLine("Proyecto: ${currentProyecto?.nombre ?: "-"}")
            appendLine("Subpresupuesto: ${currentSubpresupuesto?.nombre ?: "-"}")
            appendLine("Unidad: ${partida.unidad ?: "-"}")
            appendLine("Metrado: ${partida.metrado}")
            appendLine("Precio unitario: ${partida.precioUnitario}")
            appendLine("Parcial: ${partida.parcial}")
            appendLine()
            appendLine("Recursos: ${analisisData.size}")
            appendLine("Total análisis: $totalAnalisis")
        }

        resumenArea.text = texto
    }

    private fun recalcularPartidaDesdeAnalisis() {
        val partida = partidasTable.selectionModel.selectedItem ?: return
        val index = partidasTable.selectionModel.selectedIndex
        if (index < 0) return

        val nuevoPrecio = analisisData.sumOf { it.parcial }
        val nuevoParcial = partida.metrado * nuevoPrecio

        val actualizada = partida.copy(
            precioUnitario = nuevoPrecio,
            parcial = nuevoParcial
        )

        partidasData[index] = actualizada
        partidasTable.selectionModel.select(index)
        actualizarResumenPartida(actualizada)
    }

    private fun updateGeneralInfo(
        proyecto: ProyectoDto?,
        sub: SubpresupuestoDto?,
        partidas: Int,
        seleccion: String
    ) {
        txtCodigo.text = proyecto?.codigo ?: "-"
        txtNombre.text = proyecto?.nombre ?: "-"
        txtCliente.text = proyecto?.cliente ?: "-"
        txtUbicacion.text = proyecto?.ubicacion ?: "-"
        txtSubpresupuesto.text = sub?.let { "${it.codSubpresupuesto ?: "-"} - ${it.nombre}" } ?: "-"
        txtPartidas.text = partidas.toString()
        txtSeleccion.text = seleccion
    }

    private fun clearGeneralInfo() {
        updateGeneralInfo(null, null, 0, "-")
    }

    private fun mostrarDialogoNuevoProyecto() {
        val dialog = Dialog<CrearProyectoCommand>()
        dialog.title = "Nuevo proyecto"
        dialog.headerText = "Crear nuevo proyecto"

        val btnCrear = ButtonType("Crear", ButtonBar.ButtonData.OK_DONE)
        dialog.dialogPane.buttonTypes.addAll(btnCrear, ButtonType.CANCEL)

        val txtCodigoField = TextField()
        val txtNombreField = TextField()
        val txtClienteField = TextField()
        val txtUbicacionField = TextField()

        val grid = GridPane().apply {
            hgap = 10.0
            vgap = 10.0
            padding = Insets(16.0)

            add(Label("Código"), 0, 0)
            add(txtCodigoField, 1, 0)

            add(Label("Nombre"), 0, 1)
            add(txtNombreField, 1, 1)

            add(Label("Cliente"), 0, 2)
            add(txtClienteField, 1, 2)

            add(Label("Ubicación"), 0, 3)
            add(txtUbicacionField, 1, 3)
        }

        dialog.dialogPane.content = grid

        dialog.setResultConverter { button ->
            if (button == btnCrear) {
                val codigo = txtCodigoField.text.trim()
                val nombre = txtNombreField.text.trim()

                if (codigo.isBlank() || nombre.isBlank()) {
                    null
                } else {
                    CrearProyectoCommand(
                        codigo = codigo,
                        nombre = nombre,
                        cliente = txtClienteField.text.trim().ifBlank { null },
                        ubicacion = txtUbicacionField.text.trim().ifBlank { null }
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