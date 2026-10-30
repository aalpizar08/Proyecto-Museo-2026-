package cr.ac.una.mahn.modelo;

import java.math.BigDecimal;

/** Subtotal, IVA y total de una venta. */
public record Totales(BigDecimal subtotal, BigDecimal iva, BigDecimal total) {
}
