package cr.ac.una.mahn.util;

import java.awt.image.BufferedImage;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/** Conversión de imágenes de JavaFX a imágenes AWT (para incluir gráficos en los PDF). */
public final class ImagenesFX {

    private ImagenesFX() {
    }

    /** Captura un nodo (por ejemplo, un gráfico) como imagen con fondo blanco. */
    public static BufferedImage capturar(Node nodo, double escala) {
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.WHITE);
        parametros.setTransform(javafx.scene.transform.Transform.scale(escala, escala));
        WritableImage captura = nodo.snapshot(parametros, null);
        return aBufferedImage(captura);
    }

    public static BufferedImage aBufferedImage(Image imagen) {
        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();
        BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        PixelReader lector = imagen.getPixelReader();
        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                resultado.setRGB(x, y, lector.getArgb(x, y));
            }
        }
        return resultado;
    }
}
