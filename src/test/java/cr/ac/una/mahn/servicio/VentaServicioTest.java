package cr.ac.una.mahn.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.ac.una.mahn.excepcion.ValidacionException;
import cr.ac.una.mahn.modelo.DetalleEntrada;
import cr.ac.una.mahn.modelo.Precio;
import cr.ac.una.mahn.modelo.Totales;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.junit.jupiter.api.Test;

class VentaServicioTest {

    private final VentaServicio servicio = new VentaServicio();

    private static Precio precio() {
        Precio p = new Precio();
        p.setIdSala(39);
        p.setSala("Salón de Dinosaurios Saurisquios");
        p.setMuseo("Museo Americano de Historia Natural");
        p.setPrecioSemana(new BigDecimal("14.00"));
        p.setPrecioDomingo(new BigDecimal("17.00"));
        return p;
    }

    @Test
    void elDomingoSeCobraElPrecioDiferido() throws ValidacionException {
        LocalDate domingo = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate lunes = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        assertEquals(new BigDecimal("17.00"), servicio.crearDetalle(precio(), domingo).getPrecio());
        assertEquals(new BigDecimal("14.00"), servicio.crearDetalle(precio(), lunes).getPrecio());
    }

    @Test
    void calculaIvaDelTreceProcientoYTotal() {
        List<DetalleEntrada> detalles = List.of(
                new DetalleEntrada(1, "A", "M", LocalDate.now(), new BigDecimal("15.00")),
                new DetalleEntrada(2, "B", "M", LocalDate.now(), new BigDecimal("15.00")),
                new DetalleEntrada(3, "C", "M", LocalDate.now(), new BigDecimal("12.00")));
        Totales t = VentaServicio.calcularTotales(detalles);
        assertEquals(new BigDecimal("42.00"), t.subtotal());
        assertEquals(new BigDecimal("5.46"), t.iva());
        assertEquals(new BigDecimal("47.46"), t.total());
    }

    @Test
    void rechazaFechasPasadasYVentasIncompletas() {
        assertThrows(ValidacionException.class, () -> servicio.crearDetalle(precio(), LocalDate.now().minusDays(1)));
        assertThrows(ValidacionException.class, () -> servicio.validarVenta("Ana Mora", null, List.of()));
        assertThrows(ValidacionException.class, () -> servicio.validarVenta("A1", null, List.of()));
    }

    @Test
    void detectaSalasRepetidasEnElMismoDia() {
        LocalDate hoy = LocalDate.now();
        List<DetalleEntrada> detalles = List.of(new DetalleEntrada(39, "S", "M", hoy, BigDecimal.ONE));
        assertTrue(VentaServicio.estaAgregada(detalles, 39, hoy));
        assertTrue(!VentaServicio.estaAgregada(detalles, 39, hoy.plusDays(1)));
    }
}
