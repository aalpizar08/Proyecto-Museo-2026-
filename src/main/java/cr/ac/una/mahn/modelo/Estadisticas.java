package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;

/** Indicadores que se muestran en la pantalla de inicio. */
public record Estadisticas(int museos, int salas, int colecciones, int especies, int tematicas,
                           int entradasHoy, int visitasHoy, BigDecimal ventasMes,
                           double promedioValoraciones, int totalValoraciones) {
}
