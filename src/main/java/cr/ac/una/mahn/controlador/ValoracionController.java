package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.modelo.ElementoExhibicion;
import cr.ac.una.mahn.modelo.FichaSala;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.modelo.Valoracion;
import cr.ac.una.mahn.servicio.ComprobanteServicio;
import cr.ac.una.mahn.servicio.FichaServicio;
import cr.ac.una.mahn.servicio.ImagenServicio;
import cr.ac.una.mahn.util.CodigoQR;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Mensajes;
import cr.ac.una.mahn.vista.ControlEstrellas;
import cr.ac.una.mahn.vista.LectorQR;
import java.time.LocalDate;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Valoración de salas (Interfaz No. 4). Al leer el QR de una sala se muestra su ficha
 * con imágenes de sus especies o temáticas, el promedio de valoración en estrellas y
 * un formulario para valorarla de forma anónima.
 */
public class ValoracionController {

    private static final int MAXIMO_OBSERVACION = 500;

    @FXML private Label lblFecha;
    @FXML private LectorQR lector;
    @FXML private VBox panelFicha;
    @FXML private Label lblSala;
    @FXML private Label lblMuseoArea;
    @FXML private Label lblDescripcion;
    @FXML private Label lblPromedio;
    @FXML private HBox contenedorPromedio;
    @FXML private Label lblCantidad;
    @FXML private Label lblTipoElementos;
    @FXML private ImageView imgElemento;
    @FXML private Label lblSinImagen;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Label lblIndice;
    @FXML private Label lblNombre;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblColeccion;
    @FXML private Label lblTipo;
    @FXML private Label lblEpoca;
    @FXML private Label lblDetalle;
    @FXML private Label lblDatos;
    @FXML private HBox contenedorSelector;
    @FXML private Label lblSeleccion;
    @FXML private TextArea txtObservacion;
    @FXML private Label lblContador;
    @FXML private VBox listaComentarios;

    private final ControlEstrellas estrellasPromedio = new ControlEstrellas(26, false);
    private final ControlEstrellas selector = new ControlEstrellas(34, true);
    private final FichaServicio fichaServicio = new FichaServicio();
    private FichaSala ficha;
    private int indice;

    @FXML
    private void initialize() {
        lector.setCarpetaInicial(ComprobanteServicio.CARPETA_SALAS);
        lector.setManejador(this::leerSala);
        contenedorPromedio.getChildren().add(estrellasPromedio);
        contenedorSelector.getChildren().add(0, selector);
        selector.valorProperty().addListener((o, anterior, valor) -> lblSeleccion.setText(valor.intValue() == 0
                ? "Haga clic en las estrellas para calificar (1 a 5)"
                : valor.intValue() + (valor.intValue() == 1 ? " estrella" : " estrellas")));
        lblSeleccion.setText("Haga clic en las estrellas para calificar (1 a 5)");
        txtObservacion.setTextFormatter(new TextFormatter<String>(cambio ->
                cambio.getControlNewText().length() <= MAXIMO_OBSERVACION ? cambio : null));
        txtObservacion.textProperty().addListener((o, anterior, texto) ->
                lblContador.setText(texto.length() + " / " + MAXIMO_OBSERVACION));
        lblContador.setText("0 / " + MAXIMO_OBSERVACION);
        lblFecha.setVisible(false);
        panelFicha.setVisible(false);
        panelFicha.setManaged(false);
        Platform.runLater(lector::enfocar);
    }

    private void leerSala(String leido) throws Exception {
        int idSala = CodigoQR.extraerIdSala(leido);
        ficha = fichaServicio.obtener(idSala);
        indice = 0;
        mostrarFicha();
        selector.setValor(0);
        txtObservacion.clear();
        lector.limpiar();
        lblFecha.setText(Formatos.fechaLarga(LocalDate.now()));
        lblFecha.setVisible(true);
        panelFicha.setVisible(true);
        panelFicha.setManaged(true);
    }

    private void mostrarFicha() {
        Sala sala = ficha.sala();
        lblSala.setText(sala.getNombre());
        lblMuseoArea.setText(sala.getMuseo() + "   ·   Área: " + sala.getArea() + "   ·   Sala #" + sala.getId());
        lblDescripcion.setText(sala.getDescripcion());
        lblTipoElementos.setText(ficha.elementos().isEmpty() ? "SIN ESPECIES NI TEMÁTICAS REGISTRADAS"
                : ficha.tieneEspecies() ? "ESPECIES EN EXHIBICIÓN" : "TEMÁTICAS DE LA SALA");
        actualizarResumen();
        mostrarElemento();
        listaComentarios.getChildren().clear();
        if (ficha.comentariosRecientes().isEmpty()) {
            Label vacio = new Label("Todavía no hay comentarios para esta sala.");
            vacio.getStyleClass().add("texto-suave");
            listaComentarios.getChildren().add(vacio);
        }
        for (Valoracion v : ficha.comentariosRecientes()) {
            Label estrellas = new Label("★".repeat(v.getEstrellas()) + "☆".repeat(5 - v.getEstrellas())
                    + "   " + Formatos.fecha(v.getFecha().toLocalDate()));
            estrellas.getStyleClass().add("comentario-estrellas");
            Label texto = new Label("«" + v.getObservacion() + "»");
            texto.setWrapText(true);
            VBox comentario = new VBox(2, estrellas, texto);
            comentario.getStyleClass().add("comentario");
            listaComentarios.getChildren().add(comentario);
        }
    }

    private void actualizarResumen() {
        double promedio = ficha.resumen().promedio();
        int cantidad = ficha.resumen().cantidad();
        estrellasPromedio.setValor(promedio);
        lblPromedio.setText(cantidad == 0 ? "—" : Formatos.promedio(promedio));
        lblCantidad.setText(cantidad == 0 ? "Sin valoraciones todavía"
                : cantidad + (cantidad == 1 ? " valoración" : " valoraciones"));
    }

    private void mostrarElemento() {
        int total = ficha.elementos().size();
        btnAnterior.setDisable(total < 2);
        btnSiguiente.setDisable(total < 2);
        if (total == 0) {
            imgElemento.setImage(null);
            lblSinImagen.setVisible(true);
            lblIndice.setText("");
            lblNombre.setText("Sin elementos en exhibición");
            lblSubtitulo.setText("");
            lblColeccion.setText("—");
            lblTipo.setText("—");
            lblEpoca.setText("—");
            lblDetalle.setText("Registre especies o temáticas en el módulo de mantenimientos.");
            lblDatos.setText("");
            return;
        }
        ElementoExhibicion elemento = ficha.elementos().get(indice);
        Image imagen = ImagenServicio.cargar(elemento.rutaImagen());
        imgElemento.setImage(imagen);
        lblSinImagen.setVisible(imagen == null || imagen.isError());
        lblIndice.setText((indice + 1) + " de " + total);
        lblNombre.setText(elemento.nombre());
        lblSubtitulo.setText(elemento.subtitulo());
        lblColeccion.setText(elemento.coleccion());
        lblTipo.setText(elemento.tipo());
        lblEpoca.setText(elemento.epoca());
        lblDetalle.setText(elemento.detalle());
        lblDatos.setText(elemento.datos());
    }

    @FXML
    private void anterior() {
        if (ficha != null && !ficha.elementos().isEmpty()) {
            indice = (indice - 1 + ficha.elementos().size()) % ficha.elementos().size();
            mostrarElemento();
        }
    }

    @FXML
    private void siguiente() {
        if (ficha != null && !ficha.elementos().isEmpty()) {
            indice = (indice + 1) % ficha.elementos().size();
            mostrarElemento();
        }
    }

    @FXML
    private void enviarValoracion() {
        Mensajes.intentar(() -> {
            int idSala = ficha.sala().getId();
            Valoracion valoracion = fichaServicio.valorar(idSala, selector.getValor(), txtObservacion.getText());
            ficha = fichaServicio.obtener(idSala);
            actualizarResumen();
            mostrarFicha();
            selector.setValor(0);
            txtObservacion.clear();
            Mensajes.informacion("¡Gracias por su valoración!", "Se registró de forma anónima su valoración de "
                    + valoracion.getEstrellas() + (valoracion.getEstrellas() == 1 ? " estrella" : " estrellas")
                    + " para la sala «" + ficha.sala().getNombre() + "».");
        });
    }
}
