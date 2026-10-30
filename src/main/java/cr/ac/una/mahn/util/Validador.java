package cr.ac.una.mahn.util;

import cr.ac.una.mahn.excepcion.ValidacionException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import javafx.scene.control.DatePicker;

/**
 * Validaciones de los datos de los formularios. Cada método devuelve el valor
 * limpio o lanza {@link ValidacionException} con un mensaje explicativo.
 */
public final class Validador {

    private static final Pattern NOMBRE_PERSONA = Pattern.compile("^\\p{L}[\\p{L} .'-]*$");
    private static final Pattern URL = Pattern.compile("^https?://[^\\s/$.?#][^\\s]*\\.[^\\s]+$",
            Pattern.CASE_INSENSITIVE);

    private Validador() {
    }

    /** Texto obligatorio con longitud mínima y máxima. */
    public static String texto(String valor, String campo, int minimo, int maximo) throws ValidacionException {
        String limpio = limpiar(valor);
        if (limpio.isEmpty()) {
            throw new ValidacionException("El campo «" + campo + "» es obligatorio.");
        }
        if (limpio.length() < minimo) {
            throw new ValidacionException("El campo «" + campo + "» debe tener al menos " + minimo + " caracteres.");
        }
        if (limpio.length() > maximo) {
            throw new ValidacionException("El campo «" + campo + "» admite como máximo " + maximo
                    + " caracteres (tiene " + limpio.length() + ").");
        }
        return limpio;
    }

    /** Texto opcional: devuelve {@code null} si viene vacío. */
    public static String textoOpcional(String valor, String campo, int maximo) throws ValidacionException {
        String limpio = limpiar(valor);
        if (limpio.isEmpty()) {
            return null;
        }
        return texto(limpio, campo, 1, maximo);
    }

    /** Nombre de una persona: solo letras, espacios, puntos, apóstrofos y guiones. */
    public static String nombrePersona(String valor, String campo) throws ValidacionException {
        String limpio = texto(valor, campo, 3, 100);
        if (!NOMBRE_PERSONA.matcher(limpio).matches()) {
            throw new ValidacionException("El campo «" + campo
                    + "» solo puede contener letras, espacios, puntos, apóstrofos y guiones.");
        }
        return limpio;
    }

    /** Dirección web opcional; si se indica debe iniciar con http:// o https://. */
    public static String url(String valor, String campo) throws ValidacionException {
        String limpio = limpiar(valor);
        if (limpio.isEmpty()) {
            return null;
        }
        if (limpio.length() > 200 || !URL.matcher(limpio).matches()) {
            throw new ValidacionException("El campo «" + campo
                    + "» debe ser una dirección válida que inicie con http:// o https:// (ej. https://www.amnh.org).");
        }
        return limpio;
    }

    /**
     * Número decimal dentro de un rango. Acepta punto o coma como separador decimal.
     *
     * @param minimoExclusivo si es {@code true}, el valor debe ser estrictamente mayor que el mínimo
     */
    public static BigDecimal decimal(String valor, String campo, BigDecimal minimo, boolean minimoExclusivo,
                                     BigDecimal maximo, int decimales) throws ValidacionException {
        String limpio = limpiar(valor).replace(" ", "").replace(',', '.');
        if (limpio.isEmpty()) {
            throw new ValidacionException("El campo «" + campo + "» es obligatorio.");
        }
        BigDecimal numero;
        try {
            numero = new BigDecimal(limpio);
        } catch (NumberFormatException ex) {
            throw new ValidacionException("El campo «" + campo + "» debe ser un número válido "
                    + "(use punto o coma para los decimales).");
        }
        if (numero.stripTrailingZeros().scale() > decimales) {
            throw new ValidacionException("El campo «" + campo + "» admite como máximo " + decimales + " decimales.");
        }
        boolean debajoDelMinimo = minimoExclusivo ? numero.compareTo(minimo) <= 0 : numero.compareTo(minimo) < 0;
        if (debajoDelMinimo) {
            throw new ValidacionException("El campo «" + campo + "» debe ser "
                    + (minimoExclusivo ? "mayor que " : "mayor o igual que ") + minimo.toPlainString() + ".");
        }
        if (numero.compareTo(maximo) > 0) {
            throw new ValidacionException("El campo «" + campo + "» no puede ser mayor que " + maximo.toPlainString() + ".");
        }
        return numero;
    }

    /** Verifica que se haya elegido un valor en un ComboBox. */
    public static <T> T seleccion(T valor, String campo) throws ValidacionException {
        if (valor == null) {
            throw new ValidacionException("Seleccione un valor en el campo «" + campo + "».");
        }
        return valor;
    }

    /**
     * Obtiene la fecha de un DatePicker. Si el usuario la escribió sin presionar Enter,
     * se interpreta el texto del editor.
     */
    public static LocalDate fecha(DatePicker selector, String campo) throws ValidacionException {
        String texto = selector.getEditor().getText();
        if (texto != null && !texto.isBlank()) {
            try {
                LocalDate escrita = selector.getConverter().fromString(texto.trim());
                if (escrita != null) {
                    selector.setValue(escrita);
                }
            } catch (DateTimeParseException ex) {
                throw new ValidacionException("La fecha del campo «" + campo
                        + "» no es válida; use el formato día/mes/año (por ejemplo 06/04/1869).");
            }
        }
        if (selector.getValue() == null) {
            throw new ValidacionException("Seleccione una fecha en el campo «" + campo + "».");
        }
        return selector.getValue();
    }

    /** Fecha que no puede estar en el futuro (por ejemplo, una fecha de fundación). */
    public static LocalDate fechaPasada(DatePicker selector, String campo) throws ValidacionException {
        LocalDate fecha = fecha(selector, campo);
        if (fecha.isAfter(LocalDate.now())) {
            throw new ValidacionException("La fecha del campo «" + campo + "» no puede ser posterior a hoy.");
        }
        return fecha;
    }

    /** Cantidad de estrellas de una valoración (1 a 5). */
    public static int estrellas(double valor) throws ValidacionException {
        int estrellas = (int) Math.round(valor);
        if (estrellas < 1 || estrellas > 5) {
            throw new ValidacionException("Seleccione de 1 a 5 estrellas haciendo clic sobre ellas.");
        }
        return estrellas;
    }

    private static String limpiar(String valor) {
        return valor == null ? "" : valor.trim().replaceAll("\\s+", " ");
    }
}
