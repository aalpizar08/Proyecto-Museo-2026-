package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.datos.EntradaDAO;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.DetalleEntrada;
import cr.ac.una.mahn.modelo.Entrada;
import cr.ac.una.mahn.servicio.ComprobanteServicio;
import cr.ac.una.mahn.util.CodigoQR;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.vista.LectorQR;
import java.time.LocalDate;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

/**
 * Validación de entradas (Interfaz No. 3). Al leer el código QR se muestran las salas
 * a las que la persona puede ingresar según la fecha del sistema. Antes de leer el
 * código solo se ven el título y el campo de lectura.
 */
public class ValidarEntradaController {

    @FXML private Label lblFecha;
    @FXML private LectorQR lector;
    @FXML private VBox panelResultado;
    @FXML private VBox bannerEstado;
    @FXML private Label lblEstado;
    @FXML private Label lblDetalleEstado;
    @FXML private Label lblCodigo;
    @FXML private Label lblVisitante;
    @FXML private Label lblCompra;
    @FXML private Label lblTarjeta;
    @FXML private TableView<DetalleEntrada> tablaHoy;
    @FXML private TableView<DetalleEntrada> tablaOtras;

    private final EntradaDAO entradaDAO = new EntradaDAO();

    @FXML
    private void initialize() {
        lector.setCarpetaInicial(ComprobanteServicio.CARPETA_ENTRADAS);
        lector.setManejador(this::validar);
        tablaHoy.setPlaceholder(new Label("Hoy no puede ingresar a ninguna sala con esta entrada."));
        tablaHoy.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaHoy.getColumns().setAll(List.of(
                Tablas.texto("Sala", DetalleEntrada::getSala, 330),
                Tablas.texto("Museo", DetalleEntrada::getMuseo, 260)));
        tablaOtras.setPlaceholder(new Label("La entrada no incluye otras fechas."));
        tablaOtras.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaOtras.getColumns().setAll(List.of(
                Tablas.fecha("Fecha", DetalleEntrada::getFechaVisita, 110),
                Tablas.texto("Día", d -> Formatos.dia(d.getFechaVisita()), 100),
                Tablas.texto("Sala", DetalleEntrada::getSala, 300),
                Tablas.texto("Museo", DetalleEntrada::getMuseo, 220),
                Tablas.texto("Estado", d -> d.getFechaVisita().isBefore(LocalDate.now()) ? "Vencida" : "Próxima visita", 120)));
        ocultarResultado();
        Platform.runLater(lector::enfocar);
    }

    private void validar(String leido) throws Exception {
        String codigo = CodigoQR.extraerCodigoEntrada(leido);
        Entrada entrada = entradaDAO.buscarPorCodigo(codigo);
        if (entrada == null) {
            ocultarResultado();
            throw new ValidacionException("No existe ninguna entrada con el código " + codigo
                    + ". Verifique que el código QR corresponda a una entrada del museo.");
        }
        LocalDate hoy = LocalDate.now();
        List<DetalleEntrada> deHoy = entrada.getDetalles().stream().filter(d -> d.getFechaVisita().equals(hoy)).toList();
        List<DetalleEntrada> otras = entrada.getDetalles().stream().filter(d -> !d.getFechaVisita().equals(hoy)).toList();

        lblFecha.setText("Fecha del sistema: " + Formatos.fechaLarga(hoy));
        lblCodigo.setText(entrada.getCodigo());
        lblVisitante.setText(entrada.getNombreVisitante());
        lblCompra.setText(Formatos.fechaHora(entrada.getFechaCompra()));
        lblTarjeta.setText(entrada.getTarjeta() + " · " + Formatos.moneda(entrada.getTotal()));
        tablaHoy.getItems().setAll(deHoy);
        tablaOtras.getItems().setAll(otras);

        bannerEstado.getStyleClass().removeAll("banner-valido", "banner-invalido");
        if (!deHoy.isEmpty()) {
            bannerEstado.getStyleClass().add("banner-valido");
            lblEstado.setText("✔  ENTRADA VÁLIDA PARA HOY");
            lblDetalleEstado.setText("Puede ingresar a " + deHoy.size() + (deHoy.size() == 1 ? " sala." : " salas."));
        } else {
            bannerEstado.getStyleClass().add("banner-invalido");
            lblEstado.setText("✖  LA ENTRADA NO ES VÁLIDA HOY");
            LocalDate proxima = otras.stream().map(DetalleEntrada::getFechaVisita).filter(f -> f.isAfter(hoy))
                    .min(LocalDate::compareTo).orElse(null);
            lblDetalleEstado.setText(proxima == null
                    ? "Todas las fechas de esta entrada ya pasaron."
                    : "La próxima visita autorizada es el " + Formatos.fechaLarga(proxima).toLowerCase() + ".");
        }
        mostrarResultado();
        lector.limpiar();
        lector.enfocar();
    }

    private void mostrarResultado() {
        lblFecha.setVisible(true);
        panelResultado.setVisible(true);
        panelResultado.setManaged(true);
    }

    private void ocultarResultado() {
        lblFecha.setVisible(false);
        panelResultado.setVisible(false);
        panelResultado.setManaged(false);
    }
}
