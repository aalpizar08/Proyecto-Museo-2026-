package cr.ac.una.mahn.vista;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.datos.ConexionBD;
import cr.ac.una.mahn.datos.ConfiguracionBD;
import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.util.Validador;
import java.io.IOException;
import java.math.BigDecimal;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Ventana que aparece al iniciar si no se puede conectar con MySQL. Permite escribir
 * los datos de conexión, los prueba y los guarda en config/db.properties.
 */
public final class DialogoConexion {

    private DialogoConexion() {
    }

    /** Devuelve {@code true} si hay conexión; {@code false} si el usuario decide salir. */
    public static boolean asegurarConexion() {
        try {
            ConexionBD.probar(ConexionBD.getConfiguracion());
            return true;
        } catch (DatosException ex) {
            return mostrar(ConexionBD.getConfiguracion(), ex.getMessage());
        }
    }

    private static boolean mostrar(ConfiguracionBD actual, String errorInicial) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("MAHN 3.0 · Conexión a MySQL");
        dialogo.setHeaderText("Configure la conexión con la base de datos");
        dialogo.getDialogPane().getStylesheets().add(App.hojaDeEstilos());
        dialogo.getDialogPane().getStyleClass().add("formulario");

        TextField host = new TextField(actual.getHost());
        TextField puerto = new TextField(String.valueOf(actual.getPuerto()));
        TextField base = new TextField(actual.getBaseDatos());
        TextField usuario = new TextField(actual.getUsuario());
        PasswordField clave = new PasswordField();
        clave.setText(actual.getClave());

        GridPane grilla = new GridPane();
        grilla.setHgap(12);
        grilla.setVgap(10);
        grilla.addRow(0, etiqueta("Servidor:"), host);
        grilla.addRow(1, etiqueta("Puerto:"), puerto);
        grilla.addRow(2, etiqueta("Base de datos:"), base);
        grilla.addRow(3, etiqueta("Usuario:"), usuario);
        grilla.addRow(4, etiqueta("Contraseña:"), clave);

        Label ayuda = new Label("Antes de iniciar ejecute el script database/MAHN_DB.sql en MySQL (Workbench o consola). "
                + "Los datos se guardan en config/db.properties para los próximos inicios.");
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("texto-ayuda");
        Label error = new Label(errorInicial);
        error.setWrapText(true);
        error.getStyleClass().add("error-formulario");
        error.setMaxWidth(Double.MAX_VALUE);

        VBox contenido = new VBox(14, error, grilla, ayuda);
        contenido.setPrefWidth(520);
        dialogo.getDialogPane().setContent(contenido);

        ButtonType conectar = new ButtonType("Conectar", ButtonBar.ButtonData.OK_DONE);
        ButtonType salir = new ButtonType("Salir", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(conectar, salir);
        Button botonConectar = (Button) dialogo.getDialogPane().lookupButton(conectar);
        botonConectar.addEventFilter(ActionEvent.ACTION, evento -> {
            try {
                ConfiguracionBD nueva = new ConfiguracionBD();
                nueva.setHost(Validador.texto(host.getText(), "Servidor", 1, 100));
                nueva.setPuerto(Validador.decimal(puerto.getText(), "Puerto", BigDecimal.ONE, false,
                        new BigDecimal("65535"), 0).intValue());
                nueva.setBaseDatos(Validador.texto(base.getText(), "Base de datos", 1, 64));
                nueva.setUsuario(Validador.texto(usuario.getText(), "Usuario", 1, 64));
                nueva.setClave(clave.getText());
                ConexionBD.probar(nueva);
                ConexionBD.setConfiguracion(nueva);
                guardar(nueva);
            } catch (ValidacionException | DatosException ex) {
                error.setText(ex.getMessage());
                evento.consume();
            }
        });
        return dialogo.showAndWait().filter(conectar::equals).isPresent();
    }

    private static void guardar(ConfiguracionBD configuracion) {
        try {
            configuracion.guardar();
        } catch (IOException ex) {
            System.err.println("No se pudo guardar la configuración de la conexión: " + ex.getMessage());
        }
    }

    private static Label etiqueta(String texto) {
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("etiqueta-campo");
        etiqueta.setMinWidth(110);
        return etiqueta;
    }
}
