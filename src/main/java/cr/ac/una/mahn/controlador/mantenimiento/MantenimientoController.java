package cr.ac.una.mahn.controlador.mantenimiento;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.Opcion;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.vista.Formulario;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

/**
 * Base de las pantallas de mantenimiento (Interfaz No. 1): filtro de texto, filtro
 * ComboBox cargado desde la base de datos, tabla y botones Eliminar, Editar e Insertar.
 * Cada subclase indica las columnas, los filtros y el formulario de su tabla.
 *
 * @param <T> tipo de registro que se administra
 */
public abstract class MantenimientoController<T> {

    @FXML protected Label lblTitulo;
    @FXML protected Label lblFecha;
    @FXML protected Label lblFiltroTexto;
    @FXML protected Label lblFiltroCombo;
    @FXML protected Label lblConteo;
    @FXML protected TextField txtFiltro;
    @FXML protected ComboBox<Opcion> cmbFiltro;
    @FXML protected TableView<T> tabla;
    @FXML protected Button btnEliminar;
    @FXML protected Button btnEditar;
    @FXML protected Button btnInsertar;
    @FXML protected HBox panelExtra;

    @FXML
    private void initialize() {
        lblTitulo.setText(titulo().toUpperCase());
        lblFecha.setText(Formatos.fechaLarga(LocalDate.now()));
        lblFiltroTexto.setText(etiquetaFiltroTexto());
        lblFiltroCombo.setText(etiquetaFiltroCombo());
        txtFiltro.setPromptText("Escriba para buscar…");
        tabla.setPlaceholder(new Label("No hay registros que coincidan con los filtros."));
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getColumns().setAll(columnas());
        tabla.setRowFactory(t -> {
            TableRow<T> fila = new TableRow<>();
            fila.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !fila.isEmpty()) {
                    editar();
                }
            });
            return fila;
        });
        configurarAcciones(panelExtra);
        Mensajes.intentar(() -> {
            cargarFiltroCombo();
            buscar();
        });
    }

    // ------------------------------------------------------------------ acciones de la pantalla

    @FXML
    protected void filtrar() {
        Mensajes.intentar(this::buscar);
    }

    @FXML
    private void limpiarFiltros() {
        txtFiltro.clear();
        cmbFiltro.getSelectionModel().selectFirst();
        filtrar();
    }

    @FXML
    private void insertar() {
        Mensajes.intentar(() -> {
            Optional<T> nuevo = crearFormulario(null).mostrar(this::guardarNuevo);
            if (nuevo.isPresent()) {
                recargarYSeleccionar(nuevo.get());
                Mensajes.informacion("Registro insertado", "Se insertó " + describir(nuevo.get()) + " correctamente.");
            }
        });
    }

    @FXML
    private void editar() {
        T seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Mensajes.advertencia("Editar", "Seleccione en la tabla el registro que desea editar.");
            return;
        }
        Mensajes.intentar(() -> {
            Optional<T> editado = crearFormulario(seleccionado).mostrar(this::guardarCambios);
            if (editado.isPresent()) {
                recargarYSeleccionar(editado.get());
                Mensajes.informacion("Registro actualizado", "Se actualizó " + describir(editado.get()) + " correctamente.");
            }
        });
    }

    @FXML
    private void eliminar() {
        T seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            Mensajes.advertencia("Eliminar", "Seleccione en la tabla el registro que desea eliminar.");
            return;
        }
        if (Mensajes.confirmar("Eliminar registro", "¿Desea eliminar " + describir(seleccionado) + "?",
                "Esta acción no se puede deshacer.")) {
            Mensajes.intentar(() -> {
                borrar(seleccionado);
                buscar();
                Mensajes.informacion("Registro eliminado", "Se eliminó " + describir(seleccionado) + ".");
            });
        }
    }

    protected void buscar() throws DatosException {
        Opcion opcion = cmbFiltro.getValue();
        List<T> registros = consultar(txtFiltro.getText().trim(), opcion == null ? null : opcion.id());
        tabla.getItems().setAll(registros);
        lblConteo.setText(registros.size() + (registros.size() == 1 ? " registro" : " registros"));
    }

    private void cargarFiltroCombo() throws DatosException {
        List<Opcion> opciones = new ArrayList<>();
        opciones.add(Opcion.TODOS);
        opciones.addAll(opcionesFiltro());
        cmbFiltro.getItems().setAll(opciones);
        cmbFiltro.setVisibleRowCount(15);
        cmbFiltro.getSelectionModel().selectFirst();
    }

    private void recargarYSeleccionar(T registro) throws DatosException {
        buscar();
        tabla.getItems().stream().filter(t -> idDe(t) == idDe(registro)).findFirst().ifPresent(t -> {
            tabla.getSelectionModel().select(t);
            tabla.scrollTo(t);
        });
    }

    // ------------------------------------------------------------------ lo que define cada mantenimiento

    protected abstract String titulo();

    protected abstract String etiquetaFiltroTexto();

    protected abstract String etiquetaFiltroCombo();

    /** Opciones del filtro ComboBox, cargadas desde la base de datos. */
    protected abstract List<Opcion> opcionesFiltro() throws DatosException;

    protected abstract List<TableColumn<T, ?>> columnas();

    protected abstract List<T> consultar(String texto, Integer idFiltro) throws DatosException;

    /** Formulario de inserción (registro {@code null}) o de edición. */
    protected abstract Formulario<T> crearFormulario(T registro) throws DatosException, ValidacionException;

    protected abstract void guardarNuevo(T registro) throws ValidacionException, DatosException;

    protected abstract void guardarCambios(T registro) throws ValidacionException, DatosException;

    protected abstract void borrar(T registro) throws DatosException;

    /** Texto para los mensajes, por ejemplo «la sala «Salón de Primates»». */
    protected abstract String describir(T registro);

    protected abstract int idDe(T registro);

    /** Permite agregar botones propios junto a Eliminar, Editar e Insertar. */
    protected void configurarAcciones(HBox panel) {
    }
}
