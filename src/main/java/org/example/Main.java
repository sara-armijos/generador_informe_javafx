package org.example;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.List;
import java.util.Map;

public class Main extends Application {
    private final ClienteDAO dao = new ClienteDAO();
    private final ChartService chartService = new ChartService();
    private final PdfReportService pdfService = new PdfReportService();

    private List<Cliente> datosOriginales;
    private List<Cliente> datosFiltrados;

    private final TableView<Cliente> tabla = new TableView<>();
    private final TextField txtNombre = new TextField();
    private final ComboBox<String> cbCiudad = new ComboBox<>();
    private final Label lblTotal = new Label("Total clientes: 0");
    private final ImageView chartView = new ImageView();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Gestión de Clientes - Informes");

        Button btnSeleccionarCsv = new Button("Seleccionar CSV");
        Button btnExportarPdf = new Button("Exportar a PDF");
        Button btnAplicarFiltros = new Button("Aplicar filtros");
        Button btnAyuda = new Button("Ayuda");

        txtNombre.setPromptText("Nombre contiene...");
        cbCiudad.getItems().add("Todas");
        cbCiudad.setValue("Todas");


        configurarTabla();

        chartView.setFitWidth(420);
        chartView.setFitHeight(300);
        chartView.setPreserveRatio(true);

        HBox top = new HBox(10, btnSeleccionarCsv, btnExportarPdf,btnAyuda);
        top.setPadding(new Insets(10));

        HBox filtros = new HBox(10, txtNombre, cbCiudad, btnAplicarFiltros);
        filtros.setPadding(new Insets(0, 10, 10, 10));

        VBox left = new VBox(10, filtros, tabla, lblTotal);
        left.setPadding(new Insets(0, 10, 10, 10));
        left.setPrefWidth(450);

        VBox right = new VBox(10, new Label("Clientes por ciudad"), chartView);
        right.setPadding(new Insets(10));
        right.setPrefWidth(500);

        SplitPane split = new SplitPane(left, right);
        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setCenter(split);

        btnSeleccionarCsv.setOnAction(e -> seleccionarCSV(stage));
        btnAplicarFiltros.setOnAction(e -> aplicarFiltrosYActualizar());
        btnExportarPdf.setOnAction(e -> exportarPDF(stage));
        btnAyuda.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Ayuda");
            alert.setHeaderText("Generador de Informes PDF");

            alert.setContentText("""
                        Autor: Sara Armijos
                        Código fuente (GitHub):
                        https://github.com/sara-armijos/generador_informe_javafx
                        """);

            alert.showAndWait();
        });


        try {
            datosOriginales = dao.obtenerTodos();
            aplicarFiltrosYActualizar();
        } catch (Exception ex) {
            mostrarError("No se pudo cargar /clientes.csv.\nPulsa 'Seleccionar CSV' y elige el archivo.", ex);
        }

        stage.setScene(new Scene(root, 1000, 600));
        stage.show();
    }

    private void configurarTabla() {
        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colNombre.setPrefWidth(160);

        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(180);

        TableColumn<Cliente, String> colCiudad = new TableColumn<>("Ciudad");
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colCiudad.setPrefWidth(120);

        tabla.getColumns().addAll(colNombre, colEmail, colCiudad);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void seleccionarCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar clientes.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fc.showOpenDialog(stage);
        if (file == null) return;

        try {
            dao.setCsvSeleccionado(file);
            datosOriginales = dao.obtenerTodos();

            // Rellenar ComboBox ciudades
            cbCiudad.getItems().setAll("Todas");
            cbCiudad.getItems().addAll(dao.ciudadesDisponibles(datosOriginales));
            cbCiudad.setValue("Todas");

            aplicarFiltrosYActualizar();
        } catch (Exception ex) {
            mostrarError("Error cargando el CSV seleccionado.", ex);
        }
    }

    private void aplicarFiltrosYActualizar() {
        if (datosOriginales == null) return;

        datosFiltrados = dao.filtrar(datosOriginales, txtNombre.getText(), cbCiudad.getValue());

        ObservableList<Cliente> obs = FXCollections.observableArrayList(datosFiltrados);
        tabla.setItems(obs);

        lblTotal.setText("Total clientes: " + datosFiltrados.size());

        Map<String, Integer> conteo = dao.contarPorCiudad(datosFiltrados);
        Image chartImage = chartService.crearGraficoTartaComoImagen(conteo, 600, 420);
        chartView.setImage(chartImage);
    }

    private void exportarPDF(Stage stage) {
        if (datosFiltrados == null) {
            mostrarInfo("No hay datos filtrados para exportar.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar informe PDF");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fc.setInitialFileName("informe_clientes.pdf");

        File destino = fc.showSaveDialog(stage);
        if (destino == null) return;

        try {
            pdfService.exportar(destino, datosFiltrados, chartView.getImage());
            mostrarInfo("PDF generado correctamente:\n" + destino.getAbsolutePath());
        } catch (Exception ex) {
            mostrarError("Error generando el PDF.", ex);
        }
    }

    private void mostrarInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Información");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void mostrarError(String msg, Exception ex) {
        ex.printStackTrace();
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText(msg);
        a.setContentText(ex.getMessage());
        a.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}