package cr.ac.una.mahn.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

/** Formatos de fechas, montos y medidas que se usan en toda la aplicación. */
public final class Formatos {

    public static final Locale ESPANOL = Locale.forLanguageTag("es-CR");

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FECHA_LARGA =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", ESPANOL);
    private static final DecimalFormatSymbols SIMBOLOS = DecimalFormatSymbols.getInstance(Locale.US);

    private Formatos() {
    }

    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : FECHA.format(fecha);
    }

    public static String fechaHora(LocalDateTime fecha) {
        return fecha == null ? "" : FECHA_HORA.format(fecha);
    }

    /** Ejemplo: «Jueves 29 de octubre de 2026». */
    public static String fechaLarga(LocalDate fecha) {
        return capitalizar(FECHA_LARGA.format(fecha));
    }

    /** Nombre del día de la semana, por ejemplo «Domingo». */
    public static String dia(LocalDate fecha) {
        return capitalizar(fecha.getDayOfWeek().getDisplayName(TextStyle.FULL, ESPANOL));
    }

    /** Monto en dólares: «$1,234.50». */
    public static String moneda(BigDecimal monto) {
        if (monto == null) {
            return "";
        }
        return "$" + new DecimalFormat("#,##0.00", SIMBOLOS).format(monto.setScale(2, RoundingMode.HALF_UP));
    }

    public static String porcentaje(BigDecimal porcentaje) {
        return porcentaje == null ? "" : new DecimalFormat("0.00", SIMBOLOS).format(porcentaje) + " %";
    }

    public static String promedio(double promedio) {
        return new DecimalFormat("0.0", SIMBOLOS).format(promedio);
    }

    /** Peso legible: gramos o miligramos para animales pequeños y kilogramos para los grandes. */
    public static String peso(BigDecimal kilogramos) {
        if (kilogramos == null) {
            return "";
        }
        double kg = kilogramos.doubleValue();
        if (kg < 0.001) {
            return numero(kg * 1_000_000) + " mg";
        }
        if (kg < 1) {
            return numero(kg * 1000) + " g";
        }
        return numero(kg) + " kg";
    }

    /** Tamaño legible: centímetros o milímetros si es menor que un metro. */
    public static String tamano(BigDecimal metros) {
        if (metros == null) {
            return "";
        }
        double m = metros.doubleValue();
        if (m < 0.01) {
            return numero(m * 1000) + " mm";
        }
        if (m < 1) {
            return numero(m * 100) + " cm";
        }
        return numero(m) + " m";
    }

    private static String numero(double valor) {
        return new DecimalFormat("#,##0.##", SIMBOLOS).format(valor);
    }

    /** Texto sin tildes ni espacios, apto para nombres de archivo: «Salón Ñandú» → «salon_nandu». */
    public static String slug(String texto) {
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String limpio = sinTildes.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        return limpio.isEmpty() ? "archivo" : limpio;
    }

    public static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
