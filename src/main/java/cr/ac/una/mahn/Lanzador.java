package cr.ac.una.mahn;

/**
 * Clase de arranque para el JAR ejecutable (target/MAHN-3.0.jar). Como no hereda de
 * Application, Java puede iniciar JavaFX aunque las librerías estén en el classpath.
 */
public final class Lanzador {

    private Lanzador() {
    }

    public static void main(String[] args) {
        App.main(args);
    }
}
