package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.datos.MuseoDAO;
import cr.ac.una.mahn.datos.ReporteDAO;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.FilaReporteValoracion;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.servicio.PdfServicio;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.util.Validador;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableView;

/**
 * Reporte de las salas mejor valoradas por museo; se puede invertir para obtener
 * las diez peores. Se exporta a PDF.
 */
public class ReporteValoracionesController {

    @FXML private Label lblFecha;
    @FXML private ComboBox<Opcion> cmbMuseo;
    @FXML private RadioButton rbMejores;
    @FXML private Spinner<Integer> spCantidad;
    @FXML private TableView<FilaReporteValoracion> tabla;
    @FXML private Label lblResumen;
    @FXML private Button btnPdf;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private List<FilaReporteValoracion> filas;
    private String museoGenerado;
    private boolean mejoresGenerado;

    @FXML
    private void initialize() {
        lblFecha.setText(Formatos.fechaLarga(LocalDate.now()));
        spCantidad.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 10));
        spCantidad.setEditable(true);
        tabla.setPlaceholder(new Label("Seleccione el museo y presione GENERAR."));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().setAll(List.of(
                Tablas.entero("#", FilaReporteValoracion::posicion, 50),
                Tablas.texto("Sala", FilaReporteValoracion::sala, 330),
                Tablas.texto("Museo", FilaReporteValoracion::museo, 250),
                Tablas.texto("Promedio", f -> Formatos.promedio(f.promedio()) + " / 5", 90),
                Tablas.estrellas("Estrellas", FilaReporteValoracion::promedio, 130),
                Tablas.entero("Valoraciones", FilaReporteValoracion::cantidad, 110)));
        btnPdf.setDisable(true);
        Mensajes.intentar(() -> {
            List<Opcion> museos = new ArrayList<>();
            museos.add(new Opcion(null, "Todos los museos"));
            new MuseoDAO().listar().forEach(m -> museos.add(new Opcion(m.getId(), m.getNombre())));
            cmbMuseo.getItems().setAll(museos);
            cmbMuseo.getSelectionModel().select(museos.size() > 1 ? 1 : 0);
            generar();
        });
    }

    @FXML
    private void generar() {
        Mensajes.intentar(() -> {
            Opcion museo = Validador.seleccion(cmbMuseo.getValue(), "Museo");
            int cantidad = Validador.decimal(spCantidad.getEditor().getText(), "Cantidad", BigDecimal.ONE, false,
                    new BigDecimal("60"), 0).intValue();
            spCantidad.getValueFactory().setValue(cantidad);
            boolean mejores = rbMejores.isSelected();
            filas = reporteDAO.valoraciones(museo.id(), mejores, cantidad);
            museoGenerado = museo.texto();
            mejoresGenerado = mejores;
            tabla.getItems().setAll(filas);
            lblResumen.setText(filas.isEmpty() ? "No hay salas valoradas para este museo."
                    : "Las " + filas.size() + " salas " + (mejores ? "mejor" : "peor")
                    + " valoradas de: " + museo.texto() + ".");
            btnPdf.setDisable(filas.isEmpty());
        });
    }

    @FXML
    private void exportarPdf() {
        Mensajes.intentar(() -> {
            if (filas == null || filas.isEmpty()) {
                throw new ValidacionException("Primero genere un reporte con resultados.");
            }
            String marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path destino = Path.of("generados", "reportes", "valoracion_salas_"
                    + (mejoresGenerado ? "mejores_" : "peores_") + marca + ".pdf");
            Path pdf = new PdfServicio().generarReporteValoraciones(museoGenerado, mejoresGenerado, filas, destino);
            App.abrirArchivo(pdf);
            Mensajes.informacion("Reporte exportado", "Se generó el reporte en PDF:\n" + pdf.toAbsolutePath());
        });
    }
}
