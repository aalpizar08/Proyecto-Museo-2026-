package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.datos.EntradaDAO;
import cr.ac.una.mahn.datos.TarjetaDAO;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.Comprobante;
import cr.ac.una.mahn.modelo.Entrada;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.servicio.ComprobanteServicio;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.vista.DialogoArchivo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

/** Historial de entradas vendidas, con la opción de volver a generar el QR y el comprobante. */
public class HistorialEntradasController {

    @FXML private Label lblFecha;
    @FXML private TextField txtFiltro;
    @FXML private ComboBox<Opcion> cmbTarjeta;
    @FXML private TableView<Entrada> tabla;
    @FXML private Label lblConteo;
    @FXML private Button btnQr;
    @FXML private Button btnPdf;

    private final EntradaDAO entradaDAO = new EntradaDAO();
    private final ComprobanteServicio comprobantes = new ComprobanteServicio();

    @FXML
    private void initialize() {
        lblFecha.setText(Formatos.fechaLarga(LocalDate.now()));
        tabla.setPlaceholder(new Label("No hay entradas que coincidan con los filtros."));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().setAll(List.of(
                Tablas.texto("Código", Entrada::getCodigo, 120),
                Tablas.texto("Visitante", Entrada::getNombreVisitante, 220),
                Tablas.texto("Compra", e -> Formatos.fechaHora(e.getFechaCompra()), 130),
                Tablas.texto("Tarjeta", Entrada::getTarjeta, 130),
                Tablas.entero("Salas", Entrada::getCantidadSalas, 60),
                Tablas.texto("Visitas", e -> e.getPrimeraVisita() == null ? "" : e.getPrimeraVisita().equals(e.getUltimaVisita())
                        ? Formatos.fecha(e.getPrimeraVisita())
                        : Formatos.fecha(e.getPrimeraVisita()) + " al " + Formatos.fecha(e.getUltimaVisita()), 190),
                Tablas.moneda("Total", Entrada::getTotal, 100)));
        btnQr.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        btnPdf.disableProperty().bind(tabla.getSelectionModel().selectedItemProperty().isNull());
        Mensajes.intentar(() -> {
            List<Opcion> opciones = new ArrayList<>();
            opciones.add(Opcion.TODOS);
            new TarjetaDAO().listar().forEach(t -> opciones.add(new Opcion(t.getId(), t.getNombre())));
            cmbTarjeta.getItems().setAll(opciones);
            cmbTarjeta.getSelectionModel().selectFirst();
            buscar();
        });
    }

    @FXML
    private void filtrar() {
        Mensajes.intentar(this::buscar);
    }

    @FXML
    private void limpiar() {
        txtFiltro.clear();
        cmbTarjeta.getSelectionModel().selectFirst();
        filtrar();
    }

    private void buscar() throws DatosException {
        Opcion tarjeta = cmbTarjeta.getValue();
        List<Entrada> entradas = entradaDAO.buscar(txtFiltro.getText().trim(), tarjeta == null ? null : tarjeta.id());
        tabla.getItems().setAll(entradas);
        lblConteo.setText(entradas.size() + (entradas.size() == 1 ? " entrada" : " entradas"));
    }

    @FXML
    private void verQr() {
        Mensajes.intentar(() -> {
            Entrada entrada = entradaCompleta();
            Comprobante comprobante = comprobantes.generar(entrada);
            DialogoArchivo.mostrar("Código QR de la entrada", "Entrada " + entrada.getCodigo() + " · "
                            + entrada.getNombreVisitante(),
                    "Puede leer esta imagen en la pantalla Validar entrada.", comprobante.imagenQr(), comprobante.pdf());
        });
    }

    @FXML
    private void abrirPdf() {
        Mensajes.intentar(() -> App.abrirArchivo(comprobantes.generar(entradaCompleta()).pdf()));
    }

    private Entrada entradaCompleta() throws DatosException, ValidacionException {
        Entrada seleccionada = tabla.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            throw new ValidacionException("Seleccione una entrada de la tabla.");
        }
        Entrada entrada = entradaDAO.buscarPorCodigo(seleccionada.getCodigo());
        if (entrada == null) {
            throw new ValidacionException("La entrada " + seleccionada.getCodigo() + " ya no existe.");
        }
        return entrada;
    }
}
