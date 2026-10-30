package cr.ac.una.mahn.controlador;

import cr.ac.una.mahn.App;
import cr.ac.una.mahn.datos.CatalogoDAO;
import cr.ac.una.mahn.datos.ReporteDAO;
import cr.ac.una.mahn.modelo.Estadisticas;
import cr.ac.una.mahn.util.Formatos;
import cr.ac.una.mahn.util.Mensajes;
import java.time.LocalDate;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/** Pantalla de inicio con los indicadores principales del museo. */
public class InicioController {

    @FXML private Label lblFecha;
    @FXML private ImageView imgLogo;
    @FXML private FlowPane panelIndicadores;
    @FXML private FlowPane panelAreas;

    @FXML
    private void initialize() {
        lblFecha.setText(Formatos.fechaLarga(LocalDate.now()));
        imgLogo.setImage(new Image(App.class.getResource("img/logo.png").toExternalForm()));
        Mensajes.intentar(() -> {
            Estadisticas e = new ReporteDAO().estadisticas(LocalDate.now());
            panelIndicadores.getChildren().setAll(
                    indicador(String.valueOf(e.museos()), "Museos registrados"),
                    indicador(String.valueOf(e.salas()), "Salas de exhibición"),
                    indicador(String.valueOf(e.colecciones()), "Colecciones"),
                    indicador(String.valueOf(e.especies()), "Especies en exhibición"),
                    indicador(String.valueOf(e.tematicas()), "Temáticas"),
                    indicador(String.valueOf(e.entradasHoy()), "Entradas vendidas hoy"),
                    indicador(String.valueOf(e.visitasHoy()), "Visitas a salas hoy"),
                    indicador(Formatos.moneda(e.ventasMes()), "Ventas del mes"),
                    indicador(Formatos.promedio(e.promedioValoraciones()) + " ★",
                            "Valoración promedio (" + e.totalValoraciones() + ")"));
            for (Map.Entry<String, Integer> area : new CatalogoDAO().salasPorArea().entrySet()) {
                Label chip = new Label(area.getKey() + " · " + area.getValue()
                        + (area.getValue() == 1 ? " sala" : " salas"));
                chip.getStyleClass().add("chip");
                panelAreas.getChildren().add(chip);
            }
        });
    }

    private VBox indicador(String valor, String etiqueta) {
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("indicador-valor");
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.getStyleClass().add("indicador-etiqueta");
        VBox caja = new VBox(2, lblValor, lblEtiqueta);
        caja.getStyleClass().add("indicador");
        return caja;
    }

    @FXML
    private void irAVenta() {
        PrincipalController.instancia().abrirVenta();
    }

    @FXML
    private void irAValidacion() {
        PrincipalController.instancia().abrirValidacion();
    }

    @FXML
    private void irAValoracion() {
        PrincipalController.instancia().abrirValoracion();
    }

    @FXML
    private void irAReportes() {
        PrincipalController.instancia().abrirReporteComisiones();
    }
}
