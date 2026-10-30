package cr.ac.una.mahn.vista;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * Cinco estrellas que muestran un valor de 0 a 5 (con relleno parcial para los promedios).
 * En modo editable el usuario elige de 1 a 5 estrellas con el mouse.
 */
public class ControlEstrellas extends HBox {

    private final DoubleProperty valor = new SimpleDoubleProperty(0);
    private final List<Rectangle> recortes = new ArrayList<>();
    private final double tamano;

    public ControlEstrellas(double tamano, boolean editable) {
        super(tamano * 0.18);
        this.tamano = tamano;
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("control-estrellas");
        for (int i = 0; i < 5; i++) {
            Polygon fondo = estrella(tamano);
            fondo.getStyleClass().add("estrella-vacia");
            Polygon relleno = estrella(tamano);
            relleno.getStyleClass().add("estrella-llena");
            Rectangle recorte = new Rectangle(0, tamano);
            relleno.setClip(recorte);
            StackPane celda = new StackPane(fondo, relleno);
            celda.setMinSize(tamano, tamano);
            celda.setPrefSize(tamano, tamano);
            celda.setMaxSize(tamano, tamano);
            if (editable) {
                int numero = i + 1;
                celda.setCursor(Cursor.HAND);
                celda.setOnMouseEntered(e -> pintar(numero));
                celda.setOnMouseClicked(e -> valor.set(numero));
            }
            recortes.add(recorte);
            getChildren().add(celda);
        }
        if (editable) {
            setOnMouseExited(e -> pintar(valor.get()));
        }
        valor.addListener((obs, anterior, nuevo) -> pintar(nuevo.doubleValue()));
    }

    private void pintar(double cantidad) {
        for (int i = 0; i < recortes.size(); i++) {
            double fraccion = Math.max(0, Math.min(1, cantidad - i));
            recortes.get(i).setWidth(tamano * fraccion);
        }
    }

    private static Polygon estrella(double tamano) {
        double centro = tamano / 2;
        double exterior = tamano / 2;
        double interior = exterior * 0.42;
        Polygon forma = new Polygon();
        for (int k = 0; k < 10; k++) {
            double radio = k % 2 == 0 ? exterior : interior;
            double angulo = Math.toRadians(-90 + k * 36);
            forma.getPoints().addAll(centro + radio * Math.cos(angulo), centro + radio * Math.sin(angulo));
        }
        return forma;
    }

    public double getValor() {
        return valor.get();
    }

    public void setValor(double nuevo) {
        valor.set(nuevo);
    }

    public DoubleProperty valorProperty() {
        return valor;
    }
}
