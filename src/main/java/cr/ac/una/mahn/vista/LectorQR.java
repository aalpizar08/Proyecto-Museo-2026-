package cr.ac.una.mahn.vista;

import cr.ac.una.mahn.servicio.QRServicio;
import cr.ac.una.mahn.util.Mensajes;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;

/**
 * Campo «LEA SU CÓDIGO QR AQUÍ». Acepta tres formas de lectura:
 * <ol>
 *   <li>Un lector USB de códigos QR (funciona como teclado y termina con Enter).</li>
 *   <li>Escribir el código a mano.</li>
 *   <li>Cargar o arrastrar la imagen JPG/PNG o el PDF que contiene el código.</li>
 * </ol>
 */
public class LectorQR extends VBox {

    /** Recibe el texto leído del código. */
    @FunctionalInterface
    public interface ManejadorCodigo {
        void procesar(String codigo) throws Exception;
    }

    private final TextField txtCodigo = new TextField();
    private final QRServicio qr = new QRServicio();
    private ManejadorCodigo manejador = codigo -> { };
    private Path carpetaInicial = Path.of("generados");

    public LectorQR() {
        super(10);
        getStyleClass().add("lector-qr");
        setAlignment(Pos.CENTER);
        setMaxWidth(680);

        SVGPath icono = new SVGPath();
        icono.setContent("M0,0H7V7H0Z M1,1V6H6V1Z M2,2H5V5H2Z M14,0H21V7H14Z M15,1V6H20V1Z M16,2H19V5H16Z"
                + " M0,14H7V21H0Z M1,15V20H6V15Z M2,16H5V19H2Z M9,0H11V2H9Z M9,4H12V6H9Z M9,9H12V12H9Z"
                + " M14,9H16V11H14Z M18,9H21V11H18Z M9,14H11V16H9Z M14,14H17V17H14Z M18,18H21V21H18Z"
                + " M14,19H16V21H14Z M0,9H2V11H0Z M4,9H7V11H4Z");
        icono.setFillRule(FillRule.EVEN_ODD);
        icono.getStyleClass().add("icono-qr");
        icono.setScaleX(1.6);
        icono.setScaleY(1.6);
        Label titulo = new Label("LEA SU CÓDIGO QR AQUÍ");
        titulo.getStyleClass().add("lector-qr-titulo");
        HBox cabecera = new HBox(22, icono, titulo);
        cabecera.setAlignment(Pos.CENTER);

        txtCodigo.setPromptText("Escanee el código con el lector o escríbalo y presione Enter");
        txtCodigo.setOnAction(e -> procesar(txtCodigo.getText()));
        Button btnLeer = new Button("Leer código");
        btnLeer.getStyleClass().add("boton-primario");
        btnLeer.setOnAction(e -> procesar(txtCodigo.getText()));
        HBox fila = new HBox(8, txtCodigo, btnLeer);
        HBox.setHgrow(txtCodigo, Priority.ALWAYS);
        fila.setAlignment(Pos.CENTER);

        Button btnArchivo = new Button("Cargar imagen o PDF del código QR…");
        btnArchivo.getStyleClass().add("boton-secundario");
        btnArchivo.setOnAction(e -> seleccionarArchivo());
        Label ayuda = new Label("También puede arrastrar a este recuadro la imagen JPG/PNG o el PDF con el código.");
        ayuda.getStyleClass().add("texto-ayuda");

        getChildren().addAll(cabecera, fila, btnArchivo, ayuda);
        configurarArrastre();
    }

    private void configurarArrastre() {
        setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        setOnDragEntered(e -> getStyleClass().add("arrastrando"));
        setOnDragExited(e -> getStyleClass().remove("arrastrando"));
        setOnDragDropped(this::soltarArchivo);
    }

    private void soltarArchivo(DragEvent evento) {
        List<File> archivos = evento.getDragboard().getFiles();
        boolean hayArchivo = archivos != null && !archivos.isEmpty();
        evento.setDropCompleted(hayArchivo);
        evento.consume();
        if (hayArchivo) {
            leerArchivo(archivos.get(0));
        }
    }

    private void seleccionarArchivo() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Seleccione la imagen o el PDF con el código QR");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imágenes y PDF", "*.jpg", "*.jpeg", "*.png", "*.bmp", "*.gif", "*.pdf"),
                new FileChooser.ExtensionFilter("Todos los archivos", "*.*"));
        if (Files.isDirectory(carpetaInicial)) {
            selector.setInitialDirectory(carpetaInicial.toAbsolutePath().toFile());
        }
        File archivo = selector.showOpenDialog(getScene().getWindow());
        if (archivo != null) {
            leerArchivo(archivo);
        }
    }

    private void leerArchivo(File archivo) {
        Mensajes.intentar(() -> {
            String contenido = qr.leer(archivo);
            txtCodigo.setText(contenido);
            manejador.procesar(contenido);
        });
    }

    private void procesar(String texto) {
        Mensajes.intentar(() -> manejador.procesar(texto));
    }

    public void setManejador(ManejadorCodigo manejador) {
        this.manejador = manejador;
    }

    public void setCarpetaInicial(Path carpetaInicial) {
        this.carpetaInicial = carpetaInicial;
    }

    /** Coloca el cursor en el campo para que el lector USB escriba directamente. */
    public void enfocar() {
        txtCodigo.requestFocus();
    }

    public void limpiar() {
        txtCodigo.clear();
    }
}
