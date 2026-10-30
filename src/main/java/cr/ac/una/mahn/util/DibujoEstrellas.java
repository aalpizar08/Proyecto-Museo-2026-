package cr.ac.una.mahn.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/** Dibuja cinco estrellas (con relleno parcial) como imagen, para los reportes en PDF. */
public final class DibujoEstrellas {

    private static final Color VACIA = new Color(0xd9d4c7);
    private static final Color LLENA = new Color(0xf2b01e);

    private DibujoEstrellas() {
    }

    public static BufferedImage imagen(double valor, int tamano) {
        int espacio = Math.max(2, tamano / 6);
        int ancho = 5 * tamano + 4 * espacio;
        BufferedImage imagen = new BufferedImage(ancho, tamano, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < 5; i++) {
            double x = i * (tamano + espacio);
            Shape estrella = estrella(x, tamano);
            g.setColor(VACIA);
            g.fill(estrella);
            double fraccion = Math.max(0, Math.min(1, valor - i));
            if (fraccion > 0) {
                Shape recorteAnterior = g.getClip();
                g.clip(new Rectangle2D.Double(x, 0, tamano * fraccion, tamano));
                g.setColor(LLENA);
                g.fill(estrella);
                g.setClip(recorteAnterior);
            }
        }
        g.dispose();
        return imagen;
    }

    /** Estrella de cinco puntas inscrita en un cuadrado de lado {@code tamano}. */
    private static Shape estrella(double x, double tamano) {
        double centroX = x + tamano / 2;
        double centroY = tamano / 2 + tamano * 0.05;
        double exterior = tamano / 2;
        double interior = exterior * 0.42;
        Path2D.Double forma = new Path2D.Double();
        for (int k = 0; k < 10; k++) {
            double radio = k % 2 == 0 ? exterior : interior;
            double angulo = Math.toRadians(-90 + k * 36);
            double px = centroX + radio * Math.cos(angulo);
            double py = centroY + radio * Math.sin(angulo);
            if (k == 0) {
                forma.moveTo(px, py);
            } else {
                forma.lineTo(px, py);
            }
        }
        forma.closePath();
        return forma;
    }
}
