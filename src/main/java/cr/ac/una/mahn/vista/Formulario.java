package cr.ac.una.mahn.vista;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Ventana modal para insertar o editar un registro. Si los datos no son válidos o la
 * base de datos los rechaza, el formulario no se cierra y muestra el motivo.
 *
 * @param <T> tipo de registro que construye el formulario
 */
public class Formulario<T> {

    /** Construye el registro a partir de los campos, validando cada dato. */
    @FunctionalInterface
    public interface Constructor<T> {
        T construir() throws ValidacionException;
    }

    /** Guarda el registro en la base de datos. */
    @FunctionalInterface
    public interface Guardado<T> {
        void guardar(T registro) throws ValidacionException, DatosException;
    }

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Dialog<T> dialogo = new Dialog<>();
    private final GridPane grilla = new GridPane();
    private final Label lblError = new Label();
    private final ButtonType botonGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
    private Constructor<T> constructor;
    private int fila;

    public Formulario(String titulo, String encabezado) {
        dialogo.setTitle("MAHN 3.0 · " + titulo);
        dialogo.setHeaderText(encabezado);
        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(botonGuardar, new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE));
        panel.getStylesheets().add(App.hojaDeEstilos());
        panel.getStyleClass().add("formulario");

        grilla.setHgap(14);
        grilla.setVgap(10);
        ColumnConstraints etiquetas = new ColumnConstraints();
        etiquetas.setMinWidth(160);
        ColumnConstraints campos = new ColumnConstraints();
        campos.setPrefWidth(440);
        campos.setHgrow(Priority.ALWAYS);
        grilla.getColumnConstraints().addAll(etiquetas, campos);

        lblError.getStyleClass().add("error-formulario");
        lblError.setWrapText(true);
        lblError.setMaxWidth(Double.MAX_VALUE);
        lblError.setVisible(false);
        lblError.setManaged(false);

        VBox contenido = new VBox(14, grilla, lblError);
        contenido.setPadding(new Insets(6, 4, 0, 4));
        panel.setContent(contenido);
        if (App.getVentana() != null) {
            dialogo.initOwner(App.getVentana());
        }
    }

    /** Agrega una fila «etiqueta: control» y devuelve el control para seguir usándolo. */
    public <N extends Node> N campo(String etiqueta, N control) {
        Label texto = new Label(etiqueta);
        texto.getStyleClass().add("etiqueta-campo");
        if (control instanceof TextArea || control instanceof SelectorImagen) {
            GridPane.setValignment(texto, VPos.TOP);
            texto.setPadding(new Insets(6, 0, 0, 0));
        }
        if (control instanceof Region region) {
            region.setMaxWidth(Double.MAX_VALUE);
        }
        grilla.addRow(fila++, texto, control);
        return control;
    }

    /** Texto de ayuda que ocupa las dos columnas. */
    public void nota(String texto) {
        Label ayuda = new Label(texto);
        ayuda.getStyleClass().add("texto-ayuda");
        ayuda.setWrapText(true);
        grilla.add(ayuda, 0, fila++, 2, 1);
    }

    public void alConstruir(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    /** Muestra el formulario; devuelve el registro guardado o vacío si se canceló. */
    public Optional<T> mostrar(Guardado<T> guardado) {
        Button guardar = (Button) dialogo.getDialogPane().lookupButton(botonGuardar);
        AtomicReference<T> resultado = new AtomicReference<>();
        guardar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                T registro = constructor.construir();
                guardado.guardar(registro);
                resultado.set(registro);
            } catch (ValidacionException | DatosException ex) {
                mostrarError(ex.getMessage());
                evento.consume();
            }
        });
        dialogo.setResultConverter(boton -> boton == botonGuardar ? resultado.get() : null);
        return dialogo.showAndWait();
    }

    private void mostrarError(String mensaje) {
        lblError.setText("⚠  " + mensaje);
        lblError.setVisible(true);
        lblError.setManaged(true);
        dialogo.getDialogPane().getScene().getWindow().sizeToScene();
    }

    // ------------------------------------------------------------------ fábricas de controles

    public static TextField texto(String valor, String sugerencia) {
        TextField campo = new TextField(valor == null ? "" : valor);
        campo.setPromptText(sugerencia);
        return campo;
    }

    public static TextArea areaTexto(String valor, String sugerencia, int filas) {
        TextArea area = new TextArea(valor == null ? "" : valor);
        area.setPromptText(sugerencia);
        area.setWrapText(true);
        area.setPrefRowCount(filas);
        return area;
    }

    /** Selector de fecha con formato día/mes/año. */
    public static DatePicker selectorFecha(LocalDate valor) {
        DatePicker selector = new DatePicker(valor);
        selector.setPromptText("dd/mm/aaaa");
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha == null ? "" : FORMATO_FECHA.format(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.trim(), FORMATO_FECHA);
            }
        });
        return selector;
    }

    /** ComboBox con los elementos dados, mostrados con el texto indicado. */
    public static <E> ComboBox<E> combo(List<E> elementos, Function<E, String> texto) {
        ComboBox<E> combo = new ComboBox<>();
        combo.getItems().setAll(elementos);
        combo.setVisibleRowCount(14);
        combo.setPromptText("Seleccione…");
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(E elemento) {
                return elemento == null ? "" : texto.apply(elemento);
            }

            @Override
            public E fromString(String cadena) {
                return null;
            }
        });
        combo.setCellFactory(lista -> new ListCell<>() {
            @Override
            protected void updateItem(E elemento, boolean vacio) {
                super.updateItem(elemento, vacio);
                setText(vacio || elemento == null ? null : texto.apply(elemento));
            }
        });
        return combo;
    }

    /** Selecciona en el ComboBox el elemento cuyo id coincide. */
    public static <E> void seleccionar(ComboBox<E> combo, ToIntFunction<E> id, int valor) {
        combo.getItems().stream().filter(e -> id.applyAsInt(e) == valor).findFirst()
                .ifPresent(e -> combo.getSelectionModel().select(e));
    }
}
