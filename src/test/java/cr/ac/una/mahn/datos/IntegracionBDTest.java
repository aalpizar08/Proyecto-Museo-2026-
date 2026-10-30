package cr.ac.una.mahn.datos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cr.ac.una.mahn.excepcion.DatosException;
import cr.ac.una.mahn.modelo.Comprobante;
import cr.ac.una.mahn.modelo.DetalleEntrada;
import cr.ac.una.mahn.modelo.Entrada;
import cr.ac.una.mahn.modelo.FichaSala;
import cr.ac.una.mahn.modelo.FilaReporteComision;
import cr.ac.una.mahn.modelo.FilaReporteValoracion;
import cr.ac.una.mahn.modelo.Precio;
import cr.ac.una.mahn.modelo.Sala;
import cr.ac.una.mahn.modelo.Tarjeta;
import cr.ac.una.mahn.servicio.ComprobanteServicio;
import cr.ac.una.mahn.servicio.FichaServicio;
import cr.ac.una.mahn.servicio.PdfServicio;
import cr.ac.una.mahn.servicio.QRServicio;
import cr.ac.una.mahn.servicio.VentaServicio;
import cr.ac.una.mahn.util.CodigoQR;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Pruebas contra una base de datos real. Solo se ejecutan si se indica el archivo de
 * conexión: {@code mvn test -Dmahn.config=ruta/db.properties}. Usar una copia de prueba,
 * porque registran ventas y valoraciones.
 */
@EnabledIfSystemProperty(named = "mahn.config", matches = ".+")
class IntegracionBDTest {

    @TempDir
    Path temporal;

    @Test
    void ventaRegistraDetallesComisionYQrLegible() throws Exception {
        Tarjeta visa = new TarjetaDAO().listarActivas().stream().filter(t -> t.getNombre().equals("VISA")).findFirst().orElseThrow();
        List<Precio> precios = new PrecioDAO().listarPorMuseo(1);
        VentaServicio venta = new VentaServicio();
        LocalDate hoy = LocalDate.now();
        List<DetalleEntrada> detalles = List.of(venta.crearDetalle(precios.get(0), hoy),
                venta.crearDetalle(precios.get(1), hoy.plusDays(1)));
        Entrada entrada = venta.vender("Prueba Integración", visa, detalles);
        assertTrue(entrada.getId() > 0);

        Entrada leida = new EntradaDAO().buscarPorCodigo(entrada.getCodigo());
        assertEquals(2, leida.getDetalles().size());
        assertEquals(entrada.getTotal(), leida.getTotal());

        // El disparador registró la comisión con el porcentaje de VISA
        List<FilaReporteComision> comisiones = new ReporteDAO().comisiones(hoy, hoy);
        FilaReporteComision filaVisa = comisiones.stream().filter(f -> f.tarjeta().equals("VISA")).findFirst().orElseThrow();
        assertTrue(filaVisa.transacciones() >= 1);
        BigDecimal esperada = entrada.getTotal().multiply(visa.getPorcentajeComision())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        assertTrue(filaVisa.comision().compareTo(esperada) >= 0);

        // El QR del JPG y del PDF se lee y corresponde a la entrada
        Comprobante comprobante = new ComprobanteServicio().generar(leida);
        QRServicio qr = new QRServicio();
        assertEquals(entrada.getCodigo(), CodigoQR.extraerCodigoEntrada(qr.leer(comprobante.imagenQr().toFile())));
        assertEquals(entrada.getCodigo(), CodigoQR.extraerCodigoEntrada(qr.leer(comprobante.pdf().toFile())));
    }

    @Test
    void fichaYValoracionDeSala() throws Exception {
        FichaServicio fichas = new FichaServicio();
        FichaSala antes = fichas.obtener(39);
        assertTrue(antes.tieneEspecies());
        assertEquals(3, antes.elementos().size());
        fichas.valorar(39, 5, "Prueba automática");
        assertEquals(antes.resumen().cantidad() + 1, fichas.obtener(39).resumen().cantidad());
        FichaSala tematica = fichas.obtener(8);
        assertFalse(tematica.tieneEspecies());
    }

    @Test
    void reportesEnPdf() throws Exception {
        ReporteDAO reportes = new ReporteDAO();
        List<FilaReporteValoracion> peores = reportes.valoraciones(1, false, 10);
        assertEquals(10, peores.size());
        assertTrue(peores.get(0).promedio() <= peores.get(9).promedio());
        PdfServicio pdf = new PdfServicio();
        Path a = pdf.generarReporteValoraciones("Museo Americano de Historia Natural", false, peores, temporal.resolve("v.pdf"));
        Path b = pdf.generarReporteComisiones(LocalDate.of(2026, 7, 1), LocalDate.now(),
                reportes.comisiones(LocalDate.of(2026, 7, 1), LocalDate.now()), null, temporal.resolve("c.pdf"));
        assertTrue(Files.size(a) > 1000 && Files.size(b) > 1000);
    }

    @Test
    void erroresDeLaBaseSeTraducenAlEspanol() throws Exception {
        SalaDAO salas = new SalaDAO();
        Sala existente = salas.buscarPorId(39);
        assertNotNull(existente);
        Sala duplicada = new Sala();
        duplicada.setIdMuseo(existente.getIdMuseo());
        duplicada.setIdArea(existente.getIdArea());
        duplicada.setNombre(existente.getNombre());
        duplicada.setDescripcion("Duplicada");
        DatosException duplicado = assertThrows(DatosException.class, () -> salas.insertar(duplicada));
        assertEquals("Ese museo ya tiene una sala con ese nombre.", duplicado.getMessage());
        DatosException referencia = assertThrows(DatosException.class, () -> new MuseoDAO().eliminar(1));
        assertTrue(referencia.getMessage().startsWith("No se puede eliminar"));
    }
}
