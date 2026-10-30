package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.controlador.mantenimiento.ColeccionesController;
import cr.ac.una.mahn.controlador.mantenimiento.ComisionesController;
import cr.ac.una.mahn.controlador.mantenimiento.EspeciesController;
import cr.ac.una.mahn.controlador.mantenimiento.MantenimientoController;
import cr.ac.una.mahn.controlador.mantenimiento.MuseosController;
import cr.ac.una.mahn.controlador.mantenimiento.PreciosController;
import cr.ac.una.mahn.controlador.mantenimiento.SalasController;
import cr.ac.una.mahn.controlador.mantenimiento.TematicasController;
import cr.ac.una.mahn.datos.ConexionBD;
import cr.ac.una.mahn.datos.ConfiguracionBD;
import cr.ac.una.mahn.util.Mensajes;
import java.io.IOException;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/** Ventana principal: menú lateral y área donde se cargan las pantallas. */
public class PrincipalController {

    private static PrincipalController instancia;

    @FXML private StackPane contenedor;
    @FXML private ImageView imgLogo;
    @FXML private Label lblConexion;
    @FXML private VBox subMantenimientos;
    @FXML private VBox subEntradas;
    @FXML private VBox subReportes;
    @FXML private Button btnInicio;
    @FXML private Button btnMantenimientos;
    @FXML private Button btnEntradas;
    @FXML private Button btnValoracion;
    @FXML private Button btnReportes;
    @FXML private Button btnMuseos;
    @FXML private Button btnSalas;
    @FXML private Button btnColecciones;
    @FXML private Button btnEspecies;
    @FXML private Button btnTematicas;
    @FXML private Button btnPrecios;
    @FXML private Button btnComisiones;
    @FXML private Button btnVender;
    @FXML private Button btnValidar;
    @FXML private Button btnHistorial;
    @FXML private Button btnReporteComisiones;
    @FXML private Button btnReporteValoraciones;

    private Button opcionActiva;

    @FXML
    private void initialize() {
        instancia = this;
        imgLogo.setImage(new Image(App.class.getResource("img/logo.png").toExternalForm()));
        for (Button boton : new Button[]{btnMantenimientos, btnEntradas, btnReportes}) {
            boton.setUserData(boton.getText());
        }
        mostrarSubmenu(subMantenimientos, btnMantenimientos, false);
        mostrarSubmenu(subEntradas, btnEntradas, false);
        mostrarSubmenu(subReportes, btnReportes, false);
        ConfiguracionBD configuracion = ConexionBD.getConfiguracion();
        lblConexion.setText("● Conectado a MySQL\n" + configuracion.getBaseDatos() + " en "
                + configuracion.getHost() + ":" + configuracion.getPuerto());
        mostrarInicio();
    }

    /** Permite que otras pantallas (por ejemplo, Inicio) naveguen por el menú. */
    public static PrincipalController instancia() {
        return instancia;
    }

    // ------------------------------------------------------------------ menú

    @FXML
    private void mostrarInicio() {
        cargarVista("Inicio.fxml", btnInicio);
    }

    @FXML
    private void alternarMantenimientos() {
        mostrarSubmenu(subMantenimientos, btnMantenimientos, !subMantenimientos.isVisible());
    }

    @FXML
    private void alternarEntradas() {
        mostrarSubmenu(subEntradas, btnEntradas, !subEntradas.isVisible());
    }

    @FXML
    private void alternarReportes() {
        mostrarSubmenu(subReportes, btnReportes, !subReportes.isVisible());
    }

    @FXML
    private void abrirMuseos() {
        cargarMantenimiento(new MuseosController(), btnMuseos);
    }

    @FXML
    private void abrirSalas() {
        cargarMantenimiento(new SalasController(), btnSalas);
    }

    @FXML
    private void abrirColecciones() {
        cargarMantenimiento(new ColeccionesController(), btnColecciones);
    }

    @FXML
    private void abrirEspecies() {
        cargarMantenimiento(new EspeciesController(), btnEspecies);
    }

    @FXML
    private void abrirTematicas() {
        cargarMantenimiento(new TematicasController(), btnTematicas);
    }

    @FXML
    private void abrirPrecios() {
        cargarMantenimiento(new PreciosController(), btnPrecios);
    }

    @FXML
    private void abrirComisiones() {
        cargarMantenimiento(new ComisionesController(), btnComisiones);
    }

    @FXML
    public void abrirVenta() {
        mostrarSubmenu(subEntradas, btnEntradas, true);
        cargarVista("VentaEntrada.fxml", btnVender);
    }

    @FXML
    public void abrirValidacion() {
        mostrarSubmenu(subEntradas, btnEntradas, true);
        cargarVista("ValidarEntrada.fxml", btnValidar);
    }

    @FXML
    public void abrirHistorial() {
        mostrarSubmenu(subEntradas, btnEntradas, true);
        cargarVista("HistorialEntradas.fxml", btnHistorial);
    }

    @FXML
    public void abrirValoracion() {
        cargarVista("Valoracion.fxml", btnValoracion);
    }

    @FXML
    public void abrirReporteComisiones() {
        mostrarSubmenu(subReportes, btnReportes, true);
        cargarVista("ReporteComisiones.fxml", btnReporteComisiones);
    }

    @FXML
    public void abrirReporteValoraciones() {
        mostrarSubmenu(subReportes, btnReportes, true);
        cargarVista("ReporteValoraciones.fxml", btnReporteValoraciones);
    }

    // ------------------------------------------------------------------ carga de pantallas

    private void cargarVista(String fxml, Button origen) {
        try {
            mostrar(FXMLLoader.load(App.class.getResource("vista/" + fxml)), origen);
        } catch (IOException ex) {
            Mensajes.errorInesperado(ex);
        }
    }

    private void cargarMantenimiento(MantenimientoController<?> controlador, Button origen) {
        try {
            FXMLLoader cargador = new FXMLLoader(App.class.getResource("vista/Mantenimiento.fxml"));
            cargador.setController(controlador);
            mostrar(cargador.load(), origen);
        } catch (IOException ex) {
            Mensajes.errorInesperado(ex);
        }
    }

    private void mostrar(Parent vista, Button origen) {
        contenedor.getChildren().setAll(vista);
        if (opcionActiva != null) {
            opcionActiva.getStyleClass().remove("activo");
        }
        opcionActiva = origen;
        origen.getStyleClass().add("activo");
        FadeTransition aparecer = new FadeTransition(Duration.millis(180), vista);
        aparecer.setFromValue(0.2);
        aparecer.setToValue(1);
        aparecer.play();
    }

    private static void mostrarSubmenu(VBox submenu, Button boton, boolean visible) {
        submenu.setVisible(visible);
        submenu.setManaged(visible);
        boton.setText(boton.getUserData() + (visible ? "   ▾" : "   ▸"));
    }
}
