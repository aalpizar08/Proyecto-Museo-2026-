package cr.ac.una.mahn.vista;

import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.servicio.ImagenServicio;
import cr.ac.una.mahn.util.Mensajes;
import java.io.File;
import java.io.IOException;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/** Vista previa y selección de la imagen de una especie o temática. */
public class SelectorImagen extends HBox {

    private final ImageView vista = new ImageView();
    private final Label lblRuta = new Label();
    private final String subcarpeta;
    private String rutaActual;
    private File archivoNuevo;

    public SelectorImagen(String rutaActual, String subcarpeta) {
        super(12);
        this.rutaActual = rutaActual;
        this.subcarpeta = subcarpeta;
        setAlignment(Pos.CENTER_LEFT);

        vista.setFitWidth(156);
        vista.setFitHeight(104);
        vista.setPreserveRatio(true);
        StackPane marco = new StackPane(vista);
        marco.getStyleClass().add("marco-imagen");
        marco.setMinSize(168, 116);
        marco.setMaxSize(168, 116);

        Button seleccionar = new Button("Seleccionar imagen…");
        seleccionar.getStyleClass().add("boton-secundario");
        seleccionar.setOnAction(e -> elegir());
        Button quitar = new Button("Quitar imagen");
        quitar.getStyleClass().add("boton-secundario");
        quitar.setOnAction(e -> {
            this.rutaActual = null;
            archivoNuevo = null;
            actualizar();
        });
        lblRuta.getStyleClass().add("texto-ayuda");
        lblRuta.setWrapText(true);
        lblRuta.setMaxWidth(240);

        getChildren().addAll(marco, new VBox(8, seleccionar, quitar, lblRuta));
        actualizar();
    }

    private void elegir() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccione una imagen");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp"));
        File archivo = selector.showOpenDialog(getScene().getWindow());
        if (archivo == null) {
            return;
        }
        Image prueba = new Image(archivo.toURI().toString());
        if (prueba.isError()) {
            Mensajes.advertencia("Imagen no válida", "No se pudo abrir «" + archivo.getName() + "» como imagen.");
            return;
        }
        archivoNuevo = archivo;
        actualizar();
    }

    private void actualizar() {
        if (archivoNuevo != null) {
            vista.setImage(new Image(archivoNuevo.toURI().toString()));
            lblRuta.setText(archivoNuevo.getName() + " (se guardará al presionar Guardar)");
        } else {
            vista.setImage(ImagenServicio.cargar(rutaActual));
            lblRuta.setText(rutaActual == null ? "Sin imagen" : rutaActual);
        }
    }

    /**
     * Ruta que se guarda en la base de datos. Si el usuario eligió una imagen nueva,
     * se copia a la carpeta {@code imagenes/} de la aplicación.
     */
    public String resolverRuta() throws ValidacionException {
        if (archivoNuevo == null) {
            return rutaActual;
        }
        try {
            rutaActual = ImagenServicio.copiar(archivoNuevo, subcarpeta);
            archivoNuevo = null;
            return rutaActual;
        } catch (IOException ex) {
            throw new ValidacionException("No se pudo copiar la imagen seleccionada: " + ex.getMessage());
        }
    }
}
