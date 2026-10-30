package cr.ac.una.mahn.util;

/** Conversión de números a números romanos para mostrar los siglos de las colecciones. */
public final class NumerosRomanos {

    private static final int[] VALORES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SIMBOLOS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private NumerosRomanos() {
    }

    /** Convierte un número entre 1 y 3999 a romano. */
    public static String aRomano(int numero) {
        if (numero < 1 || numero > 3999) {
            throw new IllegalArgumentException("Solo se pueden convertir números entre 1 y 3999: " + numero);
        }
        StringBuilder romano = new StringBuilder();
        int resto = numero;
        for (int i = 0; i < VALORES.length; i++) {
            while (resto >= VALORES[i]) {
                romano.append(SIMBOLOS[i]);
                resto -= VALORES[i];
            }
        }
        return romano.toString();
    }

    /** Siglo con era: 19 → «XIX d. C.», -1 → «I a. C.». */
    public static String siglo(int siglo) {
        if (siglo == 0) {
            return "—";
        }
        return aRomano(Math.abs(siglo)) + (siglo < 0 ? " a. C." : " d. C.");
    }
}
