package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.datos.ReporteDAO;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.FilaReporteComision;
import cr.ac.una.mahn.servicio.PdfServicio;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.ImagenesFX;
import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import cr.ac.una.mahn.vista.Formulario;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

/**
 * Reporte de la cantidad total de comisiones que se le debe pagar a cada marca de
 * tarjeta en un rango de fechas, con exportación a PDF.
 */
public class ReporteComisionesController {

    @FXML private Label lblFecha;
    @FXML private HBox contenedorFechas;
    @FXML private TableView<FilaReporteComision> tabla;
    @FXML private Label lblTransacciones;
    @FXML private Label lblProcesado;
    @FXML private Label lblComision;
    @FXML private BarChart<String, Number> grafico;
    @FXML private Button btnPdf;

    private final DatePicker dpDesde = Formulario.selectorFecha(null);
    private final DatePicker dpHasta = Formulario.selectorFecha(LocalDate.now());
    private final ReporteDAO reporteDAO = new ReporteDAO();
    private List<FilaReporteComision> filas;
    private LocalDate desdeGenerado;
    private LocalDate hastaGenerado;

    @FXML
    private void initialize() {
        lblFecha.setText(Formatos.fechaLarga(LocalDate.now()));
        dpDesde.setPrefWidth(150);
        dpHasta.setPrefWidth(150);
        contenedorFechas.getChildren().setAll(etiqueta("Desde:"), dpDesde, etiqueta("Hasta:"), dpHasta);
        tabla.setPlaceholder(new Label("Seleccione el rango de fechas y presione GENERAR."));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().setAll(List.of(
                Tablas.texto("Tarjeta", FilaReporteComision::tarjeta, 200),
                Tablas.porcentaje("% vigente", FilaReporteComision::porcentajeVigente, 110),
                Tablas.entero("Transacciones", FilaReporteComision::transacciones, 120),
                Tablas.moneda("Monto procesado", FilaReporteComision::montoProcesado, 160),
                Tablas.moneda("Comisión a pagar", FilaReporteComision::comision, 160)));
        btnPdf.setDisable(true);
        Mensajes.intentar(() -> {
            LocalDate primera = reporteDAO.primeraFechaComision();
            dpDesde.setValue(primera == null ? LocalDate.now().withDayOfMonth(1) : primera);
            generar();
        });
    }

    private static Label etiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta-campo");
        return etiqueta;
    }

    @FXML
    private void generar() {
        Mensajes.intentar(() -> {
            LocalDate desde = Validador.fecha(dpDesde, "Desde");
            LocalDate hasta = Validador.fecha(dpHasta, "Hasta");
            if (desde.isAfter(hasta)) {
                throw new ValidacionException("La fecha «Desde» (" + Formatos.fecha(desde)
                        + ") no puede ser posterior a la fecha «Hasta» (" + Formatos.fecha(hasta) + ").");
            }
            filas = reporteDAO.comisiones(desde, hasta);
            desdeGenerado = desde;
            hastaGenerado = hasta;
            tabla.getItems().setAll(filas);
            int transacciones = filas.stream().mapToInt(FilaReporteComision::transacciones).sum();
            BigDecimal procesado = filas.stream().map(FilaReporteComision::montoProcesado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal comision = filas.stream().map(FilaReporteComision::comision).reduce(BigDecimal.ZERO, BigDecimal::add);
            lblTransacciones.setText(String.valueOf(transacciones));
            lblProcesado.setText(Formatos.moneda(procesado));
            lblComision.setText(Formatos.moneda(comision));
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            for (FilaReporteComision fila : filas) {
                serie.getData().add(new XYChart.Data<>(fila.tarjeta(), fila.comision()));
            }
            grafico.getData().setAll(List.of(serie));
            btnPdf.setDisable(false);
        });
    }

    @FXML
    private void exportarPdf() {
        Mensajes.intentar(() -> {
            if (filas == null) {
                throw new ValidacionException("Primero genere el reporte con el botón GENERAR.");
            }
            Path destino = Path.of("generados", "reportes",
                    "comisiones_" + desdeGenerado + "_a_" + hastaGenerado + ".pdf");
            Path pdf = new PdfServicio().generarReporteComisiones(desdeGenerado, hastaGenerado, filas,
                    ImagenesFX.capturar(grafico, 2), destino);
            App.abrirArchivo(pdf);
            Mensajes.informacion("Reporte exportado", "Se generó el reporte en PDF:\n" + pdf.toAbsolutePath());
        });
    }
}
