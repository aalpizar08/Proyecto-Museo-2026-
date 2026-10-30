package cr.ac.una.mahn.util;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * Mensajes al usuario. {@link #intentar(Accion)} centraliza el manejo de excepciones:
 * cada tipo de error se muestra con un mensaje explicativo.
 */
public final class Mensajes {

    /** Acción de la interfaz que puede lanzar excepciones. */
    @FunctionalInterface
    public interface Accion {
        void ejecutar() throws Exception;
    }

    private Mensajes() {
    }

    /** Ejecuta la acción y muestra el mensaje adecuado si ocurre una excepción. */
    public static void intentar(Accion accion) {
        try {
            accion.ejecutar();
        } catch (ValidacionException ex) {
            advertencia("Revise los datos", ex.getMessage());
        } catch (DatosException ex) {
            error("Error de base de datos", ex.getMessage());
        } catch (IOException ex) {
            error("Error de archivos", "No se pudo leer o escribir un archivo: " + ex.getMessage());
        } catch (Exception ex) {
            errorInesperado(ex);
        }
    }

    public static void informacion(String titulo, String mensaje) {
        mostrar(crear(Alert.AlertType.INFORMATION, titulo, mensaje));
    }

    public static void advertencia(String titulo, String mensaje) {
        mostrar(crear(Alert.AlertType.WARNING, titulo, mensaje));
    }

    public static void error(String titulo, String mensaje) {
        mostrar(crear(Alert.AlertType.ERROR, titulo, mensaje));
    }

    /** Pide confirmación y devuelve {@code true} si el usuario acepta. */
    public static boolean confirmar(String titulo, String pregunta, String detalle) {
        Alert alerta = crear(Alert.AlertType.CONFIRMATION, titulo, detalle);
        alerta.setHeaderText(pregunta);
        ButtonType si = new ButtonType("Sí, continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType no = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerta.getButtonTypes().setAll(si, no);
        return alerta.showAndWait().filter(si::equals).isPresent();
    }

    /** Error no previsto: se muestra el detalle técnico en una sección desplegable. */
    public static void errorInesperado(Throwable error) {
        error.printStackTrace();
        StringWriter traza = new StringWriter();
        error.printStackTrace(new PrintWriter(traza));
        Runnable mostrarError = () -> {
            Alert alerta = crear(Alert.AlertType.ERROR, "Error inesperado",
                    "Ocurrió un error inesperado: " + error.getClass().getSimpleName()
                            + (error.getMessage() == null ? "" : " - " + error.getMessage()));
            TextArea detalle = new TextArea(traza.toString());
            detalle.setEditable(false);
            detalle.setPrefRowCount(12);
            alerta.getDialogPane().setExpandableContent(detalle);
            alerta.show();
        };
        Platform.runLater(mostrarError);
    }

    private static Alert crear(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle("MAHN 3.0 · " + titulo);
        alerta.setHeaderText(titulo);
        alerta.setContentText(mensaje);
        alerta.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alerta.getDialogPane().setPrefWidth(500);
        alerta.getDialogPane().getStylesheets().add(App.hojaDeEstilos());
        Stage ventana = App.getVentana();
        if (ventana != null && ventana.isShowing()) {
            alerta.initOwner(ventana);
        }
        return alerta;
    }

    private static void mostrar(Alert alerta) {
        try {
            alerta.showAndWait();
        } catch (IllegalStateException ex) {
            // showAndWait no se permite durante una animación o el cálculo del diseño
            alerta.show();
        }
    }
}
