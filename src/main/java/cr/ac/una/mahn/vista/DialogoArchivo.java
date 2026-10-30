package cr.ac.una.mahn.vista;

import cr.ac.una.mahn.App;
import java.nio.file.Path;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/** Muestra un código QR generado con botones para abrir sus archivos. */
public final class DialogoArchivo {

    private DialogoArchivo() {
    }

    /**
     * @param imagen imagen JPG del código QR
     * @param pdf    comprobante en PDF o {@code null} si no aplica
     */
    public static void mostrar(String titulo, String encabezado, String descripcion, Path imagen, Path pdf) {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("MAHN 3.0 · " + titulo);
        dialogo.setHeaderText(encabezado);
        dialogo.getDialogPane().getStylesheets().add(App.hojaDeEstilos());
        dialogo.getDialogPane().getStyleClass().add("formulario");
        if (App.getVentana() != null) {
            dialogo.initOwner(App.getVentana());
        }

        ImageView vista = new ImageView(new Image(imagen.toUri().toString()));
        vista.setFitWidth(300);
        vista.setPreserveRatio(true);
        Label texto = new Label(descripcion);
        texto.setWrapText(true);
        texto.setMaxWidth(460);
        Label ruta = new Label("Archivos en: " + imagen.getParent());
        ruta.getStyleClass().add("texto-ayuda");
        ruta.setWrapText(true);
        ruta.setMaxWidth(460);

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        if (pdf != null) {
            Button abrirPdf = new Button("Abrir comprobante PDF");
            abrirPdf.getStyleClass().add("boton-primario");
            abrirPdf.setOnAction(e -> App.abrirArchivo(pdf));
            botones.getChildren().add(abrirPdf);
        }
        Button abrirImagen = new Button("Abrir imagen JPG");
        abrirImagen.getStyleClass().add("boton-secundario");
        abrirImagen.setOnAction(e -> App.abrirArchivo(imagen));
        Button abrirCarpeta = new Button("Abrir carpeta");
        abrirCarpeta.getStyleClass().add("boton-secundario");
        abrirCarpeta.setOnAction(e -> App.abrirArchivo(imagen.getParent()));
        botones.getChildren().addAll(abrirImagen, abrirCarpeta);

        VBox contenido = new VBox(12, vista, texto, botones, ruta);
        contenido.setAlignment(Pos.CENTER);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().getButtonTypes().add(new ButtonType("Cerrar", ButtonType.CLOSE.getButtonData()));
        dialogo.showAndWait();
    }
}
