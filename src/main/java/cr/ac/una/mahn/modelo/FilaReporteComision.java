package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;

/** Total de comisiones que se le deben pagar a una marca de tarjeta en un rango de fechas. */
public record FilaReporteComision(String tarjeta, BigDecimal porcentajeVigente, int transacciones,
                                  BigDecimal montoProcesado, BigDecimal comision) {
}
