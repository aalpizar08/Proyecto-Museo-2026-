package cr.ac.una.mahn;

import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.vista.DialogoConexion;
import java.io.IOException;
import java.nio.file.Path;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * MAHN 3.0 - Sistema del Museo Americano de Historia Natural.
 * Inicia la conexión con MySQL y abre la ventana principal.
 */
public class App extends Application {

    private static HostServices servicios;
    private static Stage ventana;

    @Override
    public void start(Stage escenario) throws IOException {
        servicios = getHostServices();
        Thread.setDefaultUncaughtExceptionHandler((hilo, error) -> Mensajes.errorInesperado(error));
        escenario.getIcons().add(new Image(App.class.getResource("img/icono.png").toExternalForm()));

        if (!DialogoConexion.asegurarConexion()) {
            Platform.exit();
            return;
        }
        ventana = escenario;
        Parent raiz = FXMLLoader.load(App.class.getResource("vista/Principal.fxml"));
        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        Scene escena = new Scene(raiz, Math.min(1360, pantalla.getWidth() * 0.96),
                Math.min(860, pantalla.getHeight() * 0.94));
        escena.getStylesheets().add(hojaDeEstilos());
        escenario.setTitle("MAHN 3.0 · Museo Americano de Historia Natural");
        escenario.setMinWidth(1080);
        escenario.setMinHeight(640);
        escenario.setScene(escena);
        escenario.show();
    }

    public static String hojaDeEstilos() {
        return App.class.getResource("css/estilos.css").toExternalForm();
    }

    public static Stage getVentana() {
        return ventana;
    }

    /** Abre un archivo o carpeta con el programa predeterminado del sistema. */
    public static void abrirArchivo(Path archivo) {
        if (servicios != null) {
            servicios.showDocument(archivo.toAbsolutePath().toUri().toString());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
