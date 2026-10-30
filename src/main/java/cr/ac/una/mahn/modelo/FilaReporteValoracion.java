package cr.ac.una.mahn.modelo;

/** Posición de una sala en el reporte de mejor o peor valoradas. */
public record FilaReporteValoracion(int posicion, int idSala, String sala, String museo,
                                    double promedio, int cantidad) {
}
