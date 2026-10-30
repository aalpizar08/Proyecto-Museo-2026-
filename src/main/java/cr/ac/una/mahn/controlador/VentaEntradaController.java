package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.datos.MuseoDAO;
import cr.ac.una.mahn.datos.PrecioDAO;
import cr.ac.una.mahn.datos.TarjetaDAO;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.Comprobante;
import cr.ac.una.mahn.modelo.DetalleEntrada;
import cr.ac.una.mahn.modelo.Entrada;
import cr.ac.una.mahn.modelo.Museo;
import cr.ac.una.mahn.modelo.Precio;
import cr.ac.una.mahn.modelo.Tarjeta;
import cr.ac.una.mahn.modelo.Totales;
import cr.ac.una.mahn.servicio.ComprobanteServicio;
import cr.ac.una.mahn.servicio.VentaServicio;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.util.Tablas;
import cr.ac.una.mahn.vista.DialogoArchivo;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.skin.DatePickerSkin;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;

/**
 * Venta de entradas (Interfaz No. 2). Las salas y sus precios se obtienen de la base
 * de datos; el precio depende del día (lunes a sábado o domingo) y se suma el IVA del 13 %.
 */
public class VentaEntradaController {

    @FXML private Label lblFecha;
    @FXML private TextField txtVisitante;
    @FXML private ComboBox<Tarjeta> cmbTarjeta;
    @FXML private Label lblComision;
    @FXML private ComboBox<Museo> cmbMuseo;
    @FXML private ListView<SalaSeleccionable> lstSalas;
    @FXML private CheckBox chkTodas;
    @FXML private StackPane contenedorCalendario;
    @FXML private Label lblInfoFecha;
    @FXML private TableView<DetalleEntrada> tablaDetalle;
    @FXML private Button btnQuitar;
    @FXML private Label lblSubtotal;
    @FXML private Label lblIva;
    @FXML private Label lblTotal;

    private final DatePicker selectorFecha = new DatePicker(LocalDate.now());
    private final ObservableList<DetalleEntrada> detalles = FXCollections.observableArrayList();
    private final VentaServicio ventaServicio = new VentaServicio();
    private final ComprobanteServicio comprobantes = new ComprobanteServicio();
    private final PrecioDAO precioDAO = new PrecioDAO();

    /** Sala de la lista con su casilla de selección. */
    public static final class SalaSeleccionable {
        private final Precio precio;
        private final BooleanProperty seleccionada = new SimpleBooleanProperty(false);

        SalaSeleccionable(Precio precio) {
            this.precio = precio;
        }

        public Precio getPrecio() {
            return precio;
        }

        public BooleanProperty seleccionadaProperty() {
            return seleccionada;
        }
    }

    @FXML
    private void initialize() {
        lblFecha.setText(Formatos.fechaLarga(LocalDate.now()));
        configurarCalendario();
        configurarTabla();
        lstSalas.setPlaceholder(new Label("Seleccione un museo para ver sus salas."));
        lstSalas.setCellFactory(CheckBoxListCell.forListView(SalaSeleccionable::seleccionadaProperty,
                new StringConverter<>() {
                    @Override
                    public String toString(SalaSeleccionable s) {
                        Precio p = s.getPrecio();
                        return p.getSala() + "   ·   L-S " + Formatos.moneda(p.getPrecioSemana())
                                + "  /  D " + Formatos.moneda(p.getPrecioDomingo());
                    }

                    @Override
                    public SalaSeleccionable fromString(String texto) {
                        return null;
                    }
                }));
        cmbMuseo.valueProperty().addListener((o, anterior, museo) -> cargarSalas(museo));
        cmbTarjeta.valueProperty().addListener((o, anterior, tarjeta) -> lblComision.setText(tarjeta == null ? ""
                : "Comisión de la marca: " + Formatos.porcentaje(tarjeta.getPorcentajeComision())));
        chkTodas.setOnAction(e -> lstSalas.getItems().forEach(s -> s.seleccionadaProperty().set(chkTodas.isSelected())));
        detalles.addListener((ListChangeListener<DetalleEntrada>) cambio -> actualizarTotales());
        btnQuitar.disableProperty().bind(tablaDetalle.getSelectionModel().selectedItemProperty().isNull());
        Mensajes.intentar(() -> {
            cmbTarjeta.getItems().setAll(new TarjetaDAO().listarActivas());
            cmbMuseo.getItems().setAll(new MuseoDAO().listar());
            cmbMuseo.getSelectionModel().selectFirst();
        });
        actualizarTotales();
        actualizarInfoFecha();
    }

    /** Calendario siempre visible (como en la interfaz), sin días pasados y con los domingos resaltados. */
    private void configurarCalendario() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(VentaServicio.DIAS_MAXIMOS_ANTICIPACION);
        selectorFecha.setDayCellFactory(selector -> new DateCell() {
            @Override
            public void updateItem(LocalDate dia, boolean vacio) {
                super.updateItem(dia, vacio);
                getStyleClass().remove("dia-domingo");
                if (vacio || dia == null) {
                    return;
                }
                if (dia.isBefore(hoy) || dia.isAfter(limite)) {
                    setDisable(true);
                } else if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    getStyleClass().add("dia-domingo");
                }
            }
        });
        DatePickerSkin piel = new DatePickerSkin(selectorFecha);
        contenedorCalendario.getChildren().setAll(piel.getPopupContent());
        selectorFecha.valueProperty().addListener((o, anterior, nueva) -> actualizarInfoFecha());
    }

    private void configurarTabla() {
        tablaDetalle.setItems(detalles);
        tablaDetalle.setPlaceholder(new Label("Agregue salas y días de visita con el botón AGREGAR."));
        tablaDetalle.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaDetalle.getColumns().setAll(List.of(
                Tablas.texto("Museo", DetalleEntrada::getMuseo, 230),
                Tablas.texto("Sala", DetalleEntrada::getSala, 290),
                Tablas.fecha("Fecha de visita", DetalleEntrada::getFechaVisita, 120),
                Tablas.texto("Día", d -> Formatos.dia(d.getFechaVisita()), 100),
                Tablas.moneda("Precio", DetalleEntrada::getPrecio, 110)));
    }

    private void cargarSalas(Museo museo) {
        chkTodas.setSelected(false);
        if (museo == null) {
            lstSalas.getItems().clear();
            return;
        }
        Mensajes.intentar(() -> {
            List<SalaSeleccionable> salas = new ArrayList<>();
            for (Precio precio : precioDAO.listarPorMuseo(museo.getId())) {
                salas.add(new SalaSeleccionable(precio));
            }
            lstSalas.getItems().setAll(salas);
            lstSalas.setPlaceholder(new Label("Este museo no tiene salas con precio registrado."));
        });
    }

    private void actualizarInfoFecha() {
        LocalDate fecha = selectorFecha.getValue();
        if (fecha == null) {
            lblInfoFecha.setText("Seleccione el día de la visita.");
            return;
        }
        boolean domingo = fecha.getDayOfWeek() == DayOfWeek.SUNDAY;
        lblInfoFecha.setText(Formatos.dia(fecha) + " " + Formatos.fecha(fecha) + "\nSe aplica el precio "
                + (domingo ? "diferido de domingo." : "de lunes a sábado."));
    }

    private void actualizarTotales() {
        Totales totales = VentaServicio.calcularTotales(detalles);
        lblSubtotal.setText(Formatos.moneda(totales.subtotal()));
        lblIva.setText(Formatos.moneda(totales.iva()));
        lblTotal.setText(Formatos.moneda(totales.total()));
    }

    // ------------------------------------------------------------------ acciones

    @FXML
    private void agregar() {
        Mensajes.intentar(() -> {
            List<SalaSeleccionable> elegidas = lstSalas.getItems().stream()
                    .filter(s -> s.seleccionadaProperty().get()).toList();
            if (elegidas.isEmpty()) {
                throw new ValidacionException("Marque al menos una sala en la lista «Salas disponibles».");
            }
            LocalDate fecha = selectorFecha.getValue();
            ventaServicio.validarFechaVisita(fecha);
            List<String> repetidas = new ArrayList<>();
            for (SalaSeleccionable sala : elegidas) {
                if (VentaServicio.estaAgregada(detalles, sala.getPrecio().getIdSala(), fecha)) {
                    repetidas.add(sala.getPrecio().getSala());
                } else {
                    detalles.add(ventaServicio.crearDetalle(sala.getPrecio(), fecha));
                }
                sala.seleccionadaProperty().set(false);
            }
            chkTodas.setSelected(false);
            FXCollections.sort(detalles, Comparator.comparing(DetalleEntrada::getFechaVisita)
                    .thenComparing(DetalleEntrada::getMuseo).thenComparing(DetalleEntrada::getSala));
            if (!repetidas.isEmpty()) {
                Mensajes.advertencia("Salas repetidas", "Estas salas ya estaban en la entrada para el "
                        + Formatos.fecha(fecha) + " y no se agregaron de nuevo:\n• " + String.join("\n• ", repetidas));
            }
        });
    }

    @FXML
    private void quitar() {
        DetalleEntrada seleccionado = tablaDetalle.getSelectionModel().getSelectedItem();
        if (seleccionado != null) {
            detalles.remove(seleccionado);
        }
    }

    @FXML
    private void limpiarTodo() {
        if (!detalles.isEmpty() && !Mensajes.confirmar("Limpiar entrada", "¿Desea quitar todas las salas agregadas?",
                "Se eliminarán " + detalles.size() + " líneas de la entrada actual.")) {
            return;
        }
        limpiarVenta();
    }

    @FXML
    private void vender() {
        Mensajes.intentar(() -> {
            String visitante = ventaServicio.validarVenta(txtVisitante.getText(), cmbTarjeta.getValue(), detalles);
            Totales totales = VentaServicio.calcularTotales(detalles);
            String resumen = "Visitante: " + visitante
                    + "\nTarjeta: " + cmbTarjeta.getValue().getNombre()
                    + "\nSalas y días: " + detalles.size()
                    + "\n\nSubtotal: " + Formatos.moneda(totales.subtotal())
                    + "\nIVA (13 %): " + Formatos.moneda(totales.iva())
                    + "\nTotal a pagar: " + Formatos.moneda(totales.total());
            if (!Mensajes.confirmar("Confirmar venta", "¿Desea registrar la venta de esta entrada?", resumen)) {
                return;
            }
            Entrada entrada = ventaServicio.vender(txtVisitante.getText(), cmbTarjeta.getValue(), detalles);
            limpiarVenta();
            try {
                Comprobante comprobante = comprobantes.generar(entrada);
                DialogoArchivo.mostrar("Venta registrada", "Entrada " + entrada.getCodigo() + " · "
                                + Formatos.moneda(entrada.getTotal()),
                        "La venta se registró correctamente y la comisión de la tarjeta se guardó automáticamente. "
                                + "Entregue al visitante el comprobante PDF o la imagen JPG con el código QR.",
                        comprobante.imagenQr(), comprobante.pdf());
            } catch (IOException ex) {
                Mensajes.advertencia("Comprobante pendiente", "La venta se registró con el código "
                        + entrada.getCodigo() + ", pero no se pudo generar el comprobante: " + ex.getMessage()
                        + "\nPuede generarlo de nuevo desde Entradas › Historial de entradas.");
            }
        });
    }

    private void limpiarVenta() {
        detalles.clear();
        txtVisitante.clear();
        cmbTarjeta.getSelectionModel().clearSelection();
        lstSalas.getItems().forEach(s -> s.seleccionadaProperty().set(false));
        chkTodas.setSelected(false);
        selectorFecha.setValue(LocalDate.now());
    }
}
