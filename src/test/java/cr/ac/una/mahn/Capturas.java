package cr.ac.una.mahn;

import cr.ac.una.mahn.controlador.VentaEntradaController;
import cr.ac.una.mahn.util.ImagenesFX;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javax.imageio.ImageIO;

/**
 * Herramienta de desarrollo: abre la aplicación, recorre las pantallas y guarda una
 * captura PNG de cada una en la carpeta indicada (por defecto docs/capturas).
 */
public final class Capturas {

    private static File carpeta;

    public static void main(String[] args) throws Exception {
        carpeta = new File(args.length > 0 ? args[0] : "docs/capturas");
        carpeta.mkdirs();
        Thread aplicacion = new Thread(() -> Application.launch(App.class));
        aplicacion.setDaemon(true);
        aplicacion.start();
        while (App.getVentana() == null || !App.getVentana().isShowing()) {
            Thread.sleep(200);
        }
        Thread.sleep(1500);
        paso("01_inicio", null);
        paso("02_mantenimiento_museos", "#btnMuseos");
        paso("03_mantenimiento_salas", "#btnSalas");
        paso("04_mantenimiento_especies", "#btnEspecies");
        paso("05_mantenimiento_precios", "#btnPrecios");
        paso("06_mantenimiento_comisiones", "#btnComisiones");
        ejecutar(() -> boton("#btnVender").fire());
        ejecutar(Capturas::prepararVenta);
        paso("07_vender_entrada", null);
        paso("08_validar_entrada_inicial", "#btnValidar");
        ejecutar(() -> leer("TNHAYDRWQPB4"));
        paso("09_validar_entrada_leida", null);
        paso("10_valoracion_inicial", "#btnValoracion");
        ejecutar(() -> leer("MAHN-SALA:39"));
        paso("11_valoracion_ficha", null);
        paso("12_historial_entradas", "#btnHistorial");
        paso("13_reporte_comisiones", "#btnReporteComisiones");
        paso("14_reporte_valoraciones", "#btnReporteValoraciones");
        Platform.exit();
        System.exit(0);
    }

    private static Scene escena() {
        return App.getVentana().getScene();
    }

    private static Button boton(String id) {
        return (Button) escena().lookup(id);
    }

    @SuppressWarnings("unchecked")
    private static void prepararVenta() {
        ((TextField) escena().lookup("#txtVisitante")).setText("Ana Rodríguez Mora");
        ListView<VentaEntradaController.SalaSeleccionable> lista =
                (ListView<VentaEntradaController.SalaSeleccionable>) escena().lookup("#lstSalas");
        for (int i = 0; i < 3 && i < lista.getItems().size(); i++) {
            lista.getItems().get(i).seleccionadaProperty().set(true);
        }
        for (Node nodo : escena().getRoot().lookupAll(".button")) {
            if (nodo instanceof Button b && "AGREGAR".equals(b.getText())) {
                b.fire();
            }
        }
    }

    private static void leer(String codigo) {
        TextField campo = (TextField) escena().lookup(".lector-qr .text-field");
        campo.setText(codigo);
        campo.fireEvent(new ActionEvent());
    }

    private static void paso(String nombre, String boton) throws Exception {
        if (boton != null) {
            ejecutar(() -> boton(boton).fire());
        }
        Thread.sleep(900);
        ejecutar(() -> {
            try {
                ImageIO.write(ImagenesFX.aBufferedImage(escena().snapshot(null)), "png", new File(carpeta, nombre + ".png"));
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
    }

    private static void ejecutar(Runnable accion) throws InterruptedException {
        CountDownLatch listo = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                accion.run();
            } finally {
                listo.countDown();
            }
        });
        listo.await(20, TimeUnit.SECONDS);
        Thread.sleep(300);
    }
}
