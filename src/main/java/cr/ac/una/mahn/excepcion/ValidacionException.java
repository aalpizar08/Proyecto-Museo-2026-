package cr.ac.una.mahn.excepcion;

/**
 * Error de validación de los datos que ingresa el usuario. El mensaje se muestra
 * tal cual en pantalla, por lo que debe explicar qué corregir.
 */
public class ValidacionException extends Exception {

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
