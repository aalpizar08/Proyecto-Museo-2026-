package cr.ac.una.mahn.util;

import cr.ac.una.mahn.excepcion.ValidacionException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formato del contenido de los códigos QR del museo.
 * <ul>
 *   <li>Entrada: {@code MAHN-ENTRADA:XXXXXXXXXXXX} (12 caracteres aleatorios).</li>
 *   <li>Sala: {@code MAHN-SALA:<id_sala>}.</li>
 * </ul>
 * Los separadores se aceptan como cualquier carácter porque algunos lectores USB
 * configurados con teclado en inglés escriben «'» en lugar de «-» o «Ñ» en lugar de «:»
 * cuando el sistema usa la distribución latinoamericana.
 */
public final class CodigoQR {

    public static final String PREFIJO_ENTRADA = "MAHN-ENTRADA:";
    public static final String PREFIJO_SALA = "MAHN-SALA:";
    public static final int LARGO_CODIGO = 12;

    /** Sin 0, O, 1 ni I para evitar confusiones al escribir el código a mano. */
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom AZAR = new SecureRandom();

    private static final Pattern ENTRADA = Pattern.compile("^(?:MAHN.?ENTRADA.?)?([A-Z0-9]{12})$");
    private static final Pattern SALA = Pattern.compile("^(?:MAHN.?SALA.?)?(\\d{1,9})$");
    private static final Pattern ES_SALA = Pattern.compile("^MAHN.?SALA.?.*$");
    private static final Pattern ES_ENTRADA = Pattern.compile("^MAHN.?ENTRADA.?.*$");

    private CodigoQR() {
    }

    public static String generarCodigoEntrada() {
        StringBuilder codigo = new StringBuilder(LARGO_CODIGO);
        for (int i = 0; i < LARGO_CODIGO; i++) {
            codigo.append(ALFABETO.charAt(AZAR.nextInt(ALFABETO.length())));
        }
        return codigo.toString();
    }

    public static String contenidoEntrada(String codigo) {
        return PREFIJO_ENTRADA + codigo;
    }

    public static String contenidoSala(int idSala) {
        return PREFIJO_SALA + idSala;
    }

    /** Obtiene el código de entrada a partir del texto leído del QR (o escrito a mano). */
    public static String extraerCodigoEntrada(String leido) throws ValidacionException {
        String texto = normalizar(leido);
        if (ES_SALA.matcher(texto).matches()) {
            throw new ValidacionException("El código leído corresponde a una sala, no a una entrada. "
                    + "Use la pantalla de Valoración para las salas.");
        }
        Matcher m = ENTRADA.matcher(texto);
        if (!m.matches()) {
            throw new ValidacionException("«" + leido.trim() + "» no es un código de entrada válido del museo.");
        }
        return m.group(1);
    }

    /** Obtiene el número de sala a partir del texto leído del QR (o escrito a mano). */
    public static int extraerIdSala(String leido) throws ValidacionException {
        String texto = normalizar(leido);
        if (ES_ENTRADA.matcher(texto).matches()) {
            throw new ValidacionException("El código leído corresponde a una entrada, no a una sala. "
                    + "Use la pantalla Validar entrada para las entradas.");
        }
        Matcher m = SALA.matcher(texto);
        if (!m.matches()) {
            throw new ValidacionException("«" + leido.trim() + "» no es un código QR de sala válido.");
        }
        return Integer.parseInt(m.group(1));
    }

    private static String normalizar(String leido) throws ValidacionException {
        if (leido == null || leido.isBlank()) {
            throw new ValidacionException("No se leyó ningún código. Escanee el código QR o cargue su imagen.");
        }
        return leido.trim().toUpperCase(Locale.ROOT);
    }
}
